package com.healthme.app.ui;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.healthme.app.AppContext;
import com.healthme.app.AppException;
import com.healthme.app.R;
import com.healthme.app.api.ApiClient;
import com.healthme.app.bean.Relative;
import com.healthme.app.common.UIHelper;
import com.healthme.common.util.RegexUtil;

    public class RelativeListAdapter extends BaseAdapter {
    	
    	static final int SEND_DEL_MESSAGE = 0;
    	static final int SEND_ADD_MESSAGE = 1;
    	static final int CANCEL_RELATIVE = 2;
    	static final int UNDO_INFO = 3;
    	static final int UPDATE_UI = 4;
		static final int ADD=1;
		static final int DEL=2;

    	
        private LayoutInflater mInflater;
        public List<Relative> myItems = new ArrayList<Relative>();
		private ViewHolder activeHolder;
		private static Context context;
		private static AppContext appContext;
		private ListView listView;
		private Map<Relative, View> cvMap=new HashMap<Relative,View>();
		private static ActionHandler handler;
        public RelativeListAdapter(Context context,List<Relative> myItems,ListView listView) {
        	mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.myItems=myItems;
            this.context=context;
            this.listView=listView;
            appContext=(AppContext) ((Activity)context).getApplication();
            handler=new ActionHandler(this);
            notifyDataSetChanged();
        }
 
        public int getCount() {
            return myItems.size();
        }
 
        public Relative getItem(int position) {
            return myItems.get(position);
        }
 
        public List<Relative> getItems() {
            return myItems;
        }
        
        public long getItemId(int position) {
            return position;
        }
 
        public ViewHolder getActiveViewHoler(){
        	return activeHolder;
        }
        
        
        public View getView(int position, View convertView, ViewGroup parent) {
           final ViewHolder holder;
           Relative item = myItems.get(position);
           holder = new ViewHolder();
           convertView = mInflater.inflate(R.layout.relative_listitem, null);
            convertView=initViewHolder(holder,item,position);
            //if id is 0, which means a new adding item
            if(item.getId()==0){
            	holder.actionLayout.setVisibility(View.VISIBLE);
	            holder.actionLayout.setBackgroundResource(R.color.gold);
	            holder.name.setFocusable(true);
	            holder.name.setFocusableInTouchMode(true);
	            holder.name.setBackgroundResource(R.drawable.edit_text);
	            holder.name.setText(item.getFullName());
//	            holder.name.setHint("亲属姓名");
	            holder.name.setHintTextColor(listView.getResources().getColor(R.color.white));
	            holder.phone.setFocusable(true);
	            holder.phone.setBackgroundResource(R.drawable.edit_text);
	            holder.phone.setFocusableInTouchMode(true);
	            holder.phone.setText(item.getPhoneNumber());
//	            holder.phone.setHint("手机号码");
	            holder.phone.setHintTextColor(listView.getResources().getColor(R.color.white));
	            holder.name.requestFocus();
            }else{
            	
	            holder.name.setText(myItems.get(position).getFullName());
	            holder.name.setFocusable(false);
	            holder.phone.setText(myItems.get(position).getPhoneNumber());
	            holder.phone.setFocusable(false);
	            holder.id.setText(myItems.get(position).getId()+"");
	            holder.actionLayout.setVisibility(View.INVISIBLE);
	            holder.actionLayout.setBackgroundResource(R.color.white);
	            holder.name.setFocusable(false);
	            holder.name.setBackgroundResource(R.color.white);
	            holder.name.setPadding(0, 0, 0, 0);
	            holder.phone.setFocusable(false);
	            holder.phone.setBackgroundResource(R.color.white);
	            holder.phone.setPadding(0, 0, 0, 0);
            }
            cvMap.put(item, convertView);
            return convertView;
        }

        
        
        
		public void addNewItem(Relative r,ListView listView) {
			clearUnsavedView();
			r.setId(0);
			r.setFullName("");
			r.setPhoneNumber("");
			myItems.add(0, r);
			notifyDataSetChanged();
			listView.setSelection(0);
//			ViewHolder holder = (ViewHolder) listView.getChildAt(0).getTag();
//			
//			holder.name.performLongClick();
		}
		
		private View initViewHolder(final ViewHolder holder, Relative relative, final int position){
            View convertView = mInflater.inflate(R.layout.relative_listitem, null);
            holder.name = (EditText) convertView
                    .findViewById(R.id.relativeName);
            holder.phone = (EditText) convertView
                    .findViewById(R.id.relativePhone);
            holder.id = (TextView) convertView
                    .findViewById(R.id.relativeId);
            holder.relative=relative;
            holder.actionLayout=(LinearLayout)convertView.findViewById(R.id.actionLayout);
            
//            holder.itemLayout=(RelativeLayout)convertView.findViewById(R.id.relativeItemLayout);
            ImageView submitButton=(ImageView) convertView.findViewById(R.id.submit);
            ImageView deleteButton=(ImageView) convertView.findViewById(R.id.delRelative);
            OnLongClickListener onLongClickListener = new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					ViewHolder activeHolder = getActiveViewHoler();
					if(activeHolder!=null){
						resetActiveView(activeHolder);
					}
					v.setBackgroundResource(R.drawable.edit_text);
					holder.actionLayout.setBackgroundResource(R.color.gold);
					holder.actionLayout.setVisibility(View.VISIBLE);
					v.clearFocus();
					v.setFocusable(true);
					v.setFocusableInTouchMode(true);
					v.requestFocus();
//					InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(PatientInfoActivity.INPUT_METHOD_SERVICE);
//					inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
					activeHolder=holder;
					return true;
				}
    			
    		};
