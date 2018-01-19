package com.healthme.common.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import com.healthme.common.util.ByteUtil;

/**-----------------------------------------------
 * |  1 |        2        |         3        |     4       ..........
 * -----------------------------------------------
 * 1: message type, 1 byte;
 * 2: seq No. 4 bytes
 * 3: message length 4 byte(int);
 * 4: message body(see the detail message)
 * 
 * 
 * @author sya
 *
 */
@SuppressLint("NewApi")
public  class Message {
	public final static byte REG_REQUEST=0x1;
	public final static byte ECG_DATA_REQUEST=0x2;
	public final static byte LOC_REQUEST=0x3;
	public final static byte ECG_END_REQUEST=0x4;
	
	public final static byte REG_RESPONSE=0x41;
	public final static byte REG_INVALID_USER=0x43;
	public final static byte LOC_RESPONSE=0x44;
	public final static byte ECG_DATA_RESPONSE=0x42;
	public final static byte ECG_END_RESPONSE=0x45;
	
	
	
	
	public static byte DEV_ECG_1_200=0x1;
	
	public static String END_FLAG="\r\n";//0x0D;0x0A
	public byte messageType;
	public Integer length;
	public Integer seq=UUID.randomUUID().hashCode();

	Logger log=Logger.getLogger(getClass());
	private byte[] body;
	public byte getMessageType() {
		return messageType;
	}

	public void setMessageType(byte messageType) {
		this.messageType = messageType;
	}

	
	/**
	 * sub class should override this method and set the body here.
	 */
	public void setBody(byte[] body){
		this.body=body;
	}
	
	public byte[] getBody(){
		return this.body;
	}
	
	protected Message parseBody(){return null;}
	public void buildBody() {};
	
	
	public Message parse() throws HandleException{
		if(messageType==REG_REQUEST){
			RegistrationRequest msg=new RegistrationRequest();
			return msg.parse(this);
		}else if(messageType==ECG_DATA_REQUEST){
			ECGDataRequest msg=new ECGDataRequest();
			return msg.parse(this);
		}else if(messageType==REG_RESPONSE){
			RegistrationResponse msg=new RegistrationResponse();
			return msg.parse(this);
		}else if(messageType==ECG_DATA_RESPONSE) {
			ECGDataResponse msg=new ECGDataResponse();
			return msg.parse(this);
		}
		return null;
	}
	public byte[] getBytes(){
		buildBody();
		byte[] body=getBody();
		length=body.length+1+4+4+2; //append messagetype, seq, length and end flag size;
		ByteArrayOutputStream bos=new ByteArrayOutputStream(length);
		
		try {
			//set message type
			bos.write(new byte[]{messageType});
			
			//set the message seq
			if(seq==null)
				seq=UUID.randomUUID().hashCode();
			bos.write(ByteUtil.intToByteArray(seq));
			//set message length and body
			bos.write(ByteUtil.intToByteArray(length));
			bos.write(body);
			
			//set end flag
			bos.write(END_FLAG.getBytes());
			if(log.isDebugEnabled())
				log.debug("message is:"+toString());
		} catch (IOException e) {
			log.error("failed to make up the mssage");
		}
		
		
		return bos.toByteArray();
	}
	
	public void parseRawData(byte[] data){
		this.seq=ByteUtil.byteArrayToInt(new byte[]{data[1],data[2],data[3],data[4]});
		this.length=ByteUtil.byteArrayToInt(new byte[]{data[5],data[6],data[7],data[8]});
		this.body=Arrays.copyOfRange(data, 9, data.length-2);
		if(log.isDebugEnabled())
			log.debug(toString());
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	@Override
	public String toString() {
		return "Message [messageType=" + messageType + ", length=" + length
				+ ", seq=" + seq + ", body="
				+ new String(body) + "]";
	}
	
	
}
