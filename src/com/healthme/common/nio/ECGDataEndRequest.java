package com.healthme.common.nio;


/**
 * BODY format
 * 
 * no body content
 * 
 * @author sya
 * 
 */
public class ECGDataEndRequest extends Message{

	public ECGDataEndRequest() {
		this.messageType=Message.ECG_END_REQUEST;
	}
	@Override
	public void buildBody() {
		setBody(new byte[]{});
	}
	
	public ECGDataEndRequest parse(Message msg) throws HandleException {
		setMessageType(msg.getMessageType());
		return this;
	}
}
