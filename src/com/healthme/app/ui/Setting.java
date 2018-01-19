package com.healthme.app.ui;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.healthme.app.AppContext;
import com.healthme.app.AppManager;
import com.healthme.app.AppStart;
import com.healthme.app.R;
import com.healthme.app.common.FileUtils;
import com.healthme.app.common.MethodsCompat;
import com.healthme.app.common.UIHelper;
import com.healthme.app.common.UpdateManager;
public class Setting extends PreferenceActivity {

	SharedPreferences mPreferences;
	Preference feedback;
	Preference update;
	Preference about;
	Preference cache;
//	CheckBoxPreference scroll;
	CheckBoxPreference voice;
	CheckBoxPreference trace;	
	CheckBoxPreference ecgCache;
	CheckBoxPreference checkup;
//	Activity main;
	
	ViewGroup localViewGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 添加Activity到堆栈
		AppManager.getAppManager().addActivity(this);

		// 设置显示Preferences
		addPreferencesFromResource(R.xml.preferences);
		// 获得SharedPreferences
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		ListView localListView = getListView();
		localListView.setBackgroundColor(0);
		localListView.setCacheColorHint(0);
		((ViewGroup) localListView.getParent()).removeView(localListView);
		localViewGroup = (ViewGroup) getLayoutInflater().inflate(
				R.layout.setting, null);
		((ViewGroup) localViewGroup.findViewById(R.id.setting_content))
				.addView(localListView, -1, -1);
		setContentView(localViewGroup);
		
		initHead(R.string.setting_head_title);

		final AppContext ac = (AppContext) getApplication();


//		// 左右滑动
//		scroll = (CheckBoxPreference) findPreference("scroll");
//		scroll.setChecked(ac.isScroll());
//		if (ac.isScroll()) {
//			scroll.setSummary("已启用左右滑动");
//		} else {
//			scroll.setSummary("已关闭左右滑动");
//		}
//		scroll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//			public boolean onPreferenceClick(Preference preference) {
//				ac.setConfigScroll(scroll.isChecked());
//				if (scroll.isChecked()) {
//					scroll.setSummary("已启用左右滑动");
//				} else {
//					scroll.setSummary("已关闭左右滑动");
//				}
//				return true;
//			}
//		});

		// 提示声音
		voice = (CheckBoxPreference) findPreference("voice");
		voice.setChecked(ac.isVoice());
		if (ac.isVoice()) {
			voice.setSummary("已开启提示声音");
		} else {
			voice.setSummary("已关闭提示声音");
		}
		voice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				ac.setConfigVoice(voice.isChecked());
				if (voice.isChecked()) {
					voice.setSummary("已开启提示声音");
				} else {
					voice.setSummary("已关闭提示声音");
				}
				return true;
			}
		});

		// 位置追踪
		trace = (CheckBoxPreference) findPreference("trace");
		trace.setChecked(ac.isTrace());
		if (ac.isTrace()) {
			trace.setSummary("已开启位置追踪");
		} else {
			trace.setSummary("已关闭位置追踪");
		}
		trace.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				ac.setConfigTrace(trace.isChecked());
				if (trace.isChecked()) {
					UIHelper.startGPS(Setting.this);
				} else {
					trace.setSummary("已关闭位置追踪");
				}
				return true;
			}
		});
		
		
		// 本地存储心电图数据
				ecgCache = (CheckBoxPreference) findPreference("ecgCache");
				ecgCache.setChecked(ac.isTrace());
				if (ac.isECGCache()) {
					ecgCache.setSummary("已开启缓存心电图数据");
				} else {
					ecgCache.setSummary("已关闭缓存心电图数据");
				}
				ecgCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						ac.setConfigECGCache(ecgCache.isChecked());
						if (ecgCache.isChecked()) {
							if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
								UIHelper.startSD(Setting.this);
							}
						} else {
							ecgCache.setSummary("已关闭缓存心电图数据");
						}
						return true;
					}
				});
		
		// 启动检查更新
		checkup = (CheckBoxPreference) findPreference("checkup");
		checkup.setChecked(ac.isCheckUp());
		checkup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				ac.setConfigCheckUp(checkup.isChecked());
				if (checkup.isChecked()) {
					checkup.setSummary("已开启检查更新");
				} else {
					checkup.setSummary("已关闭启动更新");
				}				
				return true;
			}
		});

		// 计算缓存大小
		long fileSize = 0;
		String cacheSize = "0KB";
		File filesDir = getFilesDir();
		File cacheDir = getCacheDir();

		fileSize += FileUtils.getDirSize(filesDir);
		fileSize += FileUtils.getDirSize(cacheDir);
		// 2.2版本才有将应用缓存转移到sd卡的功能
		if (AppContext.isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
			File externalCacheDir = MethodsCompat.getExternalCacheDir(this);
			fileSize += FileUtils.getDirSize(externalCacheDir);
		}
		if (fileSize > 0)
			cacheSize = FileUtils.formatFileSize(fileSize);

		// 清除缓存
				cache = (Preference) findPreference("cache");
				cache.setSummary(cacheSize);
				cache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						UIHelper.clearAppCache(Setting.this);
						cache.setSummary("0KB");
						return true;
					}
				});

		// 意见反馈
		feedback = (Preference) findPreference("feedback");
		feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				UIHelper.showFeedBack(Setting.this);
				return true;
			}
		});

		// 版本更新
		update = (Preference) findPreference("update");
		update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				UpdateManager.getUpdateManager().checkAppUpdate(Setting.this,
						true);
				return true;
			}
		});

		// 关于我们
		about = (Preference) findPreference("about");
		about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				UIHelper.showAbout(Setting.this);
				return true;
			}
		});

	}



		/**
		 * 复写onActivityResult，这个方法
		 * 是要等到SimpleTaskActivity点了提交过后才会执行的
		 */
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data)
		{
			switch (requestCode) {
			case 0:
				trace = (CheckBoxPreference) findPreference("trace");
				LocationManager alm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
				{
					Toast.makeText(this, "GPS设置成功", Toast.LENGTH_SHORT).show();
					trace.setChecked(true);
					trace.setSummary("已开启位置追踪");
					((AppStart)AppManager.getAppManager().getActivity(AppStart.class)).initGPSTask();
					
				}
				else
				{
					Toast.makeText(this, "GPS设置失败", Toast.LENGTH_SHORT).show();
					trace.setSummary("GPS没有开启");
					trace.setChecked(false);						
					
				}
				break;
			case 1:
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					ecgCache = (CheckBoxPreference) findPreference("ecgCache");
					ecgCache.setChecked(true);
				}
			default:
				break;
			}

		}
		
	public void back(View paramView) {
		finish();
	}
	
	@Override
	public void startActivity(Intent intent){
		super.startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
	}
	
	@Override
	public void finish(){
		super.finish();
		overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 结束Activity&从堆栈中移除
		AppManager.getAppManager().finishActivity(this);
	}
	
	protected void initHead(int titleRes){
		((TextView)localViewGroup.findViewById(R.id.head_title)).setText(titleRes);
		localViewGroup.findViewById(R.id.head_back).setOnClickListener(backListener);
		localViewGroup.findViewById(R.id.head_home).setOnClickListener(homeListener);
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
