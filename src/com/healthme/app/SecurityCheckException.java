package com.healthme.app;

/**
 * ��ִ��j_security_check��鷢���쳣ʱ������
 * 
 * @author newman.huang
 * @version 1.0 2007/2/26
 */
public class SecurityCheckException extends Exception {
	
	public SecurityCheckException(){
		super();
	}
	
	public SecurityCheckException(String message){
		super(message);
	}
}
