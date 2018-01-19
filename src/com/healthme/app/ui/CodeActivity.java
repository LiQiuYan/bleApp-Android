package com.healthme.app.ui;

import java.util.HashMap;

import org.apache.commons.httpclient.HttpStatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.healthme.app.AppContext;
import com.healthme.app.R;
import com.healthme.app.api.ApiClient;
import com.healthme.app.bean.ResponseBase;

public class CodeActivity extends Activity {
	public   Handler handler;
	private Button next;
	private EditText code;
	private EditText send;
	private String codestr;
	private String phone;
//	private String identifier;
	private TextView tishi;
	private ProgressBar bar;
	private InputMethodManager im;
	public static CodeActivity codeActivity;
	public CodeActivity() {
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.code_layout);
		phone = getIntent().getStringExtra("phone");
//		identifier = getIntent().getStringExtra("identifier");
		init();
		allListener();
		tishi.setText("我们已给你的手机号码 "+phone+" 发送了一条验证短信。");
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.arg2) {
				case 0:
					send.setEnabled(false);
					send.setTextColor(getResources().getColor(R.color.gray));
					send.setText("重新发送(" + msg.arg1 + ")");
					if (msg.arg1 == 1) {
						send.setEnabled(true);
						send.setText("重新发送");
						send.setTextColor(getResources().getColor(R.color.blue));
					}
					break;
				case 1:
					if (msg.what == 0) {
						bar.setVisibility(View.GONE);
//						Toast.makeText(CodeActivity.this, "短信验证成功！", Toast.LENGTH_LONG).show();
						Intent intent = new Intent(CodeActivity.this,SetPassActivity.class);
						intent.putExtra("phone", phone);
						intent.putExtra("code", codestr);
						startActivity(intent);
					}else{
						bar.setVisibility(View.GONE);
						Toast.makeText(CodeActivity.this, "短信验证失败！", Toast.LENGTH_LONG).show();
					}
					break;
				case 2:
					if (msg.what == 0) {
						bar.setVisibility(View.GONE);
						Toast.makeText(CodeActivity.this, "短信发送成功！", Toast.LENGTH_LONG).show();
					}else{
						bar.setVisibility(View.GONE);
						Toast.makeText(CodeActivity.this, "短信发送失败！", Toast.LENGTH_LONG).show();
					}
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
		new Thread(new Times()).start();
		codestr = code.getText().toString().trim();
		codeActivity=this;
	}

	/**
	 * 初始化
	 */
	private void init() {
		next = (Button) findViewById(R.id.next);
		code = (EditText) findViewById(R.id.code);
		send = (EditText) findViewById(R.id.send);
		send.setInputType(InputType.TYPE_NULL); 
		bar = (ProgressBar) findViewById(R.id.bar2);
		tishi = (TextView) findViewById(R.id.tishi);
		im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	private void allListener() {
		send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				bar.setVisibility(View.VISIBLE);
				new Thread(new C()).start();
				new Thread(new Times()).start();
			}
		});
		next.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
				
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					bar.setVisibility(View.VISIBLE);
					codestr = code.getText().toString().trim();
					new Thread(new G()).start();
				}
				return true;
			}
		});
		code.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (code.getText().toString().trim().length() == 6) {
					next.setEnabled(true);
				} else {
					next.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				if (code.getText().toString().trim().length() == 6) {
					next.setEnabled(true);
				} else {
					next.setEnabled(false);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (code.getText().toString().trim().length() == 6) {
					next.setEnabled(true);
				} else {
					next.setEnabled(false);
				}
			}
		});
	}

	
	class G implements Runnable{
		@Override
		public void run() {
			Message message = new Message();
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("phone", phone);	
			map.put("code", code.getText().toString().trim());
			try {
				//fake code
				int codes=0;
				message.arg2 = 1;
				message.what = codes;
				handler.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	class Times implements Runnable{
		@Override
		public void run() {

			int a = 60;
			while(true){
				try {
					Thread.sleep(1000);
					Message message = new Message();
					message.arg2 = 0;
					message.arg1 = a;
					handler.sendMessage(message);
					if (a == 1) {
						break;
					}
					a = a-1;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
					
		}
	}
	class C implements Runnable{
		@Override
		public void run() {
			Message message = new Message();
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("phone", phone);	
			try {
				
				AppContext appContext = (AppContext) getApplication();
				ResponseBase result = ApiClient.sendSMS(appContext, map);
				message.arg2 = 2;
				if (result.getCode() == HttpStatus.SC_OK) {
					message.what = 0;
				}else{
					message.what = 1;
				}
			} catch (Exception e) {
				e.printStackTrace();
				message.what = 1;
			}
			handler.sendMessage(message);

		}
	}
}
