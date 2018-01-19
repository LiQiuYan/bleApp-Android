package com.healthme.app.bean;

import java.util.Date;


public class Patient extends Base {

    private Long id;
    private String groupId;
    private String username;                    // required
    private String password;                    // required
    private String confirmPassword;
    private String passwordHint;
    private String firstName;                    
    private String lastName;                     
    private String fullName;					// required
    private String email;                        
    private String phoneNumber;
    private String website;
    private String cardNo;
    private SEX sex;
    private Address address = new Address();
    private Date birthday;    
    private Integer version;
    private boolean enabled;
    private boolean accountExpired;
    private boolean accountLocked;
    private boolean credentialsExpired;
    
    private Integer age=-1;
    
    


    public Long getId() {
		return id;
	}




	public void setId(Long id) {
		this.id = id;
	}




	public String getGroupId() {
		return groupId;
	}




	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}




	public String getUsername() {
		return username;
	}




	public void setUsername(String username) {
		this.username = username;
	}




	public String getPassword() {
		return password;
	}




	public void setPassword(String password) {
		this.password = password;
	}




	public String getConfirmPassword() {
		return confirmPassword;
	}




	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}




	public String getPasswordHint() {
		return passwordHint;
	}




	public void setPasswordHint(String passwordHint) {
		this.passwordHint = passwordHint;
	}




	public String getFirstName() {
		return firstName;
	}




	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}




	public String getLastName() {
		return lastName;
	}




	public void setLastName(String lastName) {
		this.lastName = lastName;
	}




	public String getFullName() {
		return fullName;
	}




	public void setFullName(String fullName) {
		this.fullName = fullName;
	}




	public String getEmail() {
		return email;
	}




	public void setEmail(String email) {
		this.email = email;
	}




	public String getPhoneNumber() {
		return phoneNumber;
	}




	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}




	public String getWebsite() {
		return website;
	}




	public void setWebsite(String website) {
		this.website = website;
	}




	public String getCardNo() {
		return cardNo;
	}




	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}




	public SEX getSex() {
		return sex;
	}




	public void setSex(SEX sex) {
		this.sex = sex;
	}




	public Address getAddress() {
		return address;
	}




	public void setAddress(Address address) {
		this.address = address;
	}




	public Date getBirthday() {
		return birthday;
	}




	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}




	public Integer getVersion() {
		return version;
	}




	public void setVersion(Integer version) {
		this.version = version;
	}




	public boolean isEnabled() {
		return enabled;
	}




	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}




	public boolean isAccountExpired() {
		return accountExpired;
	}




	public void setAccountExpired(boolean accountExpired) {
		this.accountExpired = accountExpired;
	}




	public boolean isAccountLocked() {
		return accountLocked;
	}




	public void setAccountLocked(boolean accountLocked) {
		this.accountLocked = accountLocked;
	}




	public boolean isCredentialsExpired() {
		return credentialsExpired;
	}




	public void setCredentialsExpired(boolean credentialsExpired) {
		this.credentialsExpired = credentialsExpired;
	}




	public Integer getAge() {
		return age;
	}




	public void setAge(Integer age) {
		this.age = age;
	}




	public static enum SEX{
    	MALE,FEMALE
    }




	@Override
	public String toString() {
		return "Patient [id=" + id + ", groupId=" + groupId + ", username="
				+ username + ", password=" + password + ", confirmPassword="
				+ confirmPassword + ", passwordHint=" + passwordHint
				+ ", firstName=" + firstName + ", lastName=" + lastName
				+ ", fullName=" + fullName + ", email=" + email
				+ ", phoneNumber=" + phoneNumber + ", website=" + website
				+ ", cardNo=" + cardNo + ", sex=" + sex + ", address="
				+ address + ", birthday=" + birthday + ", version=" + version
				+ ", enabled=" + enabled + ", accountExpired=" + accountExpired
				+ ", accountLocked=" + accountLocked + ", credentialsExpired="
				+ credentialsExpired + ", age=" + age + "]";
	};
    
    
    
}
