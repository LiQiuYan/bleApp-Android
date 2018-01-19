package com.healthme.common.nio.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
public class NetService {
	// min and max number of threads in the worker pool
	public static final int DEFAULT_NUM_WORKERS = DispatcherPool.DEFAULT_DISPATCHER_POOL_SIZE * 2;
	// buffer size to receive TCP data
	public static final int DEFAULT_DATA_PACKET_BUFFER_SIZE = 1024; //8192;

	// define data receiving mode
	public static final short RECVMODE_RECEIVE_ALL = 0;				// invoke the handler for all available ByteBuffers
	public static final short RECVMODE_RECEIVE_ONE_BY_ONE = 1;		// default, invoke the handler per ByteBuffer
	public static final short RECVMODE_RECEIVE_STREAM_OBJECT = 2;	// stream object
	public static final short RECVMODE_CHECK_BOUNDARY = 3;			// check input boundary by the handler

	private static Logger log = Logger.getLogger(NetService.class);

	private boolean isRunning = false;

	// dispatcher management
	private DispatcherPool dispatcherPool = null;
	// dispatcherEventHandler is used by all dispatchers, only one instance, should be multithread safe
	private DispatcherEventHandler dispatcherEventHandler = null;
	// workerPool and its memoryManager are used by all dispatcherEventHandlers, should be multithread safe 
	private WorkerPool workerPool = null;

	// timeout
	private int timeoutSec = 0;

	// default parameters
	private short defaultRecvMode = RECVMODE_RECEIVE_ALL;
		
	// server object parameters
	private int numOfDispatchers = DispatcherPool.DEFAULT_DISPATCHER_POOL_SIZE;
	private int bufferSize = DEFAULT_DATA_PACKET_BUFFER_SIZE;

	// socket parameters
	private boolean soKeepAlive = false;
	private boolean soOOBInline = false;
	private boolean soReuseAddress = true;
	private int soLinger = -1;				// -1, disable
	private boolean soTcpNoDelay = false;		

//	private Map<String, ServiceInfoData> serviceInfo = new ConcurrentHashMap<String, ServiceInfoData>();

	// runtime statistics;
	private AtomicInteger numOfTcpServiceChannels = new AtomicInteger(0);
	private AtomicInteger numOfUdpServiceChannels = new AtomicInteger(0);
	private AtomicInteger numOfTcpClientChannels = new AtomicInteger(0);

	class ServiceInfoData {
		Dispatcher dispatcher;
		ChannelInfo cinfo;

		ServiceInfoData(Dispatcher dispatcher, ChannelInfo cinfo) {
			this.dispatcher = dispatcher;
			this.cinfo = cinfo;
		}
	}

	public NetService() {
		// initilize server objects
		workerPool = new WorkerPool(DEFAULT_NUM_WORKERS);
		dispatcherEventHandler = new DispatcherEventHandler(workerPool, bufferSize);
		dispatcherPool = new DispatcherPool(numOfDispatchers, dispatcherEventHandler);
	}

	public ChannelInfo addTCPService(int port, IAppHandler appHandler) throws IOException {
		return addTCPService(null, port, appHandler, false);
	}

