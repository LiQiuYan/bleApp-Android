package com.healthme.common.nio.socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.healthme.common.nio.Message;
import com.healthme.common.nio.User;

public class ChannelInfo implements ICheckMessage{
	public static final short TCP_CHANNEL = 0; // used to send and receive TCP
	public static final short TCP_SERVER = 1; // used to accept TCP
												// UDP_SERVER is readable

	public static final int DEFAULT_RECV_QUEUE_TIMEOUT = 2000; // in
																// milliseconds
	private final short channelType;
	private final SelectableChannel channel;
	private short recvMode = NetService.RECVMODE_RECEIVE_ALL;

	private final Dispatcher dispatcher;
	private final IAppHandler appHandler;
	private boolean isConnectionScoped = false; // only for TCP_SERVER,
												// UDP_SERVER
	private boolean flushAfterSend = true; // whether immediately flush after
											// send
	private final Object sendLock = new Object(); // exclusive lock to avoid
													// sending concurrently

	// timing
	private long lastActiveTime = 0;
	private long lastRecvQueueTime = 0; // time to put receive data back to
										// lastRecvQueue
	private long timeout; // 0 means no timeout, only meaningful for
							// TCP_CHANNEL, in milliseconds
	// 0 means no timeout, only meaningful for
	// NetService.RECVMODE_CHECK_BOUNDARY, in milliseconds
	private int recvQueueTimeout = DEFAULT_RECV_QUEUE_TIMEOUT;

	// send, receive queue to hold send and receive data
	// lastRecv queue for client to return receive data because of receive
	// premature
	private final Queue<ByteBuffer> sendQueue = new ConcurrentLinkedQueue<ByteBuffer>();
	private final Queue<ByteBuffer> receiveQueue = new ConcurrentLinkedQueue<ByteBuffer>();
	private final LinkedList<ByteBuffer> lastRecvQueue = new LinkedList<ByteBuffer>();

	private User user;
	
	private Object customizedObject = null;
	private String _remoteAddress = null;

	
	Logger log=Logger.getLogger(getClass());
	private Object session;
//	private ECGSession session;
	// constructor for TCP_CHANNEL
	public ChannelInfo(SocketChannel channel, IAppHandler appHandler,
			Dispatcher dispatcher) {
		channelType = TCP_CHANNEL;
		this.channel = channel;
		this.appHandler = appHandler;
		this.dispatcher = dispatcher;
		remoteAddress();
		lastActiveTime = System.currentTimeMillis();
	}

	// constructor for TCP_SERVER ServerSocketChannel
	public ChannelInfo(ServerSocketChannel channel, IAppHandler appHandler,
			Dispatcher dispatcher, boolean isConnectionScoped) {
		channelType = TCP_SERVER;
		this.channel = channel;
		this.appHandler = appHandler;
		this.dispatcher = dispatcher;
		this.isConnectionScoped = isConnectionScoped;
		remoteAddress();
		lastActiveTime = System.currentTimeMillis();
	}

	/*
	 * Channel can only be one of channels
	 */
	public short getChannelType() {
		return channelType;
	}

	public SelectableChannel getSelectableChannel() {
		return channel;
	}

	public SocketChannel getChannel() {
		return channelType == TCP_CHANNEL ? (SocketChannel) channel : null;
	}

	public ServerSocketChannel getServerChannel() {
		return channelType == TCP_SERVER ? (ServerSocketChannel) channel : null;
	}

	public IAppHandler getAppHandler() {
		return appHandler;
	}

	public Dispatcher getDispatcher() {
		return dispatcher;
	}

	public boolean getAppHandlerScoped() {
		return isConnectionScoped;
	}

	// timing control
	public void resetTimer() {
		lastActiveTime = System.currentTimeMillis();
	}

	public void setRecvMode(short recvMode) {
		this.recvMode = recvMode;
	}

	public short getRecvMode() {
		return recvMode;
	}

	public void setTimeoutSec(int timeoutSec) {
		this.timeout = timeoutSec * 1000;
	}

	public int getTimeoutSec() {
		return (int) (timeout / 1000);
	}

	public void setFlushAfterSend(boolean willFlush) {
		flushAfterSend = willFlush;
	}

	public boolean getFlushAfterSend() {
		return flushAfterSend;
	}

	// customized object can be used between callback
	public Object getCustomizedObject() {
		return customizedObject;
	}

	public void setCustomizedObject(Object obj) {
		customizedObject = obj;
	}

