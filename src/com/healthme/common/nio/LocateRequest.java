package com.healthme.common.nio;

import android.annotation.SuppressLint;
import java.util.Arrays;


/**
 * BODY format
 * 
 * -----------------------------------------------
 * |        1             ..........
 * -----------------------------------------------
 * 1: longitude and latitude information, split by #, for example: 116.364#39.927
 * 
 * @author sya
 *
 */
@SuppressLint("NewApi")
public class LocateRequest extends Message{

	private String longitude;
	private String latitude;
	public LocateRequest() {
		messageType=Message.LOC_REQUEST;
	}
	
	public LocateRequest parse(Message msg) throws HandleException{
		 setMessageType(msg.getMessageType());
		 String pair=new String(Arrays.copyOfRange(msg.getBody(),0,msg.getBody().length));
		 String[] tokens = pair.split("#");
		 if(tokens.length!=2){
			 throw new HandleException(HandleException.ERROR_INVALIDE_MESSAGE);
		 }
		 setLongitude(tokens[0]);
		 setLatitude(tokens[1]);
		 return this;
	}

	public void buildBody() {
		byte[] pair=(longitude+"#"+latitude).getBytes();
		byte[] body=new byte[pair.length];
		super.setBody(body);
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
}