	public ChannelInfo addTCPService(String intf, int port, IAppHandler appHandler, boolean isConnectionScoped) throws IOException {
		ServerSocketChannel serverChannel = null;
		ChannelInfo cinfo = null;
		try {
			start();

			// create a new server socket 
			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);

			// set TCP socket parameters
			serverChannel.socket().setReuseAddress(soReuseAddress);

			// and bind it to the port
			if (intf == null) {
				serverChannel.socket().bind(new InetSocketAddress(port));
			} else {
				serverChannel.socket().bind(new InetSocketAddress(intf, port));
			}

			Dispatcher dispatcher = dispatcherPool.nextDispatcher();
			cinfo = new ChannelInfo(serverChannel, appHandler, dispatcher, isConnectionScoped);
			cinfo.setTimeoutSec(timeoutSec);	// for TCP connection, not for acceptor
			cinfo.setRecvMode(defaultRecvMode);
			dispatcher.register(cinfo, SelectionKey.OP_ACCEPT);

//			serviceInfo.put("TCP:" + port, new ServiceInfoData(dispatcher, cinfo));
			numOfTcpServiceChannels.getAndIncrement();

		} catch (IOException be) {
			if (serverChannel != null) {
				serverChannel.close();
			}
			log.error("error occured while add TCP port " + port + ": " + be.getMessage());
			throw be;
		}
		return cinfo;
	}


	public ChannelInfo addTCPClient(String destHost, int destPort, IAppHandler appHandler) throws IOException {
		SocketChannel clientChannel = null;
		ChannelInfo cinfo = null;
		try {
//			cleanUpService();
			start();

//			clientChannel = SocketChannel.open(new InetSocketAddress(destHost, destPort));
			AsyncSocket socket=new AsyncSocket();
			clientChannel=socket.connect(new InetSocketAddress(destHost, destPort), 2000);
			clientChannel.configureBlocking(false);

			// set Socket parameters
			setSocketParams(clientChannel.socket());

			Dispatcher dispatcher = dispatcherPool.nextDispatcher();
			cinfo = new ChannelInfo(clientChannel, appHandler, dispatcher);
			cinfo.setRecvMode(defaultRecvMode);
			dispatcher.register(cinfo, SelectionKey.OP_READ);

//			serviceInfo.put("TCP Client:" + destHost + ":" + destPort, new ServiceInfoData(dispatcher, cinfo));
			numOfTcpClientChannels.getAndIncrement();

		} catch (IOException be) {
			if (clientChannel != null) {
				clientChannel.close();
			}
			log.error("error occured while connect to " + destHost + ":" + destPort + ": " + be.getMessage());
			throw be;
		}
		return cinfo;
	}

	public void setSocketParams(Socket socket) throws SocketException {
		socket.setKeepAlive(soKeepAlive);
		socket.setOOBInline(soOOBInline);
		socket.setReuseAddress(soReuseAddress);
		socket.setSoLinger(soLinger >= 0, soLinger);
		socket.setTcpNoDelay(soTcpNoDelay);
	}

	public void removeTCPService(int port) throws IOException {
//		boolean b = removeService("TCP", port);
//		if (b) numOfTcpServiceChannels.getAndDecrement();
//		return b;
	    numOfTcpServiceChannels.getAndDecrement();
	}

	public void removeUDPService(int port) throws IOException {
//		boolean b = removeService("UDP", port);
//		if (b) numOfUdpServiceChannels.getAndDecrement();
//		return b;
	    numOfUdpServiceChannels.getAndDecrement();
	}

	public void removeTCPClient(String destHost, int destPort) throws IOException {
//		boolean b = removeService("TCP Client:" + destHost, destPort);
//		if (b) numOfTcpClientChannels.getAndDecrement();
//		return b;
	    numOfTcpClientChannels.getAndDecrement();
	}

//	protected boolean removeService(String service, int port) throws IOException {
//		ServiceInfoData myService = (ServiceInfoData)serviceInfo.get(service + ":" + port);
//		if (myService == null) {
//			return false;
//		}
//		// close channel
//		myService.dispatcher.deregister(myService.cinfo);
//		myService.cinfo.getSelectableChannel().close();
//		serviceInfo.remove(service + ":" + port);
//		return true;
//	}

//	protected void cleanUpService() {
//		ChannelInfo cinfo;
//		for (Map.Entry<String, ServiceInfoData>entry: serviceInfo.entrySet()) {
//			cinfo = entry.getValue().cinfo;
//			if (cinfo.getChannelType() == ChannelInfo.TCP_CHANNEL && !cinfo.isConnected()) {
//				try {
//					entry.getValue().dispatcher.deregister(cinfo);
//					cinfo.getSelectableChannel().close();
//				} catch (Exception ignore) {
//				}
//				serviceInfo.remove(entry.getKey());
//				numOfTcpClientChannels.getAndDecrement();
//			}
//		}
//	}

