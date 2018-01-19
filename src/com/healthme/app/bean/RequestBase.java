package com.healthme.app.bean;

import java.util.Map;

public class RequestBase implements MessageParams{
	private String url;
	private String method="GET";	
	private Map<String, String> headers;
	private Map<String,Object> params;
	private int times;
	private byte[] body;
	public RequestBase(){
		
	}
	
	public RequestBase(String url){
		this(url,null);
	}
	
	public RequestBase(String url,Map<String,Object> params){
		this(url,"GET",params);
	}
	
	public RequestBase(String url,String method,Map<String,Object> params){
		this.url=url;
		this.method=method;
		this.params=params;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	
	public byte[] getBody() {
		return body;
	}
	public void setBody(byte[] body) {
		this.body = body;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	@Override
	public String toString() {
		return "RequestBase [url=" + url + ", method=" + method + ", headers="
				+ headers + ", params=" + params + "]";
	}

}
