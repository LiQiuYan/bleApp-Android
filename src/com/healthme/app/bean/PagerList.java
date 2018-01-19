package com.healthme.app.bean;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class PagerList<T> extends Entity {
    /** current page index, starts at 0 */
    private int index;

    /** number of results per page (number of rows per page to be displayed ) */
    private int pageSize;

    /** total number of results (the total number of rows ) */
    private int fullListSize;

    /** list of results (rows found ) in the current page */
    protected List<T> list=new ArrayList<T>();

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getFullListSize() {
		return fullListSize;
	}

	public void setFullListSize(int fullListSize) {
		this.fullListSize = fullListSize;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}
    
	public static <T> PagerList<T> parsePager(String json, Class<T> t) {
		Gson gson = new GsonBuilder().registerTypeAdapter(java.util.Date.class,
				new UtilDateDeserializer()).create();
		PagerList<T> list1 = gson.fromJson(json,  new TypeToken<PagerList<T>>() {}.getType());
		return list1;
	}
}
