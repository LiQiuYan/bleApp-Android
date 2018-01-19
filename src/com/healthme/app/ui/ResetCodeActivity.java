package com.healthme.app.ui;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

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

import com.healthme.app.R;

public class ResetCodeActivity extends Activity {
	public   Handler handler;
	private Button next;
	private EditText code;
	private EditText send;
	private String codestr;
//	private String phone;
//	private String identifier;
	private TextView tishi;
	private ProgressBar bar;
	private InputMethodManager im;
	private String phone;
	private String userName;
	public static ResetCodeActivity resetCodeActivity;
	public ResetCodeActivity() {
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reset_code);
		
		init();
		allListener();
		tishi.setText("我们已给您注册时的手机发送了一条验证短信。");
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
						Toast.makeText(ResetCodeActivity.this, "短信验证成功！", Toast.LENGTH_LONG).show();
						codestr = code.getText().toString().trim();
						Intent intent = new Intent(ResetCodeActivity.this,ResetPassword.class);
						intent.putExtra("phone", phone);
						intent.putExtra("verifyCode", codestr);
						intent.putExtra("userName", userName);
						startActivity(intent);
					}else{
						bar.setVisibility(View.GONE);
						Toast.makeText(ResetCodeActivity.this, "短信验证失败！", Toast.LENGTH_LONG).show();
					}
					break;
				case 2:
					if (msg.what == 0) {
						bar.setVisibility(View.GONE);
						Toast.makeText(ResetCodeActivity.this, "短信发送成功！", Toast.LENGTH_LONG).show();
					}else{
						bar.setVisibility(View.GONE);
						Toast.makeText(ResetCodeActivity.this, "短信发送失败！", Toast.LENGTH_LONG).show();
					}
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
		new Thread(new Times()).start();
		resetCodeActivity=this;
	}

	/**
	 * 初始化
	 */
	private void init() {
		phone=getIntent().getStringExtra("phone");
		userName=getIntent().getStringExtra("userName");
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
//					next.setBackgroundResource(R.drawable.button_s);
//					next.setTextColor(getResources().getColor(R.color.white));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
//					next.setTextColor(getResources().getColor(R.color.white));
//					next.setBackgroundResource(R.drawable.button);
					bar.setVisibility(View.VISIBLE);
					new Thread(new G()).start();
				}
				return false;
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
//					next.setBackgroundResource(R.drawable.button);
//					next.setTextColor(getResources().getColor(R.color.white));
				} else {
					next.setEnabled(false);
//					next.setBackgroundResource(R.drawable.code_btn);
//					next.setTextColor(getResources().getColor(R.color.gray));
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (code.getText().toString().trim().length() == 6) {
					next.setEnabled(true);
//					next.setBackgroundResource(R.drawable.button);
//					next.setTextColor(getResources().getColor(R.color.white));
				} else {
					next.setEnabled(false);
//					next.setBackgroundResource(R.drawable.code_btn);
//					next.setTextColor(getResources().getColor(R.color.gray));
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
//			String result = Utils.getData(map, "GBK");
//			System.out.println(result);
//			JSONObject jsonObject;
			try {
//				jsonObject = new JSONObject(result);
//				int codes = jsonObject.getInt("code");
				
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
			HashMap<String, String> map = new HashMap<String, String>();
			try {
				message.arg2 = 2;
				message.what = 0;
				handler.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
