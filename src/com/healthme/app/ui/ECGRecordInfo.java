package com.healthme.app.ui;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.healthme.app.AppContext;
import com.healthme.app.AppException;
import com.healthme.app.R;
import com.healthme.app.bean.EcgRecord;
import com.healthme.app.common.StringUtils;
import com.healthme.app.common.UIHelper;
import com.healthme.app.widget.LoadingDialog;
import com.healthme.ecg.HMECGCODES;

public class ECGRecordInfo extends BaseActivity {

	private ImageView back;
	
	private TextView start_time;
	private TextView end_time;
	private TextView duration;
	private TextView rate_avg;
	private TextView rate_max;
	private TextView rate_min;
	private TextView total_heartbeat_num;
	private TextView pvc_num;
	private TextView svpb_num;
	private TextView pause_num;
	private TextView afib_num;
	private LinearLayout pvc_info;
	private LinearLayout svpb_info;
	private LinearLayout pause_info;
	private LinearLayout afib_info;
	private LoadingDialog loading;
	private EcgRecord record;
	private Handler mHandler;

	private ImageView refresh;	
	private int recordId;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_info);

		ArrayList<EcgRecord> records=(ArrayList<EcgRecord>) getIntent().getSerializableExtra("ListObject");
		record=records.get(0);
		recordId=record.getId();
		if(record.getStatus().equals("DOING")){
			recordId=record.getId();
			record=null;
		}
		// 初始化视图控件
		this.initView();
		// 初始化视图数据
		this.initData();
	}

	private void initView() {
//		back = (ImageView) findViewById(R.id.record_info_back);
//		back.setOnClickListener(UIHelper.finish(this));
//
//		refresh = (ImageView) findViewById(R.id.record_info_refresh);
//		refresh.setOnClickListener(refreshClickListener);
		initHead(R.string.main_menu_recordinfo);
		
		start_time = (TextView) findViewById(R.id.record_start_time);
		end_time = (TextView) findViewById(R.id.record_end_time);
		duration = (TextView) findViewById(R.id.record_duration);
		rate_avg = (TextView) findViewById(R.id.record_rate_avg);
		rate_max = (TextView) findViewById(R.id.record_rate_max);
		rate_min = (TextView) findViewById(R.id.record_rate_min);
		total_heartbeat_num = (TextView) findViewById(R.id.total_heartbeat_num);
		pvc_num = (TextView) findViewById(R.id.record_pvc_num);
		svpb_num = (TextView) findViewById(R.id.record_svpb_num);
		pause_num = (TextView) findViewById(R.id.record_pause_num);
		afib_num = (TextView) findViewById(R.id.record_afib_num);
		
		pvc_info = (LinearLayout) findViewById(R.id.record_pvc_info);
		pvc_info.setOnClickListener(pvcInfoClickListener);
		svpb_info = (LinearLayout) findViewById(R.id.record_svpb_info);
		svpb_info.setOnClickListener(svpbInfoClickListener);
		pause_info = (LinearLayout) findViewById(R.id.record_pause_info);
		pause_info.setOnClickListener(pauseInfoClickListener);
		afib_info = (LinearLayout) findViewById(R.id.record_afib_info);
		afib_info.setOnClickListener(afibInfoClickListener);		
	}

	private void initData() {
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (loading != null)
					loading.dismiss();

				if (msg.what == 1 && msg.obj != null) {
					record = (EcgRecord) msg.obj;

					// 其他资料
					if(null==record.getStartTime()){
						start_time.setText("未知");
					}
					else
						start_time.setText(StringUtils.formatDate(record.getStartTime()));
					if(null==record.getEndTime()){
						end_time.setText("未知");
					}else
						end_time.setText(StringUtils.formatDate(record.getEndTime()));
					if(record.getStartTime()!=null&&record.getEndTime()!=null){
						duration.setText(StringUtils.formatTimeDurationCH(record.getEndTime().getTime()-record.getStartTime().getTime()));
					}else
						duration.setText("检测中...");
					if(!StringUtils.isEmptyOr0(record.getAverageHeartbeat())){
						rate_avg.setText(record.getAverageHeartbeat()+"");
					}
					if(!StringUtils.isEmptyOr0(record.getMaxHeartbeat())){
						rate_max.setText(record.getMaxHeartbeat()+"");
					}
					if(!StringUtils.isEmptyOr0(record.getMinHeartbeat())){
						rate_min.setText(record.getMinHeartbeat()+"");
					}
					
					if(!StringUtils.isEmptyOr0(record.getTotalBeatNumber())){
						total_heartbeat_num.setText(record.getTotalBeatNumber()+"");
					}
					
					if(!StringUtils.isEmptyOr0(record.getTotalPvcNumber())){
						pvc_num.setText(record.getTotalPvcNumber()+"");
					}
					
					if(!StringUtils.isEmptyOr0(record.getTotalSvpbNumber())){
						svpb_num.setText(record.getTotalSvpbNumber()+"");						
					}
					if(!StringUtils.isEmptyOr0(record.getPauseNumber())){
						pause_num.setText(record.getPauseNumber()+"");						
					}
					if(!StringUtils.isEmptyOr0(record.getAfibNumber())){
						afib_num.setText(record.getAfibNumber()+"");
					}					

				} else if (msg.obj != null) {
					((AppException) msg.obj).makeToast(ECGRecordInfo.this);
				} else if (msg.what== -1) {
					AppContext appContext=(AppContext) getApplication();
					Toast.makeText(appContext, "没有记录",	Toast.LENGTH_SHORT).show();
				}
			}
		};
		this.loadRecordInfoThread(false);
	}

	private void loadRecordInfoThread(final boolean isRefresh) {
		loading = new LoadingDialog(this);
		loading.show();

		new Thread() {
			public void run() {
				Message msg = new Message();
				msg.what = -1;
				if(record!=null){
					msg.what = 1;
					msg.obj = record;
				}
				AppContext appContext=(AppContext) getApplication();
				try {				
					EcgRecord record1=appContext.getRecordDetail(recordId, isRefresh);
					if(record1!=null){
						msg.what = 1;
						msg.obj = record1;						
					}
				} catch (AppException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					msg.what = 0;
					msg.obj = e;
				}
				
				mHandler.sendMessage(msg);
			}
		}.start();
	}


	private View.OnClickListener refreshClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			loadRecordInfoThread(true);
		}
	};

	private View.OnClickListener pvcInfoClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if(record!=null&&!StringUtils.isEmptyOr0(record.getTotalPvcNumber())){
				UIHelper.showRecordDetail(v.getContext(),record,HMECGCODES.PVC);
			}			
		}
	};
	
	private View.OnClickListener svpbInfoClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if(record!=null&&!StringUtils.isEmptyOr0(record.getTotalSvpbNumber())){
				UIHelper.showRecordDetail(v.getContext(),record,HMECGCODES.SVPB);
			}			
		}
	};
	
	private View.OnClickListener pauseInfoClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if(record!=null&&!StringUtils.isEmptyOr0(record.getPauseNumber())){
				UIHelper.showRecordDetail(v.getContext(),record,HMECGCODES.PAUSE);
			}			
		}
	};
	
	private View.OnClickListener afibInfoClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if(record!=null&&!StringUtils.isEmptyOr0(record.getAfibNumber())){
				UIHelper.showRecordDetail(v.getContext(),record,HMECGCODES.AFIB);
			}			
		}
	};


}
