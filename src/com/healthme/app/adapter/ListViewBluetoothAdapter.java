package com.healthme.app.adapter;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.healthme.app.AppContext;
import com.healthme.app.R;
import com.healthme.app.bean.CustomizedBluetoothDevice;
import com.healthme.app.ui.BTDeviceSet;
import com.healthme.app.ui.Main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 问答Adapter类
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class ListViewBluetoothAdapter extends BaseAdapter {
	private Context 					context;//运行上下文
	private List<CustomizedBluetoothDevice> 					listItems;//数据集合
	private LayoutInflater 				listContainer;//视图容器
	private int 						itemViewResource;//自定义项视图源 
	private BluetoothAdapter mBluetoothAdapter;
	private TextView lvBluetooth_cur_device;//本机设备
	private TextView lvBluetooth_selected_device;//选定设备
	private TextView lvBluetooth_file_location;//文件存储位置
	private AppContext appContext;
	public static class ListItemView{				//自定义控件集合  
			public ImageView paired;
	        public TextView name;  
		    public TextView address;
			public ImageView checked;		    

	 }  

	/**
	 * 实例化Adapter
	 * @param context
	 * @param data
	 * @param resource
	 * @param mBluetoothAdapter 
	 */
	public ListViewBluetoothAdapter(Context context, List<CustomizedBluetoothDevice> data,int resource, BluetoothAdapter mBluetoothAdapter) {
		this.context = context;			
		this.listContainer = LayoutInflater.from(context);	//创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.listItems = data;
		this.mBluetoothAdapter=mBluetoothAdapter;
		lvBluetooth_cur_device = (TextView) ((Activity) context).findViewById(R.id.cur_device);
		lvBluetooth_selected_device = (TextView) ((Activity) context).findViewById(R.id.selected_device);	
		lvBluetooth_file_location = (TextView) ((Activity) context).findViewById(R.id.file_location);
		appContext = (AppContext)((Activity) context).getApplication();		
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
			listItemView.paired = (ImageView)convertView.findViewById(R.id.btdevice_listitem_icon);
			listItemView.name = (TextView)convertView.findViewById(R.id.btdevice_listitem_name);
			listItemView.address = (TextView)convertView.findViewById(R.id.btdevice_listitem_address);
			listItemView.checked = (ImageView)convertView.findViewById(R.id.btdevice_listitem_radio);
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}	
      
		//设置文字和图片
		CustomizedBluetoothDevice device = listItems.get(position);

		if(device.isStatusPaired()){
			listItemView.paired.setImageResource(R.drawable.bluetooth_enable);
		}else{
			listItemView.paired.setImageResource(R.drawable.bluetooth_disable);
		}
		if(device.isChecked()){
			listItemView.checked.setImageResource(R.drawable.radio_button_on_icon);
		}else{
			listItemView.checked.setImageResource(R.drawable.radio_button_off_icon);
		}		
		listItemView.paired.setOnClickListener(pairedClickListener);
		listItemView.paired.setTag(device);
		listItemView.checked.setOnClickListener(checkedClickListener);
		listItemView.checked.setTag(device);		
		listItemView.name.setText(device.getName());
		listItemView.name.setTag(device);//设置隐藏参数(实体类)
		listItemView.address.setText(device.getAddress());
//		listItemView.date.setText(StringUtils.friendly_time(device.getPubDate()));
//		listItemView.count.setText(device.getAnswerCount()+"回|"+device.getViewCount()+"阅");
		
		return convertView;
	}
    private void pairDevice(BluetoothDevice device) {
	try {


	    Method m = device.getClass()
		    .getMethod("createBond", (Class[]) null);
	    m.invoke(device, (Object[]) null);
	} catch (Exception e) {

	}
    }

    private void unpairDevice(BluetoothDevice device) {
	try {
	    Method m = device.getClass()
		    .getMethod("removeBond", (Class[]) null);
	    m.invoke(device, (Object[]) null);
	} catch (Exception e) {

	}
    }	
	private View.OnClickListener pairedClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			CustomizedBluetoothDevice device = (CustomizedBluetoothDevice)v.getTag();
//			if (device.isStatusPaired())
//			{
//				unpairDevice(device.device);
//				
//			} else
//			{
//				pairDevice(device.device);
//			}
			
//			UIHelper.showUserCenter(v.getContext(), post.getAuthorId(), post.getAuthor());
		}
	};
	private View.OnClickListener checkedClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			CustomizedBluetoothDevice device = (CustomizedBluetoothDevice)v.getTag();
			Main main=(Main)context;
//			AppContext appContext = (AppContext) main.getApplication();
//			appContext.setBluetoothDevice(device);
			main.setMatchDevice(device.device);
//			TextView selected_device = (TextView) main.findViewById(R.id.selected_device);
//			selected_device.setText(device.getName());
//			ChangeRadioImg(device);
//			if (appContext.getBluetoothDevice()!=null) {
//				Log.v("ListviewBT", "send Broadcast------"+UIHelper.ACTION_REFRESHDEVICE);
//				//Log.v("ListviewBT", context.getApplicationContext().getClass().getName());
//				
//				return;
//				//UIHelper.sendBroadCast(appContext, notice)
//				
////				EcgMainView vEcgMainView;
////				vEcgMainView = (EcgMainView)main.findViewById (R.id.ecgMain_SV);
////				vEcgMainView.refreshMonitor();
//			}
		}
	};


	private void ChangeRadioImg(CustomizedBluetoothDevice curDevice) {
		for (int i = 0; i < listItems.size(); i++) {
			CustomizedBluetoothDevice device = listItems.get(i);
			if (curDevice == device)
				device.setCheck(true);
			else
				device.setCheck(false);

			notifyDataSetChanged();
		}		
	}	         
	  	
	@Override
	public void notifyDataSetChanged() {
//		boolean checked=false;
//		for(CustomizedBluetoothDevice bt:listItems){
//			if(!checked){
//				if(appContext.getBluetoothDevice()==null||bt.equals(appContext.getBluetoothDevice())){					
//					bt.setCheck(true);
//					checked=true;
//				}
//				else{
//					bt.setCheck(false);
//				}
//			}
//			else{
//				bt.setCheck(false);
//			}
//		}
//		if (listItems.size()>0 && appContext.getBluetoothDevice()==null)
//		{
//
//			lvBluetooth_selected_device.setText(listItems
//				.get(0).getName());
//			appContext.setBluetoothDevice(listItems.get(0));
//			listItems.get(0).setCheck(true);
//		}
		
		lvBluetooth_cur_device.setText(mBluetoothAdapter.getName());
		if(appContext.getBluetoothDevice()!=null){
			lvBluetooth_selected_device.setText(appContext.getBluetoothDevice().getName());
			lvBluetooth_file_location.setText(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ecgraw/");
		}
		else{
			lvBluetooth_selected_device.setText("");
			lvBluetooth_file_location.setText("");
		}
		
		super.notifyDataSetChanged();
		
	}
}