package com.healthme.common.nio.client;

import org.apache.log4j.Logger;

import com.healthme.common.nio.ECGDataEndRequest;
import com.healthme.common.nio.ECGDataEndResponse;
import com.healthme.common.nio.ECGDataRequest;
import com.healthme.common.nio.ECGDataResponse;
import com.healthme.common.nio.HandleException;
import com.healthme.common.nio.HandlerCallback;
import com.healthme.common.nio.HandlerContext;
import com.healthme.common.nio.Message;
import com.healthme.common.nio.MessageHandler;
import com.healthme.common.nio.RegistrationRequest;
import com.healthme.common.nio.RegistrationResponse;
import com.healthme.common.nio.User;
import com.healthme.common.nio.socket.ChannelInfo;

public class ClientECGMessageHandler implements MessageHandler {
	Logger log = Logger.getLogger(getClass());
	private HandlerCallback callback;

	public ClientECGMessageHandler() {
		// TODO Auto-generated constructor stub
	}
	public ClientECGMessageHandler(HandlerCallback clientHandlerCallback) {
		this.callback = clientHandlerCallback;
	}

	public void initialize(HandlerContext context) {
	}

	public HandlerCallback getCallback() {
		return callback;
	}
	public void setCallback(HandlerCallback callback) {
		this.callback = callback;
	}
	@Override
	public void handle(Message message, ChannelInfo cinfo)
			throws HandleException {

		byte type = message.getMessageType();
		switch (type) {
		// register message
		case Message.REG_REQUEST:
			RegistrationRequest regMsg = (RegistrationRequest) (message.parse());
			handleRegistration(regMsg, cinfo);
			break;
		case Message.REG_RESPONSE:
			RegistrationResponse regRes = (RegistrationResponse) (message
					.parse());
			handleRegistrationResponse(regRes, cinfo);
			break;
		case Message.ECG_DATA_REQUEST:
			ECGDataRequest ecgMsg = (ECGDataRequest) (message.parse());
			handleEcgMessageRequest(ecgMsg, cinfo);
			break;
		case Message.ECG_DATA_RESPONSE:
			ECGDataResponse resp = (ECGDataResponse) (message.parse());
			handleEcgMessageResponse(resp, cinfo);
			break;
		case Message.ECG_END_REQUEST:
			ECGDataEndRequest endReq = (ECGDataEndRequest) (message.parse());
			handleEcgEndRequest(endReq, cinfo);
			break;
		case Message.ECG_END_RESPONSE:
			ECGDataEndResponse endRes = (ECGDataEndResponse) (message.parse());
			handleEcgEndResponse(endRes, cinfo);
		default:
			break;
		}

	}

	private void handleEcgMessageResponse(ECGDataResponse resp,
			ChannelInfo cinfo) {
		if (callback != null)
			callback.callback(resp);
	}
	private void handleEcgEndResponse(ECGDataEndResponse endRes,
			ChannelInfo cinfo) {
		log.info("finish transfering ecg data to client:" + cinfo.getRecvMode());
		cinfo.close();
	}

	private void handleEcgEndRequest(ECGDataEndRequest endReq, ChannelInfo cinfo) {
		log.info("not suppported!");
	}

	private void handleRegistrationResponse(RegistrationResponse regRes,
			ChannelInfo cinfo) {
		byte code = regRes.getCode();
		User u = new User();
		u.setName(regRes.getUserName());
		u.setPassword(regRes.getPassword());
		cinfo.setUser(u);
		if (callback != null)
			callback.callback(regRes);
	}

	private void handleEcgMessageRequest(ECGDataRequest ecgMsg, ChannelInfo cinfo) {
		log.info("not suppported!");
	}

	public void handleRegistration(RegistrationRequest reqMsg, ChannelInfo cinfo) {
		log.info("not suppported!");
	}

}
