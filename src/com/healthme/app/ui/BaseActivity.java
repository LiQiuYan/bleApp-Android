package com.healthme.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.healthme.app.AppManager;
import com.healthme.app.R;
/**
 * 应用程序Activity的基类
 * 
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-9-18
 */
public class BaseActivity extends Activity {

	// 是否允许全屏
	private boolean allowFullScreen = true;

	// 是否允许销毁
	private boolean allowDestroy = true;

	private View view;
	
	protected final String TAG = this.getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		allowFullScreen = true;
		// 添加Activity到堆栈
		AppManager.getAppManager().addActivity(this);
	}
	
	
	
	protected void initHead(int titleRes){
		initHead(titleRes,true,true);
	}
	protected void initHead(String title){
		initHead(title,true,true);
	}
	protected void initHead(int titleRes,boolean back,boolean home){
		initHead(getResources().getString(titleRes),back,home);		
	}	
	protected void initHead(String title,boolean back,boolean home){
		if(title!=null)
			((TextView)findViewById(R.id.head_title)).setText(title);
		if(back){
			findViewById(R.id.head_back).setOnClickListener(backListener);
		}
		else{
			findViewById(R.id.head_back).setVisibility(View.GONE);
		}
		if(home){
			findViewById(R.id.head_home).setOnClickListener(homeListener);
		}
		else{
			findViewById(R.id.head_home).setVisibility(View.GONE);
		}	
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 结束Activity&从堆栈中移除
		AppManager.getAppManager().finishActivity(this);
	}
	
	@Override
	public void startActivity(Intent intent){
		super.startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
	}

	public boolean isAllowFullScreen() {
		return allowFullScreen;
	}
	
	@Override
	public void finish(){
		super.finish();
		if(!(this instanceof Main))
			overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
	}
	
	

	/**
	 * 设置是否可以全屏
	 * 
	 * @param allowFullScreen
	 */
	public void setAllowFullScreen(boolean allowFullScreen) {
		this.allowFullScreen = allowFullScreen;
	}

	public void setAllowDestroy(boolean allowDestroy) {
		this.allowDestroy = allowDestroy;
	}

	public void setAllowDestroy(boolean allowDestroy, View view) {
		this.allowDestroy = allowDestroy;
		this.view = view;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && view != null) {
			view.onKeyDown(keyCode, event);
			if (!allowDestroy) {
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private View.OnClickListener backListener=new View.OnClickListener() {		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			finish();
		}
	};
	
	private View.OnClickListener homeListener=new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			while(!(AppManager.getAppManager().currentActivity() instanceof Main)){
				AppManager.getAppManager().finishActivity();
			}
		}
	};
}
