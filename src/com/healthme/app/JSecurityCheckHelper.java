package com.healthme.app;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * <code>JSecurityCheckHelper</code>利用HttpClient实现JAAS签权。
 * 
 * @author newman.huang
 * @version 1.0 2007/2/26
 */
public class JSecurityCheckHelper {
	
	private static boolean usedProxy;
	private static String proxyHost;
	private static int proxyPort;
	private static String proxyUserName;
	private static String proxyPassword;
	private static String proxyDomain;
	private static final boolean debugOn=true; 
	
	private HttpClient httpClient;
	
	static{
		loadProxyConfig();
//		registerHttps();
	}
		
	//加载代理属性
	private static void loadProxyConfig(){
		ClassLoader loader=Thread.currentThread().getContextClassLoader();
		ResourceBundle res=null;
		if(loader==null){
			res=ResourceBundle.getBundle(JSecurityCheckHelper.class.getName());
		}else{
			res=ResourceBundle.getBundle(JSecurityCheckHelper.class.getName(),Locale.getDefault(),loader);
		}
		usedProxy="true".equals(res.getString("usedproxy"));
		debug("usedproxy=" + usedProxy);
		proxyHost=res.getString("proxy.host");
		debug("proxyHost=" + proxyHost);
		proxyPort=Integer.parseInt(res.getString("proxy.port"));
		debug("proxyPort=" + proxyPort);
		proxyDomain=res.getString("proxy.domain");
		debug("proxyDomain=" + proxyDomain);
		proxyUserName=res.getString("proxy.user");
		debug("proxyUserName=" + proxyUserName);
		proxyPassword=res.getString("proxy.password");
		debug("proxyPassword=" + proxyPassword);
	}
	
	//注册https协议以支持HttpClient通过SSL通讯
	@SuppressWarnings("deprecation")
//	private static void registerHttps(){
//		Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
//		Protocol.registerProtocol("https", easyhttps);
//	}
	
	/**
	 * 构建。
	 *
	 */
	public JSecurityCheckHelper(){
		this.httpClient=new HttpClient();
		if(usedProxy){
			setProxy();
		}
	}
	
	/**
	 * 执行j_security_check。
	 * 
	 * @param userName 待签权的用户名。
	 * @param password 待签权用户的密码。
	 * @param jSessionId 代表当前web会话的session id。	 
	 * @param jSecCheckFullURL j_security_check的完整url。
	 * @return 返回签权成功后的重定向url。
	 * 
	 * @throws SecurityCheckException 如果执行j_security_check的过程发生异常、或者签权不通过时掷出。
	 */
	public String doCheck(String userName, String password,
										String jSessionId, String jSecCheckFullURL) throws SecurityCheckException {
		
		debug("执行j_security_check签权:");
		debug("j_security_check url=" + jSecCheckFullURL);
		debug("jSessionId=" + jSessionId);
		debug("jUserName=" + userName);
		debug("jPassword=" + password);
		
		if (userName == null || "".equals(userName.trim())) {
			throw new SecurityCheckException("用户名不能为空");
		}
		
		String redirectURL = null;
		PostMethod postMethod = null;
		try {
			httpClient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpClient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
			HttpState initialState = new HttpState();
			initialState.addCookie(genRequestCookie(jSessionId,jSecCheckFullURL));
			httpClient.setState(initialState);
			postMethod = new PostMethod(jSecCheckFullURL);
			NameValuePair[] postData = new NameValuePair[2];
			postData[0] = new NameValuePair("j_username", userName);
			postData[1] = new NameValuePair("j_password", password);
			postMethod.addParameters(postData);
			try {
				int statusCode = httpClient.executeMethod(postMethod);
				Header locationHeader = postMethod.getResponseHeader("location");
				redirectURL = locationHeader != null ? locationHeader.getValue() : null;
				if (HttpStatus.SC_MOVED_PERMANENTLY == statusCode
						|| HttpStatus.SC_MOVED_TEMPORARILY == statusCode) {
					// 如果条件满足，说明签权成功
					debug("签权成功！");
				} else {
					debug("签权失败，返回状态码："+ statusCode);
					debug("返回消息体:" +postMethod.getResponseBodyAsString());
					throw new SecurityCheckException("执行j_security_check完毕，但是签权失败");
				}

			} catch (HttpException httpe) {
				httpe.printStackTrace();
				throw new SecurityCheckException("发生HttpException异常");
			} catch (IOException ioe) {
				throw new SecurityCheckException("发生IOException异常");
			}
		} finally {
			if (postMethod != null) {
				postMethod.releaseConnection();
			}
		}
		return redirectURL;
	}
	
