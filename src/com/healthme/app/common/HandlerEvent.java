package com.healthme.app.common;

public interface HandlerEvent {
	
	public static final int REFRESH_RATE = 200;//ms 
	public static final int SAMPLE_RATE = 128;
	public static final int HEARTBEAT_NUM = 5;
	public static final int DELAY_TIME = 5; //millisecond
	public static final int SAMPLE_SIZE = SAMPLE_RATE*DELAY_TIME;
	public static final int DISPLAY_CAPACITY = SAMPLE_SIZE/2;
    public static final int HANDLER_MSG_DEV=1;
    public static final int HANDLER_MSG_PLOT=2;
    public static final int HANDLER_MSG_CLIENT=3;
    public static final int MSG_SEND_ECG_DATA=4;
    public static final int HANDLER_UPDATE_UI=5;
    
    public static final int DEV_CONNECT_OK=1;
	public static final int DEV_CONNECT_FAILED=2;
	public static final int DEV_STOP_OK=3;
	public static final int DEV_SIGNAL=4;
	public static final int DEV_ON_CONNECT=5;
	
	
	public static final int CLIENT_SEND_DATA=1;
	public static final int CLIENT_DATA_RESPONSE=2;
}
