package com.healthme.app.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class Hmessage implements Serializable {

	private static class HEADER {
		public final static String MESSAGEID = "message-id";
		public final static String TIMESTAMP = "timestamp";
		public final static String DESTINATION = "destination";
		public final static String PRIORITY = "priority";
	};

	private String messageId;
	private String destination;
	private int priority;
	private Date sendTime;
	private User sender;
	
	private Date viewTime;

	private String content;	

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}	

	public Date getViewTime() {
		return viewTime;
	}

	public void setViewTime(Date viewTime) {
		this.viewTime = viewTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public int hashCode(){
		return messageId==null?super.hashCode():this.messageId.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Hmessage){
			Hmessage m=(Hmessage)o;
			return this.messageId==null?this.messageId==m.messageId:this.messageId.equals(m.messageId);
		}
		return false;
	}

	@Override
	public String toString() {
		return "HMessage [messageId=" + messageId + ", destination="
				+ destination + ", priority=" + priority + ", sendTime="
				+ sendTime + ", sender=" + sender + ", content=" + content
				+ "]";
	}

	public static Hmessage fromStomp(Map<String, String> headers, String body) {
		Hmessage message = null;
		if (headers != null) {
			message = new Hmessage();
			message.messageId = headers.get(HEADER.MESSAGEID);
//			if (message.messageId != null) {
//				message.messageId = message.messageId.replaceAll(
//						"[\\\\][\\\\]c", ":");
//			}
			message.destination = headers.get(HEADER.DESTINATION);
			String priority = headers.get(HEADER.PRIORITY);
			if (priority != null && StringUtils.isNumeric(priority)) {
				message.priority = Integer.parseInt(priority);
			}
			String timestamp = headers.get(HEADER.TIMESTAMP);
			if (timestamp != null && StringUtils.isNumeric(timestamp)) {
				message.sendTime = new Date(Long.parseLong(timestamp));
			}
			message.content = body;
		}
		return message;
	}

}
