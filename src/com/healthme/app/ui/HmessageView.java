package com.healthme.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.healthme.app.AppContext;
import com.healthme.app.R;
import com.healthme.app.adapter.ListViewHmessageAdapter;
import com.healthme.app.bean.Hmessage;
import com.healthme.app.common.UIHelper;
import com.healthme.app.swipelistview.BaseSwipeListViewListener;
import com.healthme.app.swipelistview.SwipeListView;

public class HmessageView extends BaseActivity {

	private final static String TAG = HmessageView.class.getSimpleName();

	AppContext appContext;

	ArrayList<Hmessage> list = new ArrayList<Hmessage>();

	private Handler mHandler;

	private ListViewHmessageAdapter adapter;

	private SwipeListView listview;

	boolean showedAll = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hmessage_view);

		appContext = (AppContext) this.getApplication();

		// Log.i(TAG, "message size:"+list.size());

		IntentFilter filter = new IntentFilter(BroadCast.MESSAGE_REFRESH);
		registerReceiver(mReceiver, filter);

		this.init();
	}

	private int getDeviceWidth() {
		return getResources().getDisplayMetrics().widthPixels;
	}

	private void init() {
		initHead(R.string.menu_message, true, false);

		listview = (SwipeListView) findViewById(R.id.listview_message);
		listview.setOffsetLeft(getDeviceWidth()*2/3);

		adapter = new ListViewHmessageAdapter(this, list, listview);

		listview.setAdapter(adapter);

		listview.setSwipeListViewListener(new MyBaseSwipeListViewListener());

		mHandler = new Handler() {

			public void handleMessage(Message msg) {

				List<Hmessage> temp = null;

				if (msg.what > 0 && msg.obj != null) {
					temp = (List<Hmessage>) msg.obj;

					if (msg.arg1 != UIHelper.LISTVIEW_ACTION_SCROLL) {
						list.clear();
					}
					
					list.addAll(temp);					

					//if (msg.arg1 != UIHelper.LISTVIEW_ACTION_SCROLL)
					//	listview.setSelection(0);
					
					adapter.notifyDataSetChanged();
				}
				
			}
		};
	}

	@Override
	public void onResume() {
		super.onResume();
		loadHmessage(0,AppContext.PAGE_SIZE, UIHelper.LISTVIEW_ACTION_REFRESH);
	}

	@Override
	public void onDestroy() {
		this.unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BroadCast.MESSAGE_REFRESH.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				Log.i(TAG, action);
				onResume();
			}
		}
	};

	private void loadHmessage(final int from,final int size, final int action) {
		new Thread() {
			public void run() {
				showedAll = false;
				Message msg = new Message();
				List<Hmessage> list = appContext.getHmessages(from,size);
				Log.i(TAG,"size:"+list.size());
				msg.what = list.size();
				if (msg.what < size)
					showedAll = true;
				msg.obj = list;
				msg.arg1 = action;
				mHandler.sendMessage(msg);
			}
		}.start();
	}

	class MyBaseSwipeListViewListener extends BaseSwipeListViewListener {

		@Override
		public void onLastListItem() {
			if (!showedAll) {
				loadHmessage(list.size(),AppContext.PAGE_SIZE,
						UIHelper.LISTVIEW_ACTION_SCROLL);
			}
		}

		@Override
		public void onClickFrontView(int position) {
			super.onClickFrontView(position);
			appContext.markHmessageAsViewed(list.get(position));
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onDismiss(int[] reverseSortedPositions) {
			for (int position : reverseSortedPositions) {
				appContext.removeHmessage(list.remove(position));				
			}
			list.addAll(appContext.getHmessages(list.size(),1));
			adapter.notifyDataSetChanged();
		}
	}
}