//	public ChannelInfo getTCPServiceChannel(int port) throws IOException {
//		return getChannelInfo("TCP", port);
//	}
//
//	public ChannelInfo getUDPServiceChannel(int port) throws IOException {
//		return getChannelInfo("UDP", port);
//	}
//
//	public ChannelInfo getTCPClientChannel(String destHost, int destPort) throws IOException {
//		return getChannelInfo("TCP Client:" + destHost, destPort);
//	}

//	protected ChannelInfo getChannelInfo(String service, int port) throws IOException {
//		ServiceInfoData myService = (ServiceInfoData)serviceInfo.get(service + ":" + port);
//		return myService == null ? null : myService.cinfo;
//	}

	public String toString() {
//		cleanUpService();
		StringBuilder sb = new StringBuilder("Total ");
		int n = numOfTcpServiceChannels.intValue() + numOfUdpServiceChannels.intValue() + numOfTcpClientChannels.intValue();
		sb.append(n).append(" service").append(n > 1 ? "s" : "").append(" registered");
		if (n <= 0) {
			return sb.toString();
		}
		sb.append(": ");

		if (numOfTcpServiceChannels.intValue() > 0) {
			sb.append(numOfTcpServiceChannels.intValue()).append(" tcp, ");
		}
		if (numOfUdpServiceChannels.intValue() > 0) {
			sb.append(numOfUdpServiceChannels.intValue()).append(" udp, ");
		}
		if (numOfTcpClientChannels.intValue() > 0) {
			sb.append(numOfTcpClientChannels.intValue()).append(" client, ");
		}
		sb.setLength(sb.length()-2);

		return sb.toString();
	}

	/* start up Multiple Net Service */
	synchronized public final void start() throws IOException {
		if (isRunning) {
			return;
		}

		dispatcherPool.run();
		isRunning = true;
	}

	/**
	 * shutdown   
	 */
	synchronized public final void shutdown() {
		if (isRunning) {
			isRunning = false;

			log.info("closing net service");
			dispatcherPool.shutdown();
		}
	}

	/**
	 * set the idle timeout in sec for socket
	 * 
	 * @param timeout idle timeout in sec
	 */
	public void setTimeoutSec(int timeoutSec) {
		this.timeoutSec = Math.max(0, timeoutSec);
	}


	/**
	 * get the idle timeout in sec for socket
	 * 
	 * @return the idle timeout in sec
	 */
	public int getTimeoutSec() {
		return timeoutSec;
	}

	public DispatcherPool getDispatcherPool() { return dispatcherPool; }

	public WorkerPool getWorkerPool() { return workerPool; }
	
	public void setNumOfWorkerThreads(int numThreads) {
		if (numThreads < 0) {
			numThreads = DEFAULT_NUM_WORKERS;
		}
		workerPool.setCorePoolSize(numThreads);
	}

	public void setNumOfDispatchers(int numDispatchers) {
		numOfDispatchers = numDispatchers;
		if (dispatcherPool != null) {
			dispatcherPool.setSize(numDispatchers);
		}
	}

	public void setBufferSize(int size) {
		bufferSize = size;
		if (dispatcherEventHandler != null) {
			dispatcherEventHandler.setBufferSize(size);
		}
	}

	public boolean isRunning() { return isRunning; }

	public void setDefaultRecvMode(short mode) { this.defaultRecvMode = mode; }
	public short getDefaultRecvMode() { return defaultRecvMode; }


	// Socket parameters
	public void setSoKeepAlive(boolean on) { soKeepAlive = on; }
	public boolean getSoKeepAlive() { return soKeepAlive; }

	public void setSoOOBInline(boolean on) { soOOBInline = on; }
	public boolean getSoOOBInline() { return soOOBInline; }

	public void setSoReuseAddress(boolean on) { soReuseAddress = on; }
	public boolean getSoReuseAddress()	 { return soReuseAddress; }

	public void setSoLinger(int value) { soLinger = value; }
	public int getSoLinger() { return soLinger; }

	public void setSoTcpNoDelay(boolean on) { soTcpNoDelay = on; }
	public boolean getSoTcpNoDelay()	 { return soTcpNoDelay; }

//	protected Map<String, ServiceInfoData> getServiceInfo() { return serviceInfo; }
}
