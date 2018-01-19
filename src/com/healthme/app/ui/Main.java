package com.healthme.app.ui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.androidplot.xy.XYPlot;
import com.bumptech.glide.Glide;
import com.healthme.app.AppContext;
import com.healthme.app.AppException;
import com.healthme.app.R;
import com.healthme.app.adapter.ListViewBluetoothAdapter;
import com.healthme.app.bean.CustomizedBluetoothDevice;
import com.healthme.app.bean.EcgRecord;
import com.healthme.app.bean.MessageParams;
import com.healthme.app.bean.Relative;
import com.healthme.app.common.HandlerEvent;
import com.healthme.app.common.StringUtils;
import com.healthme.app.common.UIHelper;
import com.healthme.app.common.UpdateManager;
import com.healthme.app.service.RFStarBLEService;
import com.healthme.app.service.RFStarBLEService.OnReceiveDataListener;
import com.healthme.app.service.StompClient;
import com.healthme.app.widget.PullToRefreshListView;
import com.healthme.common.nio.socket.WorkerPool;
import com.healthme.common.util.SampleByteBuffer;
import com.healthme.ecg.HMECGCODES;
import com.healthme.message.BLEMessage;
import static com.healthme.app.service.RFStarBLEService.STATUS_TRANSPORT;


/**
 * 应用程序首页
 * 
 * @version 1.0
 * @created 2014 12-12-18
 */
public class Main extends BaseActivity implements HandlerEvent, BluetoothAdapter.LeScanCallback {


	public static final int REQUEST_ENABLE_BT = 2;  
    public static final float PANEL_SIZE_RATE=0.80f;
    public static final short LEADLOSTMASK = 0x1000;
    public static final short LOWBATTMASK = 0x2000;
    public static final short LEADLOSTSHFT = 12;
    public static final short LOWBATTSHFT = 13;
    public static final String TAG="MAIN";
    public static final int ECG_UI=0;
    public static final int ELECTRODE_UI=1;
    public static final int BATTERY_UI=1;
    public short fullMask = (short)(LEADLOSTMASK | LOWBATTMASK);
	
	
	private ImageView mHeadLogo;
	private ImageView menuButton;
	
	

	private AppContext appContext;// 全局Context
	
	private RFStarBLEService bleService;
	
    private BluetoothAdapter mBluetoothAdapter;	
	private RPM rpm;
	private Handler msgProcessHandler;
	private ConcurrentLinkedQueue<BLEMessage> dataPool = new ConcurrentLinkedQueue<BLEMessage>();
	private View btStarting;
	private ViewSwitcher mViewSwitcher;
	protected DataHandlerThread handlerThread;
	private boolean leadLost = false;
	private boolean lowBatt = false;
//	private BTDeviceMonitor btMonitor;
	private DynamicPlot dynamicPlot;
	private WorkerPool fixedThreadPool=new WorkerPool("data-worker",1,1,new LinkedBlockingQueue<Runnable>());
	private ProgressBar pvcProgress;
	private TextView pvcNumText;
	private ProgressBar svpbProgress;
	private TextView svpbNumText;
	private ProgressBar longRRProgress;
	private TextView longRRNumText;
	private ProgressBar pauseProgress;
	private TextView pauseNumText;
	private TextView afNumText;
	private ProgressBar afProgress;
	private TextView startTime;
	private TextView monitorTime;
	private TimeShowingThread timeShowingThead; 
	private EcgRecord record;
	private ImageView detailAf;
	private View detailPause;
	private ImageView detailLongRR;
	private View detailSvpb;
	private View detailPvc;


	private CustomizedBluetoothDevice matchDev;
	private BluetoothAdapter bluetoothAdapter;
	private PullToRefreshListView listview;
	private ListViewBluetoothAdapter adapter;
	private View lv_footer;
	private TextView lv_footer_more;
	private ProgressBar lv_footer_progress;
	private List<CustomizedBluetoothDevice> list = new ArrayList<CustomizedBluetoothDevice>();
	private List<BluetoothDevice> scannedDevice = new ArrayList<BluetoothDevice>();
	private Handler handler;
	private boolean scanning=false;


	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;


	private int lvSize;

	private boolean alive=true;

