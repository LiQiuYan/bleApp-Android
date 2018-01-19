package com.healthme.app.bean;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.healthme.app.AppException;

/**
 * 实体类
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public abstract class Entity extends Base {

	protected int id;

	public int getId() {
		return id;
	}
	
	public void setId(int id){
		this.id=id;
	}

	protected String cacheKey;

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}
	
	public static <T> T parse(String json, Class<T> t) {
		Gson gson = new GsonBuilder().registerTypeAdapter(java.util.Date.class,
				new UtilDateDeserializer()).create();
		T list = gson.fromJson(json, t);
		return list;
	}
	
	public static <T> List<T>  parseList(String json, Class<T> t) {
		Gson gson = new GsonBuilder().registerTypeAdapter(java.util.Date.class,
				new UtilDateDeserializer()).create();
		List<T> list = gson.fromJson(json,  new TypeToken<List<T>>() {}.getType());
		return list;
	}
}
