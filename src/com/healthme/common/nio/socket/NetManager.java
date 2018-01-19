package com.healthme.common.nio.socket;

import java.nio.ByteBuffer;
import java.util.List;

public class NetManager {

	private static NetManager singleton = null;
	private NetService netService = null;
	
	private NetManager() {
	}
	
	public static NetManager getInstance() {
		if (singleton == null) {
			singleton = new NetManager();
		}
		return singleton;
	}
	
	/* put NetService under one roof in NetManager
	 * All Channels (TCP server/client, UDP server) share the same thread model.
	 */
	public NetService getNetService() {
		return netService == null ? getNetServiceImpl() : netService;
	}
	
	synchronized private NetService getNetServiceImpl() {
		if (netService == null) {
			netService = new NetService();
			defaultNetService();
		}
		return netService;
	}
	
	private void defaultNetService() {
		netService.setDefaultRecvMode(NetService.RECVMODE_RECEIVE_ALL);
		netService.setTimeoutSec(0);		// no idle timeout for TCP connection
		ByteBuffer bf;
	}
}
