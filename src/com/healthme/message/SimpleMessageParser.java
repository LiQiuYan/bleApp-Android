package com.healthme.message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.healthme.app.common.HandlerEvent;

import android.os.Environment;
import android.util.Log;

public class SimpleMessageParser implements MessageParser {
	LinkedList<Byte> buffer;

	public final static String TAG = SimpleMessageParser.class.getSimpleName();

	public final static int PACKAGE_SIZE = 2 + 4 + HandlerEvent.SAMPLE_RATE * 3 / 2;

	private final static String DataOutPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ecgRaw/";

	public SimpleMessageParser() {
		buffer = new LinkedList<Byte>();
		File dir = new File(DataOutPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	@Override
	public synchronized List<BLEMessage> parse(byte[] data) {
		if (data != null) {
			for (byte b : data) {
				buffer.add(b);
			}
			Log.d(TAG, "RECEIVED DATA:" + buffer.toString());
		}
		List<BLEMessage> messageList = new LinkedList<BLEMessage>();
		int size = buffer.size();
		if (size >= PACKAGE_SIZE) {
			if (buffer.get(0) == AD_DATA && (buffer.get(1)&-2) == 0
					&& (size == PACKAGE_SIZE || buffer.get(PACKAGE_SIZE) == AD_DATA)) {
				BLEMessage message = new BLEMessage();
				int pos = (buffer.get(2) & 0xFF) << 24;
				pos += (buffer.get(3) & 0xFF) << 16;
				pos += (buffer.get(4) & 0xFF) << 8;
				pos += (buffer.get(5) & 0xFF);
				message.setPos(pos);
				message.setType(AD_DATA);
				List<Number> nlist = new ArrayList<Number>(HandlerEvent.SAMPLE_RATE);
				int datapos = 6;
				for (int i = 0; i < HandlerEvent.SAMPLE_RATE; i += 2) {
					byte byte0 = buffer.get(datapos++);
					byte byte1 = buffer.get(datapos++);
					byte byte2 = buffer.get(datapos++);
					nlist.add((short) (((byte1 & 0xF) << 8) + (byte0 & 0xFF)) - 2048);
					nlist.add((short) (((byte1 & 0xF0) << 4) + (byte2 & 0xFF)) - 2048);
				}

				message.setLength((short) nlist.size());
				message.setData(nlist);
				messageList.add(message);
				saveData2Disk(pos, nlist, buffer.get(1).intValue()==1);
				for (int i = 0; i < PACKAGE_SIZE; i++) {
					buffer.removeFirst();
				}
			} else {
				while (size >= 2) {
					if (buffer.get(0) == AD_DATA) {
						if ((buffer.get(1)&-2) == 0) {
							break;
						} else {
							buffer.removeFirst();
							buffer.removeFirst();
							size -= 2;
						}
					} else {
						buffer.removeFirst();
						size--;
					}
				}
				return parse(null);
			}
		}

		return messageList;
	}

	private String outFileName = null;

	private void saveData2Disk(int dataPos, byte[] buf, boolean vnt) {
		try {
			if (outFileName == null && dataPos != 0) {
				// 查询最近的文件
				File[] olds = new File(DataOutPath).listFiles(new FilenameFilter() {
					public boolean accept(File f, String name) {
						return name.matches("[0-9]{14}.dat");
					}
				});
				File last = null;
				for (File f : olds) {
					if (last == null || f.lastModified() > last.lastModified()) {
						last = f;
					}
				}
				if (last != null && dataPos >= last.length() * 2 / 3) {
//					long dataseconds = (dataPos - last.length() * 2 / 3) / HandlerEvent.SAMPLE_RATE;
//					long dateseconds = (System.currentTimeMillis() - last.lastModified()) / 1000;
//					if (Math.abs(dataseconds - dateseconds) < 30) {
//						outFileName = last.getAbsolutePath();
//						Log.i(TAG, "append to file: " + outFileName);
//					}
					Log.i(TAG, "append to file: " + outFileName);
					outFileName = last.getAbsolutePath();
				}
			}
			if (outFileName == null || dataPos == 0) {
				outFileName = DataOutPath + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".dat";
				File f = new File(outFileName);
				if (!f.exists()) {
					f.createNewFile();
					Log.i(TAG, "create file: " + outFileName);
				}
			}
			RandomAccessFile randomAccessFile = new RandomAccessFile(outFileName, "rw");
			randomAccessFile.seek(dataPos * 3 / 2);
			randomAccessFile.write(buf);
			Log.i(TAG, "write :pos " + dataPos + ";size " + buf.length);
			randomAccessFile.close();
			if(vnt){
				String vntFile=outFileName.substring(0,outFileName.length()-4)+".vnt";
				FileOutputStream fos=new FileOutputStream(vntFile,true);
				fos.write(new byte[]{(byte)(0x10|(dataPos>>24)),(byte)((dataPos>>16)&0xFF),(byte)((dataPos>>8)&0xFF),(byte)(dataPos&0xFF)});
				fos.close();
			}
		} catch (Exception e) {
			Log.e(TAG, "saveData2Disk error: " + e);
		}
	}

	private void saveData2Disk(int dataPos, List<Number> data, boolean vnt) {
		int num = data.size();
		byte[] buf = new byte[num * 3 / 2];
		int index = 0;
		for (int i = 0; i < num; i += 2) {
			int d0 = data.get(i).shortValue();
			int d1 = data.get(i + 1).shortValue();
			buf[index++] = (byte) (d0 & 0xFF);
			buf[index++] = (byte) (((d0 >> 8) & 0xF) | ((d1 >> 4) & 0xF0));
			buf[index++] = (byte) (d1 & 0xFF);
		}
		saveData2Disk(dataPos, buf, vnt);
	}
}
