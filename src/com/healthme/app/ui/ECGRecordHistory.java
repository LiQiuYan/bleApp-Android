package com.healthme.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.healthme.app.AppContext;
import com.healthme.app.AppException;
import com.healthme.app.R;
import com.healthme.app.adapter.ListViewEcgRecordAdapter;
import com.healthme.app.bean.EcgRecord;
import com.healthme.app.bean.EcgRecordList;
import com.healthme.app.common.StringUtils;
import com.healthme.app.common.UIHelper;
import com.healthme.app.widget.PullToRefreshListView;

public class ECGRecordHistory extends BaseActivity {
	
	private AppContext appContext;

	private Handler mHandler;

	private List<EcgRecord> list = new ArrayList<EcgRecord>();

	private ListViewEcgRecordAdapter adapter;

	private PullToRefreshListView listview;

	private int lv_size;
	private View lv_footer;
	private TextView lv_footer_more;
	private ProgressBar lv_footer_process;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ecg_record_history);
		appContext=(AppContext)getApplication();
		this.init();
	}
	
	private void init() {
		initHead(R.string.menu_record);

		listview = (PullToRefreshListView)findViewById(R.id.listview_record);

		adapter = new ListViewEcgRecordAdapter(this, list,
				R.layout.ecg_record_listitem);

		listview.setAdapter(adapter);

		lv_footer = getLayoutInflater().inflate(
				R.layout.listview_footer, null);
		lv_footer_more = (TextView) lv_footer
				.findViewById(R.id.listview_foot_more);
		lv_footer_process = (ProgressBar) lv_footer
				.findViewById(R.id.listview_foot_progress);

		listview.addFooterView(lv_footer);

		listview.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				listview.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (list.isEmpty())
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lv_footer) == view
							.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(listview.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					listview.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lv_footer_more.setText(R.string.load_ing);
					lv_footer_process.setVisibility(View.VISIBLE);
					// 当前pageIndex
					int pageIndex = lv_size / AppContext.PAGE_SIZE + 1;
					loadEcgRecord(pageIndex,
							UIHelper.LISTVIEW_ACTION_SCROLL);
				}

			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				listview.onScroll(view, firstVisibleItem, visibleItemCount,
						totalItemCount);
			}
		});

		listview.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				loadEcgRecord(1, UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		});

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView a, View view, int position,
					long id) {

				ListViewEcgRecordAdapter.ListItemView liv = (ListViewEcgRecordAdapter.ListItemView) view
						.getTag();

				if (liv != null) {
					EcgRecord record=(EcgRecord)liv.time_range.getTag();
					if(record!=null){
						UIHelper.showEcgRecordDetail(ECGRecordHistory.this, record);
					}
				}
			}
		});

		mHandler = new Handler() {

			public void handleMessage(Message msg) {

				List<EcgRecord> temp = null;

				if (msg.what > 0 && msg.obj != null) {
					temp = (List<EcgRecord>) msg.obj;

					if (msg.arg1 != UIHelper.LISTVIEW_ACTION_SCROLL) {
						list.clear();
					}
					list.addAll(temp);
					lv_size = list.size();

					if (temp.size() < AppContext.PAGE_SIZE) {
						listview.setTag(UIHelper.LISTVIEW_DATA_FULL);
						lv_footer_more.setText(R.string.load_full);
					} else {
						listview.setTag(UIHelper.LISTVIEW_DATA_MORE);
						lv_footer_more.setText(R.string.load_more);
					}

					adapter.notifyDataSetChanged();

					listview.onRefreshComplete();
					if(msg.arg1 != UIHelper.LISTVIEW_ACTION_SCROLL)
						listview.setSelection(0);
				} else if (msg.what == 0) {
					if (list.size() == 0) {
						listview.setTag(UIHelper.LISTVIEW_DATA_EMPTY);
						lv_footer_more.setText(R.string.load_empty);

					} else {
						listview.setTag(UIHelper.LISTVIEW_DATA_FULL);
						lv_footer_more.setText(R.string.load_full);
					}
				} else if (msg.what == -1) {					
					((AppException)msg.obj).makeToast(ECGRecordHistory.this);
				}

				lv_footer_process.setVisibility(ProgressBar.GONE);
			}
		};
		//loadEcgRecord(1, UIHelper.LISTVIEW_ACTION_INIT);
		if(!appContext.isLogin()){
			UIHelper.showLoginDialog(this);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(list.size()==0){
			loadEcgRecord(1, UIHelper.LISTVIEW_ACTION_REFRESH);
		}
	}

	private void loadEcgRecord(final int pageIndex, final int action) {
		// loading = new LoadingDialog(parent);
		// loading.show();
		new Thread() {
			public void run() {
				Message msg = new Message();
				msg.what = 0;			
				try {
					EcgRecordList list1 = appContext
							.getRecordList(pageIndex);
					List<EcgRecord> list2 = list1.getList();					
					msg.what = list2.size();
					msg.obj = list2;
				} catch (AppException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				mHandler.sendMessage(msg);
			}
		}.start();
	}
}
