package com.healthme.app.adapter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.healthme.app.R;
import com.healthme.app.bean.Hmessage;
import com.healthme.app.swipelistview.SwipeListView;

/**
 * 动弹Adapter类
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class ListViewHmessageAdapter extends BaseAdapter {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	
	private List<Hmessage> data;
    private Context context;
	private LayoutInflater minInflater;
	
	SwipeListView swipeListView;

    public ListViewHmessageAdapter(Context context, List<Hmessage> data, SwipeListView swipeListView) {
        this.context = context;
        this.data = data;        
        this.swipeListView = swipeListView;
        minInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Hmessage getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Hmessage item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
        	
            convertView = minInflater.inflate(R.layout.hmessage_listitem, parent, false);
            
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.hmessage_listitem_icon);
            holder.sendTime = (TextView) convertView.findViewById(R.id.hmessage_listitem_sendTime);
            holder.sender = (TextView) convertView.findViewById(R.id.hmessage_listitem_sender);
            holder.content = (TextView) convertView.findViewById(R.id.hmessage_listitem_content);
            holder.remove = (ImageView) convertView.findViewById(R.id.id_remove);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
       
        holder.icon.setImageResource(item.getViewTime()==null?R.drawable.msg_new:R.drawable.msg_nor);
        holder.sendTime.setText(sdf.format(item.getSendTime()));
        holder.content.setText(item.getContent());

        holder.remove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				swipeListView.closeAnimate(position);
				swipeListView.dismiss(position);
			}
		});
        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView sendTime;
        TextView sender;
        TextView content;       
        ImageView remove;
    }
}