package com.healthme.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.healthme.app.AppContext;
import com.healthme.app.R;
import com.healthme.app.adapter.ListViewBluetoothAdapter;
import com.healthme.app.bean.CustomizedBluetoothDevice;
import com.healthme.app.service.RFStarBLEService;
import com.healthme.app.widget.PullToRefreshListView;

public class BTDeviceSet extends BaseActivity implements BluetoothAdapter.LeScanCallback {

	private AppContext appContext;
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
	
	RFStarBLEService bleService;
	
	// Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

//	private int curBluetoothCatalog = 0;
//
//	private Button framebtn_Question_ask;
//	private Button framebtn_Question_share;

	private int lvSize;

	private boolean alive=true;
	
	private TextView selected_device;

//	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//
//			// When discovery finds a device
//			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//				// Get the BluetoothDevice object from the Intent
//				BluetoothDevice device = intent
//						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//				// If it's already paired, skip it, because it's been listed
//				// already
//				CustomizedBluetoothDevice mDevice = new CustomizedBluetoothDevice(
//						device);
//				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//
//					if (list.contains(mDevice) == false) {
//						list.add(mDevice);
//						adapter.notifyDataSetChanged();
//					}
//				}
//			}
//			else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
//				int prevBondState = intent.getIntExtra(
//						BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
//				int bondState = intent.getIntExtra(
//						BluetoothDevice.EXTRA_BOND_STATE, -1);
//
//				if (prevBondState == BluetoothDevice.BOND_BONDED
//						&& bondState == BluetoothDevice.BOND_NONE) {
//					BluetoothDevice device = intent
//							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//					BluetoothDevice select=appContext.getBluetoothDevice();
//					if(select!=null&&device.getAddress().equals(select.getAddress())){
//						appContext.setBluetoothDevice(null);
//					}
//				}
//				loadLvBluetoothData(curBluetoothCatalog,0,UIHelper.LISTVIEW_ACTION_REFRESH);
//			}
//		}
//	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frame_btset);
		appContext = (AppContext) this.getApplication();		
		
		this.initHead(R.string.main_menu_btsetting);

		selected_device = (TextView) findViewById(R.id.selected_device);
		
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
		
		bindService(new Intent(this, RFStarBLEService.class), bleServiceConnection,
				Service.BIND_AUTO_CREATE);
		
//		appContext.setRFstarBLEManagerListener(this);
//		appContext.startScanBluetoothDevice(this,true);
//		initBluetoothListData();
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

//	private void loadLvBluetoothData(final int catalog, final int pageIndex,
//			final int action) {
//		// mHeadProgress.setVisibility(ProgressBar.VISIBLE);
//		new Thread() {
//			public void run() {
//				Message msg = new Message();
//				boolean isRefresh = false;
//				if (action == UIHelper.LISTVIEW_ACTION_REFRESH
//						|| action == UIHelper.LISTVIEW_ACTION_SCROLL)
//					isRefresh = true;
//				try {
//					BluetoothDeviceList list = appContext
//							.getBluetoothDeviceList(catalog, pageIndex,
//									isRefresh);
//					msg.what = list.getPageSize();
//					msg.obj = list;
//				} catch (AppException e) {
//					e.printStackTrace();
//					msg.what = -1;
//					msg.obj = e;
//				}
//				msg.arg1 = action;
//
//				handler.sendMessage(msg);
//			}
//		}.start();
//	}
//
//	private void initBluetoothListData() {
//		handler = new Handler() {
//			public void handleMessage(Message msg) {
//				if (msg.what >= 0) {
//					// listview数据处理
//					Notice notice = handleLvData(msg.what, msg.obj, msg.arg1);
//
//					if (msg.what < AppContext.PAGE_SIZE) {
//						listview.setTag(UIHelper.LISTVIEW_DATA_FULL);
//						adapter.notifyDataSetChanged();
//						lv_footer_more.setText(R.string.load_full);
//					} else if (msg.what == AppContext.PAGE_SIZE) {
//						listview.setTag(UIHelper.LISTVIEW_DATA_MORE);
//						adapter.notifyDataSetChanged();
//						lv_footer_more.setText(R.string.load_more);
//					}
//					// 发送通知广播
//					if (notice != null) {
//						UIHelper.sendBroadCast(listview.getContext(), notice);
//					}
//					// // 是否清除通知信息
//					// if (isClearNotice) {
//					// ClearNotice(curClearNoticeType);
//					// isClearNotice = false;// 重置
//					// curClearNoticeType = 0;
//					// }
//				} else if (msg.what == -1) {
//					// 有异常--显示加载出错 & 弹出错误消息
//					listview.setTag(UIHelper.LISTVIEW_DATA_MORE);
//					lv_footer_more.setText(R.string.load_error);
//					((AppException) msg.obj).makeToast(BTDeviceSet.this);
//				}
//				if (adapter.getCount() == 0) {
//					listview.setTag(UIHelper.LISTVIEW_DATA_EMPTY);
//					lv_footer_more.setText(R.string.load_empty);
//				}
//				lv_footer_progress.setVisibility(ProgressBar.GONE);
//				// mHeadProgress.setVisibility(ProgressBar.GONE);
//				if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH) {
//					listview.onRefreshComplete(getString(R.string.pull_to_refresh_update)
//							+ new Date().toLocaleString());
//					listview.setSelection(0);
//				} else if (msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG) {
//					listview.onRefreshComplete();
//					listview.setSelection(0);
//				}
//
//			}
//		};
//	}
//
//	private Notice handleLvData(int what, Object obj, int actiontype) {
//		Notice notice = null;
//		if (actiontype == UIHelper.LISTVIEW_ACTION_INIT
//				|| actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
//
//			int newdata = 0;
//			BluetoothDeviceList plist = (BluetoothDeviceList) obj;
//			// notice = BluetoothDeviceList.getNotice();
//			lvSize = what;
//			if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
//				if (list.size() > 0) {
//					for (CustomizedBluetoothDevice post1 : plist
//							.getBluetoothDevicelist()) {
//						boolean b = false;
//						for (CustomizedBluetoothDevice post2 : list) {
//							if (post1.getAddress() == post2.getAddress()) {
//								b = true;
//								break;
//							}
//						}
//						if (!b)
//							newdata++;
//					}
//				} else {
//					newdata = what;
//				}
//			}
//			list.clear();// 先清除原有数�?
//			list.addAll(plist.getBluetoothDevicelist());
//
//			if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
////				// 提示新加载数据
////				if (newdata > 0) {
////					NewDataToast
////							.makeText(
////									this,
////									getString(R.string.new_data_toast_message,
////											newdata), appContext.isAppSound())
////							.show();
////				} else {
////					NewDataToast.makeText(this,
////							getString(R.string.new_data_toast_none), false)
////							.show();
////				}
//			}
//		} else if (actiontype == UIHelper.LISTVIEW_ACTION_SCROLL) {
//			BluetoothDeviceList plist = (BluetoothDeviceList) obj;
//			notice = plist.getNotice();
//			lvSize += what;
//			if (list.size() > 0) {
//				for (CustomizedBluetoothDevice post1 : plist
//						.getBluetoothDevicelist()) {
//					boolean b = false;
//					for (CustomizedBluetoothDevice post2 : list) {
//						if (post1.getAddress() == post2.getAddress()) {
//							b = true;
//							break;
//						}
//					}
//					if (!b)
//						list.add(post1);
//				}
//			} else {
//				list.addAll(plist.getBluetoothDevicelist());
//			}
//		}
//		return notice;
//	}
	
