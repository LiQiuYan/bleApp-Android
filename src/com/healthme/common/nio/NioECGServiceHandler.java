package com.healthme.common.nio;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.healthme.common.nio.socket.ChannelInfo;
import com.healthme.common.nio.socket.IAppHandler;

public class NioECGServiceHandler implements IAppHandler {
	private static Logger log = Logger.getLogger(NioECGServiceHandler.class);

	protected MessageHandler handler = null;
	
	private HandlerCallback callback=null;

	public NioECGServiceHandler(MessageHandler handler) {
		this.handler = handler;
	}
	
	public NioECGServiceHandler(MessageHandler handler,HandlerCallback callback) {
		this.handler = handler;
		this.callback=callback;
	}

	public boolean onData(ChannelInfo cinfo, Message data) {
		NioSender ecgSender = new NioSender(cinfo);
		try {
			handler.handle(data, cinfo);
		} catch (HandleException ex) {
			int code = ex.errorCode;
			switch (code) {
			case HandleException.ERROR_INVALIDE_MESSAGE:

				break;

			case HandleException.ERROR_LOGIN_MESSAGE:

				break;
			case HandleException.ERROR_UNKNOWN_DEVICE:

				break;

			default:
				break;
			}
		}

		return true;
	}

	// not used
	public boolean onConnect(ChannelInfo cinfo) throws Exception {
		return true;
	}

	// not used
	public boolean onDisconnect(ChannelInfo cinfo) throws Exception {
		return true;
	}

	private void sendInvalidResponse(NioSender sender, String reason,
			ByteBuffer data) {
	}

}
