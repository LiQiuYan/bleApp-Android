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
import com.healthme.app.bean.ECGClassification;

/**
 * 动弹Adapter类
 * 
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class ListViewECGClassificationAdapter extends MyBaseAdapter {
	private Context context;// 运行上下文
	private int itemViewResource;
	private List<ECGClassification> listItems;// 数据集合
	private LayoutInflater listContainer;// 视图容器

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
			Locale.CHINA);

	public static class ListItemView { // 自定义控件集合
		public ImageView radio;
		public TextView start;
		public TextView end;		
	}

	/**
	 * 实例化Adapter
	 * 
	 * @param context
	 * @param data
	 * @param resource
	 */
	public ListViewECGClassificationAdapter(Context context,
			List<ECGClassification> data, int resource) {
		this.context = context;
		this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.listItems = data;
	}

	public int getCount() {
		return listItems.size();
	}

	public Object getItem(int arg0) {
		if(listItems!=null&&listItems.size()>arg0)
			return listItems.get(arg0);
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		// Log.d("method", "getView");

		// 自定义视图
		ListItemView listItemView = null;

		if (convertView == null) {
			// 获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);

			listItemView = new ListItemView();

			listItemView.radio = (ImageView) convertView
					.findViewById(R.id.ecgclassification_listitem_radio);
			listItemView.start = (TextView) convertView
					.findViewById(R.id.ecgclassification_listitem_start);
			listItemView.end = (TextView) convertView
					.findViewById(R.id.ecgclassification_listitem_end);
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}
		
		// 设置文字和图片
		ECGClassification record = listItems.get(position);
		
		int checked=0;
		if(record.getChecked()!=null){
			checked=record.getChecked().intValue();
		}
		
		listItemView.radio.setImageResource(checked>0?R.drawable.radio_button_on_icon:R.drawable.radio_button_off_icon);

		

		if (record.getStartTime() != null) {
			listItemView.start.setText(sdf.format(record.getStartTime()));
		}
//		if (record.getEndTime() != null) {
//			listItemView.end.setText(sdf.format(record.getEndTime()));
//		}
		listItemView.radio.setTag(record);		
		
		return convertView;
	}
	
	

}