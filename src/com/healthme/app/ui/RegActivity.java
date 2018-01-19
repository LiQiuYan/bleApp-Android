package com.healthme.app.ui;

import java.util.HashMap;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.healthme.app.AppContext;
import com.healthme.app.AppManager;
import com.healthme.app.R;
import com.healthme.app.api.ApiClient;
import com.healthme.app.bean.ResponseBase;
import com.healthme.common.util.RegexUtil;

public class RegActivity extends Activity {
	private Button next;
	private EditText phone;
	private CheckBox che;
	private Handler handler;
	private Message msg;
	private ProgressBar bar;
	private InputMethodManager im;
	public AppContext appContext;
	public static RegActivity regActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.verify_phone);
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					Toast.makeText(RegActivity.this, "发送验证短信成功！", Toast.LENGTH_LONG).show();
					bar.setVisibility(View.GONE);
					Intent intent = new Intent(RegActivity.this,CodeActivity.class);
					intent.putExtra("phone", phone.getText().toString().trim());
//					intent.putExtra("identifier", msg.obj.toString());
					startActivity(intent);
					break;
				case 1:
					Toast.makeText(RegActivity.this, "发送验证短信失败！", Toast.LENGTH_LONG).show();
					bar.setVisibility(View.GONE);
					break;
				default:
					Toast.makeText(RegActivity.this, "未知错误！", Toast.LENGTH_LONG).show();
					bar.setVisibility(View.GONE);
					break;
				}
				super.handleMessage(msg);
			}
		};
		init();
		listener();
		regActivity=this;
		AppManager.getAppManager().addActivity(this);
	}
	
	private void init(){
		phone = (EditText) findViewById(R.id.phone);
		che = (CheckBox) findViewById(R.id.agreement);
		next = (Button) findViewById(R.id.next);
		bar = (ProgressBar) findViewById(R.id.progressBar1);
		im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		EditText phone_addr = (EditText) findViewById(R.id.phone_addr);
		phone_addr.setEnabled(false);
		phone.setText("");
		che.setChecked(true);
	}
	
	
	private void listener(){
		next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), 0);
				if (isNull()) {
					bar.setVisibility(View.VISIBLE);
					new Thread(new G()).start();
				}else{
					Toast.makeText(RegActivity.this, "格式错误！", Toast.LENGTH_LONG).show();
				}
			}
		});
		
//		phone.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
//				im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
//			}
//		});
//		xiayibu.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View arg0, MotionEvent arg1) {
//				
//				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
////					xiayibu.setBackgroundResource(R.drawable.button_s);
//				}else if(arg1.getAction() == MotionEvent.ACTION_UP){
////					xiayibu.setBackgroundResource(R.drawable.button);
//					if (isNull()) {
//						bar.setVisibility(View.VISIBLE);
//						new Thread(new G()).start();
//					}else{
//						Toast.makeText(RegActivity.this, "格式错误！", Toast.LENGTH_LONG).show();
//					}
//				}
//				return false;
//			}
//		});
		
		
	}
	private boolean isNull(){
		String number = phone.getText().toString().trim();
		if (number.equals("") ||!che.isChecked()||!RegexUtil.isMobileNum(number)) {
			return false;
		}else{
			return true;
		}
	}
	class G implements Runnable{
		@Override
		public void run() {
			Message message = new Message();
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("phone", phone.getText().toString().trim());	
			appContext = (AppContext) getApplication();
			try {
				ResponseBase result = ApiClient.sendSMS(appContext,map);
				if (result.getCode()==HttpStatus.SC_OK) {
					message.what = 0;
				}else{
					message.what = 1;
				}
			} catch (Exception e) {
				message.what = 1;
			}
			handler.sendMessage(message);

		}
	}
}
