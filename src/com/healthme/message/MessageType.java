package com.healthme.message;

public interface MessageType {
	public static final byte AD_DATA=0x00;
	public static final byte AXIS_DATA=0x01;
	public static final byte LEAD_LOST_WARN=0x02;
	public static final byte LOW_BAT_WARN=0x03;
	public static final byte START_BLE_TRANSFER=0x08;
	public static final byte STOP_BLE_TRANSFER=0x09;
	public static final byte START_FLASH_WRITE=0x0A;
	public static final byte STOP_FLASH_WRITE=0x0B;
	public static final byte SN_QUERY =0x0C;
	public static final byte END_FLAG=0x7F;
	
	public static final byte BAT_REPLY = 0x0D;
}