	// tell whether or not this channel's network socket is connected
	public boolean isConnected() {
		switch (channelType) {
		case TCP_CHANNEL:
			return ((SocketChannel) channel).isConnected();
		case TCP_SERVER:
			return ((ServerSocketChannel) channel).isOpen();
		}
		return false;
	}

	public boolean hasReadableData() {
		return !receiveQueue.isEmpty() || !lastRecvQueue.isEmpty();
	}

	public boolean hasWriteableData() {
		return !sendQueue.isEmpty();
	}

	// connection info: remote socket address
	public InetSocketAddress getRemoteAddress() {
		try {
			switch (channelType) {
			case TCP_CHANNEL:
				return (InetSocketAddress) ((SocketChannel) channel).socket()
						.getRemoteSocketAddress();
			case TCP_SERVER: // return local socket address
				return (InetSocketAddress) ((ServerSocketChannel) channel)
						.socket().getLocalSocketAddress();
			}
		} catch (Exception ex) {
			// ignore RuntimeException
		}
		return null;
	}

	// connection info: local socket address
	public InetSocketAddress getLocalAddress() {
		try {
			switch (channelType) {
			case TCP_CHANNEL:
				return (InetSocketAddress) ((SocketChannel) channel).socket()
						.getLocalSocketAddress();
			case TCP_SERVER: // return local socket address
				return (InetSocketAddress) ((ServerSocketChannel) channel)
						.socket().getLocalSocketAddress();
			}
		} catch (Exception ex) {
			// ignore RuntimeException
		}
		return null;
	}

	public void close() {
		try {
			dispatcher.deregister(this);
		} catch (Exception ignore) {
		}
	}

	// if no timeout set, return -1. otherwise, return time left in millisecond
	// before timeout
	public long timeLeft() {
		if (channelType != TCP_CHANNEL)
			return -1;
		long t1 = timeout <= 0 ? -1 : Math.max(0,
				timeout - System.currentTimeMillis() + lastActiveTime);
		long t2 = recvQueueTimeout <= 0 || lastRecvQueue.isEmpty()
				|| lastRecvQueueTime <= 0 ? -1 : Math.max(0, recvQueueTimeout
				- System.currentTimeMillis() + lastRecvQueueTime);
		return t2 == -1 || t1 != -1 && t1 < t2 ? t1 : t2;
	}

	public void send(byte[] bytes) throws IOException {
		if (bytes != null && bytes.length > 0) {
			synchronized (sendLock) {
				sendQueue.offer(ByteBuffer.wrap(bytes));
				if (flushAfterSend)
					flush();
			}
		}
	}

	// send object in string
	public void send(Object obj) throws IOException {
		send(obj == null ? null : obj.toString().getBytes());
	}

	public void send(InputStream in) throws IOException {
		byte[] buf = new byte[in.available()];
		in.read(buf);
		send(buf);
	}

