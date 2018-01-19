package com.healthme.app.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.healthme.app.AppContext;
import com.healthme.app.bean.User;
import com.healthme.app.bean.UtilDateDeserializer;

import com.healthme.app.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class EcgUserInfoView extends LinearLayout implements OnScrollListener{  
	   
	private static DateFormat ymd= new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private final static String TAG = "BaseView";  
    
    // 初始化   
    private final static int INIT = 0; 
//    // 松开刷新标志   
//    private final static int RELEASE_To_REFRESH = 1; 
    // 正在刷新标志   
    private final static int REFRESHING = 2;  
    // 刷新完成标志   
    private final static int DONE = 3;  
  
    private LayoutInflater inflater;  
  
    private LinearLayout headView;  
    private ProgressBar progressBar;  
  
    private int headContentWidth;  
    private int headContentHeight;  
    private int headContentOriginalTopPadding;
  
    private int firstItemIndex;  
    private int currentScrollState;
  
    private int state;  
  
    private boolean isBack;  
  
    public OnRefreshListener refreshListener;

	private List<User> data;

	private TextView userName;

	private Activity main;

	private TextView gender;

	private TextView address;

	private TextView birthday;

	private TextView city;

	private TextView cuntry;

	private TextView joinTime;

	private TextView phoneNumber;

	private TextView postalCode;

	private TextView province;

	private static boolean init;  
    
    public EcgUserInfoView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        init(context);  
    }  
    
    @TargetApi(11)
    public EcgUserInfoView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        init(context);  
    }  
  
    private void init(Context context) {   
    	if (context instanceof Activity)
    	{
    	    main = (Activity)context;
    	}
        inflater = LayoutInflater.from(context);  
        headView = (LinearLayout) inflater.inflate(R.layout.pull_to_refresh_head, null);  
        progressBar = (ProgressBar) headView.findViewById(R.id.head_progressBar);  
        headContentOriginalTopPadding = headView.getPaddingTop();  
        
        measureView(headView);  
        headContentHeight = headView.getMeasuredHeight();  
        headContentWidth = headView.getMeasuredWidth(); 
        
        headView.setPadding(headView.getPaddingLeft(), -1 * headContentHeight, headView.getPaddingRight(), headView.getPaddingBottom());  
        headView.invalidate();  
    }  
  
    public void onScroll(AbsListView view, int firstVisiableItem, int visibleItemCount,  int totalItemCount) {  
        firstItemIndex = firstVisiableItem;  
    }  
  
    public void onScrollStateChanged(AbsListView view, int scrollState) {  
    	currentScrollState = scrollState;
    }  
  
  
    // 当状态改变时候，调用该方法，以更新界面   
    private void changeHeaderViewByState() {  
        User u;
		switch (state) {  
        case INIT:
        	userName=(TextView) findViewById(R.id.ecg_user_username);
        	gender=(TextView) findViewById(R.id.ecg_user_gender);
        	address=(TextView) findViewById(R.id.ecg_user_address);
        	birthday=(TextView) findViewById(R.id.ecg_user_birthday);
        	city=(TextView) findViewById(R.id.ecg_user_city);
        	cuntry=(TextView) findViewById(R.id.ecg_user_country);
        	joinTime=(TextView) findViewById(R.id.ecg_user_jointime);
        	phoneNumber=(TextView) findViewById(R.id.ecg_user_phoneNumber);
        	postalCode=(TextView) findViewById(R.id.ecg_user_postalCode);
        	province=(TextView) findViewById(R.id.ecg_user_province);
        	
        	break;
        case REFRESHING:   
        	//System.out.println("刷新REFRESHING-TopPad："+headContentOriginalTopPadding);
        	headView.setPadding(headView.getPaddingLeft(), headContentOriginalTopPadding, headView.getPaddingRight(), headView.getPaddingBottom());   
            headView.invalidate();  
  
            progressBar.setVisibility(View.VISIBLE);  
            
            
            Log.v(TAG, "当前状态,正在刷新...");  
            break;  
        case DONE:  
        	//System.out.println("完成DONE-TopPad："+(-1 * headContentHeight));
        	AppContext ac = (AppContext)main.getApplication();
        	u=ac.getLoginInfo();
        	if(ac.isLogin()&&u!=null){
        	userName.setText(u.getName());
        	gender.setText(u.getGender());
        	address.setText(u.getAddress());
        	if(u.getBirthday()!=null)
        		birthday.setText(ymd.format(u.getBirthday()));
        	city.setText(u.getCity());
        	cuntry.setText(u.getCountry());
        	joinTime.setText(u.getJointime());
        	phoneNumber.setText(u.getPhoneNumber());
        	postalCode.setText(u.getPostalCode());
        	province.setText(u.getProvince());
        	}else{
        		userName.setText("");
            	gender.setText("");
            	address.setText("");
            	birthday.setText("");
            	city.setText("");
            	cuntry.setText("");
            	joinTime.setText("");
            	phoneNumber.setText("");
            	postalCode.setText("");
            	province.setText("");
        	}
        	headView.setPadding(headView.getPaddingLeft(), -1 * headContentHeight, headView.getPaddingRight(), headView.getPaddingBottom());  
            headView.invalidate();  
  
            progressBar.setVisibility(View.GONE);  
            Log.v(TAG, "当前状态，done");  
            break;  
        }  
    }  
  
    //点击刷新
    public void clickRefresh() {
    	state = REFRESHING;  
        changeHeaderViewByState();  
        onRefresh(); 
    }
    
    public void setOnRefreshListener(OnRefreshListener refreshListener) {  
        this.refreshListener = refreshListener;  
    }  
  
    public interface OnRefreshListener {  
        public void onRefresh();  
    }  
  
    public void onRefreshComplete(String update) {  
        onRefreshComplete();
    } 
    
    public void onRefreshComplete() {  
        state = DONE;  
        changeHeaderViewByState();  
    }  
  
    private void onRefresh() {  
        if (refreshListener != null) {  
            refreshListener.onRefresh();  
        }  
    }  
  
    // 计算headView的width及height值  
    private void measureView(View child) {  
        ViewGroup.LayoutParams p = child.getLayoutParams();  
        if (p == null) {  
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,  
                    ViewGroup.LayoutParams.WRAP_CONTENT);  
        }  
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);  
        int lpHeight = p.height;  
        int childHeightSpec;  
        if (lpHeight > 0) {  
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,  
                    MeasureSpec.EXACTLY);  
        } else {  
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,  
                    MeasureSpec.UNSPECIFIED);  
        }  
        child.measure(childWidthSpec, childHeightSpec);  
    }

	public void setData(List<User> bvEcgUserData) {
		this.data=bvEcgUserData;
	} 
    
	public void onInit(){
		if(!init){
			init=true;
			this.state=INIT;
			changeHeaderViewByState();
		}
	}
}