	@Override
	protected void onResume(){
		super.onResume();
		matchDev=appContext.getBluetoothDevice();
		scanLeDevice(true);
	}
	
	@Override
	protected void onDestroy() {
		alive=false;
		this.unbindService(bleServiceConnection);
		super.onDestroy();
//		appContext.stopScanBluetoothDevice();
//		unregisterReceiver(tweetReceiver);
//		if (bluetoothAdapter != null) {
//			bluetoothAdapter.cancelDiscovery();
//		}
		// Unregister broadcast listeners
		//this.unregisterReceiver(mReceiver);		
	}

//	private void initFrameButton() {
//		// 初始化按钮控件
//
//		framebtn_Question_ask = (Button) findViewById(R.id.frame_btn_question_ask);
//		framebtn_Question_share = (Button) findViewById(R.id.frame_btn_question_share);
//
//		framebtn_Question_ask.setEnabled(false);
//		framebtn_Question_ask.setOnClickListener(frameQuestionBtnClick(
//				framebtn_Question_ask, 0));
//		framebtn_Question_share.setOnClickListener(frameQuestionBtnClick(
//				framebtn_Question_share, 1));
//	}
//
//	private View.OnClickListener frameQuestionBtnClick(final Button btn,
//			final int catalog) {
//		return new View.OnClickListener() {
//			public void onClick(View v) {
//				curBluetoothCatalog = catalog;
//				loadLvBluetoothData(curBluetoothCatalog, 0,
//						UIHelper.LISTVIEW_ACTION_REFRESH);
//
//			}
//		};
//	}
	
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
			bluetoothAdapter.startLeScan(BTDeviceSet.this);
		} else {
			Log.i(TAG,"stop scan...");
			listview.onRefreshComplete();
			lv_footer_progress.setVisibility(View.GONE);
			lv_footer_more.setText(list.size()==0?R.string.scan_empty:R.string.scan_full);
			bluetoothAdapter.stopLeScan(BTDeviceSet.this);
			scanning = false;
			if(bleService.getCurBleDevice()!=null&&!scannedDevice.contains(bleService.getCurBleDevice())){
				onLeScan(bleService.getCurBleDevice(),0,null);
			}
		}
	}
	
	private ServiceConnection bleServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			bleService = null;
			Log.i(TAG, "unbindBleService");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			bleService = ((RFStarBLEService.LocalBinder) service).getService();
		}
	};

}
