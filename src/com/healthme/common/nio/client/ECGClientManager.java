
package com.healthme.common.nio.client;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.healthme.common.nio.ECGDataEndRequest;
import com.healthme.common.nio.ECGDataRequest;
import com.healthme.common.nio.HandlerCallback;
import com.healthme.common.nio.Message;
import com.healthme.common.nio.NioECGServiceHandler;
import com.healthme.common.nio.RegistrationRequest;
import com.healthme.common.nio.socket.ChannelInfo;
import com.healthme.common.nio.socket.NetManager;
import com.healthme.common.nio.socket.NetService;

public class ECGClientManager {

	private static final Logger log = Logger.getLogger(ECGClientManager.class);
	private static ECGClientManager singleton = null;

	private ChannelInfo ecgConnection = null;
	private NioECGServiceHandler handler = null;
	private int retryTimes = 2;
	private String host = "localhost";
	private int port = 9876;
	private ClientECGMessageHandler messageHandler;
	private Object syncLock=new Object();
	private boolean isClose;

	private ECGClientManager() {
		retryTimes = 3;
		messageHandler=new ClientECGMessageHandler();
		handler = new NioECGServiceHandler(messageHandler);
	}

	public static ECGClientManager getInstance() {
		if (singleton == null) {
			singleton = new ECGClientManager();
		}
		return singleton;
	}

	
	public HandlerCallback getHandlerCallback() {
		return messageHandler.getCallback();
	}

	public void setHandlerCallback(HandlerCallback handlerCallback) {
		this.messageHandler.setCallback(handlerCallback);
	}

	public static String setRequestUrl(String str, String host, int port) {
		if (StringUtils.isNotBlank(host)) {
			str = StringUtils.replace(str, "${Server}", host);
		}
		if (port > 0) {
			str = StringUtils.replace(str, "${Port}", String.valueOf(port));
		}
		return str;
	}

	public void send(Message message) throws IOException {
		// set to default if not set
		log.info("sending message...");
		if (StringUtils.isEmpty(host) || port <= 0) {
			throw new IllegalArgumentException("Invalid ECG Server " + host
					+ " or port " + port);
		}

		// re-connect if possible
		if(isClose){
			throw new IOException("channel has already been closed!");
		}
		ChannelInfo cinfo = getChannelInfo();
		log.info("find the connection:"+cinfo.getRemoteAddress());
		if (cinfo == null) {
			throw new IOException("Cannot connect to ECG Server");
		}

		// retry if failed
		for (int i = 0; i < retryTimes; i++) {
			try {
				cinfo.send(message.getBytes());
				log.info("send message:\r\n" + message + " to the host " + host
						+ " over the port " + port);
				break;

			} catch (IOException ex) {
				if (i >= retryTimes - 1) {
					throw ex;
				}
				cinfo.close();
				NetManager.getInstance().getNetService()
						.removeTCPClient(host, port);
				cinfo = getChannelInfo();
			}
		}
	}

	protected String getKeyName(String host, int port) {
		return host + ":" + port;
	}

	public ChannelInfo getChannelInfo() throws IOException {
		if (ecgConnection != null) {
			if (ecgConnection.isConnected()) {
				return ecgConnection;
			} else {
				NetManager.getInstance().getNetService()
						.removeTCPClient(host, port);
			}
		}

		// connect or re-connect to ECG Server
		ecgConnection = connectToECGServer(host, port);
		if (ecgConnection.getChannel() == null || !ecgConnection.isConnected()) {
			throw new ConnectException();
		}
		return ecgConnection;
	}

	public boolean isValid(){
		return (ecgConnection!=null&&ecgConnection.isValid());
	}
	synchronized private ChannelInfo connectToECGServer(String host,
			int port) {
		// check race condition for multi-threads
		if (ecgConnection != null && ecgConnection.isConnected()) {
			return ecgConnection;
		}

		if (log.isDebugEnabled())
			log.debug("connect to ECG server: " + host + ":" + port);
		NetService netService = NetManager.getInstance().getNetService();
		netService.setDefaultRecvMode(NetService.RECVMODE_CHECK_BOUNDARY);
		try {
			ecgConnection = netService.addTCPClient(host, port, handler);
		} catch (IOException e) {
			log.warn("create a connection:" + host + ":" + port, e);
		}
		return ecgConnection;
	}
	
	
	/**
	 * 
	 * @param userName
	 * @param password
	 * @param host
	 * @param port
	 * @return
	 * @throws IOException
	 */
	public boolean login(String userName, String password, String host, int port)
			throws IOException {
		byte dev = 0x1;
		this.host=host;
		isClose=false;
		log.info("login...");
		RegistrationRequest request = new RegistrationRequest();
		request.setDeviceType(dev);
		request.setUserName(userName);
		request.setPassword(password);
		send(request);

		boolean result = false;
		try {
			for (int i = 0; i < 5; i++) {
				ChannelInfo channel = getChannelInfo();
				if (channel != null && channel.isValid()) {
					result = true;
					break;
				}
				synchronized (syncLock) {
					syncLock.wait(1000);
				}
			}
		} catch (InterruptedException e) {
			log.warn(e);
		}
		isClose=false;
		return result;
	}

	static List<Short> buildECGData() {
		ArrayList<Short> al = new ArrayList<Short>();
		al.add(new Short("1"));
		al.add(new Short("2"));
		al.add(new Short("3"));
		al.add(new Short("3"));
		al.add(new Short("4"));
		return al;
	}

	public void sendEcgData(List<Short> al) throws IOException {
		ECGDataRequest ecgMessage = new ECGDataRequest();
		ecgMessage.setEcgData(al);
		ecgMessage.setStartTime(new Date().getTime());
		send(ecgMessage);
	}

	public static void main(String[] args) {
		ECGClientManager client = getInstance();
		try {
			// 1 login and register channel
			long t1 = System.currentTimeMillis();
			boolean res = client.login("admin", "admin", "localhost", 9876);
			long t2 = System.currentTimeMillis();
			System.out.println(t2 - t1 + " login result " + res);

			// 2 send ecg data
			List<Short> date = buildECGData();
			client.sendEcgData(date);

			Thread.sleep(10000);

			ECGDataEndRequest end = new ECGDataEndRequest();
			client.send(end);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() throws IOException{
		isClose=true;
		ChannelInfo c = getChannelInfo();
		if(c==null&&c.isConnected()){
			c.close();
		}
				
	}
}