	private TextView selected_device,curStatusText;
	private ImageView statusImgView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		appContext = (AppContext) getApplication();
		Log.i("DISPLAY_SERVICE", "INIT............");
		initHandler();
		initBluetooth();
		// 网络连接判断
		if (!appContext.isNetworkConnected())
			UIHelper.ToastMessage(this, R.string.network_not_connected);

		selected_device = (TextView) findViewById(R.id.selected_device);
		curStatusText= (TextView) findViewById(R.id.cur_status);
		statusImgView= (ImageView) findViewById(R.id.status_imgview);

		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		if(bluetoothManager!=null)
			bluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (bluetoothAdapter == null) {
			Toast.makeText(this, R.string.msg_ble_invalid, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		initBluetoothListView();

		handler=new Handler();

		initHeadView();
		initPanel();
		initEcgSimpleXYPlot();
		initRecordUI();
		// 检查新版本
		if (appContext.isCheckUp()) {
			UpdateManager.getUpdateManager().checkAppUpdate(this, false);
		}
		
		timeShowingThead = new TimeShowingThread();
		timeShowingThead.start();
//		this.startService(new Intent(this,StompClient.class));
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		//For Test
		matchDev=appContext.getBluetoothDevice();
		scanLeDevice(true);
		if(bleService!=null){
			bleService.queryCharge(new OnReceiveDataListener(){
				public void onReceiveData(BLEMessage msg){
					Log.i(TAG,"bat:"+msg.getData());
				}
			});
		}
	}

	/**
	 * 初始化帖子列表
	 */
	private void initBluetoothListView() {
		adapter = new ListViewBluetoothAdapter(this,
				list, R.layout.btdevice_listitem, bluetoothAdapter);
		lv_footer = getLayoutInflater().inflate(
				R.layout.listview_footer, null);
		lv_footer_more = (TextView) lv_footer
				.findViewById(R.id.listview_foot_more);
		// lvBluetooth_cur_device = (TextView) findViewById(R.id.cur_device);
		// lvBluetooth_selected_device = (TextView)
		// findViewById(R.id.selected_device);
		lv_footer_progress = (ProgressBar) lv_footer
				.findViewById(R.id.listview_foot_progress);
		listview = (PullToRefreshListView) findViewById(R.id.frame_listview_question);
		listview.addFooterView(lv_footer);// 添加底部视图 必须在setAdapter�?
		listview.setAdapter(adapter);
		listview
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
											final int position, long id) {

						Log.i(TAG,"click device");
						ListViewBluetoothAdapter.ListItemView liv = (ListViewBluetoothAdapter.ListItemView) view
								.getTag();

						if (liv != null) {
							CustomizedBluetoothDevice device=(CustomizedBluetoothDevice)liv.name.getTag();
							setMatchDevice(device.device);
						}

					}
				});
		listview.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				Log.i(TAG,"onScrollStateChanged");
//				listview.onScrollStateChanged(view, scrollState);
//				// 数据为空--不用继续下面代码�?
//				if (list.isEmpty())
//					return;
//
//				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lv_footer) == view
							.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				scanLeDevice(true);
//
//				int lvDataState = StringUtils.toInt(listview.getTag());
//				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
//					listview.setTag(UIHelper.LISTVIEW_DATA_LOADING);
//					lv_footer_more.setText(R.string.load_ing);
//					lv_footer_progress.setVisibility(View.VISIBLE);
//					// 当前pageIndex
//					int pageIndex = lvSize / AppContext.PAGE_SIZE;
//					loadLvBluetoothData(curBluetoothCatalog, pageIndex,
//							UIHelper.LISTVIEW_ACTION_SCROLL);
//
//				}
//				return;
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
								 int visibleItemCount, int totalItemCount) {
				listview.onScroll(view, firstVisibleItem, visibleItemCount,
						totalItemCount);

			}
		});
		listview
				.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
					public void onRefresh() {
						//loadLvBluetoothData(curBluetoothCatalog, 0,UIHelper.LISTVIEW_ACTION_REFRESH);
						Log.i(TAG, "onRefresh");
						scanLeDevice(true);
					}
				});

