package com.healthme.app.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.healthme.app.AppContext;
import com.healthme.app.AppException;
import com.healthme.app.bean.BluetoothDeviceList;
import com.healthme.app.bean.ECGClassificationList;
import com.healthme.app.bean.EcgRecord;
import com.healthme.app.bean.EcgRecordList;
import com.healthme.app.bean.Entity;
import com.healthme.app.bean.HeartBeatData;
import com.healthme.app.bean.MessageParams;
import com.healthme.app.bean.Notice;
import com.healthme.app.bean.PagerList;
import com.healthme.app.bean.Patient;
import com.healthme.app.bean.Relative;
import com.healthme.app.bean.RelativeList;
import com.healthme.app.bean.RequestBase;
import com.healthme.app.bean.ResponseBase;
import com.healthme.app.bean.Result;
import com.healthme.app.bean.URLs;
import com.healthme.app.bean.Update;
import com.healthme.app.bean.User;
import com.healthme.app.bean.UtilDateDeserializer;
import com.healthme.common.nio.HandlerCallback;

/**
 * API客户端接口：用于访问网络数据
 * 
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class ApiClient {

	public static final String UTF_8 = "UTF-8";
	public static final String DESC = "descend";
	public static final String ASC = "ascend";

	private final static int TIMEOUT_CONNECTION = 2000;
	private final static int TIMEOUT_SOCKET = 20000;
	private final static int RETRY_TIME = 3;

	private static String appCookie;
	private static String appUserAgent;
	private static final Logger log = Logger.getLogger(ApiClient.class);

	public static void cleanCookie(AppContext appContext) {
		appCookie = "";
		appContext.setProperty("cookie", "");
	}

	public static String getCookie(AppContext appContext) {
		if (appCookie == null || appCookie == "") {
			appCookie = appContext.getProperty("cookie");
		}
		return appCookie;
	}

	private static String getUserAgent(AppContext appContext) {
		if (appUserAgent == null || appUserAgent == "") {
			StringBuilder ua = new StringBuilder("HealthMeApp");
			ua.append('/' + appContext.getPackageInfo().versionName + '_'
					+ appContext.getPackageInfo().versionCode);// App版本
			ua.append("/Android");// 手机系统平台
			ua.append("/" + android.os.Build.VERSION.RELEASE);// 手机系统版本
			ua.append("/" + android.os.Build.MODEL); // 手机型号
			ua.append("/" + appContext.getAppId());// 客户端唯一标识
			appUserAgent = ua.toString();
		}
		return appUserAgent;
	}

	private static HttpClient getHttpClient() {
		HttpClient httpClient = new HttpClient();
		// 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		// 设置 默认的超时重试处理策略
		httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());
		// 设置 连接超时时间
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(TIMEOUT_CONNECTION);
		// 设置 读数据超时时间
		httpClient.getHttpConnectionManager().getParams()
				.setSoTimeout(TIMEOUT_SOCKET);
		// 设置 字符集
		httpClient.getParams().setContentCharset(UTF_8);
		return httpClient;
	}

	private static GetMethod getHttpGet(String url, String cookie,
			String userAgent) {
		GetMethod httpGet = new GetMethod(url);
		// 设置 请求超时时间
		httpGet.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpGet.setRequestHeader("Host", URLs.HOST);
		httpGet.setRequestHeader("Connection", "Keep-Alive");
		if(cookie!=null)
			httpGet.setRequestHeader("Cookie", cookie);
		httpGet.setRequestHeader("User-Agent", userAgent);
		return httpGet;
	}

	private static PostMethod getHttpPost(String url, String cookie,
			String userAgent) {
		PostMethod httpPost = new PostMethod(url);
		// 设置 请求超时时间
		httpPost.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpPost.setRequestHeader("Host", URLs.HOST);
		httpPost.setRequestHeader("Connection", "Keep-Alive");
		httpPost.setRequestHeader("Cookie", cookie);
		httpPost.setRequestHeader("User-Agent", userAgent);
		return httpPost;
	}

	private static String _MakeURL(String p_url, Map<String, Object> params) {
		StringBuilder url = new StringBuilder(p_url);
		if (url.indexOf("?") < 0)
			url.append('?');

		for (String name : params.keySet()) {
			url.append('&');
			url.append(name);
			url.append('=');
//			url.append(String.valueOf(params.get(name)));
			// 不做URLEncoder处理
			try {
				url.append(URLEncoder.encode(String.valueOf(params.get(name)),UTF_8));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String strUrl = url.toString().replace("?&", "?");
		Log.i(ApiClient.class.getName(), strUrl);
		return strUrl;
		
	}

//	/**
//	 * get请求URL
//	 * 
//	 * @param url
//	 * @throws AppException
//	 */
//	private static InputStream http_get(AppContext appContext, String url)
//			throws AppException {
//		// System.out.println("get_url==> "+url);
//		String cookie = getCookie(appContext);
//		String userAgent = getUserAgent(appContext);
//
//		HttpClient httpClient = null;
//		GetMethod httpGet = null;
//
//		String responseBody = "";
//		int time = 0;
//		do {
//			try {
//				httpClient = getHttpClient();
//				httpGet = getHttpGet(url, cookie, userAgent);
//				int statusCode = httpClient.executeMethod(httpGet);
//				if (statusCode != HttpStatus.SC_OK) {
//					throw AppException.http(statusCode);
//				}
//				responseBody = httpGet.getResponseBodyAsString();
//				// System.out.println("XMLDATA=====>"+responseBody);
//				break;
//			} catch (HttpException e) {
//				time++;
//				if (time < RETRY_TIME) {
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e1) {
//					}
//					continue;
//				}
//				// 发生致命的异常，可能是协议不对或者返回的内容有问题
//				e.printStackTrace();
//				throw AppException.http(e);
//			} catch (IOException e) {
//				time++;
//				if (time < RETRY_TIME) {
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e1) {
//					}
//					continue;
//				}
//				// 发生网络异常
//				e.printStackTrace();
//				throw AppException.network(e);
//			} finally {
//				// 释放连接
//				httpGet.releaseConnection();
//				httpClient = null;
//			}
//		} while (time < RETRY_TIME);
//
//		responseBody = responseBody.replaceAll("\\p{Cntrl}", "");
//		if (responseBody.contains("result")
//				&& responseBody.contains("errorCode")
//				&& appContext.containsProperty("user.uid")) {
//			try {
//				Result res = Result.parse(new ByteArrayInputStream(responseBody
//						.getBytes()));
//				if (res.getErrorCode() == 0) {
//					appContext.Logout();
//					appContext.getUnLoginHandler().sendEmptyMessage(1);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return new ByteArrayInputStream(responseBody.getBytes());
//	}	
	
	private static ResponseBase _get(AppContext appContext, RequestBase request)
			throws AppException {
		String url=request.getUrl();
		// System.out.println("post_url==> "+url);
		ResponseBase ret = new ResponseBase();

		String cookie = getCookie(appContext);

		String userAgent = getUserAgent(appContext);

		HttpClient httpClient = null;
		GetMethod httpGet = null;
		
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				Log.i("HTTP",url);
				httpGet = getHttpGet(url, cookie, userAgent);
				httpGet.addRequestHeader("Accept", "application/json");
				int statusCode = httpClient.executeMethod(httpGet);
				
				ret.setCode(statusCode);

				if (httpClient.getState() != null) {
					Cookie[] cookies = httpClient.getState().getCookies();
					if (cookies != null && cookies.length != 0) {
						String tmpcookies = "";
						for (Cookie ck : cookies) {
							tmpcookies += ck.toString() + ";";
						}
						// 保存cookie
						if (appContext != null && !tmpcookies.equals("")) {
							appContext.setProperty("cookie", tmpcookies);
							appCookie = tmpcookies;
							ret.setCookie(tmpcookies);
						}
					}
				}

				if (statusCode == HttpStatus.SC_OK) {
					StringBuffer sb=new StringBuffer();
					try{
						BufferedReader br=new BufferedReader(new InputStreamReader(httpGet.getResponseBodyAsStream(),UTF_8));
						char[] arr=new char[4096];
						int len;
						while((len=br.read(arr))>0){
							sb.append(new String(arr,0,len));
						}
						br.close();
					}
					catch(Exception e){
						Log.e("HTTP", e.getMessage());
					}
					ret.setBody(sb.toString());
				}

				Header[] headers = httpGet.getResponseHeaders();
				if (headers != null) {
					for (Header header : headers) {
						ret.addHeader(header.getName(), header.getValue());
					}
				}

				Log.i("HTTP", ret.toString());

				break;
			} catch (HttpException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 释放连接
				httpGet.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);
		
		_checkSession(appContext,request,ret);

		return ret;
	}
	
	/**
	 * 公用post方法
	 * 
	 * @param url
	 * @param params
	 * @param files
	 * @throws AppException
	 */
	private static ResponseBase _post(AppContext appContext, RequestBase request)
			throws AppException {
		// System.out.println("post_url==> "+url);
		String url=request.getUrl();
		Map<String,Object> params=request.getParams();
		
		ResponseBase ret = new ResponseBase();

		String cookie = getCookie(appContext);

		String userAgent = getUserAgent(appContext);

		HttpClient httpClient = null;
		PostMethod httpPost = null;
		
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				Log.i("HTTP", url);
				httpPost = getHttpPost(url, cookie, userAgent);
				if (params != null)
					for (String name : params.keySet()) {
						httpPost.addParameter(name,
								String.valueOf(params.get(name)));
					}
				// MultipartRequestEntity(parts,httpPost.getParams()));
				int statusCode = httpClient.executeMethod(httpPost);				

				ret.setCode(statusCode);

				if (httpClient.getState() != null) {
					Cookie[] cookies = httpClient.getState().getCookies();
					if (cookies != null && cookies.length != 0) {
						String tmpcookies = "";
						for (Cookie ck : cookies) {
							tmpcookies += ck.toString() + ";";
						}
						// 保存cookie
						if (appContext != null && !tmpcookies.equals("")) {
							appContext.setProperty("cookie", tmpcookies);
							appCookie = tmpcookies;
							ret.setCookie(tmpcookies);
						}
					}
				}

				if (statusCode == HttpStatus.SC_OK) {
					StringBuffer sb=new StringBuffer();
					try{
						BufferedReader br=new BufferedReader(new InputStreamReader(httpPost.getResponseBodyAsStream(),UTF_8));
						char[] arr=new char[4096];
						int len;
						while((len=br.read(arr))>0){
							sb.append(new String(arr,0,len));
						}
						br.close();
					}
					catch(Exception e){
						Log.e("HTTP", e.getMessage());
					}
					ret.setBody(sb.toString());
				}

				Header[] headers = httpPost.getResponseHeaders();
				if (headers != null) {
					for (Header header : headers) {
						ret.addHeader(header.getName(), header.getValue());
					}
				}

				Log.i("HTTP", ret.toString());

				break;
			} catch (HttpException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 释放连接
				httpPost.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);
		
		_checkSession(appContext,request,ret);
		
		return ret;
	}
	
	
	
	/**
	 * 公用post方法
	 * 
	 * @param url
	 * @param params
	 * @param files
	 * @throws AppException
	 */
	private static ResponseBase _post(AppContext appContext, RequestBase request,int acceptCode)
			throws AppException {
		// System.out.println("post_url==> "+url);
		String url=request.getUrl();
		Map<String,Object> params=request.getParams();
		
		ResponseBase ret = new ResponseBase();

		String cookie = getCookie(appContext);

		String userAgent = getUserAgent(appContext);

		HttpClient httpClient = null;
		PostMethod httpPost = null;
		
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				Log.i("HTTP", url);
				httpPost = getHttpPost(url, cookie, userAgent);
				if (params != null)
					for (String name : params.keySet()) {
						httpPost.addParameter(name,
								String.valueOf(params.get(name)));
					}
				
				if(request.getBody()!=null){
					RequestEntity requestEntity=new ByteArrayRequestEntity(request.getBody());
					httpPost.setRequestEntity(requestEntity); 
					Log.i("INFO", "BODY:"+new String(request.getBody()));
				}
				// MultipartRequestEntity(parts,httpPost.getParams()));
				int statusCode = httpClient.executeMethod(httpPost);				

				ret.setCode(statusCode);

				if (httpClient.getState() != null) {
					Cookie[] cookies = httpClient.getState().getCookies();
					if (cookies != null && cookies.length != 0) {
						String tmpcookies = "";
						for (Cookie ck : cookies) {
							tmpcookies += ck.toString() + ";";
						}
						// 保存cookie
						if (appContext != null && !tmpcookies.equals("")) {
							appContext.setProperty("cookie", tmpcookies);
							appCookie = tmpcookies;
							ret.setCookie(tmpcookies);
						}
					}
				}

				
				if (statusCode == acceptCode) {
					StringBuffer sb=new StringBuffer();
					try{
						BufferedReader br=new BufferedReader(new InputStreamReader(httpPost.getResponseBodyAsStream(),UTF_8));
						char[] arr=new char[4096];
						int len;
						while((len=br.read(arr))>0){
							sb.append(new String(arr,0,len));
						}
						br.close();
					}
					catch(Exception e){
						Log.e("HTTP", e.getMessage());
					}
					ret.setBody(sb.toString());
				}

				Header[] headers = httpPost.getResponseHeaders();
				if (headers != null) {
					for (Header header : headers) {
						ret.addHeader(header.getName(), header.getValue());
					}
				}

				Log.i("HTTP", ret.toString());

				break;
			} catch (HttpException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 释放连接
				httpPost.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);
		
		_checkSession(appContext,request,ret);
		
		return ret;
	}
	
	private static void _checkSession(AppContext appContext,RequestBase request,ResponseBase response) throws AppException{
		
		if(response.getCode()==HttpStatus.SC_UNAUTHORIZED){
			//重新登录
			appContext.Logout();
			throw AppException.relogin(new Exception());
		}
	}

