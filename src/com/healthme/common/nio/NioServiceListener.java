package com.healthme.common.nio;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.healthme.common.nio.socket.ChannelInfo;
import com.healthme.common.nio.socket.ListenerConfiguration;
import com.healthme.common.nio.socket.NetManager;
import com.healthme.common.nio.socket.NetService;

public class NioServiceListener implements ECGListener {
	private static Logger log = Logger.getLogger(NioServiceListener.class);

	protected String host;
	protected int port;
	protected int threads;	// max number of threads
	protected MessageHandler handler;

	protected static NetService netService = null;

	public void initialize(ListenerConfiguration config, MessageHandler handler)
	throws UnknownHostException{
		host = config.getHost();
		port = config.getPort();
		threads = config.getThreads();
		this.handler = handler;
		NioECGServiceHandler appHandler = new NioECGServiceHandler(handler);

		if( log.isDebugEnabled() )
			log.debug("Initializing listener on host " + this.host + " and port " + config.getPort());
		try {
			netService = NetManager.getInstance().getNetService();
			netService.setDefaultRecvMode(NetService.RECVMODE_CHECK_BOUNDARY);
			netService.setNumOfWorkerThreads(threads);
			netService.setTimeoutSec(0);		// no idle timeout for TCP connection

			// register TCP service on host:port
			ChannelInfo cinfo = netService.addTCPService(host, port, appHandler, false);
			cinfo.setRecvMode(NetService.RECVMODE_CHECK_BOUNDARY);

		} catch (IOException ex) {
			log.error("Cannot create TCP listenter on " + host + ":" + port, ex);
			throw new UnknownHostException("Cannot create TCP listenter on " + host + ":" + port);
		}
	}

	public String getHost() { return host; }
	public int getPort() { return port; }
	
	public boolean isStarted() { return netService.isRunning(); }

	public void start() {
		try {
			netService.start();
		} catch (IOException ex) {
			log.error("Cannot start listener", ex);
		}
	}

	public void stop() {
		netService.shutdown();
	}
}
