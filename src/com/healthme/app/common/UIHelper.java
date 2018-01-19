package com.healthme.app.common;

import greendroid.widget.MyQuickAction;
import greendroid.widget.QuickAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.healthme.app.AppContext;
import com.healthme.app.AppException;
import com.healthme.app.AppManager;
import com.healthme.app.R;
import com.healthme.app.api.ApiClient;
import com.healthme.app.bean.EcgRecord;
import com.healthme.app.bean.Notice;
import com.healthme.app.bean.Result;
import com.healthme.app.ui.About;
import com.healthme.app.ui.BTDeviceSet;
import com.healthme.app.ui.ECGCodeDetail;
import com.healthme.app.ui.ECGRecordHistory;
import com.healthme.app.ui.ECGRecordInfo;
import com.healthme.app.ui.FeedBack;
import com.healthme.app.ui.ImageDialog;
import com.healthme.app.ui.ImageZoomDialog;
import com.healthme.app.ui.LoginDialog;
import com.healthme.app.ui.Main;
import com.healthme.app.ui.Setting;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * 
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class UIHelper {

	public final static int LISTVIEW_ACTION_INIT = 0x01;
	public final static int LISTVIEW_ACTION_REFRESH = 0x02;
	public final static int LISTVIEW_ACTION_SCROLL = 0x03;
	public final static int LISTVIEW_ACTION_CHANGE_CATALOG = 0x04;

	public final static int LISTVIEW_DATA_MORE = 0x01;
	public final static int LISTVIEW_DATA_LOADING = 0x02;
	public final static int LISTVIEW_DATA_FULL = 0x03;
	public final static int LISTVIEW_DATA_EMPTY = 0x04;

	public final static int LISTVIEW_DATATYPE_NEWS = 0x01;
	public final static int LISTVIEW_DATATYPE_BLOG = 0x02;
	public final static int LISTVIEW_DATATYPE_POST = 0x03;
	public final static int LISTVIEW_DATATYPE_RECORD = 0x04;
	public final static int LISTVIEW_DATATYPE_ACTIVE = 0x05;
	public final static int LISTVIEW_DATATYPE_MESSAGE = 0x06;
	public final static int LISTVIEW_DATATYPE_COMMENT = 0x07;

	public final static int REQUEST_CODE_FOR_RESULT = 0x01;
	public final static int REQUEST_CODE_FOR_REPLY = 0x02;
	
	public final static String ACTION_REFRESHDEVICE="com.healthme.action.refreshdevice";

	/** 表情图片匹配 */
	private static Pattern facePattern = Pattern
			.compile("\\[{1}([0-9]\\d*)\\]{1}");

	/** 全局web样式 */
	public final static String WEB_STYLE = "<style>* {font-size:16px;line-height:20px;} p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} "
			+ "img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;} "
			+ "pre {font-size:9pt;line-height:12pt;font-family:Courier New,Arial;border:1px solid #ddd;border-left:5px solid #6CE26C;background:#f6f6f6;padding:5px;} "
			+ "a.tag {font-size:15px;text-decoration:none;background-color:#bbd6f3;border-bottom:2px solid #3E6D8E;border-right:2px solid #7F9FB6;color:#284a7b;margin:2px 2px 2px 0;padding:2px 4px;white-space:nowrap;}</style>";

	/**
	 * 显示首页
	 * 
	 * @param activity
	 */
	public static void showHome(Activity activity) {
		Intent intent = new Intent(activity, Main.class);
		activity.startActivity(intent);
		activity.finish();
	}

	/**
	 * 显示登录页面
	 * 
	 * @param activity
	 */
	public static void showLoginDialog(Context context) {
		Intent intent = new Intent(context, LoginDialog.class);
		if (context instanceof Main){
			intent.putExtra("LOGINTYPE", LoginDialog.LOGIN_MAIN);
		}
		else if (context instanceof Setting)
			intent.putExtra("LOGINTYPE", LoginDialog.LOGIN_SETTING);
		else
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//intent.putExtra("main", context);
		context.startActivity(intent);
	}

	
	public static void showEcgRecordHistory(Context context){
		Intent intent = new Intent(context, ECGRecordHistory.class);		
		context.startActivity(intent);		
	}


	/**
	 * 显示动弹详情及评论
	 * 
	 * @param context
	 * @param tweetId
	 */
	public static void showEcgRecordDetail(Context context, EcgRecord record) {
		Intent intent = new Intent(context, ECGRecordInfo.class);
		ArrayList<EcgRecord> objectList = new ArrayList<EcgRecord>();  
		objectList.add(record);  
		intent.putExtra("ListObject", objectList);  
		context.startActivity(intent);
	}



	/**
	 * 显示图片对话框
	 * 
	 * @param context
	 * @param imgUrl
	 */
	public static void showImageDialog(Context context, String imgUrl) {
		Intent intent = new Intent(context, ImageDialog.class);
		intent.putExtra("img_url", imgUrl);
		context.startActivity(intent);
	}

	public static void showImageZoomDialog(Context context, String imgUrl) {
		Intent intent = new Intent(context, ImageZoomDialog.class);
		intent.putExtra("img_url", imgUrl);
		context.startActivity(intent);
	}

	/**
	 * 显示系统设置界面
	 * 
	 * @param context
	 */
	public static void showSetting(Context context) {
		Intent intent = new Intent(context, Setting.class);
		context.startActivity(intent);
	}
	
	public static void showBTSetting(Context context){
		Intent intent = new Intent(context, BTDeviceSet.class);
		context.startActivity(intent);
	}







//	/**
//	 * 显示用户收藏夹
//	 * 
//	 * @param context
//	 * @param record 
//	 */
//	public static void showPVCInfo(Context context, EcgRecord record) {
//		Intent intent = new Intent(context, PVCInfo.class);
//		intent.putExtra("record", record);
//		context.startActivity(intent);
//	}
	
	/**
	 * 显示记录数据详细  室早/室上早/停博/
	 * 
	 * @param context
	 * @param record
	 * @param code
	 */
	public static void showRecordDetail(Context context, EcgRecord record,short ecgCode){
		Intent intent = new Intent(context, ECGCodeDetail.class);
		intent.putExtra("record", record);
		intent.putExtra("code", ecgCode);
		context.startActivity(intent);
	}




	/**
	 * 加载显示用户头像
	 * 
	 * @param imgFace
	 * @param faceURL
	 */
	public static void showUserFace(final ImageView imgFace,
			final String faceURL) {
		showLoadImage(imgFace, faceURL,
				imgFace.getContext().getString(R.string.msg_load_userface_fail));
	}

	/**
	 * 加载显示图片
	 * 
	 * @param imgFace
	 * @param faceURL
	 * @param errMsg
	 */
	public static void showLoadImage(final ImageView imgView,
			final String imgURL, final String errMsg) {
		// 读取本地图片
		if (StringUtils.isEmpty(imgURL) || imgURL.endsWith("portrait.gif")) {
			Bitmap bmp = BitmapFactory.decodeResource(imgView.getResources(),
					R.drawable.widget_dface);
			imgView.setImageBitmap(bmp);
			return;
		}

		// 是否有缓存图片
		final String filename = FileUtils.getFileName(imgURL);
		// Environment.getExternalStorageDirectory();返回/sdcard
		String filepath = imgView.getContext().getFilesDir() + File.separator
				+ filename;
		File file = new File(filepath);
		if (file.exists()) {
			Bitmap bmp = ImageUtils.getBitmap(imgView.getContext(), filename);
			imgView.setImageBitmap(bmp);
			return;
		}

		// 从网络获取&写入图片缓存
		String _errMsg = imgView.getContext().getString(
				R.string.msg_load_image_fail);
		if (!StringUtils.isEmpty(errMsg))
			_errMsg = errMsg;
		final String ErrMsg = _errMsg;
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1 && msg.obj != null) {
					imgView.setImageBitmap((Bitmap) msg.obj);
					try {
						// 写图片缓存
						ImageUtils.saveImage(imgView.getContext(), filename,
								(Bitmap) msg.obj);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					ToastMessage(imgView.getContext(), ErrMsg);
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					Bitmap bmp = ApiClient.getNetBitmap(imgURL);
					msg.what = 1;
					msg.obj = bmp;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}





	/**
	 * 打开浏览器
	 * 
	 * @param context
	 * @param url
	 */
	public static void openBrowser(Context context, String url) {
		try {
			Uri uri = Uri.parse(url);
			Intent it = new Intent(Intent.ACTION_VIEW, uri);
			context.startActivity(it);
		} catch (Exception e) {
			e.printStackTrace();
			ToastMessage(context, "无法浏览此网页", 500);
		}
	}



	/**
	 * 获取TextWatcher对象
	 * 
	 * @param context
	 * @param tmlKey
	 * @return
	 */
	public static TextWatcher getTextWatcher(final Activity context,
			final String temlKey) {
		return new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 保存当前EditText正在编辑的内容
				((AppContext) context.getApplication()).setProperty(temlKey,
						s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		};
	}



	/**
	 * 清除文字
	 * 
	 * @param cont
	 * @param editer
	 */
	public static void showClearWordsDialog(final Context cont,
			final EditText editer, final TextView numwords) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		builder.setTitle(R.string.clearwords);
		builder.setPositiveButton(R.string.sure,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 清除文字
						editer.setText("");
						numwords.setText("160");
					}
				});
		builder.setNegativeButton(R.string.cancle,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * 发送通知广播
	 * 
	 * @param context
	 * @param notice
	 */
	public static void sendBroadCast(Context context, Notice notice) {
		if (!((AppContext) context.getApplicationContext()).isLogin())
			return;
		Intent intent = new Intent("com.healthme.app.action.APPWIDGET_UPDATE");
//		intent.putExtra("atmeCount", notice.getAtmeCount());
//		intent.putExtra("msgCount", notice.getMsgCount());
//		intent.putExtra("reviewCount", notice.getReviewCount());
//		intent.putExtra("newFansCount", notice.getNewFansCount());
		context.sendBroadcast(intent);
	}

	/**
	 * 发送广播-发布动弹
	 * 
	 * @param context
	 * @param notice
	 */
	public static void sendBroadCastTweet(Context context, int what,
			Result res, EcgRecord tweet) {
		if (res == null && tweet == null)
			return;
		Intent intent = new Intent("net.oschina.app.action.APP_TWEETPUB");
		intent.putExtra("MSG_WHAT", what);
		if (what == 1)
			intent.putExtra("RESULT", res);
		else
			intent.putExtra("TWEET", tweet);
		context.sendBroadcast(intent);
	}

	


	/**
	 * 弹出Toast消息
	 * 
	 * @param msg
	 */
	public static void ToastMessage(Context cont, String msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, int msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, String msg, int time) {
		Toast.makeText(cont, msg, time).show();
	}

	/**
	 * 点击返回监听事件
	 * 
	 * @param activity
	 * @return
	 */
	public static View.OnClickListener finish(final Activity activity) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				activity.finish();
			}
		};
	}

	/**
	 * 显示关于我们
	 * 
	 * @param context
	 */
	public static void showAbout(Context context) {
		Intent intent = new Intent(context, About.class);
		context.startActivity(intent);
	}

	/**
	 * 显示用户反馈
	 * 
	 * @param context
	 */
	public static void showFeedBack(Context context) {
		Intent intent = new Intent(context, FeedBack.class);
		context.startActivity(intent);
	}

	/**
	 * 菜单显示登录或登出
	 * 
	 * @param activity
	 * @param menu
	 */
	public static void showMenuLoginOrLogout(Activity activity, Menu menu) {
		if (((AppContext) activity.getApplication()).isLogin()) {
			menu.findItem(R.id.main_menu_user).setTitle(
					R.string.main_menu_logout);
			menu.findItem(R.id.main_menu_user).setIcon(
					R.drawable.ic_menu_logout);
		} else {
			menu.findItem(R.id.main_menu_user).setTitle(
					R.string.main_menu_login);
			menu.findItem(R.id.main_menu_user)
					.setIcon(R.drawable.ic_menu_login);
		}
	}

	/**
	 * 快捷栏显示登录与登出
	 * 
	 * @param activity
	 * @param qa
	 */
	public static void showSettingLoginOrLogout(Activity activity,
			QuickAction qa) {
		if (((AppContext) activity.getApplication()).isLogin()) {
			qa.setIcon(MyQuickAction.buildDrawable(activity,
					R.drawable.ic_menu_logout));
			qa.setTitle(activity.getString(R.string.main_menu_logout));
		} else {
			qa.setIcon(MyQuickAction.buildDrawable(activity,
					R.drawable.ic_menu_login));
			qa.setTitle(activity.getString(R.string.main_menu_login));
		}
	}


	/**
	 * 用户登录或注销
	 * 
	 * @param activity
	 */
	public static void loginOrLogout(Activity activity) {
		AppContext ac = (AppContext) activity.getApplication();
		if (ac.isLogin()) {
			ac.Logout();
			ToastMessage(activity, "已退出登录");
		} else {
			showLoginDialog(activity);
		}
	}


	/**
	 * 清除app缓存
	 * 
	 * @param activity
	 */
	public static void clearAppCache(Activity activity) {
		final AppContext ac = (AppContext) activity.getApplication();
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					ToastMessage(ac, "缓存清除成功");
				} else {
					ToastMessage(ac, "缓存清除失败");
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					ac.clearAppCache();
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}
	/**
	 * 清除app缓存
	 * 
	 * @param activity
	 */
	public static void startGPS(Activity activity) {

		LocationManager alm = (LocationManager) activity
				.getSystemService(Context.LOCATION_SERVICE);
		if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			Toast.makeText(activity, "GPS模块正常", Toast.LENGTH_SHORT).show();
			return ;
		}

		Toast.makeText(activity, "请开启GPS！", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		activity.startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面


		    		
//		final AppContext ac = (AppContext) activity.getApplication();
//		final Handler handler = new Handler() {
//			public void handleMessage(Message msg) {
//				if (msg.what == 1) {
//					ToastMessage(ac, "缓存清除成功");
//				} else {
//					ToastMessage(ac, "缓存清除失败");
//				}
//			}
//		};
//		new Thread() {
//			public void run() {
//				Message msg = new Message();
//				try {
//					ac.clearAppCache();
//					msg.what = 1;
//				} catch (Exception e) {
//					e.printStackTrace();
//					msg.what = -1;
//				}
//				handler.sendMessage(msg);
//			}
//		}.start();
	}

	
	/**
	 * 清除app缓存
	 * 
	 * @param activity
	 */
	public static void startSD(Activity activity) {
		

		Toast.makeText(activity, "请开启SD！", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
		activity.startActivityForResult(intent, 1); // 此为设置完成后返回到获取界面
	}
	
	/**
	 * 发送App异常崩溃报告
	 * 
	 * @param cont
	 * @param crashReport
	 */
	public static void sendAppCrashReport(final Context cont,
			final String crashReport) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_error);
		builder.setMessage(R.string.app_error_message);
		builder.setPositiveButton(R.string.submit_report,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 发送异常报告
						Intent i = new Intent(Intent.ACTION_SEND);
						// i.setType("text/plain"); //模拟器
						i.setType("message/rfc822"); // 真机
						i.putExtra(Intent.EXTRA_EMAIL,
								new String[] { "jxsmallmouse@163.com" });
						i.putExtra(Intent.EXTRA_SUBJECT,
								"开源中国Android客户端 - 错误报告");
						i.putExtra(Intent.EXTRA_TEXT, crashReport);
						cont.startActivity(Intent.createChooser(i, "发送错误报告"));
						// 退出
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.setNegativeButton(R.string.sure,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 退出
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.show();
	}

	/**
	 * 退出程序
	 * 
	 * @param cont
	 */
	public static void Exit(final Context cont) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_menu_surelogout);
		builder.setPositiveButton(R.string.sure,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 退出
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.setNegativeButton(R.string.cancle,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}


	/**
	 * 添加网页的点击图片展示支持
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public static void addWebImageShow(final Context cxt, WebView wv) {
		wv.getSettings().setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new OnWebViewImageListener() {

			@Override
			public void onImageClick(String bigImageUrl) {
				if (bigImageUrl != null)
					UIHelper.showImageZoomDialog(cxt, bigImageUrl);
			}
		}, "mWebViewImageListener");
	}
}