//		loadLvBluetoothData(curBluetoothCatalog, 0,
//				UIHelper.LISTVIEW_ACTION_REFRESH);
	}

	/**
	 * 扫描到的蓝牙设备
	 */
	@Override
	public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
		if(scannedDevice.contains(device)){
			return;
		}
		scannedDevice.add(device);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(alive){
					Log.i(TAG, "scanrecord : " + device.getAddress());// device.getName());
					CustomizedBluetoothDevice dev=new CustomizedBluetoothDevice(device);
					if(matchDev!=null&&dev.getAddress().equals(matchDev.getAddress())){
						dev.setStatusPaired(true);
						dev.setCheck(true);
					}
					list.add(dev);
					if(adapter!=null)
						adapter.notifyDataSetChanged();
				}
			}
		});
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			if(scanning){
				return;
			}
			scanning = true;
			// Stops scanning after a pre-defined scan period.
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					scanLeDevice(false);
				}
			}, SCAN_PERIOD);

			Log.i(TAG,"start scan...");
			list.clear();
			adapter.notifyDataSetChanged();
			lv_footer_progress.setVisibility(View.VISIBLE);
			lv_footer_more.setText(R.string.scan_ing);
			scannedDevice.clear();
			bluetoothAdapter.startLeScan(Main.this);
		} else {
			Log.i(TAG,"stop scan...");
			listview.onRefreshComplete();
			lv_footer_progress.setVisibility(View.GONE);
			lv_footer_more.setText(list.size()==0?R.string.scan_empty:R.string.scan_full);
			bluetoothAdapter.stopLeScan(this);
			scanning = false;
			if(bleService.getCurBleDevice()!=null&&!scannedDevice.contains(bleService.getCurBleDevice())){
				onLeScan(bleService.getCurBleDevice(),0,null);
			}
		}
	}


	public void setMatchDevice(BluetoothDevice device){
		selected_device.setText(device.getName());
		CustomizedBluetoothDevice cd=new CustomizedBluetoothDevice(device);
		cd.setCheck(true);
		cd.setStatusPaired(true);
		appContext.setBluetoothDevice(cd);
		this.matchDev=cd;
		for(CustomizedBluetoothDevice d:list){
			if(d.equals(cd)){
				d.setCheck(true);
				d.setStatusPaired(true);
			}
			else{
				d.setCheck(false);
				d.setStatusPaired(false);
			}
		}
		adapter.notifyDataSetChanged();
		scanLeDevice(false);
		if(bleService!=null)
			bleService.disConnectBle();
	}


	private void initRecordUI() {
		pvcProgress = (ProgressBar) findViewById(R.id.pvc_progress);
		pvcNumText = (TextView) findViewById(R.id.num_pvc);
		pvcProgress.setProgress(0);
		pvcNumText.setText("0");
		detailPvc=(ImageView)findViewById(R.id.detail_pvc);
		detailPvc.setOnClickListener(new ShowDetailListener(HMECGCODES.PVC));
		
		svpbProgress = (ProgressBar) findViewById(R.id.svpb_progress);
		svpbNumText = (TextView) findViewById(R.id.num_svpb);
		svpbProgress.setProgress(0);
		svpbNumText.setText("0");
		detailSvpb=(ImageView)findViewById(R.id.detail_svpb);
		detailSvpb.setOnClickListener(new ShowDetailListener(HMECGCODES.SVPB));
		
		longRRProgress = (ProgressBar) findViewById(R.id.longRR_progress);
		longRRNumText = (TextView) findViewById(R.id.num_longRR);
		longRRProgress.setProgress(0);
		longRRNumText.setText("0");
		detailLongRR=(ImageView)findViewById(R.id.detail_longRR);
		detailLongRR.setOnClickListener(new ShowDetailListener(HMECGCODES.LONGRR));
		
		pauseProgress = (ProgressBar) findViewById(R.id.pause_progress);
		pauseNumText = (TextView) findViewById(R.id.num_pause);
		pauseProgress.setProgress(0);
		pauseNumText.setText("0");
		detailPause=(ImageView)findViewById(R.id.detail_pause);
		detailPause.setOnClickListener(new ShowDetailListener(HMECGCODES.PAUSE));
		
		afProgress = (ProgressBar) findViewById(R.id.af_progress);
		afNumText = (TextView) findViewById(R.id.num_af);
		afProgress.setProgress(0);
		afNumText.setText("0");
		detailAf=(ImageView)findViewById(R.id.detail_af);
		detailAf.setOnClickListener(new ShowDetailListener(HMECGCODES.AFIB));
		
		startTime=(TextView) findViewById(R.id.start_time);
		monitorTime=(TextView) findViewById(R.id.monitor_time);
	}

	
	private void updateRecordUI(EcgRecord record) {
		pvcProgress = (ProgressBar) findViewById(R.id.pvc_progress);
		pvcNumText = (TextView) findViewById(R.id.num_pvc);
		int num = caculateProgress(record.getTotalPvcNumber());
		pvcProgress.setProgress(num);
		pvcNumText.setText(num+"");
		
		svpbProgress = (ProgressBar) findViewById(R.id.svpb_progress);
		svpbNumText = (TextView) findViewById(R.id.num_svpb);
		num = caculateProgress(record.getTotalSvpbNumber());
		svpbProgress.setProgress(num);
		svpbNumText.setText(num+"");
		
		longRRProgress = (ProgressBar) findViewById(R.id.longRR_progress);
		longRRNumText = (TextView) findViewById(R.id.num_longRR);
		num = caculateProgress(record.getLongRRNumber());
		longRRProgress.setProgress(num);
		longRRNumText.setText(num+"");
		
		pauseProgress = (ProgressBar) findViewById(R.id.pause_progress);
		pauseNumText = (TextView) findViewById(R.id.num_pause);
		num = caculateProgress(record.getPauseNumber());
		pauseProgress.setProgress(num);
		pauseNumText.setText(num+"");
		
		afProgress = (ProgressBar) findViewById(R.id.af_progress);
		afNumText = (TextView) findViewById(R.id.num_af);
		num = caculateProgress(record.getAfibNumber());
		afProgress.setProgress(num);
		afNumText.setText(num+"");
		
		//TODO by default we calculate the heartbeat by 5 RR interval.
		if(record.getCurRRPieceInterval()>0)
			rpm.setRPM(60*SAMPLE_RATE*HEARTBEAT_NUM/record.getCurRRPieceInterval());
		//rpm.setRPM(System.currentTimeMillis()%100);
		
		//long dur = record.getEndTime().getTime()-record.getStartTime().getTime();
		//monitorTime.setText(""+dur/3600000+":"+dur%3600000/60000+":"+dur%60000/1000);
	}
	
	private int caculateProgress(int p){
		int r=(int) (Math.log(p) / Math.log(2));
		return (r>100?100:r);
	}
	private void initHandler() {
		msgProcessHandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HANDLER_MSG_CLIENT:
					if(appContext.isLogin())
						handleClientEvent(msg.arg1,msg.obj);
					break;
					
//				case HANDLER_MSG_DEV:
//					handleDevEvent(msg);
//					break;
					
				case HANDLER_UPDATE_UI:
					handleUIEvent(msg);
					break;
				default:
					break;
				}
			}
		};
		
	}
	private void handleUIEvent(Message msg) {
		switch (msg.arg1) {
		case ECG_UI:
			updateRecordUI((EcgRecord) msg.obj);
			break;
		default:
			break;
		}
//		msg.recycle();
	}
	@SuppressWarnings("unchecked")
	private void handleClientEvent(int subType, Object dataObject) {
		final DataEntity de=(DataEntity) dataObject;
//		Log.i("CSEQ", de.cseq+" Data:["+de.data.get(0)+"...]");
		switch (subType) {
		
		case CLIENT_SEND_DATA:
			Runnable runner = new Runnable() {
				@Override
				public void run() {
					Map<String, Object> params=new HashMap<String, Object>();
					
					params.put(MessageParams.CSEQ, de.cseq);
					params.put(MessageParams.POWER, !lowBatt==true?1:0);
					if(handlerThread.record==null){
						handlerThread.cseq=new AtomicInteger(0);
						params.put(MessageParams.DEVICETYPE, "ECG_BT");
						params.put(MessageParams.USERNAME, appContext.getLoginInfo().getAccount());
						try {
							record = appContext.createTransferSession(params);
							handlerThread.setRecord(record);
							Log.i("INFO ",record.toString());
						} catch (AppException e) {
							e.printStackTrace();
							UIHelper.ToastMessage(appContext, "创建会话失败！");
							return;
						}
					}
					Log.i("BEFORE", "before send data to server:");
					params.put(MessageParams.ID, handlerThread.record.getId());
					params.put(MessageParams.ECG_DATA,de.data);
					record = appContext.sendUserData(params);
					Log.i("INFO","transfer data successfully");
					if(record!=null){
						Log.i("RECORD:", record.toString());
						Message newMsg = msgProcessHandler.obtainMessage(HANDLER_UPDATE_UI,ECG_UI,0,record);
						newMsg.sendToTarget();
						Log.i("AFTER", "after send data to server"); 
					}
				}
			};
			fixedThreadPool.execute(runner);
			break;
		case CLIENT_DATA_RESPONSE:
			
			break;        

		default:
			break;
		}
//		msg.recycle();
	}
	
	private void handleDevEvent(Message msg) {
		int type = msg.arg1;
		switch (type) {
		case DEV_SIGNAL:
//			handleSignal(msg);
			break;
		case DEV_CONNECT_OK:
			dynamicPlot.resume();
		    handlerThread=new DataHandlerThread();
			handlerThread.start();
		    mViewSwitcher.showPrevious();
			break;
		case DEV_CONNECT_FAILED:
			dynamicPlot.pause();
			//mViewSwitcher.showPrevious();
			break;
		case DEV_ON_CONNECT:
			timeShowingThead.startShowing(new Date());
			AnimationDrawable startAnimation = (AnimationDrawable)btStarting.getBackground();
	        startAnimation.start();
	        mViewSwitcher.showNext();
	        if(handlerThread!=null&&handlerThread.isRunning){
	        	handlerThread.stopWorking();
	        }
			break;
		default:
			break;
		}
//		msg.recycle();
	}

	public void sendProcessMessage(int type, int subType, Object value){
		Message msg = msgProcessHandler.obtainMessage(type, subType, 0, value);
		msg.sendToTarget();
	}
	
	
	@Override
	protected void onDestroy() {
		try{
//			this.stopService(new Intent(this,StompClient.class));
			bleService.stopAll();
//			bleService.disConnectBle();
//			appContext.disconnectBluetoothDevice();
			this.unbindService(bleServiceConnection);
			if(handlerThread!=null){
				handlerThread.stopWorking();
				handlerThread=null;
			}
//			if(btMonitor!=null)btMonitor.end();
			if (mBluetoothAdapter != null) {
				mBluetoothAdapter.cancelDiscovery();
			}
			appContext.Logout();
			timeShowingThead.stopRunning();
		}
		catch(Exception e){
			Log.i(TAG, "onDestroy error:"+e.getMessage());
		}
		super.onDestroy();
	}

	
	
	private void initBluetooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.bluetooth_disabled,
					Toast.LENGTH_SHORT).show();			
			 finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		//btMonitor=new BTDeviceMonitor(appContext,dataPool,msgProcessHandler);
		
		bindService(new Intent(this, RFStarBLEService.class), bleServiceConnection,
				Service.BIND_AUTO_CREATE);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
			} else {
				// User did not enable Bluetooth or an error occured

				Toast.makeText(this, R.string.bluetooth_disabled,
						Toast.LENGTH_SHORT).show();
				// finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 初始化头部视图
	 */
	@SuppressLint("NewApi")
	private void initHeadView() {
		mHeadLogo = (ImageView) findViewById(R.id.main_head_logo);
		menuButton=(ImageView) findViewById(R.id.main_head_action);
		
		if(appContext.isLogin()){
			mHeadLogo.setBackgroundResource(R.drawable.head_log_on);
		}
		
		mHeadLogo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(appContext.isLogin()){
					ArrayList<Relative> relativeList=new ArrayList<Relative>();
					for (int i=1;i<5;i++) {
						Relative r=new Relative();
						r.setId(i+1000);
						r.setFullName("亲属-"+i);
						r.setPhoneNumber("56789123"+i);
						relativeList.add(r);
					}
					Intent intent = new Intent(Main.this,PatientInfoActivity.class);
					intent.putExtra("RELATIVE_LIST", relativeList);
					startActivity(intent);
				}else{
					UIHelper.showLoginDialog(Main.this);
				}
			}
		});
		
		menuButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popUpOverflowMenu();
			}
		});
		
		
		btStarting = (View)findViewById(R.id.bt_Starting);
		mViewSwitcher = (ViewSwitcher)findViewById(R.id.ecgMain_view_switcher);
	}


	
	private void initEcgSimpleXYPlot() {
		// get handles to our View defined in layout.xml:
		XYPlot plot = (XYPlot) findViewById(R.id.ecgSimpleXYPlot);
		dynamicPlot = new DynamicPlot(appContext,plot);
//		new Thread(data).start();
	}
	

	@SuppressLint("NewApi")
	public void initPanel(){
		LinearLayout panel = (LinearLayout)findViewById(R.id.panelLayout);
		LayoutParams lp = panel.getLayoutParams();
		lp.height=getPanelDiameter();
		lp.width=getPanelDiameter();
		rpm = (RPM)findViewById(R.id.rpm);
		rpm.getLayoutParams().height=(int) (lp.height*0.96f);
		rpm.getLayoutParams().width=(int) (lp.height*0.96f);
		int top = ((FrameLayout.LayoutParams)lp).topMargin;
		((FrameLayout.LayoutParams)rpm.getLayoutParams()).topMargin=(int) (top+lp.height*0.012);// means 5%
	}
	public int getPanelDiameter(){
		WindowManager wm = this.getWindowManager();
		int width = wm.getDefaultDisplay().getWidth();
		return (int) (width*PANEL_SIZE_RATE);
	}

	/**
	 * 监听返回--是否退出程序
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean flag = true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 是否退出应用
			UIHelper.Exit(this);
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			popUpOverflowMenu();
			// 展示快捷栏&判断是否登录
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			// 展示搜索页
//			UIHelper.showSearch(Main.this);
		} else {
			flag = super.onKeyDown(keyCode, event);
		}
		return flag;
	}
	
	
    private View.OnKeyListener keyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                                && keyCode == KeyEvent.KEYCODE_MENU) {
                	if(popMenu!=null)
                		popMenu.dismiss();
                    return true;
                }
                return false;
        }
    };
    
    private View.OnClickListener recordListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub			
			if(popMenu!=null){
				popMenu.dismiss();
			}
			UIHelper.showEcgRecordHistory(Main.this);			
		}
	};
    
    private View.OnClickListener setListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub			
			UIHelper.showSetting(Main.this);	
			
			if(popMenu!=null){
				popMenu.dismiss();
			}
		}
	};
	
	private View.OnClickListener btsetListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub			
				UIHelper.showBTSetting(Main.this);
				if(popMenu!=null){
					popMenu.dismiss();
				}
			}
		};
		
	private View.OnClickListener logoutListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub				
				if(popMenu!=null){
					popMenu.dismiss();
				}
				UIHelper.Exit(Main.this);
			}
		};

    PopupWindow popMenu;

	private void popUpOverflowMenu() {      
		if(popMenu!=null){
			popMenu.dismiss();
			return;
		}
		
        View btn=this.findViewById(R.id.main_head_action);
        int[] location = new int[2];
        btn.getLocationOnScreen(location);        
        int x=5;
        int y=location[1]+btn.getHeight();
        
        final View parentView = this.findViewById(R.id.main);
        
        View popView = getLayoutInflater().inflate(
                R.layout.action_menu, null);
        
        popView.setFocusableInTouchMode(true);
        popView.setOnKeyListener(keyListener);       
        popView.setFocusableInTouchMode(true); //这里很重要，否则再次按menu键时会没反应
        
        popView.findViewById(R.id.linear_record).setOnClickListener(recordListener);
        popView.findViewById(R.id.linear_set).setOnClickListener(setListener);
        popView.findViewById(R.id.linear_btset).setOnClickListener(btsetListener);
        popView.findViewById(R.id.linear_logout).setOnClickListener(logoutListener);
        
        popMenu = new PopupWindow(popView,
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);//popView即popupWindow的布局，ture设置focusAble.
        
        //必须设置BackgroundDrawable后setOutsideTouchable(true)才会有效。这里在XML中定义背景，所以这里设置为null;
        popMenu.setBackgroundDrawable(new BitmapDrawable(getResources(),
                (Bitmap) null));
        popMenu.setFocusable(true);//让导航菜单可以得到焦点
        popMenu.setTouchable(true);
        popMenu.setOutsideTouchable(true);//菜单以外的地方也可以单击
        
        popMenu.setAnimationStyle(android.R.style.Animation_Dialog);    //设置一个动画。
        //设置Gravity，让它显示在右上角。
        popMenu.showAtLocation(parentView, Gravity.RIGHT | Gravity.TOP,
                x, y);  
        popMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
			
			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				popMenu=null;
			}
		});

    }
	
	class DataHandlerThread extends Thread {
		short sigMask; 
		short lastSig;
		final private short dataMask = 0xFFF;
		int sigCount;
		AtomicInteger cseq=new AtomicInteger(0);
		EcgRecord record;
		private boolean isRunning=true;		
		SampleByteBuffer sampleBuffer = new SampleByteBuffer();
		public DataHandlerThread() {
			sigMask = 0; 
			lastSig = sigMask;
			sigCount = 0;
		}
	
		public void stopWorking(){
			isRunning=false;
			try {
				sampleBuffer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			int uiInterval = 0;
			DataEntity de=new DataEntity();
			while (isRunning) {
				BLEMessage message = dataPool.poll();
				
				if (message == null) {
					try {
						Thread.sleep(5);
						uiInterval = uiInterval + 5;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {

					//add it to display buffer
					List<Number> samples = message.getData();
					for (Number sample : samples) {
						dynamicPlot.addSerialData(sample);
						Log.d(TAG, "addSerialData: "+sample);
						sampleBuffer.addShort(sample.shortValue());
					}
					if (uiInterval > (DELAY_TIME*1000)||sampleBuffer.size()>SAMPLE_SIZE) {
						de.data=sampleBuffer.retrieveBytes();
						sendProcessMessage(HANDLER_MSG_CLIENT, CLIENT_SEND_DATA,de);
						cseq.addAndGet(1);
						uiInterval=0;
					}
				}
			}
		}
        public boolean isRunning(){
            return isRunning;
        }
		public void setRecord(EcgRecord record){
			this.record=record;
		}
	}
	
	class TimeShowingThread extends Thread{
		
		private boolean show;
		private Date start;
		private boolean running=true;	
		
		private int HANDLER_MSG_SHOWDUR = 0;
		private int HANDLER_MSG_SHOWSTART = 1;
		
		private DateFormat df=new SimpleDateFormat("HH:mm:ss");
		
		private Handler handler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==HANDLER_MSG_SHOWSTART){
					startTime.setText((String)msg.obj);
				}
				else if(msg.what==HANDLER_MSG_SHOWDUR){
					monitorTime.setText((String)msg.obj);
				}
			}
		};
		
		public void startShowing(Date start){
			show=true;
			this.start=start;			
			handler.sendMessage(handler.obtainMessage(HANDLER_MSG_SHOWSTART, df.format(start)));
		}	
		
		public void stopShowing(){
			show=false;
		}
		
		public void stopRunning(){
			this.running=false;
		}
		
		private String formatDur(long ms){
			long sec=ms/1000;
			int h=(int)sec/3600;
			int m=(int)sec%3600/60;
			int s=(int)sec%60;
			return (h<10?"0":"")+h+":"+(m<10?"0":"")+m+":"+(s<10?"0":"")+s;
		}
		
		@Override
		public void run(){
			while(running){
				if(show){
					handler.sendMessage(handler.obtainMessage(HANDLER_MSG_SHOWDUR, formatDur(System.currentTimeMillis()-start.getTime())));				
				}				
				try{
					Thread.sleep(1000);
				}
				catch(Exception e){
					;
				}
			}			
		}
		
		
	}
	class DataEntity{
		int cseq;
		byte[] data;
		EcgRecord record;
	}
	
	class ShowDetailListener implements OnClickListener{
		private short code;
		public ShowDetailListener(short code) {
			this.code=code;
		}
		public void onClick(View v) {
			if(record!=null&&!StringUtils.isEmptyOr0(record.getAfibNumber())){
				UIHelper.showRecordDetail(v.getContext(),record,code);
			}			
		}
	}

	 Handler statusHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String currStatus= (String) msg.obj;
			if (curStatusText != null){
				curStatusText.setText(currStatus);
			}
			int status=msg.arg1;

			if (status == RFStarBLEService.STATUS_PREPARE_CONNECT){  //准备连接
				statusImgView.setVisibility(View.VISIBLE);
				Glide.with(Main.this).load(R.drawable.prepare_connect).into(statusImgView);
			}else if (status == RFStarBLEService.STATUS_CONNECT){   //已连接
				statusImgView.setVisibility(View.VISIBLE);
				Glide.with(Main.this).load(R.drawable.already_connect).into(statusImgView);
			}else if (status ==RFStarBLEService.STATUS_DISCONNECT){   //断开连接
				statusImgView.setVisibility(View.VISIBLE);
				Glide.with(Main.this).load(R.drawable.icon_disconnect).into(statusImgView);
			}else if (status == RFStarBLEService.STATUS_TRANSPORT){   //传输中
				statusImgView.setVisibility(View.VISIBLE);
				Glide.with(Main.this).load(R.drawable.transporting).into(statusImgView);
			}else {
				statusImgView.setVisibility(View.GONE);
			}
		}
	};
	
	/**
	 * 连接服务
	 */
	private ServiceConnection bleServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			bleService.setOnRecADdata(null);
			bleService = null;
			Log.i(TAG, "unbindBleService");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			// Log.d(BLEApp.KTag, "55 serviceConnected :   服务启动 ");
			bleService = ((RFStarBLEService.LocalBinder) service).getService();

			bleService.setConnectStaustListener(new RFStarBLEService.ConnectStaustListener() {
				@Override
				public void currentStatus(int status,String currentStatus) {
					Message message=Message.obtain();
					message.arg1=status;
					message.obj=currentStatus;
					statusHandler.sendMessage(message);
				}
			});
			bleService.setOnRecADdata(new RFStarBLEService.OnReceiveDataListener() {
				@Override
				public void onReceiveData(BLEMessage msg) {
					Message message=Message.obtain();
					message.obj="Transmitting.....";
					message.arg1=STATUS_TRANSPORT;
					statusHandler.sendMessage(message);
					dataPool.add(msg);
					if(handlerThread==null){
						handlerThread = new DataHandlerThread();
                        handlerThread.start();
					}
				}
			});
			bleService.setOnRec3Xdata(new RFStarBLEService.OnReceiveDataListener() {
                @Override
                public void onReceiveData(BLEMessage msg) {
                    // TODO Auto-generated method stub
                    Log.i(TAG, "receive:" + String.valueOf(msg.getData()));
                }
            });
			bleService.setOnLeadLoss(new RFStarBLEService.OnReceiveDataListener() {
                @Override
                public void onReceiveData(BLEMessage msg) {
                    if (msg.getData().get(0).intValue() > 0)
                        leadLost = true;
                    else
                        leadLost = false;
                }
            });
			bleService.setOnBatLow(new RFStarBLEService.OnReceiveDataListener() {
                @Override
                public void onReceiveData(BLEMessage msg) {
                    // TODO Auto-generated method stub
                    Log.i(TAG, "receive:" + String.valueOf(msg.getData()));
                    lowBatt = true;
//                    BLEMessage
//                    appContext.sendBLEMessage();
                }
            });
            bleService.setActionCallback(new RFStarBLEService.OnReceiveDataListener() {
                @Override
                public void onReceiveData(BLEMessage msg) {
                    // TODO Auto-generated method stub
                    Log.i(TAG, "receive:" + String.valueOf(msg.getData()));
                    boolean startBLE = false;
                    switch (msg.getType()) {
                        case BLEMessage.START_BLE_TRANSFER:
                            startBLE = true;
                            break;
                        case BLEMessage.STOP_BLE_TRANSFER:
                            break;
                        case BLEMessage.START_FLASH_WRITE:
                        case BLEMessage.STOP_FLASH_WRITE:
                        default:
                            Log.d(TAG, "unkown message " + msg.toString());
                    }
                    if (startBLE && handlerThread == null) {
                        handlerThread = new DataHandlerThread();
                        handlerThread.start();
                    } else if (!startBLE && handlerThread != null) {
                        handlerThread.stopWorking();
                        handlerThread = null;
                    } else {
                        Log.i(TAG, "no action on handlerThread");
                    }
                }
            });
            Log.i(TAG, "bindBleService");
		}
	};
	
}
	