	//设置代理以通过代理执行j_security_check校验
	private void setProxy(){
		//设置代理主机和端口
		httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
		//设置为提前签权方式
		httpClient.getParams().setAuthenticationPreemptive(true);
		//在参数中设置credentials provider
		httpClient.getParams().setParameter(CredentialsProvider.PROVIDER, 
				new CredentialsProvider(){
						public Credentials getCredentials(AuthScheme arg0, 
								String arg1, int arg2, boolean arg3) throws CredentialsNotAvailableException {
						return new UsernamePasswordCredentials(proxyUserName,proxyPassword);
						}
				});
		boolean isNotDefinedRealm=proxyDomain==null || "".equals(proxyDomain.trim());
		//设置代理的credentials
		httpClient.getState().setProxyCredentials(
				new AuthScope(proxyHost, 
						AuthScope.ANY_PORT, 
						isNotDefinedRealm?AuthScope.ANY_REALM:proxyDomain),
	            new UsernamePasswordCredentials(proxyUserName, proxyPassword));
	}
	
	//生成HttpClient Cookie
	private Cookie genRequestCookie(String jSessionId,String fullURL){
		Cookie cookie=new Cookie();
		cookie.setDomain(parseCookieDomainName(fullURL));
		cookie.setPath("/");
		cookie.setName("JSESSIONID");
		cookie.setValue(jSessionId);
		return cookie;
	}
	
	//通过url获取cookie的域名
	private static String parseCookieDomainName(String url){
		if(url==null)
			return null;
		String domainName=null;
		String aUrl=url.replaceAll(" ", "");
		if(!aUrl.startsWith("http")){
			return null;
		}
		int beginPos=7;
		if(aUrl.startsWith("https://")){
			beginPos=8;
		}
		aUrl=aUrl.substring(beginPos,aUrl.length());
		while(aUrl.endsWith("/")){
			aUrl=aUrl.substring(0,aUrl.length()-1);
		}
		int firstSlashPos=aUrl.indexOf("/");
		String[] urlSegs=null;
		if(firstSlashPos>0){
			urlSegs=aUrl.split("/");
		}else{
			urlSegs=new String[1];
			urlSegs[0]=aUrl;
		}
		if(urlSegs[0].indexOf(":")>0){
			domainName=urlSegs[0].split(":")[0];
		}else{
			domainName=urlSegs[0];
		}
		debug("cookie的域名为:" + domainName);
		return domainName;
	}
	
	//辅助方法，执行一个get请求，仅供测试使用
	private void doGetRequest(String url){
		GetMethod getMethod=null;
		try{
			getMethod=new GetMethod(url);
			int statusCode=httpClient.executeMethod(getMethod);
			debug("执行一个get请求返回状态码：" + statusCode);
		}catch(Exception x){
			x.printStackTrace();
		}finally{
			try{
				getMethod.releaseConnection();
			}catch(Exception x1){
				
			}			
		}
	}
	
	//辅助方法，获取当前JSESSIONID，仅供测试使用
	private String getJSessionId(){
		String jSessionId=null;
		Cookie[] cookies=httpClient.getState().getCookies();
		for(Cookie cookie: cookies){
			if("JSESSIONID".equals(cookie.getName())){
				jSessionId=cookie.getValue();
				break;
			}
		}
		return jSessionId;
	}
	
	//辅助方法，简单log屏幕输出
	private static void debug(String message){
		if(debugOn){
			System.out.println(message);
		}
	}
	
	/**
	 * 测试用。
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		//受保护的url
		String securityUrl="http://http//localhost:8080/HealthClub/";
		//j_security_check的url
		String jSecChkUrl="http://http//localhost:8080/HealthClub/j_security_check";		
		String userName="username";//受访问系统的用户名
		String password="password";//受访问系统的用户密码
		JSecurityCheckHelper helper=new JSecurityCheckHelper();
		try{
			//先访问受保护的url
			helper.doGetRequest(securityUrl);
			//获取当前JSESSIONID
			String jSessionId=helper.getJSessionId();
			if(jSessionId==null){
				throw new SecurityCheckException("无法获取需要进行签权的会话ID");
			}
			//执行签权
			String redirectUrl=helper.doCheck(userName, password, jSessionId, jSecChkUrl);
			//返回的redirectUrl应该=securityUrl
			debug("重定向url=" + redirectUrl);
		}catch(Exception x){
			x.printStackTrace();
		}
	}
	
	
}
