package com.healthme.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.healthme.app.AppContext;
import com.healthme.app.AppException;
import com.healthme.app.R;
import com.healthme.app.adapter.ListViewECGClassificationAdapter;
import com.healthme.app.bean.ECGClassification;
import com.healthme.app.bean.ECGClassificationList;
import com.healthme.app.bean.EcgRecord;
import com.healthme.app.common.StringUtils;
import com.healthme.app.common.UIHelper;
import com.healthme.app.widget.PullToRefreshListView;

public class ECGCodeDetailList extends Fragment {

	private Handler mHandler;

	private EcgRecord record;

	private short code;

	private View view;

	private List<ECGClassification> list = new ArrayList<ECGClassification>();

	private ListViewECGClassificationAdapter adapter;

	private PullToRefreshListView listview;

	private int lv_size;
	private View lv_footer;
	private TextView lv_footer_more;
	private ProgressBar lv_footer_process;

	public ECGCodeDetailList(Activity app, EcgRecord record, short code) {
		// this.parent=app;
		this.record = record;
		this.code = code;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.v("INFO", "ECGCodeDetailList!!!!!!");
		view = inflater
				.inflate(R.layout.ecg_code_detail_list, container, false);
		if (listview == null) {
			this.init();
		}
		return view;
	}

	private void init() {
		listview = (PullToRefreshListView) view
				.findViewById(R.id.ecg_code_list);

		adapter = new ListViewECGClassificationAdapter(getActivity(), list,
				R.layout.ecgclassification_listitem);

		listview.setAdapter(adapter);

		lv_footer = getActivity().getLayoutInflater().inflate(
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
					loadECGClassification(pageIndex,
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
				loadECGClassification(1, UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		});

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView a, View view, int position,
					long id) {

				ListViewECGClassificationAdapter.ListItemView liv = (ListViewECGClassificationAdapter.ListItemView) view
						.getTag();

				if (liv != null) {

					ECGClassification ecg = (ECGClassification) liv.radio
							.getTag();

					for (ECGClassification o : list) {
						o.setChecked((byte)(o == ecg ? 2 : 0));
					}

					adapter.notifyDataSetChanged();

					drawWave(record.getId(), ecg.getStartPos().intValue(),
							ecg.getEndPos()==null?0:ecg.getEndPos().intValue());
				}
			}
		});

		mHandler = new Handler() {

			public void handleMessage(Message msg) {

				List<ECGClassification> temp = null;

				if (msg.what > 0 && msg.obj != null) {
					temp = (List<ECGClassification>) msg.obj;

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
					AppContext appContext = (AppContext) getActivity()
							.getApplication();
					Toast.makeText(appContext, "没有记录", Toast.LENGTH_SHORT)
							.show();
				}

				lv_footer_process.setVisibility(ProgressBar.GONE);
			}
		};
		loadECGClassification(1, UIHelper.LISTVIEW_ACTION_INIT);
	}

	private void loadECGClassification(final int pageIndex, final int action) {
		// loading = new LoadingDialog(parent);
		// loading.show();
		new Thread() {
			public void run() {
				Message msg = new Message();

				msg.what = 0;

				AppContext appContext = (AppContext) getActivity()
						.getApplication();

				try {
					ECGClassificationList list1 = appContext
							.getECGClassificationList(record.getId(), code,
									pageIndex, true);
					List<ECGClassification> list2 = list1.getList();
					if (record != null && record.getStartTime() != null) {
						long start = record.getStartTime().getTime();
						for (ECGClassification ecg : list2) {
							if (ecg.getStartPos() != null) {
								ecg.setStartTime(new Date(start
										+ ecg.getStartPos() * 1000L
										/ record.getSampleRate()));
							}
							if (ecg.getEndPos() != null) {
								ecg.setEndTime(new Date(start + ecg.getEndPos()
										* 1000L / record.getSampleRate()));
							}
							ecg.setChecked((byte)0);
						}
					}
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

	public void drawWave(int recordId, int start, int end) {

	}

}
