package com.healthme.app.ui;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.healthme.app.AppContext;
import com.healthme.app.AppException;
import com.healthme.app.R;
import com.healthme.app.api.ApiClient;
import com.healthme.app.bean.Patient;
import com.healthme.app.bean.Relative;
import com.healthme.app.bean.User;
import com.healthme.app.common.UIHelper;

@SuppressLint("NewApi")
public class PatientInfoActivity extends BaseActivity {
	static final int SEND_MESSAGE=0;
	static final int UPDATE_INFO_UI=1;
	static final int GET_RELATIVE_UI=2;
	private List<Relative> relatives=new ArrayList<Relative>();
	private ListView relativeList;
	private ListView patientInfoList;
	private ImageView addRelativeButton;
	private OnClickListener relativeActionListener;
	private RelativeListAdapter relativeListItemAdapter;
	private TextView userName;
	private InputMethodManager inputMethodManager;
	private ImageView cancelButton;
	private CircleImageView photo;
	private ImageView undoButton;
	private ImageView submitButton;
	private BasicPatientInfoListAdapter patientItemAdapter;
	private AppContext appContext;
	private ActionHandler actionHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appContext = (AppContext) getApplication();
		setContentView(R.layout.patient_info);
		this.initHead("我的信息");
		init();
		setListener();
	}

	@SuppressWarnings("unchecked")
	public void init() {
		addRelativeButton = (ImageView) findViewById(R.id.addRelative);
		cancelButton = (ImageView) findViewById(R.id.cancel);
		userName = (TextView) findViewById(R.id.userName);
		
		undoButton = (ImageView) findViewById(R.id.undo);
		submitButton=(ImageView) findViewById(R.id.submit);
		relativeList = (ListView) findViewById(R.id.relativeList);
		patientInfoList = (ListView) findViewById(R.id.patientInfoList);
		
		DisplayMetrics metric = new DisplayMetrics();		
		getWindowManager().getDefaultDisplay().getMetrics(metric);
	    int height = metric.heightPixels;   // 屏幕高度（像素）
	    photo = (CircleImageView) findViewById(R.id.photo);
		photo.getLayoutParams().height=height/5;
		photo.getLayoutParams().width=height/5;
		
		inputMethodManager = (InputMethodManager) getSystemService(PatientInfoActivity.INPUT_METHOD_SERVICE);
		initPatientInfoList();
		relatives=appContext.getRelatives();
		if(relatives==null||relatives.size()==0){
			relatives=new ArrayList<Relative>();
		}
		initRelativeList();
		actionHandler = new ActionHandler();
	}

	private void initPatientInfoList() {
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		User p=appContext.getLastLoginInfo();
		appContext.getLoginInfo();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ItemTitle", "姓       名:");
		map.put("ItemText", p.getName());
		map.put("field", "fullName");
		listItem.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemTitle", "邮       箱:");
		map.put("ItemText", p.getEmail());
		map.put("field", "email");
		listItem.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemTitle", "生       日:");
		map.put("ItemText",
				new SimpleDateFormat("yyyy-MM-dd").format(p.getBirthday()));
		map.put("field", "birthday");
		listItem.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemTitle", "性       别:");
		map.put("ItemText", (p.getGender().equalsIgnoreCase(Patient.SEX.MALE.name())) ? "男" : "女");
		map.put("field", "sex");
		listItem.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemTitle", "手机号码:");
		map.put("ItemText", p.getPhoneNumber());
		map.put("field", "phone");
		listItem.add(map);
		userName.setText(p.getName());
		// 生成适配器的Item和动态数组对应的元素
		patientItemAdapter = new BasicPatientInfoListAdapter(this, listItem,// 数据源
				R.layout.basic_patientinfo_listitem,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "ItemTitle", "ItemText","field" },
				// ImageItem的XML文件里面的两个TextView ID
				new int[] { R.id.nameTitle, R.id.info,R.id.field },patientInfoList);

		// 添加并且显示
		patientInfoList.setAdapter(patientItemAdapter);
		setListViewHeightBasedOnChildren(patientInfoList,patientItemAdapter);
	}

	
	
	private void initRelativeList() {
		relativeListItemAdapter = new RelativeListAdapter(PatientInfoActivity.this, relatives,relativeList);
		// 添加并且显示
		relativeList.setAdapter(relativeListItemAdapter);
		setListViewHeightBasedOnChildren(relativeList,relativeListItemAdapter);
	}

	
	public void setListViewHeightBasedOnChildren(ListView listView,BaseAdapter adatper) {
	    int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
	    int totalHeight = 0;
	    View view = null;
	    for (int i = 0; i < adatper.getCount(); i++) {
	        view = adatper.getView(i, view, listView);
	        if (i == 0) {
	            view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));
	        }
	        view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
	        totalHeight += view.getMeasuredHeight();
	    }
	    ViewGroup.LayoutParams params = listView.getLayoutParams();
	    params.height = totalHeight + (listView.getDividerHeight() * (adatper.getCount() - 1));
	    listView.setLayoutParams(params);
	    listView.requestLayout();
	}
	private void setListener() {
		undoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				patientItemAdapter.c
				patientItemAdapter.notifyDataSetInvalidated();
			}
		});
		
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Map<String, String> data = patientItemAdapter.getChangedData();
				Message msg = actionHandler.obtainMessage(UPDATE_INFO_UI);
				msg.obj=data;
				actionHandler.sendMessage(msg);
			}
		});
		
		relativeActionListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == addRelativeButton.getId()) {
					if (relativeListItemAdapter.getItems().size()>=4){
						UIHelper.ToastMessage(appContext, "超过亲属数量限制");
						return;
					}
					Relative r=new Relative();
					relativeListItemAdapter.addNewItem(r, relativeList);
					setListViewHeightBasedOnChildren(relativeList, relativeListItemAdapter);
				} else
					Log.i(getClass().getName(), "unkown view action:" + v);
			}
		};
		addRelativeButton.setOnClickListener(relativeActionListener);
		OnClickListener cancelOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				List<Relative> items = relativeListItemAdapter.getItems();
				for (Iterator iterator = items.iterator(); iterator.hasNext();) {
					Relative relative = (Relative) iterator.next();
					if(relative.getId()==0){
						iterator.remove();
					}
				}
				relativeListItemAdapter.notifyDataSetChanged();
				setListViewHeightBasedOnChildren(relativeList, relativeListItemAdapter);
			}
		};
		cancelButton.setOnClickListener(cancelOnClickListener);

	}
	
	class ActionHandler extends Handler {
		
		public ActionHandler() {
		}

		public ActionHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			final Object data;
			switch (msg.what) {
			case SEND_MESSAGE:
				data=msg.obj;
				new Thread(){
					public void run() {
						appContext.updateCurUser((Map<String, String>) data);
						actionHandler.obtainMessage(UPDATE_INFO_UI);
					};
				}.start();
				break;
			case GET_RELATIVE_UI:
//				try {
//					final int patientId = appContext.getLoginUid();
//					relatives=ApiClient.getRelatives(appContext, patientId);
//				} catch (Exception e) {
//					UIHelper.ToastMessage(appContext, "获取亲属信息失败");
//				}
//				if(relatives==null||relatives.size()==0){
//					relatives=new ArrayList<Relative>();
//				}
//				initRelativeList();
				break;
			case UPDATE_INFO_UI:
				patientItemAdapter.updateUI();
				UIHelper.ToastMessage(appContext, "信息更新成功！");
				break;
			default:
				break;
			}
			
		}
	}

}