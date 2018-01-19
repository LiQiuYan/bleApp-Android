package com.healthme.common.nio;


public interface HandlerCallback {
	public static final byte SUCCESS=0x10;
	public static final byte ERR_ACC=0x20;
	public static final byte INVALIDE_USER=0x20;
	void callback(Message msg);
}
