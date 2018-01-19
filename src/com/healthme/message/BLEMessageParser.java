package com.healthme.message;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.healthme.app.common.StringUtils;
import com.healthme.common.util.ByteUtil;

public class BLEMessageParser implements MessageParser {
	LinkedList<Byte> buffer;
	List<BLEMessage> messages;
	
	public final static String TAG=BLEMessageParser.class.getSimpleName();

	public BLEMessageParser() {
		buffer = new LinkedList<Byte>();
		messages = new LinkedList<BLEMessage>();
	}

	@Override
	public synchronized List<BLEMessage> parse(byte[] data) {
		for(byte b:data){
			buffer.add(b);
		}
        Log.d(TAG,"RECEIVED DATA:"+StringUtils.byteArray2String(data));
		List<BLEMessage> messageList = new LinkedList<BLEMessage>();
		while (buffer.size()>0) {
			BLEMessage message = null;
            byte type = (Byte) buffer.getFirst();
//			byte type = (byte) (first & 0xf0);
			switch (type) {
			case AD_DATA: // AD data
				message = parseADMessage(buffer);
				break;
			case AXIS_DATA: // 3 Aixs
			case LEAD_LOST_WARN:// lead lost
			case LOW_BAT_WARN:// low battery
			case SN_QUERY:// S/N response
				message = parseSinglePosValueMessage(buffer);
				break;
			case START_BLE_TRANSFER:
			case STOP_BLE_TRANSFER:
			case START_FLASH_WRITE:
			case STOP_FLASH_WRITE:
				message = parseSingleByteValueMessage(buffer);
				break;
			default:
				Log.w(TAG, "find unknown message ");
				List<Byte> badData = new LinkedList<Byte>();
				for (Iterator iterator = buffer.iterator(); iterator.hasNext();) {
					Byte byte1 = (Byte) iterator.next();
					badData.add(byte1);
                    iterator.remove();
                    if (byte1 == END_FLAG){
                        Log.w(TAG, "find end flag in bad message ");
                        break;
					}
				}
			}
			if (message == null) {
				break;
			} else {
				message.setType(type);
				messageList.add(message);
			}
		}

		return messageList;
	}

	private BLEMessage parseSingleByteValueMessage(LinkedList<Byte> dataBuffer) {
		byte value = dataBuffer.removeFirst();
		BLEMessage message = new BLEMessage();
		message.setType(value);
		//remove end flag;
		dataBuffer.removeFirst();
		return message;
	}

	private BLEMessage parseSinglePosValueMessage(LinkedList<Byte> dataBuffer) {
		if (dataBuffer.size() >=8) {
			BLEMessage message = new BLEMessage();
			message.setType(dataBuffer.removeFirst());
			short length = ByteUtil.byteArrayToShort(new byte[]{dataBuffer.removeFirst(),dataBuffer.removeFirst()});
			message.setLength(length);
			byte[] bytes = new byte[] { dataBuffer.removeFirst(),
					dataBuffer.removeFirst(), dataBuffer.removeFirst(),
					dataBuffer.removeFirst()};
			int pos = ByteUtil.byteArrayToInt(bytes);
			List<Number> data = new LinkedList<Number>();
			data.add(pos);
			message.setData(data);
			if (dataBuffer.removeFirst() == END_FLAG)
				return message;
			else
				Log.w(TAG, "can't find the end flag");
		}
		return null;
	}

	private BLEMessage parseADMessage(LinkedList<Byte> dataBuffer) {
		// if the data length is not enough and have no the first sample
		// then parse it later.
		if(dataBuffer.size()<3)return null;
		short length = ByteUtil.byteArrayToShort(new byte[]{dataBuffer.get(1),dataBuffer.get(2)});
		// check is the buffer has complete message
		int total = length + 7;//7 is the length of length and byte.
		if (dataBuffer.size() >= total) {
            Log.d(TAG,"AD DATA:"+dataBuffer.toString());
			short headSample = 0;
			BLEMessage message = new BLEMessage();
			message.setType(AD_DATA);
			message.setLength(length);
			dataBuffer.removeFirst(); // remove the first 3 bytes,including type and length
			dataBuffer.removeFirst();
			dataBuffer.removeFirst();
			//parse the pos value.
			byte[] posArray = new byte[]{dataBuffer.removeFirst(),dataBuffer.removeFirst(),dataBuffer.removeFirst(),dataBuffer.removeFirst()};
			message.setPos(ByteUtil.byteArrayToInt(posArray));
			
			List<Number> sampleData = new LinkedList<Number>();
			byte byteH=dataBuffer.removeFirst(),byteL=dataBuffer.removeFirst();
			headSample = (short)((byteL & 0xFF) | (((byteH&0x0f)<<28)>>20));
//			headSample = ByteUtil.byteArrayToShort(new byte[] {
//					dataBuffer.removeFirst(), dataBuffer.removeFirst() });
			message.setData(sampleData);
			sampleData.add(headSample);
			Log.d(TAG,"headSample:"+headSample);
			int sampleNum = length - 2;// sample num is data length add
												// 9 bytes(  pos 4 bytes, first sample 1 plus) and
												// end flag(0xFF)
			int remains = sampleNum - 1;
			for (int i = 0; i < remains; i++) {
				byte diff = dataBuffer.removeFirst();
				headSample = (short) (headSample + diff);
				sampleData.add(headSample);
			}
			// check the end flag
			if (dataBuffer.removeFirst().byteValue() == END_FLAG) {
				return message;
			} else
				Log.w(TAG, "can't find the end flag");
		}
		return null;
	}
}
