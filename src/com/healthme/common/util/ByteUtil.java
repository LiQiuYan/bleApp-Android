package com.healthme.common.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;


public class ByteUtil {
	public static byte[] intToByteArray(int i) {
		byte[] result = new byte[4];
		result[0] = (byte) ((i >> 24) & 0xFF);
		result[1] = (byte) ((i >> 16) & 0xFF);
		result[2] = (byte) ((i >> 8) & 0xFF);
		result[3] = (byte) (i & 0xFF);
		return result;

	}
	
	
	public static byte[] longToByteArray(long i) {
		byte[] result = new byte[8];
		result[0] = (byte) ((i >> 56) & 0xFF);
		result[1] = (byte) ((i >> 48) & 0xFF);
		result[2] = (byte) ((i >> 40) & 0xFF);
		result[3] = (byte) ((i >> 32) & 0xFF);
		result[4] = (byte) ((i >> 24) & 0xFF);
		result[5] = (byte) ((i >> 16) & 0xFF);
		result[6] = (byte) ((i >> 8) & 0xFF);
		result[7] = (byte) (i & 0xFF);
		return result;

	}
	
	
	
	public static byte[] shortToByteArray(short i) {
		byte[] result = new byte[2];
		result[0] = (byte) ((i >> 8) & 0xFF);
		result[1] = (byte) (i & 0xFF);
		return result;

	}

	public static int byteArrayToInt(byte[] bytes) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (bytes[i] & 0x000000FF) << shift;
		}
		return value;
	}
	
	public static short byteArrayToShort(byte[] bytes) {
		short value = 0;
		for (int i = 0; i < 2; i++) {
			int shift = (2 - 1 - i) * 8;
			value += (bytes[i] & 0x000000FF) << shift;
		}
		return value;
	}
	
	
	public static int byteArrayToLong(byte[] bytes) {
		int value = 0;
		for (int i = 0; i < 8; i++) {
			int shift = (8 - 1 - i) * 8;
			value += (bytes[i] & 0x000000FF) << shift;
		}
		return value;
	}
	
	/**
	 * 字节转十六进制 为相应的字符串显示
	 * 
	 * @param data
	 * @return
	 */
	public static String byte2Hex(byte data[]) {
		if (data != null && data.length > 0) {
			StringBuilder sb = new StringBuilder(data.length);
			for (byte tmp : data) {
				sb.append(String.format("%02X ", tmp));
			}
			return sb.toString();
		}
		return "no data";
	}

	/**
	 * 将byte转换为一个长度为8的byte数组，数组每个值代表bit
	 */
	public static byte[] getBooleanArray(byte b) {
		byte[] array = new byte[8];
		for (int i = 7; i >= 0; i--) {
			array[i] = (byte) (b & 1);
			b = (byte) (b >> 1);
		}
		return array;
	}

	/**
	 * 
	 * 把byte转为bit
	 * 
	 * @param array
	 * @return
	 */
	public static byte getByteFromBooleanArray(byte[] array) {
		byte bit = 0;
		for (int i = 0; i < array.length; i++) {
			array[i] = (byte) (array[i] & 0x1);
			bit += (array[i] << (array.length - i - 1));
		}
		// bit = (byte) (bit + 127);
		return (byte) bit;
	}

	/**
	 * 把byte转为字符串的bit
	 */
	public static String byteToBit(byte b) {
		return "" + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1) + " "
				+ (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1) + " "
				+ (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1) + " "
				+ (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
	}

	/**
	 * 把byte转为字符串的bit
	 * 
	 * @param b
	 * @param length
	 * @return
	 */
	public static String byteToBit(byte b, int length) {

		String bitString = null;
		for (int i = (length - 1); i >= 0; i--) {
			bitString += (byte) (b >> i) & 0x1;
			b = (byte) (b >> 1);
		}
		return bitString;
	}

	/**
	 * 获取 imei唯一识别码
	 * 
	 * @param context
	 * @return
	 */
	public static String getIMEI(Context context) {

		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	/**
	 * 获取屏幕管理类
	 * 
	 * @return DisplayMetrics 屏幕管理对象
	 */
	public static DisplayMetrics getDisplayMetrics(Context context) {
		DisplayMetrics displayMetrics = null;
		if (displayMetrics == null) {
			displayMetrics = new DisplayMetrics();
		}
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		return displayMetrics;
	}

	/**
	 * 图片圆角
	 * 
	 * @param bitmap
	 * @param pixels
	 * @return
	 */
	public static Bitmap toRoundCorner(Bitmap bitmap, int color) {

		int jiange = 10;
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(jiange, jiange, bitmap.getWidth() - jiange,
				bitmap.getHeight() - jiange);
		final RectF rectF = new RectF(rect);
		final float roundPx = bitmap.getHeight() / 2;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap.extractAlpha(), rect, rect, paint);

		return output;
	}
	
}