	// send by Stream Object
	public void sendStreamObject(Object obj) throws IOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new ObjectOutputStream(out).writeObject(obj);
			send(out.toByteArray());
		} catch (Exception ex) {
			log.error("failed to send stream object",ex);
		}
	}

	public void sendStream(ByteArrayOutputStream out) throws IOException {
		try {
			send(out.toByteArray());
		} catch (Exception ex) {
			log.error("failed to send stream",ex);
		}
	}
	// flush send queue and start to send data if exist
	public void flush() throws IOException {
		if (!sendQueue.isEmpty()) {
			// dispatcher.announceWriteNeed(this); // scheduled by dispatcher
			// main selector thread
			dispatcher.writeNow(this); // use current thread
		}
	}

	public String toString() {
		switch (channelType) {
		case TCP_CHANNEL:
			return "channel->" + _remoteAddress;
		case TCP_SERVER:
			return "server channel " + _remoteAddress;
		}
		return "channel";
	}

	public ByteBuffer[] drainSendQueue() {
		ByteBuffer buf;
		ArrayList<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
		while ((buf = sendQueue.poll()) != null) {
			buffers.add(buf);
		}
		return buffers.toArray(new ByteBuffer[buffers.size()]);
	}

	/*
	 * receive data from receive queue, return null if no data available all
	 * receive methods are synchronized because lastRecvQueue is not safe
	 * application should not call this method to receive data. Instead, get
	 * data from event handler
	 */
	public synchronized ByteBuffer receive() {
		return lastRecvQueue.isEmpty() ? receiveQueue.poll() : lastRecvQueue
				.pollFirst();
	}

	public synchronized ByteBuffer receiveAll() {
		ByteBuffer buf;
		ArrayList<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
		while ((buf = lastRecvQueue.pollFirst()) != null) { // first from
															// lastRecv queue
			buffers.add(buf);
		}
		while ((buf = receiveQueue.poll()) != null) { // then from receive queue
			buffers.add(buf);
		}

		if (buffers.size() == 0) {
			return null;
		} else if (buffers.size() == 1) {
			return buffers.get(0);
		}

		int size = 0;
		for (ByteBuffer b : buffers) {
			size += b.remaining();
		}
		if (size == 0) {
			return null;
		}

		buf = ByteBuffer.allocate(size);
		for (ByteBuffer b : buffers) {
			buf.put(b);
		}
		buf.rewind();
		return buf;
	}

	/* client should call this function to return premature receive data */
	public synchronized void returnReceive(ByteBuffer buf) {
		if (buf.remaining() == 0) {
			return;
		}
		lastRecvQueue.offerFirst(buf);
	}

	/*--- The following method are only intended for NIO socket internal use. 
	 *--- Application should not try to call these method.
	 */

	private void remoteAddress() {
		InetSocketAddress addr = getRemoteAddress();
		if (addr == null) {
			_remoteAddress = "";
		}
		String s = addr.toString();
		int idx = s.lastIndexOf('/');
		_remoteAddress = idx >= 0 ? s.substring(idx + 1) : s;
	}

	protected void putIntoReceiveQueue(ByteBuffer... buffers) {
		for (ByteBuffer buf : buffers) {
			if (buf != null && buf.hasRemaining()) {
				receiveQueue.offer(buf);
			}
		}
	}

	protected void putIntoSendQueue(ByteBuffer... buffers) {
		for (ByteBuffer buf : buffers) {
			if (buf != null && buf.hasRemaining()) {
				sendQueue.offer(buf);
			}
		}
	}

	protected void resetReturnQueueTime() {
		lastRecvQueueTime = System.currentTimeMillis();
	}

	protected boolean isReturnQueueEmpty() {
		return lastRecvQueue.isEmpty();
	}

	protected boolean isReturnQueueTimeout() {
		return lastRecvQueueTime > 0
				&& !lastRecvQueue.isEmpty()
				&& System.currentTimeMillis() - lastRecvQueueTime >= recvQueueTimeout;
	}

	protected synchronized void clearReturnQueue() {
		lastRecvQueue.clear();
	}

	@Override
	public List<Message> onCheck(ByteBuffer data) throws IOException {
		Message msg=null;
		int newFlag=0;
		List<Message> list=new ArrayList<Message>();
		byte[] buffArray = data.array();
		msg= new Message();
		byte type = data.get();
		msg.setMessageType(type);
		log.debug("message type is :"+type);
		for (int i = 0; i < data.capacity(); i++) {
			if(i==0){
			}
			
			//look up the end flag;
			if(buffArray[i]==0xD&&(++i)<buffArray.length&&buffArray[i]==0x0A){
				byte[] rawdata = Arrays.copyOfRange(buffArray, newFlag, i+1);
				msg.parseRawData(rawdata);
				list.add(msg);
				newFlag=i;
				
				//check if it still has next message.
				if(buffArray.length>=++i)
				msg=new Message();
				msg.setMessageType(buffArray[i]);
			}
		}
		
		//keep the rest body
		if( data.capacity()-1-newFlag>0){
			byte[] restArray = Arrays.copyOfRange(buffArray, newFlag+1, buffArray.length);
			if (isReturnQueueTimeout()) {
				// leave application to handle timeout data in the return queue
				log.warn("Drop timeout buffer:"+new String(data.array()));
			} else {
				ByteBuffer rest = ByteBuffer.wrap(restArray);
				rest.flip();
				log.debug("incomplete message received, return to the queue");
				returnReceive(rest);
				if (isReturnQueueEmpty() || list != null && !list.isEmpty()) {
					resetReturnQueueTime();
				}
				dispatcher.updateInterestSet(this, SelectionKey.OP_READ);
			}
			
		}
		return list;
	}
	
	
	public boolean isValid(){
		return (user!=null);
	}
	
	public void setUser(User user){
		this.user=user;
	}
	
	public User getUser(){
		return this.user;
	}

	public void setECGSession(Object session) {
		this.session=session;
	}
	
	public Object getECGSession(){
		return session;
	}
}
