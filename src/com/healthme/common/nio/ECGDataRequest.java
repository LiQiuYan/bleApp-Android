package com.healthme.common.nio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import com.healthme.common.util.ByteUtil;

/**
 * BODY format
 * 
 * ----------------------------------------------- 
 * | 1 | 2 ..........
 * ----------------------------------------------- 
 * 1: start time, 8 bytes; -1
 * means finish transferring. 
 * 2: each point occupied 4 bytes
 * 
 * @author sya
 * 
 */
@SuppressLint("NewApi")
public class ECGDataRequest extends Message {
	private List<Short> ecgData = null;
	private long startTime;
	private Logger log = Logger.getLogger(getClass());

	public ECGDataRequest() {
		messageType=Message.ECG_DATA_REQUEST;
	}
	
	@Override
	public void buildBody() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			bos.write(ByteUtil.longToByteArray(startTime));
			for (Short s : ecgData) {
				bos.write(ByteUtil.shortToByteArray(s));
			}
		} catch (IOException e) {
			log.error(e);
		}
		
		super.setBody(bos.toByteArray());
	}

	public ECGDataRequest parse(Message msg) throws HandleException {
		setMessageType(msg.getMessageType());
		ByteArrayInputStream bis = new ByteArrayInputStream(msg.getBody());
		startTime=ByteUtil.byteArrayToLong(Arrays.copyOfRange(msg.getBody(),0,8));
		byte[] data = Arrays.copyOfRange(msg.getBody(),8,msg.getBody().length);
		try {
			if (ecgData == null)
				ecgData = new ArrayList<Short>();
			for (int i = 0; i <data.length / 2; i++) {
				ecgData.add(ByteUtil.byteArrayToShort(new byte[]{data[i*2],data[i*2+1]}));
			}
		} catch (Exception e1) {
			log.error(e1);
		}
		return this;
	}

	public List<Short> getEcgData() {
		return ecgData;
	}

	public void setEcgData(List<Short> ecgData) {
		this.ecgData = ecgData;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	@Override
	public String toString() {
		return "ECGDataMessage [ecgData=" + ecgData + ", startTime="
				+ startTime + ", messageType=" + messageType + ", length="
				+ length + ", seq=" + seq + "]";
	}

}
