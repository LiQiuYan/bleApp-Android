package com.healthme.app.bean;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.healthme.app.AppException;


public class PVCItem extends Entity{

	public Date createTime;
	public Date endTime;
	public List<Short> samples;
	public long fromPos;
	public long toPos;
	public static PVCItem parse(String body)
			throws IOException, AppException {
		Gson gson = new GsonBuilder()
		.registerTypeAdapter(java.util.Date.class,new UtilDateDeserializer())
		.setDateFormat(DateFormat.LONG).create();
		PVCItem record = gson.fromJson(body, PVCItem.class);
		return record;
	}
	public boolean inter(long pos){
		if (pos >=fromPos && pos <toPos)
			return true;
		else
			return false;
	}
}
