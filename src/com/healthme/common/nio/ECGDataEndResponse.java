package com.healthme.common.nio;


public class ECGDataEndResponse extends ECGDataEndRequest{

	public ECGDataEndResponse() {
		this.messageType=Message.ECG_END_RESPONSE;
	}
}
