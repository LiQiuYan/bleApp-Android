package com.healthme.common.nio.client;
import com.healthme.common.nio.ECGDataResponse;
import com.healthme.common.nio.HandlerCallback;
import com.healthme.common.nio.Message;
import com.healthme.common.nio.RegistrationResponse;

public class ClientHandlerCallback implements HandlerCallback {

	Object lock;

	public ClientHandlerCallback(Object lock) {
		this.lock = lock;
	}

	@Override
	public void callback(Message msg) {
		if(msg.getMessageType()==Message.REG_RESPONSE){
			synchronized (lock) {
				lock.notifyAll();
			}
			updateInfoByRegistrationResponse((RegistrationResponse)msg);
		}
		if (msg.getMessageType() == Message.ECG_DATA_RESPONSE) {
			updateInfoByDataResponse((ECGDataResponse)msg);
		}
	}

	public void updateInfoByDataResponse(ECGDataResponse response) {
		// TODO Auto-generated method stub
		
	}

	public void updateInfoByRegistrationResponse(RegistrationResponse response) {
		// TODO Auto-generated method stub
		
	}
	
}
