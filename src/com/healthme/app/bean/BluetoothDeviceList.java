package com.healthme.app.bean;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.healthme.app.AppContext;
import com.healthme.app.AppException;

import com.healthme.app.R;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 帖子列表实体类
 * 
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class BluetoothDeviceList extends Entity {

	private static final String TAG = "DeviceListActivity";
	private static final boolean D = false;

	private static final int REQUEST_ENABLE_BT = 2;
	protected static final String PREFS_NAME = "Antonio081014 Bluetooth Android";
	protected static final String PREFS_DEVICE_ADDR = "Antonio081014 Bluetooth Address";


	public final static int CATALOG_ASK = 1;
	public final static int CATALOG_SHARE = 2;
	public final static int CATALOG_OTHER = 3;
	public final static int CATALOG_JOB = 4;
	public final static int CATALOG_SITE = 5;

	private int pageSize;
	private int postCount;
    private int currentPosition=0;	
	private List<CustomizedBluetoothDevice> mBluetoothDevicelist = new ArrayList<CustomizedBluetoothDevice>();
		

	public int getPageSize() {
		return pageSize;
	}

	public int getPostCount() {
		return postCount;
	}

	public List<CustomizedBluetoothDevice> getBluetoothDevicelist() {
		return mBluetoothDevicelist;
	}

	public static BluetoothDeviceList parse(AppContext appContext)
			throws IOException, AppException {
		BluetoothDeviceList bluetoothlist = new BluetoothDeviceList();;

		// 获得XmlPullParser解析器
		// XmlPullParser xmlParser = Xml.newPullParser();
		// try {
		// xmlParser.setInput(inputStream, UTF8);
		// //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
		// int evtType=xmlParser.getEventType();
		// //一直循环，直到文档结束
		// while(evtType!=XmlPullParser.END_DOCUMENT){}
		// } catch (XmlPullParserException e) {
		// throw AppException.xml(e);
		// } finally {
		// inputStream.close();
		// }
	    final BluetoothAdapter mBluetoothAdapter;			
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			return bluetoothlist;
	
		}


	
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() > 0) {

			bluetoothlist=new BluetoothDeviceList();
			for (BluetoothDevice device : pairedDevices) {
				CustomizedBluetoothDevice customizedDevice = new CustomizedBluetoothDevice(
						device);
				bluetoothlist.getBluetoothDevicelist().add(customizedDevice);
			}



		}
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBluetoothAdapter.startDiscovery();		
		try {
			appContext.wait(2000);
		} catch (Exception e) {
		}

//				
		return bluetoothlist;
	}






}
