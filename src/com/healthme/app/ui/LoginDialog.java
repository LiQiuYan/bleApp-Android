package com.healthme.app.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.healthme.app.AppContext;
import com.healthme.app.AppException;
import com.healthme.app.R;
import com.healthme.app.api.ApiClient;
import com.healthme.app.bean.Relative;
import com.healthme.app.bean.Result;
import com.healthme.app.bean.User;
import com.healthme.app.common.StringUtils;
import com.healthme.app.common.UIHelper;
import com.healthme.app.service.StompClient;

/**
 * 用户登录对话框
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class LoginDialog extends BaseActivity{
	
	private AppContext appContext;
	private ViewSwitcher mViewSwitcher;
	private ImageButton btn_close;
	private Button btn_login;
	private AutoCompleteTextView mAccount;
	private EditText mPwd;
	private AnimationDrawable loadingAnimation;
	private View loginLoading;
	private CheckBox chb_rememberMe;
	private TextView register;
	private int curLoginType;
	private InputMethodManager imm;
	private TextView forgetPassword;
	
	public final static int LOGIN_OTHER = 0x00;
	public final static int LOGIN_MAIN = 0x01;
	public final static int LOGIN_SETTING = 0x02;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog);
        
        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        
        curLoginType = getIntent().getIntExtra("LOGINTYPE", LOGIN_OTHER);
        
        mViewSwitcher = (ViewSwitcher)findViewById(R.id.logindialog_view_switcher);       
        loginLoading = (View)findViewById(R.id.login_loading);
        register=(TextView) findViewById(R.id.register);
        register.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginDialog.this,RegActivity.class);
				startActivity(intent);
				finish();
			}
		});
        forgetPassword=(TextView) findViewById(R.id.forgetPassword);
        forgetPassword.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginDialog.this,ResetActivity.class);
				startActivity(intent);
				finish();
			}
		});
        mAccount = (AutoCompleteTextView)findViewById(R.id.login_account);
        mPwd = (EditText)findViewById(R.id.login_password);
        chb_rememberMe = (CheckBox)findViewById(R.id.login_checkbox_rememberMe);
        mPwd.setText("");
        mAccount.setText("");
        btn_close = (ImageButton)findViewById(R.id.login_close_button);
        btn_close.setOnClickListener(UIHelper.finish(this));        
        
        btn_login = (Button)findViewById(R.id.login_btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//隐藏软键盘
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
				
//				String account = mAccount.getText().toString();
//				String pwd = mPwd.getText().toString();
				boolean isRememberMe = chb_rememberMe.isChecked();
				
				String account = mAccount.getText().toString(); //"admin";
				String pwd = mPwd.getText().toString(); //"admin";
				
				//判断输入
				if(StringUtils.isEmpty(account)){
					UIHelper.ToastMessage(v.getContext(), getString(R.string.msg_login_email_null));
					return;
				}
				if(StringUtils.isEmpty(pwd)){
					UIHelper.ToastMessage(v.getContext(), getString(R.string.msg_login_pwd_null));
					return;
				}
				
		        btn_close.setVisibility(View.GONE);
		        loadingAnimation = (AnimationDrawable)loginLoading.getBackground();
		        loadingAnimation.start();
		        mViewSwitcher.showNext();

		        login(account, pwd, isRememberMe);
			}
		});

        //是否显示登录信息
        appContext = (AppContext)getApplication();
        User user = appContext.getLastLoginInfo();
        if(user==null || !user.isRememberMe()) return;
        if(!StringUtils.isEmpty(user.getAccount())){
        	mAccount.setText(user.getAccount());
        	mAccount.selectAll();
        	chb_rememberMe.setChecked(user.isRememberMe());
        }
        if(!StringUtils.isEmpty(user.getPwd())){
        	mPwd.setText(user.getPwd());
        }
    }    

    //登录验证
    private void login(final String account, final String pwd, final boolean isRememberMe) {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if(msg.what == 1){
					User user = (User)msg.obj;
					if(user != null){
						//清空原先cookie
						//ApiClient.cleanCookie((AppContext)getApplication());
						//发送通知广播
						UIHelper.sendBroadCast(LoginDialog.this, user.getNotice());
						//提示登陆成功
						UIHelper.ToastMessage(LoginDialog.this, R.string.msg_login_success);
						Map<String,String> params=new HashMap<String,String>();						
						params.put("cookie", ApiClient.getCookie((AppContext)getApplication()));
						params.put("user", user.getAccount());
						appContext.triggerStompAction(StompClient.CMD.CONNECT, params);
						if(curLoginType == LOGIN_MAIN){
							//跳转--加载用户动态
							Intent intent = new Intent(LoginDialog.this, Main.class);
							intent.putExtra("LOGIN", true);
							startActivity(intent);
						}else if(curLoginType == LOGIN_SETTING){
							//跳转--用户设置页面
							Intent intent = new Intent(LoginDialog.this, Setting.class);
							intent.putExtra("LOGIN", true);
							startActivity(intent);
						}
						finish();
					}
				}else if(msg.what == 0){
					mViewSwitcher.showPrevious();
					btn_close.setVisibility(View.VISIBLE);
					UIHelper.ToastMessage(LoginDialog.this, getString(R.string.msg_login_fail)+msg.obj);
				}else if(msg.what == -1){
					mViewSwitcher.showPrevious();
					btn_close.setVisibility(View.VISIBLE);
					((AppException)msg.obj).makeToast(LoginDialog.this);
				}
			}
		};
		new Thread(){
			public void run() {
				Message msg =new Message();
				try {
					AppContext ac = (AppContext)getApplication(); 
	                User user = ac.loginVerify(account, pwd);
	                user.setAccount(account);
	                user.setPwd(pwd);
	                user.setRememberMe(isRememberMe);
	                Result res = user.getValidate();
	                if(res.OK()){
	                	ac.saveLoginInfo(user);//保存登录信息
	                	msg.what = 1;//成功
	                	msg.obj = user;
	                }else{
	                	ac.cleanLoginInfo();//清除登录信息
	                	msg.what = 0;//失败
	                	msg.obj = res.getErrorMessage();
	                }
	            } catch (AppException e) {
	            	e.printStackTrace();
			    	msg.what = -1;
			    	msg.obj = e;
	            }
				handler.sendMessage(msg);
			}
		}.start();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
    		this.onDestroy();
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    class LoginIntentService extends IntentService {
    	 
    	public LoginIntentService()
    	{
    		super("LOGIN_INTENT_SERVICE");
    	}
    	@Override
    	public void onCreate() {
    		super.onCreate();
    	}
    	@Override
    	protected void onHandleIntent(Intent intent) {
    		
    		login("admin", "", false);
    	}
     
    	@Override
    	public void onDestroy() {
    		super.onDestroy();
    	}
     
     
    }
 
}
