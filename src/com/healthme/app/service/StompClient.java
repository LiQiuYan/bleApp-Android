package com.healthme.app.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.healthme.app.bean.Hmessage;
import com.healthme.app.bean.URLs;
import com.healthme.app.common.UIHelper;
import com.healthme.app.ui.BroadCast;
import com.healthme.stomp.client.ListenerSubscription;
import com.healthme.stomp.client.ListenerWSNetwork;
import com.healthme.stomp.client.Stomp;
import com.healthme.stomp.client.Subscription;

public class StompClient extends Service {

	public final static String TAG = StompClient.class.getSimpleName();

	public final static class CMD {
		public final static String CONNECT = "CONNECT";
		public final static String SEND = "SEND";
		public final static String DISCONNECT = "DISCONNECT";
	}

	private String cookie;
	private String user;

	int mStartMode;
	MyBinder myBinder=new MyBinder();

	StompThread stompThread;
	
	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The service is starting, due to a call to startService()
		Log.i(TAG, "onStartCommand:" + intent.getExtras());
		String cmd = intent.getStringExtra("cmd");
		if(cmd!=null){
			Log.i(TAG, "onStartCommand:"+cmd);
			if (CMD.CONNECT.equals(cmd)) {
				if (stompThread != null)
					stompThread.cancel();
				cookie = intent.getStringExtra("cookie");
				user = intent.getStringExtra("user");
				if (cookie != null) {
					stompThread = new StompThread();
					stompThread.start();
				}
			} else if (CMD.DISCONNECT.equals(cmd)) {
				if (stompThread != null)
					stompThread.cancel();
			} else if (CMD.SEND.equals(cmd)) {
				// to do
			}
		}

		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return myBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		// The service is no longer used and is being destroyed
		Log.i(TAG, "onDestroy");
		if (stompThread != null)
			stompThread.cancel();
		super.onDestroy();
	}

	private class StompThread extends Thread {

		private Stomp client;

		@Override
		public void run() {

			Map<String, String> extraHeaders = new HashMap<String, String>();
			extraHeaders.put("Cookie", cookie);

			String url = URLs.WS_API_HOST + RandomStringUtils.randomNumeric(3)
					+ "/"
					+ RandomStringUtils.randomAlphanumeric(6).toLowerCase()
					+ "/websocket";
			client = new Stomp(url, extraHeaders, new ListenerWSNetwork() {
				public void onState(int state) {
				}
			});

			client.connect();
			
			Log.i(TAG, "stomp connect.");

			client.subscribe(new Subscription("/topic/public", new ListenerSubscription() {
				public void onMessage(Map<String, String> headers, String body) {
					Hmessage msg=Hmessage.fromStomp(headers, body);
					if(msg.getMessageId()!=null){
						Log.i(TAG, "receive:" + msg);
						Intent in=new Intent(BroadCast.STOMP_RECEIVE);
						in.putExtra("msg", msg);
						sendBroadcast(in);
					}
				}
			}));
		}

		public void cancel() {
			if (client != null) {
				Log.i(TAG, "stomp close.");
				client.disconnect();
				client = null;
			}
		}
	}

	public class MyBinder extends Binder {

		public StompClient getService() {
			return StompClient.this;
		}
	}
	
}