//	/**
//	 * 公用post方法
//	 * 
//	 * @param url
//	 * @param params
//	 * @param files
//	 * @throws AppException
//	 */
//	private static InputStream _post(AppContext appContext, String url,
//			Map<String, Object> params, Map<String, File> files)
//			throws AppException {
//		// System.out.println("post_url==> "+url);
//		String cookie = getCookie(appContext);
//		String userAgent = getUserAgent(appContext);
//
//		HttpClient httpClient = null;
//		PostMethod httpPost = null;
//
//		// post表单参数处理
//		int length = (params == null ? 0 : params.size())
//				+ (files == null ? 0 : files.size());
//		Part[] parts = new Part[length];
//		int i = 0;
//		if (params != null)
//			for (String name : params.keySet()) {
//				parts[i++] = new StringPart(name, String.valueOf(params
//						.get(name)), UTF_8);
//				System.out.println("post_key==> " + name + "    value==>"
//						+ String.valueOf(params.get(name)));
//			}
//		if (files != null)
//			for (String file : files.keySet()) {
//				try {
//					parts[i++] = new FilePart(file, files.get(file));
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				}
//				System.out.println("post_key_file==> " + file);
//			}
//
//		String responseBody = "";
//		int time = 0;
//		do {
//			try {
//				httpClient = getHttpClient();
//				httpPost = getHttpPost(url, cookie, userAgent);
//				httpPost.setRequestEntity(new MultipartRequestEntity(parts,
//						httpPost.getParams()));
//				int statusCode = httpClient.executeMethod(httpPost);
//				if (statusCode != HttpStatus.SC_OK) {
//					throw AppException.http(statusCode);
//				} else if (statusCode == HttpStatus.SC_OK) {
//					Cookie[] cookies = httpClient.getState().getCookies();
//					String tmpcookies = "";
//					for (Cookie ck : cookies) {
//						tmpcookies += ck.toString() + ";";
//					}
//					// 保存cookie
//					if (appContext != null && tmpcookies != "") {
//						appContext.setProperty("cookie", tmpcookies);
//						appCookie = tmpcookies;
//					}
//				}
//				responseBody = httpPost.getResponseBodyAsString();
//				// System.out.println("XMLDATA=====>"+responseBody);
//				break;
//			} catch (HttpException e) {
//				time++;
//				if (time < RETRY_TIME) {
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e1) {
//					}
//					continue;
//				}
//				// 发生致命的异常，可能是协议不对或者返回的内容有问题
//				e.printStackTrace();
//				throw AppException.http(e);
//			} catch (IOException e) {
//				time++;
//				if (time < RETRY_TIME) {
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e1) {
//					}
//					continue;
//				}
//				// 发生网络异常
//				e.printStackTrace();
//				throw AppException.network(e);
//			} finally {
//				// 释放连接
//				httpPost.releaseConnection();
//				httpClient = null;
//			}
//		} while (time < RETRY_TIME);
//
//		responseBody = responseBody.replaceAll("\\p{Cntrl}", "");
//		if (responseBody.contains("result")
//				&& responseBody.contains("errorCode")
//				&& appContext.containsProperty("user.uid")) {
//			try {
//				Result res = Result.parse(new ByteArrayInputStream(responseBody
//						.getBytes()));
//				if (res.getErrorCode() == 0) {
////					appContext.Logout();
////					appContext.getUnLoginHandler().sendEmptyMessage(1);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return new ByteArrayInputStream(responseBody.getBytes());
//	}
//
//	/**
//	 * post请求URL
//	 * 
//	 * @param url
//	 * @param params
//	 * @param files
//	 * @throws AppException
//	 * @throws IOException
//	 * @throws
//	 */
//	private static Result http_post(AppContext appContext, String url,
//			Map<String, Object> params, Map<String, File> files)
//			throws AppException, IOException {
//		return Result.parse(_post(appContext, url, params, files));
//	}

	/**
	 * 获取网络图片
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getNetBitmap(String url) throws AppException {
		// System.out.println("image_url==> "+url);
		HttpClient httpClient = null;
		GetMethod httpGet = null;
		Bitmap bitmap = null;
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				httpGet = getHttpGet(url, null, null);
				int statusCode = httpClient.executeMethod(httpGet);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				}
				InputStream inStream = httpGet.getResponseBodyAsStream();
				bitmap = BitmapFactory.decodeStream(inStream);
				inStream.close();
				break;
			} catch (HttpException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 释放连接
				httpGet.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);
		return bitmap;
	}

	/**
	 * 检查版本更新
	 * 
	 * @param url
	 * @return
	 */
	public static Update checkVersion(AppContext appContext)
			throws AppException {
		try {
			return Entity.parse(_get(appContext, new RequestBase(URLs.UPDATE_VERSION)).getBody(),Update.class);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * 登录， 自动处理cookie
	 * 
	 * @param url
	 * @param username
	 * @param pwd
	 * @return
	 * @throws AppException
	 */
	public static User login(AppContext appContext, String username, String pwd)
			throws AppException {
		try {			
			ResponseBase response=_login(appContext, username, pwd);
			if(URLs.JSECCHKURL_HTTP_OK.equals(response.getHeader("Location"))){				
				User user = currentUser(appContext);
				ArrayList<Relative> relatives = getRelatives(appContext, user.getUid());
				user.setRelatives(relatives);
				return user;
			}
			else{
				//登录失败
				User user=new User();
				Result res=new Result();
				res.setErrorMessage(response.getCode()==HttpStatus.SC_MOVED_TEMPORARILY?"用户名密码错误":"未知错误");
				user.setValidate(res);
				return user;
//				throw AppException.http(response.getCode());
			}
			
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}
	
	private static ResponseBase _login(AppContext appContext, String username, String pwd) throws AppException{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("j_username", username);
		params.put("j_password", pwd);
		params.put("token", "patient");
		cleanCookie(appContext);			
		return _post(appContext, new RequestBase(URLs.JSECCHKURL_HTTP, "POST",params));		
	}

	/**
	 * 更新用户之间关系（加关注、取消关注）
	 * 
	 * @param uid
	 *            自己的uid
	 * @param hisuid
	 *            对方用户的uid
	 * @param newrelation
	 *            0:取消对他的关注 1:关注他
	 * @return
	 * @throws AppException
	 */
	public static Result updateRelation(AppContext appContext, int uid,
			int hisuid, int newrelation) throws AppException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("uid", uid);
		params.put("hisuid", hisuid);
		params.put("newrelation", newrelation);

		try {
			return Entity.parse(_post(appContext, new RequestBase(URLs.USER_UPDATERELATION,"POST",
					params)).getBody(),Result.class);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}
	
	private static User currentUser(AppContext appContext) throws AppException{
		try{
			String url = URLs.ECG_CURRENT_USER;
			ResponseBase ret = _get(appContext,new RequestBase(url));
			if(ret.getCode()==HttpStatus.SC_OK){
				Gson gson = new GsonBuilder()
				.registerTypeAdapter(java.util.Date.class,new UtilDateDeserializer()).create();
				Patient p=gson.fromJson(ret.getBody(), Patient.class);				
				User user=new User();
				user.setName(p.getFullName());
				user.setAccount(p.getUsername());
				user.setPwd(" ");			
				user.setUid(p.getId().intValue());
				user.setAddress("");
				user.setProvince("");
				user.setCity("");
				user.setCountry("CN");
				user.setPostalCode("");
				user.setEmail(p.getEmail());
				user.setPhoneNumber(p.getPhoneNumber());				
				user.setRememberMe(true);
				user.setBirthday(p.getBirthday());
				user.setJointime(new Date().toLocaleString());
				user.setGender(p.getSex().toString());
				Result res=new Result();				
				res.setErrorCode(1);	
				user.setValidate(res);				
				return user;					
			}
			
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
		
		return null;
	}

	/**
	 * 获取用户通知信息
	 * 
	 * @param uid
	 * @return
	 * @throws AppException
	 */
	public static Notice getUserNotice(AppContext appContext, int uid)
			throws AppException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("uid", uid);

		try {
			return Entity.parse(_post(appContext, new RequestBase(URLs.USER_NOTICE,"POST",params)).getBody(),Notice.class);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * 清空通知消息
	 * 
	 * @param uid
	 * @param type
	 *            1:@我的信息 2:未读消息 3:评论个数 4:新粉丝个数
	 * @return
	 * @throws AppException
	 */
	public static Result noticeClear(AppContext appContext, int uid, int type)
			throws AppException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("uid", uid);
		params.put("type", type);

		try {
			return Entity.parse(_post(appContext, new RequestBase(URLs.NOTICE_CLEAR,"POST",params)).getBody(),Result.class);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * 获取帖子列表
	 * 
	 * @param url
	 * @param catalog
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public static BluetoothDeviceList getBluetoothDeviceList(AppContext appContext,
			final int catalog, final int pageIndex, final int pageSize)
			throws AppException {


		try {
			return BluetoothDeviceList.parse(appContext);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * 通过Tag获取帖子列表
	 * 
	 * @param url
	 * @param catalog
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
//	public static PostList getPostListByTag(AppContext appContext,
//			final String tag, final int pageIndex, final int pageSize)
//			throws AppException {
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("tag", tag);
//		params.put("pageIndex", pageIndex);
//		params.put("pageSize", pageSize);
//
//		try {
//			return PostList.parse(_post(appContext, URLs.POST_LIST, params,
//					null));
//		} catch (Exception e) {
//			if (e instanceof AppException)
//				throw (AppException) e;
//			throw AppException.network(e);
//		}
//	}






	/**
	 * 获取动弹列表
	 * 
	 * @param uid
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws AppException
	 */
	public static EcgRecordList getRecordList(AppContext appContext,
			final int pageIndex, final int pageSize) throws AppException {

		try {
			Map params = new HashMap<String, Object>();
			params.put("page", pageIndex);
			params.put("pageSize", pageSize);
			//params.put("userName", user.getAccount());
			String newUrl = _MakeURL(URLs.ECG_GET_ECG_RECORD_LIST, params);
			String body = _get(appContext,new RequestBase(newUrl)).getBody();
			EcgRecordList list = EcgRecordList.parse(body);
			
			return list;
			
//			return EcgRecordList.parse(http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}
	
	public static ECGClassificationList getECGClassificationList(AppContext appContext, final int recordId,final short code,
			final int pageIndex, final int pageSize) throws AppException {

		try {
			Map params = new HashMap<String, Object>();
			params.put("id", recordId);
			params.put("code", code);
			params.put("page", pageIndex);
			params.put("pageSize", pageSize);			
			//params.put("userName", user.getAccount());
			String newUrl = _MakeURL(URLs.ECG_GET_ECGCLASSIFICATION_LIST, params);
			String body = _get(appContext,new RequestBase(newUrl)).getBody();
			ECGClassificationList list = Entity.parse(body,ECGClassificationList.class);			
			return list;
			
//			return EcgRecordList.parse(http_get(appContext, newUrl));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * 获取动弹详情
	 * 
	 * @param recordId
	 * @return
	 * @throws AppException
	 */
	public static EcgRecord getRecordDetail(AppContext appContext, final int recordId)
			throws AppException {
		String newUrl = _MakeURL(URLs.ECG_GET_ECG_RECORD,
				new HashMap<String, Object>() {
					{
						put("id", recordId);
					}
				});
		try {			
			String body = _get(appContext,new RequestBase(newUrl)).getBody();
			if(body!=null){
				return Entity.parse(body, EcgRecord.class);
			}
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
		return null;
	}
	
	private final static DateFormat MESSAGEDATEFORMAT = new SimpleDateFormat(
			"yyyyMMddHHmmssSSS",Locale.CHINA);

	public static HeartBeatData getRawData(AppContext appContext,
			final int recordId, final Integer start, final Integer end)
			throws AppException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", recordId);
		params.put("flag", "0");
		if(start!=null){
			params.put("start", start);
		}
		if(end!=null){
			params.put("end", end);
		}
		try {			
			String body = _get(appContext,
					new RequestBase(_MakeURL(URLs.ECG_GET_ECG_RAW, params))).getBody();

			return Entity.parse(body, HeartBeatData.class);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

//	/**
//	 * 发动弹
//	 * 
//	 * @param EcgRecord
//	 *            -uid & msg & image
//	 * @return
//	 * @throws AppException
//	 */
//	public static Result pubTweet(AppContext appContext, EcgRecord tweet)
//			throws AppException {
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("uid", tweet.getAuthorId());
//		params.put("msg", tweet.getBody());
//
//		Map<String, File> files = new HashMap<String, File>();
//		if (tweet.getImageFile() != null)
//			files.put("img", tweet.getImageFile());
//
//		try {
//			return http_post(appContext, URLs.TWEET_PUB, params, files);
//		} catch (Exception e) {
//			if (e instanceof AppException)
//				throw (AppException) e;
//			throw AppException.network(e);
//		}
//	}
//
//
//
//	/**
//	 * 用户收藏列表
//	 * 
//	 * @param pageIndex
//	 *            页面索引 0表示第一页
//	 * @param pageSize
//	 *            每页的数量
//	 * @return
//	 * @throws AppException
//	 */
//	public static PVCInfoList getPVCInfoList(AppContext appContext,
//			final int recordId, final int pageIndex,
//			final int pageSize) throws AppException {
//		final User user = appContext.getLoginInfo();
//		Map<String,Object> params=
//				new HashMap<String, Object>() {
//					{
//						put("recordId", recordId);
//						put("defectType","pvc");
//						put("pageIndex", pageIndex);
//						put("pageSize", pageSize);
//						put("userName", user.getAccount());
//					}
//				};
//
//		try {
//			String body=_post(appContext, URLs.ECG_GET_ECG_DEFECT_LIST,params).getBody();
//		
//			return PVCInfoList.parse(body);
//		} catch (Exception e) {
//			if (e instanceof AppException)
//				throw (AppException) e;
//			throw AppException.network(e);
//		}
//	}
//
//
//
//
//
//

//	public static PVCItem getPVCSamples(AppContext appContext,
//			final int recordId, final int fromPos, final int toPos) throws AppException{
//		final User user = appContext.getLoginInfo();
//		Map<String,Object> params=
//				new HashMap<String, Object>() {
//					{
//						put("recordId", recordId);
//						put("fromPos",fromPos);
//						put("toPos", toPos);
//						put("userName",user.getAccount());
//					}
//				};
//		try {
//			//User user = appContext.getLoginInfo();
//			String body=_post(appContext,URLs.ECG_GET_ECG_SAMPLES,params).getBody();
//				
//			return PVCItem.parse(body);
//		} catch (Exception e) {
//			if (e instanceof AppException)
//				throw (AppException) e;
//			throw AppException.network(e);
//		}
//	}
	
	public static Map<String,String> checkParam(Map<String,Object> params, AppContext appContext) throws AppException{
		try {
			String url = _MakeURL(URLs.USER_CHECK_PARAM, params);
			ResponseBase response = _get(appContext, new RequestBase(url));
			return response.getHeaders();
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}
	
	
	public static User register(Map<String,Object> params, AppContext appContext) throws AppException{
		try {
			ResponseBase response = _post(appContext, new RequestBase(URLs.USER_REGISTER,"POST",params));
			if(response.getCode()==HttpStatus.SC_OK){
				return login(appContext, params.get("userName").toString(), params.get("password").toString());
			}else{
				//登录失败
				User user=new User();
				Result res=new Result();
				res.setErrorMessage(response.getCode()==HttpStatus.SC_MOVED_TEMPORARILY?"用户名密码错误":"未知错误");
				user.setValidate(res);
				return user;
			}
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}
	
	
	public static User changePassword(Map<String,Object> params, AppContext appContext) throws AppException{
		try {
			ResponseBase response = _post(appContext, new RequestBase(URLs.USER_CHANGE_PASSWORD,"POST",params));
			if(response.getCode()==HttpStatus.SC_OK){
				return login(appContext, params.get("userName").toString(), params.get("password").toString());
			}else if(response.getCode()==HttpStatus.SC_EXPECTATION_FAILED){
				//登录失败
				User user=new User();
				Result res=new Result();
				res.setErrorCode(HttpStatus.SC_EXPECTATION_FAILED);
				res.setErrorMessage(response.getCode()==HttpStatus.SC_MOVED_TEMPORARILY?"修改密码失败":"验证码错误");
				user.setValidate(res);
				return user;
			}else{
				User user=new User();
				Result res=new Result();
				res.setErrorCode(HttpStatus.SC_BAD_REQUEST);
				res.setErrorMessage(response.getCode()==HttpStatus.SC_MOVED_TEMPORARILY?"修改密码失败":"用户名错误");
				user.setValidate(res);
				return user;
			}
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static ResponseBase sendSMS(AppContext appContext,
			HashMap<String, Object> params) throws AppException {
		String url = _MakeURL(URLs.USER_SEND_SMS, params);
		ResponseBase response;
		try {
			response = _get(appContext, new RequestBase(url));
		} catch (AppException e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
		return response;
	}
	
	public static EcgRecord sendEcgData(AppContext appContext, Map<String, Object> params) throws AppException {

		try {
			//params.put("userName", user.getAccount());
			byte[] body = (byte[]) params.remove(MessageParams.ECG_DATA);
			String newUrl = _MakeURL(URLs.ECG_SEND_DATA, params);
//			String body = _get(appContext,new RequestBase(newUrl)).getBody();
			RequestBase request = new RequestBase(newUrl,"POST",params);
			request.setBody(body);
			ResponseBase response = _post(appContext, request,HttpStatus.SC_OK);
			String responseBody = response.getBody();
			if(body!=null){
				EcgRecord record = Entity.parse(responseBody,EcgRecord.class);			
				return record;
			}
			return null;
			
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}
	
	
	public static Relative addRelative(AppContext appContext, final Map<String, Object> params) throws AppException {

		try {
			//params.put("userName", user.getAccount());
			String newUrl = _MakeURL(URLs.ECG_ADD_RELATIVE, params);
			String body = _get(appContext,new RequestBase(newUrl)).getBody();
			Relative relative = Entity.parse(body,Relative.class);			
			return relative;
			
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}
	
	public static boolean delRelative(AppContext appContext, final Map<String, Object> params){
 
		try {
			String newUrl = _MakeURL(URLs.ECG_DEL_RELATIVE, params);
			ResponseBase response=_get(appContext,new RequestBase(newUrl));
			if(response.getCode()==HttpStatus.SC_OK){
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static ArrayList<Relative> getRelatives(AppContext appContext, int patientId) throws AppException {
		try {
			Map<String, Object> params=new HashMap<String, Object>();
			params.put("patientId", patientId);
			String newUrl = _MakeURL(URLs.ECG_GET_RELATIVES, params);
			String body = _get(appContext,new RequestBase(newUrl)).getBody();
			RelativeList rl = RelativeList.parse(body);
			List<Relative> relatives = rl.getList();
			return (ArrayList<Relative>) relatives;
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}
	public static EcgRecord createTransferSession(AppContext appContext,Map<String, Object> params) throws AppException {
		String newUrl = _MakeURL(URLs.ECG_CREATE_SESSION, params);
		ResponseBase response = _post(appContext, new RequestBase(newUrl,"POST",params));
//		String body = response.getBody();
//		EcgRecord record = Entity.parse(body,EcgRecord.class);
		EcgRecord record = new EcgRecord();
		if(response.getCode()==HttpStatus.SC_CREATED)
			record.setId(new Integer(response.getHeader(MessageParams.ID)));
		else
			AppException.run(new Exception("Bad Response Code:"+response.getCode()));
		return record;
	}
}
