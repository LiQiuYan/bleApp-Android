package com.healthme.app.ui;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.healthme.app.AppContext;
import com.healthme.app.AppException;
import com.healthme.app.R;
import com.healthme.app.bean.Result;
import com.healthme.app.bean.User;
import com.healthme.app.common.StringUtils;

public class SetPassActivity extends Activity{
	private Button complete;	//完成按钮
	private EditText pass;		//密码
	private EditText name;		//昵称
	private String 	phone;		//电话
	private EditText real_Name;	//真实名称
	private EditText email;	//身份证
	private StringBuffer buffer;//错误提示
	private Handler handler;	
	private RadioGroup sex;
	private String sexStr;
	private EditText birthday;
	private AnimationDrawable loadingAnimation;
	private static final int DATAPICKER=1;
	
	public static SetPassActivity setPassActivity;
	private View loginLoading;
	private ViewSwitcher mViewSwitcher;
	private String verifyCode;
	private RadioButton maleButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setpass_layout);
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.arg1) {
				case 0:
					Toast.makeText(SetPassActivity.this, "注册成功！", Toast.LENGTH_LONG).show();
					RegActivity.regActivity.finish();
					CodeActivity.codeActivity.finish();
					SetPassActivity.setPassActivity.finish();
					break;
				case 1:
					mViewSwitcher.showPrevious();
					Toast.makeText(SetPassActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
					break;
				case -1:
					mViewSwitcher.showPrevious();
					Toast.makeText(SetPassActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}
		};
		init();
		allListener();
		phone = getIntent().getStringExtra("phone");
		verifyCode = getIntent().getStringExtra("code");
		setPassActivity=this;
	}
	
	
	
	private void init() {
		complete = (Button) findViewById(R.id.ok);
		pass = (EditText) findViewById(R.id.pass);
		name = (EditText) findViewById(R.id.name);
		real_Name = (EditText) findViewById(R.id.real_name);
		email = (EditText) findViewById(R.id.email);
		sex = (RadioGroup)findViewById(R.id.sex);
		maleButton = (RadioButton)findViewById(R.id.male); 
		
		birthday=(EditText)findViewById(R.id.birthday);
		loginLoading = (View)findViewById(R.id.login_loading);
		mViewSwitcher = (ViewSwitcher)findViewById(R.id.logindialog_view_switcher);
		Calendar calendar = Calendar.getInstance(Locale.CHINA);  
        
        int mYear         = calendar.get(Calendar.YEAR);  
        int mMonth  = calendar.get(Calendar.MONTH);  
        int mDay   = calendar.get(Calendar.DAY_OF_MONTH);  
		//设置文本的内容：  
		birthday.setText(new StringBuilder()  
		.append(mYear).append("-")  
		.append(mMonth + 1).append("-")//得到的月份+1，因为从0开始  
		.append(mDay));  
		
		//fake init data
		long id=System.currentTimeMillis()%1000;
		name.setText("healthme-"+id);
		email.setText("admin-"+id+"@healthme.com.cn");
		real_Name.setText("海思敏-"+id);
		pass.setText("505healthme");
		
		
	}
	private void allListener(){
//		pass.addTextChangedListener(this);
//		name.addTextChangedListener(this);
//		real_Name.addTextChangedListener(this);
//		idCard.addTextChangedListener(this);
//		email.addTextChangedListener(new TextWatcher() { 
//	        public void afterTextChanged(Editable s) { 
//	            if (email.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+") && s.length() > 0)
//	            {
//	            	Toast.makeText(SetPassActivity.this, "邮箱格式正确", Toast.LENGTH_LONG).show();
//	            }
//	            else
//	            {
//	            	Toast.makeText(SetPassActivity.this, "邮箱格式错误", Toast.LENGTH_LONG).show();
//	            }
//	        } 
//	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {} 
//	        public void onTextChanged(CharSequence s, int start, int before, int count) {} 
//	    });
		birthday.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()== MotionEvent.ACTION_UP){
				Intent intent = new Intent();  
                intent.setClass(SetPassActivity.this, DatePickerActivity.class);  
                SetPassActivity.this.startActivityForResult(intent, 1000);//数字随意  
				}
				return true;
			}
		});
		
		complete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				boolean isFormat = isFormat();
				if (isFormat) {
					Toast.makeText(SetPassActivity.this, buffer.toString(),
							Toast.LENGTH_LONG).show();
				} else {
//					bar.setVisibility(View.VISIBLE);
					loadingAnimation = (AnimationDrawable)loginLoading.getBackground();
			        loadingAnimation.start();
			        mViewSwitcher.showNext();
					new Thread(new Register()).start();
				}
			}

		});
		
		sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
            
          public void onCheckedChanged(RadioGroup group, int checkedId) {  
              if(maleButton.getId() == checkedId){  
            	  sexStr="male";
              }  else
            	  sexStr="female";
          }  
      });  
	}

	
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		int type = data.getIntExtra("type",1);
		switch (type) {
		case DATAPICKER:
			int mYear = Integer.parseInt(data.getStringExtra("year"));  
			int mMonth = Integer.parseInt(data.getStringExtra("month"));  
			int mDay = Integer.parseInt(data.getStringExtra("day"));  
			//设置文本的内容：  
			birthday.setText(new StringBuilder()  
			.append(mYear).append("-")  
			.append(mMonth + 1).append("-")//得到的月份+1，因为从0开始  
			.append(mDay));  
			break;

		default:
			break;
		}
    }  
   
	
	@SuppressLint("NewApi")
	private boolean isFormat() {
		buffer = null;
		buffer = new StringBuffer();
		String strPass = pass.getText().toString().trim();
		String strName = name.getText().toString().trim();
		String strReal_Name = real_Name.getText().toString().trim();
		String emailText = email.getText().toString().trim();
		
		if (strPass.length() < 6 || strPass.length() > 16) {
			buffer.append("密码格式错误;\r\n");
		}
		if (strName.length() == 0) {
			buffer.append("昵称格式错误;\r\n");
		}
		if (strReal_Name.length() == 0) {
			buffer.append("姓名格式错误;\r\n");
		}
		if (emailText.length() > 0&&android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
		} else {
			buffer.append("邮箱格式错误;");
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
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userName", name.getText().toString().trim());
			map.put("phone", phone);
			map.put("code", verifyCode);
			map.put("password", pass.getText().toString().trim());
			map.put("birthday",birthday.getText());
			map.put("email", email.getText().toString().trim());
			map.put("fullName", real_Name.getText().toString().trim());
			map.put("sex", sexStr);
			Message msg = new Message();
			msg.arg1 = 1;
			try {
				AppContext ac = (AppContext)getApplication(); 
                Map<String, String> result = ac.checkRegisterParam(map);
                if(result.size()>0){
                	buffer = new StringBuffer();
                	if (result.get("password")!=null) {
            			buffer.append("密码格式错误;\r\n");
            		}
            		if (result.get("userName")!=null) {
            			buffer.append("用戶名已经被使用;\r\n");
            		}
            		if (result.get("email")!=null) {
            			buffer.append("该Email已经被使用;\r\n");
            		}
            		String original=result.get("code");
            		if(!StringUtils.isEmpty(original)){
            			buffer.append("验证码无效或者已过期;\r\n");
            		}
            		if(buffer.length()>0){
	            		msg.obj=buffer.toString().substring(0,buffer.length()-3);
	            		handler.sendMessage(msg);
	            		return ;
            		}
                }
                
                User user = ac.register(map);
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
            }
			
			handler.sendMessage(msg);
		}
			
		
	}
}
