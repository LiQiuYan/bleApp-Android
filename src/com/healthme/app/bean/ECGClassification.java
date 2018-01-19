package com.healthme.app.bean;

import java.util.Date;

public class ECGClassification extends Entity {
	
	private short code;
	private Integer startPos=-1;
	private Integer endPos=-1;
	private double value=0.0;
	private Byte checked;	
	private Long medicalRecordId;
	private Date startTime;
	private Date endTime;
	
	public short getCode() {
		return code;
	}
	public void setCode(short code) {
		this.code = code;
	}
	public Integer getStartPos() {
		return startPos;
	}
	public void setStartPos(Integer startPos) {
		this.startPos = startPos;
	}
	public Integer getEndPos() {
		return endPos;
	}
	public void setEndPos(Integer endPos) {
		this.endPos = endPos;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public Byte getChecked() {
		return checked;
	}
	public void setChecked(Byte checked) {
		this.checked = checked;
	}	
	public Long getMedicalRecordId() {
		return medicalRecordId;
	}
	public void setMedicalRecordId(Long medicalRecordId) {
		this.medicalRecordId = medicalRecordId;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	

}
