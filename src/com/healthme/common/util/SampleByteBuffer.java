package com.healthme.common.util;



import java.io.ByteArrayOutputStream;
import java.io.IOException;



public class SampleByteBuffer extends ByteArrayOutputStream{



//	private ByteArrayOutputStream output;

	boolean isFull=true;

	byte[] buffer = new byte[3];

	public SampleByteBuffer(){
//		this.output = new ByteArrayOutputStream();
	}


	/**

	 * 以12bits写入一个short

	 * 

	 * @param data

	 * @throws IOException

	 */

	public void addShort(short data){

		if (isFull) {
			buffer[0] = (byte) ((data & 0xFF));
			buffer[1] = (byte) ((data>>8 & 0xF));
			isFull=false;
		} else {
			buffer[2] = (byte) ((data & 0xFF));
			byte temp = (byte) ( buffer[1] | ((data & 0x0F00) >> 4));
			buffer[1] = temp;
			isFull=true;
		}
		if (isFull){
			try {
				write(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public byte[] retrieveBytes(){
		byte[] data = toByteArray(); 
		reset();
		return data;
	}


	/**

	 * it's bad performance so far.

	 * @param shorts

	 * @throws Exception

	 */

	public void addShorts(Short[] shorts) throws Exception {
		for (short one : shorts) {
			addShort(one);
		}
		flush();
	}



}