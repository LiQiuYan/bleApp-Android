package com.healthme.app.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.healthme.app.AppContext;
import com.healthme.app.R;
import com.healthme.app.bean.Hmessage;

/**
 * 通知信息广播接收器
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-4-16
 */
public class BroadCast extends BroadcastReceiver {
	
	public final static String APPWIDGET_UPDATE="com.healthme.app.action.APPWIDGET_UPDATE";
	public final static String STOMP_RECEIVE="com.healthme.app.action.STOMP_RECEIVE";
	public final static String MESSAGE_REFRESH="com.healthme.app.action.MESSAGE_REFRESH";
	public final static String STOMP_ACTOIN="com.healthme.app.action.STOMP_ACTOIN";
	
	private AppContext appContext;

	private final static int NOTIFICATION_ID = R.layout.main;
	
	private static int lastNoticeCount;
	
	private static final String TAG = BroadCast.class.getSimpleName();
	
	private final static int NOTIFICATION_MESSAGE = R.layout.hmessage_view;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(appContext==null)appContext=(AppContext)context.getApplicationContext();
		String ACTION_NAME = intent.getAction();
		Log.i(TAG,ACTION_NAME);
		if(APPWIDGET_UPDATE.equals(ACTION_NAME))
		{	
//			Activity main= AppManager.getAppManager().currentActivity();
//			while (main instanceof LoginDialog)
//			{
//				AppManager.getAppManager().finishActivity();
//				main= AppManager.getAppManager().currentActivity();
//			}
//			PullToRefreshListView lvEcgRecord = (PullToRefreshListView) main.findViewById(R.id.frame_listview_record);
			//EditText mPwd=(EditText) ((LoginDialog)context).findViewById(R.id.login_password);
//			lvEcgRecord.clickRefresh();
//			EcgUserInfoView bvEcgUser = (EcgUserInfoView)  main.findViewById(R.id.frame_baseview_ecg);
//			bvEcgUser.clickRefresh();
//			int atmeCount = intent.getIntExtra("atmeCount", 0);//@我
//			int msgCount = intent.getIntExtra("msgCount", 0);//留言
//			int reviewCount = intent.getIntExtra("reviewCount", 0);//评论
//			int newFansCount = intent.getIntExtra("newFansCount", 0);//新粉丝
//			int activeCount = atmeCount + reviewCount + msgCount + newFansCount;//信息总数
//			
//
//			//通知栏显示
//			this.notification(context, activeCount);
		}
		else if(STOMP_RECEIVE.equals(ACTION_NAME)){			
			Hmessage msg=(Hmessage)intent.getSerializableExtra("msg");
			int countNew=appContext.receiveNewHmessage(msg);
			context.sendBroadcast(new Intent(MESSAGE_REFRESH));
			notification(context,msg,countNew);			
		}
	}
	
	private void notification(Context context,Hmessage msg,int countNew){
				
		//创建 NotificationManager
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				
		Notification notification = new NotificationCompat.Builder(context)
			//.setLargeIcon(icon)
			.setSmallIcon(R.drawable.nfc_msg_new)
			.setTicker("新消息:"+msg.getContent())
			//.setContentInfo("contentInfo")
			.setContentTitle("新消息").setContentText(msg.getContent())
			.setNumber(countNew)
			//.setAutoCancel(true)
			.build();
				
		 Intent intent = new Intent(context, HmessageView.class);  
	     PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);  
	     // 点击状态栏的图标出现的提示信息设置  
	     notification.setLatestEventInfo(context, "新消息", msg.getContent(), pendingIntent);
	     
	     notification.flags |= Notification.FLAG_AUTO_CANCEL;
	     
	     notificationManager.notify(NOTIFICATION_MESSAGE, notification);
	}

	private void notification(Context context, String contentTitle, String contentText){		
		//创建 NotificationManager
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification notification = new NotificationCompat.Builder(context)
				//.setLargeIcon(icon)
				.setSmallIcon(android.R.drawable.ic_dialog_alert)
				.setTicker("新消息:"+contentText)
				//.setContentInfo("contentInfo")
				.setContentTitle(contentTitle).setContentText(contentText)				
				//.setAutoCancel(true)
				.build();
		notificationManager.notify(0, notification);
//				
//		
//		String contentTitle = "开源中国";
//		String contentText = "您有 " + noticeCount + " 条最新信息";
//		int _lastNoticeCount;
//		
//		//判断是否发出通知信息
//		if(noticeCount == 0)
//		{
//			notificationManager.cancelAll();
//			lastNoticeCount = 0;
//			return;
//		}
//		else if(noticeCount == lastNoticeCount)
//		{
//			return; 
//		}
//		else
//		{
//			_lastNoticeCount = lastNoticeCount;
//			lastNoticeCount = noticeCount;
//		}
//		
//		//创建通知 Notification
//		Notification notification = null;
//		
//		if(noticeCount > _lastNoticeCount) 
//		{
//			String noticeTitle = "您有 " + (noticeCount-_lastNoticeCount) + " 条最新信息";
//			notification = new Notification(R.drawable.icon, noticeTitle, System.currentTimeMillis());
//		}
//		else
//		{
//			notification = new Notification();
//		}
//		
//		//设置点击通知跳转
//		Intent intent = new Intent(context, Main.class);
//		intent.putExtra("NOTICE", true);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK); 
//		
//		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//		
//		//设置最新信息
//		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
//		
//		//设置点击清除通知
//		notification.flags = Notification.FLAG_AUTO_CANCEL;
//		
//		if(noticeCount > _lastNoticeCount) 
//		{
//			//设置通知方式
//			notification.defaults |= Notification.DEFAULT_LIGHTS;
//			
//			//设置通知音-根据app设置是否发出提示音
//			if(((AppContext)context.getApplicationContext()).isAppSound())
//				notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notificationsound);
//			
//			//设置振动 <需要加上用户权限android.permission.VIBRATE>
//			//notification.vibrate = new long[]{100, 250, 100, 500};
//		}
//		
//		//发出通知
//		notificationManager.notify(NOTIFICATION_ID, notification);		
	}
	
}
