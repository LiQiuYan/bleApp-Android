package com.healthme.app.bean;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Relative extends Entity {
	/**
	 * 
	 */
	Long patientId;
	String phoneNumber;
	String fullName;
	Boolean inEdit;
	public long getPatientId() {
		return patientId;
	}
	public void setPatientId(long patientId) {
		this.patientId = patientId;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public boolean isInEdit() {
		return inEdit;
	}
	public void setInEdit(boolean inEdit) {
		this.inEdit = inEdit;
	}
	
	@Override
	public String toString() {
		return "Relative [patientId=" + patientId + ", phoneNumber="
				+ phoneNumber + ", fullName=" + fullName + ", inEdit=" + inEdit
				+ "]";
	}

}
