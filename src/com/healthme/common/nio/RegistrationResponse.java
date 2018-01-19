package com.healthme.common.nio;

import android.annotation.SuppressLint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;


/**
 * 
 * The body is
 * 
 * ------------------------
 *   1 |       2
 * ------------------------
 * 
 * 1. response code
 * 2. TBD
 * @author sya
 *
 */
@SuppressLint("NewApi")
public class RegistrationResponse extends Message{

	public static final byte SUCCESS=0x10;
	public static final byte ERR_ACC=0x20;
	public static final byte INVALIDE_USER=0x30;
	public static final byte ERR_SERVER_INTTERNAL=0x40;
	public String userName;
	public String password;
	public long recordId;
	public byte code;
	Logger log=Logger.getLogger(getClass());
	public RegistrationResponse() {
		messageType=Message.REG_RESPONSE;
	}
	
	
	public RegistrationResponse parse(Message msg) throws HandleException{
		 setMessageType(msg.getMessageType());
		 code=msg.getBody()[0];
		 String pair=new String(Arrays.copyOfRange(msg.getBody(), 1, msg.getBody().length));
		 String[] tokens = pair.split("#");
		 if(tokens.length!=3){
			 throw new HandleException(HandleException.ERROR_INVALIDE_MESSAGE);
		 }
		 setUserName(tokens[0]);
		 setPassword(tokens[1]);
		 setRecordId(new Long(tokens[2]));
		 return this;
	}
	
	
	public void buildBody(){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		try {
			bos.write(new byte[]{code});
			bos.write((userName+"#"+password+"#"+recordId).getBytes());
			setBody(bos.toByteArray());
		} catch (IOException e) {
			log.error("faild to build body:",e);
		}
	}

	public byte getCode() {
		return code;
	}

	public void setCode(byte code) {
		this.code = code;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public long getRecordId() {
		return recordId;
	}

	public void setRecordId(long recordId) {
		this.recordId = recordId;
	}

	@Override
	public String toString() {
		return "RegistrationResponse [userName=" + userName + ", password="
				+ password + ", recordId=" + recordId + ", code=" + code + "]";
	}
}
