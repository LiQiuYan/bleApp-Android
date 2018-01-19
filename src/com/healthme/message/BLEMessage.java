package com.healthme.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.util.Log;

import com.healthme.common.util.ByteUtil;

public class BLEMessage implements MessageType{
	
	public byte type;
	public short length;
	public int pos;
	public List<Number> data;

	public static final byte APP = 0x1;
	public static final byte DEV = 0x2;

	public BLEMessage() {
	}

	public static BLEMessage newEmptyMessage(byte type){
		BLEMessage ble=new BLEMessage();
		ble.setType(type);
		return ble;
	}
	
	public static BLEMessage newPosMessage(byte type,int pos){
		BLEMessage ble=new BLEMessage();
		ble.setType(type);
		ble.setLength((short) 8);
		ble.setPos(pos);
		return ble;
	}
	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public short getLength() {
		return length;
	}

	public void setLength(short length) {
		this.length = length;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public List<Number> getData() {
		return data;
	}

	public void setData(List<Number> data) {
		this.data = data;
	}

	public byte[] toBytes(){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		try {
			bos.write(type);
			if(length!=0)
				bos.write(ByteUtil.shortToByteArray(length));
			if(pos!=0)
				bos.write(ByteUtil.intToByteArray(pos));
			if(data!=null||data.size()>0)
				bos.write(getDataBytes());
			bos.write(END_FLAG);
            return bos.toByteArray();
        } catch (IOException e) {
            Log.i("INFO", e.getMessage());
        }finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public byte[] toOneByte(){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        try {
            bos.write(type);
            return bos.toByteArray();
        } catch (Exception e) {
            Log.i("INFO",e.getMessage());
        }finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

	public byte[] getDataBytes() {
		return new byte[0];
	}
}
