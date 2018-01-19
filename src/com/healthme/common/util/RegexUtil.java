package com.healthme.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

	/**
	  * 验证是否为合法邮箱地址
	  * @param address 邮箱地址
	  * @return 是否合法
	  */
	 public static final boolean isEMailAddress(String address){
	  String strPattern = "^[a-zA-Z][//w//.-]*[a-zA-Z0-9]@[a-zA-Z0-9][//w//.-]*[a-zA-Z0-9]//.[a-zA-Z][a-zA-Z//.]*[a-zA-Z]$";    
	  Pattern pattern = Pattern.compile(strPattern,Pattern.CASE_INSENSITIVE);    
	  Matcher matcher = pattern.matcher(address);    
	  return matcher.matches();    
	 }
	 
	
	 /**
	  * 判别手机是否为正确手机号码；
	 *号码段分配如下：
	 *移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188 
	 *联通：130、131、132、152、155、156、185、186 
	 *电信：133、153、180、189、（1349卫通）
	  */
	 public static boolean isMobileNum(String mobiles)
	 {
	  Pattern p = Pattern
	    .compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
	  Matcher m = p.matcher(mobiles);  
	  return m.matches();
	 }
}
