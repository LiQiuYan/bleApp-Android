package com.healthme.app.bean;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class UtilDateDeserializer implements JsonDeserializer<java.util.Date> {
	
	public static DateFormat ymd= new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

	public static DateFormat ymdhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

	@Override
	public java.util.Date deserialize(JsonElement json, Type typeOfT,

	JsonDeserializationContext context)

	throws JsonParseException {
		try {
			if (json != null) {
				String str=json.getAsJsonPrimitive().getAsString();		
				if(StringUtils.isNumeric(str)){
					return new java.util.Date(Long.parseLong(str));
				}
				return (str.length()==10?ymd:ymdhms).parse(str);
			}
		} catch (Exception e) {
			Log.i("ERROR", e.getMessage());
		}
		return null;
		// return new java.util.Date(json.getAsJsonPrimitive().getAsLong());
	}

}