//            holder.itemLayout.setOnLongClickListener(onLongClickListener);
    		holder.phone.setOnLongClickListener(onLongClickListener);
//    		holder.phone.addTextChangedListener(new TextWatcher() {
//				private CharSequence charSequence;
//
//				@Override
//    			public void onTextChanged(CharSequence s, int start, int before, int count) {
//					charSequence = s;
//    			}
//    			
//    			@Override
//    			public void beforeTextChanged(CharSequence s, int start, int count,
//    					int after) {
//    				
//    			}
//    			
//    			@Override
//    			public void afterTextChanged(Editable s) {
//    	            // 限定EditText只能输入10字符，并且达到10个的时候用红色显示
//    	            if (charSequence.length()>0&&(!RegexUtil.isMobileNum(charSequence.toString()))) {
//    	                Toast.makeText(context, "无效手机格式"+charSequence.toString(),
//    	                        Toast.LENGTH_SHORT).show();
//    	                holder.phone.setTextColor(Color.RED);
//    	            }
//    			}
//    		});
            holder.name.setOnLongClickListener(onLongClickListener);
            holder.name.addTextChangedListener(new TextWatcher() {
				private CharSequence charSequence;

				@Override
    			public void onTextChanged(CharSequence s, int start, int before, int count) {
					charSequence = s;
    			}
    			
    			@Override
    			public void beforeTextChanged(CharSequence s, int start, int count,
    					int after) {
    				
    			}
    			
    			@Override
    			public void afterTextChanged(Editable s) {
    	            // 限定EditText只能输入10字符，并且达到10个的时候用红色显示
    	            if (charSequence.length() > 8) {
    	                Toast.makeText(context, "你输入的用户名已经超过了长度限制！",
    	                        Toast.LENGTH_SHORT).show();
    	                holder.name.setTextColor(Color.RED);
    	            }
    			}
    		});
            
            OnFocusChangeListener focusListener = new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(!hasFocus){
						resetActiveView(getActiveViewHoler());
					}
					
				}
			};
            holder.name.setOnFocusChangeListener(focusListener);
            holder.phone.setOnFocusChangeListener(focusListener);
            holder.submitButton=submitButton;
            holder.deleteButton=deleteButton;
            holder.submitButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Relative item = myItems.get(position);
					item.setFullName(holder.name.getText().toString().trim());
					String number = holder.phone.getText().toString().trim();
					if(!RegexUtil.isMobileNum(number)){
						UIHelper.ToastMessage(appContext, "手机格式错误");
						return ;
					}
					item.setPhoneNumber(number);
					Message msg = handler.obtainMessage(SEND_ADD_MESSAGE);
					msg.arg1=ADD;
					msg.obj=item;
					handler.sendMessage(msg);
				}
			});
			deleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Message msg = handler.obtainMessage(SEND_DEL_MESSAGE);
					msg.arg1=DEL;
					msg.obj=holder.relative;
					handler.sendMessage(msg);
				}
			});
            convertView.setTag(holder);
            
            return convertView;
		}
		
		public void resetActiveView(ViewHolder activeHolder){
			if(activeHolder==null)
				return;
			activeHolder.actionLayout.setVisibility(View.INVISIBLE);
			activeHolder.name.setBackgroundResource(R.color.white);
			activeHolder.name.setPadding(0, 0, 0, 0);
			activeHolder.name.setText(activeHolder.relative.getFullName());
			activeHolder.name.setFocusable(false);
			activeHolder.phone.setBackgroundResource(R.color.white);
			activeHolder.phone.setPadding(0, 0, 0, 0);
			activeHolder.phone.setText(activeHolder.relative.getPhoneNumber());
			activeHolder.phone.setFocusable(false);
		}

		public void clearUnsavedView(){
			for (Iterator iterator = myItems.iterator(); iterator.hasNext();) {
				Relative relative = (Relative) iterator.next();
				if(relative.getId()==0)
					iterator.remove();
			}
			notifyDataSetChanged();
		}
		
		public void resetSize(){
			((PatientInfoActivity)context).setListViewHeightBasedOnChildren(listView, RelativeListAdapter.this);
		}
		
		static class ActionHandler extends Handler{
			 WeakReference<RelativeListAdapter> adaptor;
			 List<Relative> myItems;
			 public ActionHandler(RelativeListAdapter adaptor) {
				 this.adaptor = new WeakReference<RelativeListAdapter>(adaptor); 
				 myItems=this.adaptor.get().getItems();
			 }
			@Override
			public void handleMessage(Message msg) {
				final Relative r=(Relative) msg.obj;
				switch (msg.what) {
				case SEND_ADD_MESSAGE:
					new Thread(){
						public void run() {
							Map<String, Object> params=new HashMap<String, Object>();
							params.put("fullName", r.getFullName());
							params.put("phoneNumber", r.getPhoneNumber());
							params.put("patientId",appContext.getLoginUid());
							params.put("id",r.getId());
							Relative result;
							try {
								result = ApiClient.addRelative(appContext, params);
								if(result!=null){
									Message message = handler.obtainMessage(UPDATE_UI);
									message.obj=result;
									message.arg1=ADD;
									handler.sendMessage(message);
								}else{
									UIHelper.ToastMessage(context, "添加失败!");
									adaptor.get().notifyDataSetChanged();
								}
							} catch (AppException e) {
								UIHelper.ToastMessage(context, "添加失败!");
								adaptor.get().notifyDataSetChanged();
							}
						};
					}.start();
					break;
					
				case SEND_DEL_MESSAGE:
					new Thread(){
						public void run() {
							//remove the unsave relative
							if(r.getId()==0){
								Message message = handler.obtainMessage(UPDATE_UI);
								message.obj=r;
								message.arg1=DEL;
								handler.sendMessage(message);
								return ;
							}
							Map<String, Object> params=new HashMap<String, Object>();
							params.put("fullName", r.getFullName());
							params.put("phoneNumber", r.getPhoneNumber());
							params.put("patientId",r.getPatientId());
							params.put("id",r.getId());
							boolean result = ApiClient.delRelative(appContext, params);
							if(result){
								Message message = handler.obtainMessage(UPDATE_UI);
								message.obj=r;
								message.arg1=DEL;
								handler.sendMessage(message);
							}else{
								UIHelper.ToastMessage(context, "刪除失败!");
								adaptor.get().notifyDataSetChanged();
							}
						};
					}.start();
					break;
				case UPDATE_UI:
					int i=0;
					for(;i<myItems.size();i++){
						if(msg.arg1==ADD){
							if(myItems.get(i).getId()==0){
								Relative item = myItems.get(i);
								item.setId(r.getId());
								item.setFullName(r.getFullName());
								item.setPatientId(r.getPatientId());
								item.setPhoneNumber(r.getPhoneNumber());
								UIHelper.ToastMessage(context, "添加成功!");
								break;
							}else if(myItems.get(i).getId()==r.getId()){
								myItems.set(i, r);
								UIHelper.ToastMessage(context, "修改成功!");
								break;
							}
						}else if(msg.arg1==DEL){
							if(myItems.get(i).getId()==r.getId()){
								myItems.remove(i);
								UIHelper.ToastMessage(context, "刪除成功!");
								break;
							}
						}
					}
					appContext.saveRelatives((ArrayList<Relative>) myItems);
					adaptor.get().notifyDataSetChanged();
					adaptor.get().resetSize();
				default:
					break;
				}
			}
		}
		class ViewHolder {
			ImageView submitButton;
			ImageView deleteButton;
			LinearLayout actionLayout;
			EditText name;
			EditText phone;
			TextView id;
			Relative relative;
			@Override
			public String toString() {
				return relative.toString();
			}
		}
    }
 
 
