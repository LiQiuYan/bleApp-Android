package com.healthme.app.bean;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.healthme.app.AppException;

/**
 * 动弹列表实体类
 * 
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class EcgRecordList extends PagerList<EcgRecord> {

	public final static int CATALOG_LASTEST = 0;
	public final static int CATALOG_HOT = -1;
	
	public EcgRecordList(){
		this.list=new ArrayList<EcgRecord>();
	}

//	public int getPageSize() {
//		return pageSize;
//	}
//
//	public int getTweetCount() {
//		return recordCount;
//	}
//
//	public List<EcgRecord> getRecordlist() {
//		return recordList;
//	}

	public static EcgRecordList parse(String body) throws IOException,
			AppException {
		EcgRecordList recordList = new EcgRecordList();
		// recordList.pageSize=20;
		// recordList.recordCount=200;
		// List<EcgRecord> l=new ArrayList<EcgRecord>();
		// Calendar cal = Calendar.getInstance();
		// for(int i=1;i<21;i++){
		// EcgRecord record = new EcgRecord();
		// record.setUserName("yong");
		// cal.setTimeInMillis(new Date().getTime()-i*10000);
		// record.setCreateTime(cal.getTime());
		// cal.setTimeInMillis(new Date().getTime()-i*1000);
		// record.setEndTime(cal.getTime());
		// record.setPVC(i*2);
		// record.setMaxRRInterval(i*1600/800);
		// record.setMinRRInterval(1);
		// record.setMaxRValue(6);
		// record.setMinRValue(i);
		// l.add(record);
		// }
		// recordList.recordList=l;
		// 获得XmlPullParser解析器
		// XmlPullParser xmlParser = Xml.newPullParser();
		// try {
		// xmlParser.setInput(inputStream, UTF8);
		// //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
		// int evtType=xmlParser.getEventType();
		// //一直循环，直到文档结束
		// while(evtType!=XmlPullParser.END_DOCUMENT){
		// String tag = xmlParser.getName();
		// switch(evtType){
		// case XmlPullParser.START_TAG:
		// if(tag.equalsIgnoreCase("tweetCount"))
		// {
		// tweetlist.tweetCount = StringUtils.toInt(xmlParser.nextText(),0);
		// }
		// else if(tag.equalsIgnoreCase("pageSize"))
		// {
		// tweetlist.pageSize = StringUtils.toInt(xmlParser.nextText(),0);
		// }
		// else if (tag.equalsIgnoreCase(EcgRecord.NODE_START))
		// {
		// tweet = new EcgRecord();
		// }
		// else if(tweet != null)
		// {
		// if(tag.equalsIgnoreCase(EcgRecord.NODE_ID))
		// {
		// tweet.id = StringUtils.toInt(xmlParser.nextText(),0);
		// }
		// else if(tag.equalsIgnoreCase(EcgRecord.NODE_FACE))
		// {
		// tweet.setFace(xmlParser.nextText());
		// }
		// else if(tag.equalsIgnoreCase(EcgRecord.NODE_BODY))
		// {
		// tweet.setBody(xmlParser.nextText());
		// }
		// else if(tag.equalsIgnoreCase(EcgRecord.NODE_AUTHOR))
		// {
		// tweet.setAuthor(xmlParser.nextText());
		// }
		// else if(tag.equalsIgnoreCase(EcgRecord.NODE_AUTHORID))
		// {
		// tweet.setAuthorId(StringUtils.toInt(xmlParser.nextText(),0));
		// }
		// else if(tag.equalsIgnoreCase(EcgRecord.NODE_COMMENTCOUNT))
		// {
		// tweet.setCommentCount(StringUtils.toInt(xmlParser.nextText(),0));
		// }
		// else if(tag.equalsIgnoreCase(EcgRecord.NODE_PUBDATE))
		// {
		// tweet.setPubDate(xmlParser.nextText());
		// }
		// else if(tag.equalsIgnoreCase(EcgRecord.NODE_IMGSMALL))
		// {
		// tweet.setImgSmall(xmlParser.nextText());
		// }
		// else if(tag.equalsIgnoreCase(EcgRecord.NODE_IMGBIG))
		// {
		// tweet.setImgBig(xmlParser.nextText());
		// }
		// else if(tag.equalsIgnoreCase(EcgRecord.NODE_APPCLIENT))
		// {
		// tweet.setAppClient(StringUtils.toInt(xmlParser.nextText(),0));
		// }
		// }
		// //通知信息
		// else if(tag.equalsIgnoreCase("notice"))
		// {
		// tweetlist.setNotice(new Notice());
		// }
		// else if(tweetlist.getNotice() != null)
		// {
		// if(tag.equalsIgnoreCase("atmeCount"))
		// {
		// tweetlist.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
		// }
		// else if(tag.equalsIgnoreCase("msgCount"))
		// {
		// tweetlist.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
		// }
		// else if(tag.equalsIgnoreCase("reviewCount"))
		// {
		// tweetlist.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
		// }
		// else if(tag.equalsIgnoreCase("newFansCount"))
		// {
		// tweetlist.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
		// }
		// }
		// break;
		// case XmlPullParser.END_TAG:
		// //如果遇到标签结束，则把对象添加进集合中
		// if (tag.equalsIgnoreCase("tweet") && tweet != null) {
		// tweetlist.getTweetlist().add(tweet);
		// tweet = null;
		// }
		// break;
		// }
		// //如果xml没有结束，则导航到下一个节点
		// evtType=xmlParser.next();
		// }
		// } catch (XmlPullParserException e) {
		// throw AppException.xml(e);
		// } finally {
		// inputStream.close();
		// }

		Gson gson = new GsonBuilder()
				.registerTypeAdapter(java.util.Date.class,new UtilDateDeserializer())
				.setDateFormat(DateFormat.LONG).create();
		EcgRecordList recordList1 = gson.fromJson(body, EcgRecordList.class);
		return recordList1;
	}
}

