package com.healthme.app.bean;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;

public class ResponseBase {
	private int code=HttpStatus.SC_NOT_FOUND;
	private String body;
	private Map<String, String> headers;
	private String cookie;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public void addHeader(String name, String value) {
		if (headers == null)
			headers = new HashMap<String, String>();
		headers.put(name, value);
	}

	public String getHeader(String name) {
		if (headers != null)
			return headers.get(name);
		return null;
	}

	@Override
	public String toString() {
		return "ResponseBase [code=" + code + ", body=" + body + ", headers="
				+ headers + ", cookie=" + cookie + "]";
	}
	
	

}
