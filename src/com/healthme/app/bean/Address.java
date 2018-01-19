package com.healthme.app.bean;

import java.io.Serializable;

public class Address implements Serializable {
    private static final long serialVersionUID = 3617859655330969141L;
    private String address;
    private String city;
    private String province;
    private String country;
    private String district;
    private String postalCode;
    
    private String provinceAndCity;

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

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getProvinceAndCity() {
		return provinceAndCity;
	}

	public void setProvinceAndCity(String provinceAndCity) {
		this.provinceAndCity = provinceAndCity;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
    
    
    
    
    
}
