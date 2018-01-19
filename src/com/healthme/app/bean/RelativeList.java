package com.healthme.app.bean;

import java.text.DateFormat;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RelativeList extends PagerList<Relative> {

	public RelativeList() {
		this.list = new ArrayList<Relative>();
	}

	public static RelativeList parse(String body) {
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(java.util.Date.class,
						new UtilDateDeserializer())
				.setDateFormat(DateFormat.LONG).create();
		RelativeList recordList = gson.fromJson(body, RelativeList.class);
		return recordList;
	}
}
