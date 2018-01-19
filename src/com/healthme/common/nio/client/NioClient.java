package com.healthme.common.nio.client;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.healthme.common.nio.Message;
import com.healthme.common.nio.MessageHandler;
import com.healthme.common.nio.NioSender;
import com.healthme.common.nio.Request;
import com.healthme.common.nio.Response;
import com.healthme.common.nio.socket.ChannelInfo;
import com.healthme.common.nio.socket.IAppHandler;
import com.healthme.common.nio.socket.NetManager;
import com.healthme.common.nio.socket.NetService;

public class NioClient {
	private static Logger log = Logger.getLogger(NioClient.class);

	protected ChannelInfo cinfo = null;
	protected NetService netService = null;

	private String host;
	private int port;
	private MessageHandler handler = null;

	private Map<String, RequestData> requests = new ConcurrentHashMap<String, RequestData>();

	public NioClient(String host, int port) {
		this.host = host;
		this.port = port;
		netService = NetManager.getInstance().getNetService();
	}

	public NioClient(String host, int port, MessageHandler handler) {
		this.host = host;
		this.port = port;
		this.handler = handler;
		netService = NetManager.getInstance().getNetService();
	}

	public void connect() throws IOException {
		if (cinfo != null && cinfo.isConnected()) {
			return;
		}
		cinfo = netService.addTCPClient(host, port, new ECGClientHandler());
	}

	protected String getKey(Request mesg) {
		return String.valueOf(mesg.hashCode());
	}

	public void send(Request request) throws IOException {
		connect();
		if (handler == null) {
			String key = getKey(request);
			if (requests.containsKey(key)) {
				throw new IOException("duplicate CSeq " + key);
			}
			requests.put(key, new RequestData());
		}
		cinfo.send(request);
	}

	public Response receive(Request request) throws IOException {
		return receive(request, 0);
	}
	
	public Response receive(Request request, int timeoutMilliSecs) throws IOException {
		if (handler != null) {
			throw new IllegalArgumentException("this method can only be used in synchronous mode");
		}
		if (timeoutMilliSecs < 0) {
			timeoutMilliSecs = 0;
		}
		
		String key = getKey(request);
		RequestData requestData = requests.get(key);
		if (requestData == null) {
			throw new IOException("invalid request");
		}

		synchronized(requestData) {
			if (requestData.response != null) {
				return requestData.response;
			}
			try {
				requestData.wait(timeoutMilliSecs);
			} catch (Exception ignore) {
			}
		}
		
		Response response = requestData.response;
		requests.remove(key);
		if (response == null && timeoutMilliSecs > 0) {
			throw new SocketTimeoutException("socket timeout after " + timeoutMilliSecs + " for cseq: " + key);
		}

		return response;
	}

	public Response sendAndReceive(Request request, int timeoutMilliSecs) throws IOException {
		if (handler != null) {
			throw new IllegalArgumentException("this method can only be used in synchronous mode");
		}

		send(request);
		return receive(request, timeoutMilliSecs);
	}

	public void close() {
		try {
			cinfo.close();
			netService.removeTCPClient(host, port);
		} catch (Exception ignore) {
		}
		cinfo = null;
	}

	@Override
	public String toString() {
		return "NioClient [cinfo=" + cinfo + ", netService=" + netService
				+ ", host=" + host + ", port=" + port + ", handler=" + handler
				+ ", requests=" + requests + "]";
	}
	
	class RequestData {
		Response response = null;
		long createTime;

		public RequestData() {
			createTime = System.currentTimeMillis();
		}
	}

	class ECGClientHandler implements IAppHandler{
		public boolean onData(ChannelInfo cinfo, Message data) {
			NioSender sender = new NioSender(cinfo);
//			cinfo.onCheck(buff);
			try {

			} catch (Exception ex) {
				if (log.isDebugEnabled()) {
					log.error("parsing request error: " + ex.getMessage() + "\n" + data, ex);
				}
				return false;
			}
			return true;
		}

	}
	
	public static void main(String[] args) {
	}
}
