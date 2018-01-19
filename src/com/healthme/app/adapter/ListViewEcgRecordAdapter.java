package com.healthme.app.adapter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.healthme.app.R;
import com.healthme.app.bean.EcgRecord;
import com.healthme.app.common.StringUtils;

/**
 * 动弹Adapter类
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class ListViewEcgRecordAdapter extends MyBaseAdapter {
	private Context 					context;//运行上下文
	private List<EcgRecord> 				listItems;//数据集合
	private LayoutInflater 				listContainer;//视图容器
	private int 						itemViewResource;//自定义项视图源
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	public static class ListItemView{				//自定义控件集合  
			public ImageView recordIcon;  
//	        public TextView username;  
		    public TextView time_range;  
		    public TextView rate;
		    public TextView summary;
	 }  

	/**
	 * 实例化Adapter
	 * @param context
	 * @param data
	 * @param resource
	 */
	public ListViewEcgRecordAdapter(Context context, List<EcgRecord> data,int resource) {
		this.context = context;			
		this.listContainer = LayoutInflater.from(context);	//创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.listItems = data;		
	}
	
	public int getCount() {
		return listItems.size();
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}
	   
	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.d("method", "getView");
		
		//自定义视图
		ListItemView  listItemView = null;
		
		if (convertView == null) {
			//获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);
			
			listItemView = new ListItemView();
			//获取控件对象
			listItemView.recordIcon = (ImageView)convertView.findViewById(R.id.record_listitem_icon);
//			listItemView.username = (TextView)convertView.findViewById(R.id.tweet_listitem_username);
			listItemView.time_range = (TextView)convertView.findViewById(R.id.record_listitem_time_range);
//			listItemView.image= (ImageView)convertView.findViewById(R.id.tweet_listitem_image);
			listItemView.rate= (TextView)convertView.findViewById(R.id.record_listitem_rate);			
			listItemView.summary= (TextView)convertView.findViewById(R.id.record_listitem_summary);			
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}
				
		//设置文字和图片
		EcgRecord record = listItems.get(position);
//		listItemView.username.setText(tweet.getAuthor());
//		listItemView.username.setTag(tweet);//设置隐藏参数(实体类)
		String range="";
		if(record.getStartTime()!=null){
			range=sdf.format(record.getStartTime());
			if(record.getEndTime()!=null)
				range+=" - "+sdf.format(record.getEndTime());
		}
		listItemView.time_range.setText(range);
//		listItemView.content.parseLinkText();
//		listItemView.content.setLinkText(tweet.getBody());
		listItemView.time_range.setTag(record);//设置隐藏参数(实体类)
//		listItemView.content.setOnClickListener(linkViewClickListener);
//		listItemView.content.setLinkClickListener(linkClickListener);		
		
		String rate="";
		if(!StringUtils.isEmptyOr0(record.getAverageHeartbeat())){
			rate+=" 平均心率: "+record.getAverageHeartbeat();
		}		
		if(!rate.equals(""))rate=rate.substring(1);
		listItemView.rate.setText(rate);
		
		String summary="";		
		if(!StringUtils.isEmptyOr0(record.getTotalPvcNumber())){
			summary+=" 室早:"+record.getTotalPvcNumber();
		}
		if(!StringUtils.isEmptyOr0(record.getTotalSvpbNumber())){
			summary+=" 室上早:"+record.getTotalSvpbNumber();
		}
		if(!StringUtils.isEmptyOr0(record.getPauseNumber())){
			summary+=" 停博:"+record.getPauseNumber();
		}
		if(!StringUtils.isEmptyOr0(record.getAfibNumber())){
			summary+=" 房颤:"+record.getAfibNumber();
		}
		if(!summary.equals(""))summary=summary.substring(1);
		listItemView.summary.setText(summary);

//		
//		switch(tweet.getAppClient())
//		{	
//			default:
//				listItemView.client.setText("");
//				break;
//			case Tweet.CLIENT_MOBILE:
//				listItemView.client.setText("来自:手机");
//				break;
//			case Tweet.CLIENT_ANDROID:
//				listItemView.client.setText("来自:Android");
//				break;
//			case Tweet.CLIENT_IPHONE:
//				listItemView.client.setText("来自:iPhone");
//				break;
//			case Tweet.CLIENT_WINDOWS_PHONE:
//				listItemView.client.setText("来自:Windows Phone");
//				break;
//		}
//		if(StringUtils.isEmpty(listItemView.client.getText().toString()))
//			listItemView.client.setVisibility(View.GONE);
//		else
//			listItemView.client.setVisibility(View.VISIBLE);
		
		if(position%2==0){
			listItemView.recordIcon.setImageResource(R.drawable.record_warning);
		}
		else{
			listItemView.recordIcon.setImageResource(R.drawable.record_ok);
		}
//		listItemView.userface.setOnClickListener(faceClickListener);
		listItemView.recordIcon.setTag(record);
		
//		String imgSmall = tweet.getImgSmall();
//		if(!StringUtils.isEmpty(imgSmall)) {
//			bmpManager.loadBitmap(imgSmall, listItemView.image, BitmapFactory.decodeResource(context.getResources(), R.drawable.image_loading));
//			listItemView.image.setOnClickListener(imageClickListener);
//			listItemView.image.setTag(tweet.getImgBig());
//			listItemView.image.setVisibility(ImageView.VISIBLE);
//		}else{
//			listItemView.image.setVisibility(ImageView.GONE);
//		}
		
		return convertView;
	}
	
//	private View.OnClickListener faceClickListener = new View.OnClickListener(){
//		public void onClick(View v) {
//			EcgRecord tweet = (EcgRecord)v.getTag();
//			UIHelper.showUserCenter(v.getContext(), tweet.getAuthorId(), tweet.getAuthor());
//		}
//	};
	
//	private View.OnClickListener imageClickListener = new View.OnClickListener(){
//		public void onClick(View v) {
//			UIHelper.showImageDialog(v.getContext(), (String)v.getTag());
//		}
//	};
//	
//	private View.OnClickListener linkViewClickListener = new View.OnClickListener() {
//		public void onClick(View v) {
//			if(!isLinkViewClick()){
//			    UIHelper.showTweetDetail(v.getContext(),((EcgRecord)v.getTag()).getId());
//			}
//			setLinkViewClick(false);
//		}
//	};
//	
//	private OnLinkClickListener linkClickListener = new OnLinkClickListener() {
//		public void onLinkClick() {
//			setLinkViewClick(true);
//		}
//	};
}