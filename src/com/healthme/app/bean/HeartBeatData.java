package com.healthme.app.bean;

public class HeartBeatData extends Entity {
	
	private Long startTime;		
	//128
	private Integer sampleRate;
	
	private Integer dataSize;
	
	private Long firstPos;
	
	private Object[] data;
	
	private Object[] srcData;
	
	private Object[] annotation;
	
	private Object[] event;

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Integer getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(Integer sampleRate) {
		this.sampleRate = sampleRate;
	}

	public Integer getDataSize() {
		return dataSize;
	}

	public void setDataSize(Integer dataSize) {
		this.dataSize = dataSize;
	}

	public Long getFirstPos() {
		return firstPos;
	}

	public void setFirstPos(Long firstPos) {
		this.firstPos = firstPos;
	}

	public Object[] getData() {
		return data;
	}

	public void setData(Object[] data) {
		this.data = data;
	}

	public Object[] getSrcData() {
		return srcData;
	}

	public void setSrcData(Object[] srcData) {
		this.srcData = srcData;
	}

	public Object[] getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Object[] annotation) {
		this.annotation = annotation;
	}

	public Object[] getEvent() {
		return event;
	}

	public void setEvent(Object[] event) {
		this.event = event;
	}
	

	
}
