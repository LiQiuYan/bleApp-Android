package com.healthme.app;

import com.healthme.app.common.StringUtils;
import com.healthme.app.common.UpdateManager;
import com.healthme.app.common.GpsTask.GpsData;
import com.healthme.app.ui.Main;
import com.healthme.app.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

/**
 * 应用程序启动类：显示欢迎界面并跳转到主界面
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class AppStart extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View view = View.inflate(this, R.layout.start, null);
		setContentView(view);
        
		//渐变展示启动屏
		AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
		aa.setDuration(2000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation arg0) {
				redirectTo();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
			
		});
		
		//兼容低版本cookie（1.5版本以下，包括1.5.0,1.5.1）
		AppContext appContext = (AppContext)getApplication();
		String cookie = appContext.getProperty("cookie");
		if(StringUtils.isEmpty(cookie)) {
			String cookie_name = appContext.getProperty("cookie_name");
			String cookie_value = appContext.getProperty("cookie_value");
			if(!StringUtils.isEmpty(cookie_name) && !StringUtils.isEmpty(cookie_value)) {
				cookie = cookie_name + "=" + cookie_value;
				appContext.setProperty("cookie", cookie);
				appContext.removeProperty("cookie_domain","cookie_name","cookie_value","cookie_version","cookie_path");
			}
		}
		
		AppManager.getAppManager().addActivity(this);
		//init GPS
		initGPSTask();
		//init login user info
		appContext.initLoginInfo();
		
		// 检查新版本
		if (appContext.isCheckUp()) {
				UpdateManager.getUpdateManager().checkAppUpdate(this, false);
		}
    }
    
    /**
     * 跳转到...
     */
    private void redirectTo(){        
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        finish();
    }
    
    /**
	 * 初始化GPS任务
	 */	
	public void initGPSTask() {
		 final LocationListener mLOC = new LocationListener() {

				private AppContext appContext;

				@Override
				public void onLocationChanged(Location location) {
					Log.i("DEBUG","GPS Updated:"+location);
					appContext = (AppContext) getApplication();
					if (appContext.isTrace())
					{
						GpsData gpsData = new GpsData();
						gpsData.setAccuracy(location.getAccuracy());
						gpsData.setAltitude(location.getAltitude());
						gpsData.setBearing(location.getBearing());
						gpsData.setLatitude(location.getLatitude());
						gpsData.setLongitude(location.getLongitude());
						gpsData.setSpeed(location.getSpeed());
						gpsData.setTime(location.getTime());
						appContext.setCurGPSData(gpsData);
					}
					
				}

				@Override
				public void onProviderDisabled(String provider) {
					// TODO Auto-generated method stub
					Log.i("INFO","GPS disabled");
					
				}

				@Override
				public void onProviderEnabled(String provider) {
					// TODO Auto-generated method stub
					Log.i("INFO","GPS enabled");
				}

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
					// TODO Auto-generated method stub
					
				}



			};
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		// 通过GPS获取位置
		if (location!=null)
			mLOC.onLocationChanged(location);
		// 设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, mLOC);
		if (locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER))
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, mLOC);

		}
}