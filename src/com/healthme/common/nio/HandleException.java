package com.healthme.common.nio;

public class HandleException extends Exception{
	
	public int errorCode;
	
	public final static int ERROR_INVALIDE_MESSAGE=1;
	public final static int ERROR_LOGIN_MESSAGE=2;
	public final static int ERROR_UNKNOWN_DEVICE=3;
	public HandleException() {
	}
	
	public HandleException(int errorCode) {
		this.errorCode=errorCode;
	}

}
