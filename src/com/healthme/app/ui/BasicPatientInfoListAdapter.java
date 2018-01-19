package com.healthme.app.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.healthme.app.R;
import com.healthme.app.common.StringUtils;
import com.healthme.app.common.UIHelper;

public class BasicPatientInfoListAdapter extends SimpleAdapter{

	private Activity context;
	private int position;
	private EditText editText;
	private ViewHolder activeView;
	private List<? extends Map<String, ?>> data;
	private ListView listView;
	private boolean isChanged;
	public BasicPatientInfoListAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to,ListView listView) {
		super(context, data, resource, from, to);
		this.data=data;
		this.context=(Activity) context;
		this.listView=listView;
	}

	public Map<String, ?> getCurItem(){
		return (Map<String, ?>) getItem(position);
	}
	
	public Map<String, String> getCurData(){
		int count = listView.getChildCount();
		Map<String, String> data=new HashMap<String, String>();
		for (int i = 0; i < count; i++) {
			View child = listView.getChildAt(i);
			EditText info = (EditText)child.findViewById(R.id.info);
			TextView field = (TextView)child.findViewById(R.id.field);
			data.put(field.getText().toString().trim(),info.getText().toString().trim());
		}
		Log.i("INFO", data.toString());
		return data;
	}
	
	public Map<String, String> getChangedData(){
		if(isChanged)
			return getCurData();
		return new HashMap<String, String>();
	}
	
	public void updateUI(){
		int count = listView.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = listView.getChildAt(i);
			EditText info = (EditText)child.findViewById(R.id.info);
			TextView field = (TextView)child.findViewById(R.id.field);
			Map<String, String> item = (Map<String, String>) listView.getItemAtPosition(i);
//			"ItemText","field" 
			item.put("ItemText", info.getText().toString().trim());
			item.put("field", field.getText().toString().trim());
		}
		notifyDataSetChanged();
		isChanged=false;
	}
	
	@Override
	public View getView(final int index, View convertView, ViewGroup parent) {
		boolean isNew=(convertView==null?true:false);
		View cv = super.getView(index, convertView, parent);
		
		final EditText info=(EditText) cv.findViewById(R.id.info);
		info.setFocusable(true);
		info.requestFocus();
		if(isNew){
			ViewHolder vh=new ViewHolder();
			vh.editText=info;
			vh.item=(Map<String, ?>) getItem(index);
			info.setTag(vh);
			info.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					position=index;
					editText=info;
					v.clearFocus();
					v.setFocusable(true);
					v.setFocusableInTouchMode(true);
					v.setBackgroundResource(R.drawable.edit_text);
					InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(PatientInfoActivity.INPUT_METHOD_SERVICE);
					inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
					activeView=(ViewHolder) v.getTag();
					isChanged=true;
					return false;
				}
			});
			
			info.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(!hasFocus){
						
						if(StringUtils.isEmpty(((EditText) v).getText().toString().trim())){
							UIHelper.ToastMessage(context, "输入值不能为空");
							return ;
						}
						v.setFocusable(false);
						v.setBackgroundResource(R.color.white);
						v.setPadding(0, 0, 0, 0);
					}
				}
			});
			if(convertView!=null)
				convertView.setTag(vh);
		}
		return cv;
	}
	
	public void resetActiveView(EditText v){
		v.setText(((ViewHolder)v.getTag()).item.get("ItemText").toString());
		v.setFocusable(false);
		v.setBackgroundResource(R.color.white);
		v.setPadding(0, 0, 0, 0);
	}
	
	class ViewHolder{
		EditText editText;
		Map<String,?> item;
	}
}

