package com.healthme.app.ui;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.healthme.app.AppContext;
import com.healthme.app.AppException;
import com.healthme.app.R;
import com.healthme.app.bean.Result;
import com.healthme.app.bean.User;

public class ResetPassword extends Activity{
	private Button complete;	//完成按钮
	private EditText pass;		//密码
	private String 	phone;		//电话
	private StringBuffer buffer;//错误提示
	private Handler handler;	
	private AnimationDrawable loadingAnimation;
	private static final int DATAPICKER=1;
	
	public static ResetPassword setPassActivity;
	private View loginLoading;
	private ViewSwitcher mViewSwitcher;
	private String verifyCode;
	private EditText repeatPassword;
	private String userName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reset_password);
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.arg1) {
				case 0:
					Toast.makeText(ResetPassword.this, "密码修改成功！", Toast.LENGTH_LONG).show();
					ResetActivity.resetActivity.finish();
					ResetCodeActivity.resetCodeActivity.finish();
					ResetPassword.setPassActivity.finish();
					break;
				case 1:
					mViewSwitcher.showPrevious();
					Toast.makeText(ResetPassword.this, "密码修改失败！", Toast.LENGTH_LONG).show();
					break;
				case -1:
					mViewSwitcher.showPrevious();
					Toast.makeText(ResetPassword.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}
		};
		init();
		allListener();
		verifyCode = getIntent().getStringExtra("verifyCode");
		setPassActivity=this;
	}
	
	
	
	private void init() {
		complete = (Button) findViewById(R.id.ok);
		pass = (EditText) findViewById(R.id.pass);
		repeatPassword = (EditText) findViewById(R.id.repeatPassword);
		loginLoading = (View)findViewById(R.id.login_loading);
		mViewSwitcher = (ViewSwitcher)findViewById(R.id.logindialog_view_switcher);
        
		phone=getIntent().getStringExtra("phone");
		verifyCode = getIntent().getStringExtra("code");
		userName = getIntent().getStringExtra("userName");
		
		//fake data
		pass.setText("admin1");
		repeatPassword.setText("admin1");
		
	}
	private void allListener(){
		complete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				boolean isFormat = isFormat();
				if (isFormat) {
					Toast.makeText(ResetPassword.this, buffer.toString(),
							Toast.LENGTH_LONG).show();
				} else {
					loadingAnimation = (AnimationDrawable)loginLoading.getBackground();
			        loadingAnimation.start();
			        mViewSwitcher.showNext();
					new Thread(new Register()).start();
				}
			}

		});
	}

	
   
	
	@SuppressLint("NewApi")
	private boolean isFormat() {
		buffer = null;
		buffer = new StringBuffer();
		String strPass = pass.getText().toString().trim();
		String strRepeatPassword = repeatPassword.getText().toString().trim();
		if (strPass.length() < 6 || strPass.length() > 16) {
			buffer.append("密码格式错误;\r\n");
		}
		if (strRepeatPassword.length() < 6 || strRepeatPassword.length() > 16) {
			buffer.append("重复密码格式错误;\r\n");
		}
		if (buffer.toString().length() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	class Register implements Runnable{

		@Override
		public void run() {
			String p1 = pass.getText().toString().trim();
			String p2 = repeatPassword.getText().toString().trim();
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("phone", phone);
			map.put("userName", userName);
			map.put("code", verifyCode);
			map.put("password", p1);
			map.put("repeatPassword", p2);
			Message msg = new Message();
			msg.arg1 = 1;
			try {
				buffer = new StringBuffer();
				if(p1.equals("")||
						p2.equals("")||
						!p1.equals(p2)){
					buffer.append("密码格式错误;\r\n");
					handler.sendMessage(msg);
            		return ;
				}
				
				AppContext ac = (AppContext)getApplication(); 
                User user = ac.changePassword(map);
                Result res = user.getValidate();
                if(res.OK()){
                	ac.saveLoginInfo(user);//保存登录信息
                	msg.what = 1;//成功
                	msg.obj = user;
                	msg.arg1=0;
                }else{
                	ac.cleanLoginInfo();//清除登录信息
                	msg.what = 0;//失败
                	msg.obj = res.getErrorMessage();
                }
            } catch (AppException e) {
            	e.printStackTrace();
		    	msg.what = -1;
		    	msg.obj = e.getMessage();
            } finally {
			}
			handler.sendMessage(msg);
			
		}
			
		
	}
}
