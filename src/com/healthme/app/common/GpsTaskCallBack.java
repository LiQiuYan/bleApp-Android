package com.healthme.app.common;

import com.healthme.app.common.GpsTask.GpsData;

public interface GpsTaskCallBack {

	public void gpsConnected(GpsData gpsdata);
	
	public void gpsConnectedTimeOut();
	
}
