package com.healthme.common.nio;

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
 * 2. ecg record data fields, split by #(heartbeat, maxRRInterval, minRRInterval, maxRValue,minRValue)
 * @author sya
 *
 */
public class ECGDataResponse extends Message{

	public static final byte SUCCESS=0x10;
	public int maxRRInterval;
	public int minRRInterval;
	public int maxRValue;
	public int minRValue;
	public int curRRInterval;
	public int pvcNum;
	public byte code;
	Logger log=Logger.getLogger(getClass());
	public ECGDataResponse() {
		messageType=Message.ECG_DATA_RESPONSE;
	}
	
	
	public ECGDataResponse parse(Message msg) throws HandleException{
		 setMessageType(msg.getMessageType());
		 code=msg.getBody()[0];
		 String pair=new String(Arrays.copyOfRange(msg.getBody(), 1, msg.getBody().length));
		 String[] tokens = pair.split("#");
		 if(tokens.length!=6)
			 throw new IllegalArgumentException();
		 curRRInterval=new Integer(tokens[0]);
		 maxRRInterval=new Integer(tokens[1]);
		 minRRInterval=new Integer(tokens[2]);
		 maxRValue=new Integer(tokens[3]);
		 minRValue=new Integer(tokens[4]);
		 pvcNum=new Integer(tokens[5]);
		 return this;
	}
	
	
	public void buildBody(){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		try {
			bos.write(new byte[]{code});
			bos.write((curRRInterval+"#"+maxRRInterval+"#"+minRRInterval+"#"+maxRValue+"#"+minRValue+"#"+pvcNum).getBytes());
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
	public int getMaxRRInterval() {
		return maxRRInterval;
	}
	public void setMaxRRInterval(int maxRRInterval) {
		this.maxRRInterval = maxRRInterval;
	}
	public int getMinRRInterval() {
		return minRRInterval;
	}
	public void setMinRRInterval(int minRRInterval) {
		this.minRRInterval = minRRInterval;
	}
	public int getMaxRValue() {
		return maxRValue;
	}
	public void setMaxRValue(int maxRValue) {
		this.maxRValue = maxRValue;
	}
	public int getMinRValue() {
		return minRValue;
	}
	public void setMinRValue(int minRValue) {
		this.minRValue = minRValue;
	}
	public static byte getSuccess() {
		return SUCCESS;
	}
	public int getCurRRInterval() {
		return curRRInterval;
	}
	public void setCurRRInterval(int curRRInterval) {
		this.curRRInterval = curRRInterval;
	}
	public int getPvcNum() {
		return pvcNum;
	}
	public void setPvcNum(int pvcNum) {
		this.pvcNum = pvcNum;
	}

}
