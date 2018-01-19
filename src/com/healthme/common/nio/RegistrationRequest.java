package com.healthme.common.nio;

import java.util.Arrays;


/**
 * BODY format
 * 
 * -----------------------------------------------
 * |        1        |                2       ..........
 * -----------------------------------------------
 * 1: device type, 1 byte;
 * 2: user, password and file information, split by #, for example: user1#password2#file3
 * 
 * @author sya
 *
 */
public class RegistrationRequest extends Message{

	private String userName;
	private String password;
	private String fileName;
	private byte deviceType;
	
	public RegistrationRequest() {
		messageType=Message.REG_REQUEST;
	}
	
	public RegistrationRequest parse(Message msg) throws HandleException{
		 setMessageType(msg.getMessageType());
		 setDeviceType(msg.getBody()[0]);
		 String pair=new String(Arrays.copyOfRange(msg.getBody(),1,msg.getBody().length));
		 String[] tokens = pair.split("#");
		 if(tokens.length!=3){
			 throw new HandleException(HandleException.ERROR_INVALIDE_MESSAGE);
		 }
		 setUserName(tokens[0]);
		 setPassword(tokens[1]);
		 setFileName(tokens[2]);
		 return this;
	}

	public void buildBody() {
		byte[] pair=(userName+"#"+password+"#"+fileName).getBytes();
		byte[] body=new byte[pair.length+1];
		body[0]=deviceType;
		for (int i = 1; i < body.length; i++) {
			body[i]=pair[i-1];
		}
		super.setBody(body);
	}
	
	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public byte getDeviceType() {
		return deviceType;
	}


	public void setDeviceType(byte deviceType) {
		this.deviceType = deviceType;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return "RegistrationRequest [userName=" + userName + ", password="
				+ password + ", deviceType=" + deviceType + ", messageType="
				+ messageType + ", length=" + length + ", seq=" + seq + "]";
	}
	
}
