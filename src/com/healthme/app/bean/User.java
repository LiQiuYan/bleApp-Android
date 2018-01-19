package com.healthme.app.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



import org.apache.commons.lang.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.healthme.app.AppException;

import android.util.Xml;

/**
 * 登录用户实体类
 * 
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class User extends Base {

	public final static int RELATION_ACTION_DELETE = 0x00;// 取消关注
	public final static int RELATION_ACTION_ADD = 0x01;// 加关注

	public final static int RELATION_TYPE_BOTH = 0x01;// 双方互为粉丝
	public final static int RELATION_TYPE_FANS_HIM = 0x02;// 你单方面关注他
	public final static int RELATION_TYPE_NULL = 0x03;// 互不关注
	public final static int RELATION_TYPE_FANS_ME = 0x04;// 只有他关注我

	private int uid;
	private String location;
	private String name;
	private int followers;
	private int fans;
	private int score;
	private String face;
	private String account;
	private String pwd;
	private Result validate;
	private boolean isRememberMe;

	private String jointime;
	private String gender;
	private String devplatform;
	private String expertise;
	private int relation;
	private String latestonline;

	private String address;
	private String city;
	private String province;
	private String country;
	private String postalCode;

	private String phoneNumber;
	private Date birthday;
	private String email;
	private ArrayList<Relative> relatives;
	static Map<String,String> countryMap=new HashMap<String, String>();
	static{
		countryMap.put("CN", "中国");
	}
	public boolean isRememberMe() {
		return isRememberMe;
	}

	public void setRememberMe(boolean isRememberMe) {
		this.isRememberMe = isRememberMe;
	}

	public String getJointime() {
		return jointime;
	}

	public void setJointime(String jointime) {
		this.jointime = jointime;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getDevplatform() {
		return devplatform;
	}

	public void setDevplatform(String devplatform) {
		this.devplatform = devplatform;
	}

	public String getExpertise() {
		return expertise;
	}

	public void setExpertise(String expertise) {
		this.expertise = expertise;
	}

	public int getRelation() {
		return relation;
	}

	public void setRelation(int relation) {
		this.relation = relation;
	}

	public String getLatestonline() {
		return latestonline;
	}

	public void setLatestonline(String latestonline) {
		this.latestonline = latestonline;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getFollowers() {
		return followers;
	}

	public void setFollowers(int followers) {
		this.followers = followers;
	}

	public int getFans() {
		return fans;
	}

	public void setFans(int fans) {
		this.fans = fans;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getFace() {
		return face;
	}

	public void setFace(String face) {
		this.face = face;
	}

	public Result getValidate() {
		return validate;
	}

	public void setValidate(Result validate) {
		this.validate = validate;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ArrayList<Relative> getRelatives() {
		return relatives;
	}

	public void setRelatives(ArrayList<Relative> relatives) {
		this.relatives = relatives;
	}

	public static User parse(String body) throws IOException,
			AppException {
		User user = new User();
		Result res = null;
		String firstName=StringUtils.substringBetween(body,"<firstName>", "</firstName>");
		String lastName=StringUtils.substringBetween(body,"<lastName>", "</lastName>");
		user.setName(firstName+" "+ lastName);
		
		String pwd=StringUtils.substringBetween(body,"<password>", "</password>");
		user.setPwd(pwd);
		
		String country=StringUtils.substringBetween(body,"<country>", "</country>");
		user.setCountry(countryMap.get(country));
		
		String province=StringUtils.substringBetween(body,"<province>", "</province>");
		user.setProvince(province);
		
		String city=StringUtils.substringBetween(body,"<city>", "</city>");
		user.setCity(city);
		
		String address=StringUtils.substringBetween(body,"<address><address>", "</address>");
		user.setAddress(address);
		
		String account=StringUtils.substringBetween(body,"<username>", "</username>");
		user.setAccount(account);
		
		String phoneNumber=StringUtils.substringBetween(body,"<phoneNumber>", "</phoneNumber>");
		user.setPhoneNumber(phoneNumber);
		
		
		String postalCode=StringUtils.substringBetween(body,"<postalCode>", "</postalCode>");
		user.setPostalCode(postalCode);
		
		
		String uid=StringUtils.substringBetween(body,"<id>", "</id>");
		user.setUid(new Integer("1"));
		
		String email=StringUtils.substringBetween(body,"<email>", "</email>");
		user.setEmail(email);
		
		String gender=StringUtils.substringBetween(body,"<gender>", "</gender>");
		if(gender==null)
			gender="未知";
		else if(gender.equals("2"))
			gender="女";
		else
			gender="男";
		
		//fake some data here
		//TODO later
		user.isRememberMe=true;
		user.setBirthday(new Date());
		user.setJointime(new Date().toLocaleString());
		user.setGender(gender);
		res=new Result();
		if(account==null){
			res.setErrorCode(0);
			res.setErrorMessage("no account!");
		} else
			res.setErrorCode(1);	
		user.setValidate(res);
		return user;
	}
}
