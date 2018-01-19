package com.healthme.app.ui;

import java.util.HashMap;

import org.apache.commons.httpclient.HttpStatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.healthme.app.AppContext;
import com.healthme.app.R;
import com.healthme.app.api.ApiClient;
import com.healthme.app.bean.ResponseBase;

public class ResetActivity extends Activity {
	private EditText userName;
	private String name;
	private String phone;
	private Handler handler;
	private Message msg;
	private ProgressBar bar;
	private InputMethodManager im;
	private Button next;
	public static ResetActivity resetActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reset_verify);
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					Toast.makeText(ResetActivity.this, "发送验证短信成功！", Toast.LENGTH_LONG).show();
					bar.setVisibility(View.GONE);
					Intent intent = new Intent(ResetActivity.this,ResetCodeActivity.class);
					intent.putExtra("phone", phone);
					intent.putExtra("userName", name);
					startActivity(intent);
					break;
				case 1:
					Toast.makeText(ResetActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
					bar.setVisibility(View.GONE);
					break;
				default:
					Toast.makeText(ResetActivity.this, "未知错误！", Toast.LENGTH_LONG).show();
					bar.setVisibility(View.GONE);
					break;
				}
				super.handleMessage(msg);
			}
		};
		init();
		listener();
		resetActivity=this;
	}
	
	private void init(){
		userName = (EditText) findViewById(R.id.name);
		next = (Button) findViewById(R.id.next);
		bar = (ProgressBar) findViewById(R.id.progressBar1);
		im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		userName.setText("healthme-313");
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
					Toast.makeText(ResetActivity.this, "用户名不能为空！", Toast.LENGTH_LONG).show();
				}
			}
		});
		
	}
	private boolean isNull(){
		if (userName.getText().toString().trim().equals("")) {
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
			name=userName.getText().toString().trim();
			map.put("userName", name);	
			AppContext appContext = (AppContext) getApplication();
			try {
				ResponseBase result = ApiClient.sendSMS(appContext,map);
				if (result.getCode()==HttpStatus.SC_OK) {
					phone=result.getHeader("phone");
					message.what = 0;
				}else if(result.getCode()==HttpStatus.SC_PRECONDITION_FAILED){
					message.what = 1;
					message.obj="用户名不存在！";
				}
			} catch (Exception e) {
				e.printStackTrace();
				message.what = 1;
				message.obj="网络连接错误！";
			}
			handler.sendMessage(message);

		}
	}
}
