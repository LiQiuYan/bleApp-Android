package com.healthme.common.nio.socket;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import org.apache.log4j.Logger;

public class Dispatcher implements Runnable {
	private static Logger log = Logger.getLogger(Dispatcher.class);

	// connection handling
	private final Selector selector;

	// event handler
	private final DispatcherEventHandler dispatcherEventHandler;
	private final DispatcherPool dispatcherPool;

	// is open flag
	private volatile boolean isRunning = true;
	private final String dispatcherName;

	// guard object for synchronizing
	private final Object dispatcherThreadGuard = new Object();

	/**
	 * constructor
	 * 
	 * @param eventHandler    the assigned event handler
	 */
	public Dispatcher(DispatcherPool dispatcherPool, DispatcherEventHandler dispatcherEventHandler, String name)  {
		this.dispatcherPool = dispatcherPool;
		this.dispatcherEventHandler = dispatcherEventHandler;
		this.dispatcherName = name;

		try {
			selector = Selector.open();
		} catch (IOException ioe) {
			String text = "exception occured while opening selector. Reason: " + ioe.toString();
			log.error(text);
			throw new RuntimeException(text, ioe);
		}
	}

	/**
	 * {@inheritDoc}
	 * register connection in this selector. 
	 * it can be de-registered either by conn.close() or by deregister()
	 */
	public void register(ChannelInfo cinfo, int ops) throws IOException {
		if( log.isDebugEnabled() )
			log.debug(dispatcherName + " registers [" + cinfo + "]");

		synchronized (dispatcherThreadGuard) {
			selector.wakeup();

			cinfo.getSelectableChannel().register(selector, ops, cinfo);
		}

		if (cinfo.getChannelType() == ChannelInfo.TCP_CHANNEL) {
			dispatcherEventHandler.onHandleConnectEvent(cinfo);
		}
	}


	/** deregister from selector and close the channel */
	public void deregister(final ChannelInfo cinfo) throws IOException {
		if( log.isDebugEnabled() )
			log.debug(dispatcherName + " de-registers [" + cinfo + "]");

		boolean b = false;
		synchronized (dispatcherThreadGuard) {
			selector.wakeup();

			SelectionKey key = cinfo.getSelectableChannel().keyFor(selector);
			if (key != null && key.isValid()) {
				key.cancel();
				b = true;
			}
		}

		if (b && cinfo.getChannelType() == ChannelInfo.TCP_CHANNEL) {
			dispatcherEventHandler.onHandleDisconnectEvent(cinfo);
		}
	}

	public Set<ChannelInfo> getChannels() {

		Set<ChannelInfo> registered = new HashSet<ChannelInfo>();

		if (selector != null) {
			SelectionKey[] selKeys = null;
			synchronized (dispatcherThreadGuard) {
				selector.wakeup();

				Set<SelectionKey> keySet = selector.keys();
				selKeys = keySet.toArray(new SelectionKey[keySet.size()]);
			}

			for (int i = 0; selKeys != null && i < selKeys.length; i++) {
				registered.add((ChannelInfo)selKeys[i].attachment());
			}
		}

		return registered;
	}

	/* return the number of channels registered in this register.
	 * return -1 if this dispatcher is closed.
	 */
	public int getNumOfChannels() {
//		if(selector.isOpen())
//			return selector.keys().size();
		return 1;
//		else
//			return -1;
	}

	/* announce write needed, so data in the sendQueue can be sent
	 * when network is ready for writing.
	 * @exception -- the channel is invalid (probably is closed)
	 */
	public final void announceWriteNeed(ChannelInfo cinfo) throws IOException {
		SelectionKey key = cinfo.getSelectableChannel().keyFor(selector);
		if (key == null || !key.isValid()) {
			throw new SocketException("channel is invalid");
		}

		synchronized (dispatcherThreadGuard) {
			selector.wakeup();
			key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
		}
	}

	/* clean current interest set and set it to the specified ops value
	 * 
	 * @exception -- the channel is invalid
	 */
	public final void updateInterestSet(ChannelInfo cinfo, int ops) throws IOException {
		SelectionKey key = cinfo.getSelectableChannel().keyFor(selector);
		if (key == null || !key.isValid()) {
			throw new SocketException("channel is invalid");
		}

		synchronized (dispatcherThreadGuard) {
			key.selector().wakeup();	
			key.interestOps(ops);
		}
	}

	/* send data in the sendQueue right now.
	 * Unlike announceWriteNeed which uses selector main thread to send data, this method
	 * uses current thread to send data. If it can't send out all data because 
	 * network is too slow, it will switch to selector main thread.
	 */
	public final void writeNow(ChannelInfo cinfo) throws IOException {
		if (!cinfo.isConnected()) {
			throw new SocketException("channel is invalid");
		}
		dispatcherEventHandler.onHandleWriteableEvent(cinfo);
	}

	public final void run() {
		log.info(dispatcherName + " selector listening ...");

		while(isRunning) {
			try {
				long sleeptime = 0; 

				// see http://developers.sun.com/learning/javaoneonline/2006/coreplatform/TS-1315.pdf
				synchronized (dispatcherThreadGuard) { 
					/* suspend the dispatcher thread */	

					ChannelInfo cinfo;
					long t;
					for (SelectionKey sk : selector.keys()) {
						cinfo = (ChannelInfo)sk.attachment();
						t = cinfo.timeLeft();

						if (t < 0) {			// no timeout set
							continue;
						} else if (t == 0) {	// already timeout
							if (cinfo.isReturnQueueTimeout()) {
								// handle return queue timeout
								dispatcherEventHandler.onHandleReadableEvent(cinfo);
							} 
							// @by JichaoWu, AMZ-1489, cinfo is handled by multiple threads, so it's lastRecvQueueTime has 
							// possiblly been changed between cinfo.timeLeft() and cinfo.isReturnQueueTimeout()
							// so we need to judge if cinfo has timeout settings before processing timeout
							else if(cinfo.getTimeoutSec() > 0) {
								// handle channel timeout
								dispatcherEventHandler.onHandleTimeoutEvent(cinfo);
							}
						} else {
							sleeptime = sleeptime == 0 ? (t + 20) : Math.min(t + 20, sleeptime);
						}
					}
				}

				// waiting for new events (data, ...)
				int eventCount = selector.select(sleeptime);

				// handle read write events
				if (eventCount > 0) {
					Set<SelectionKey> selectedEventKeys = selector.selectedKeys();
					Iterator<SelectionKey> it = selectedEventKeys.iterator();

					// handle read & write
					while (it.hasNext()) {
						SelectionKey eventKey = it.next();
						it.remove();

						ChannelInfo cinfo = (ChannelInfo) eventKey.attachment();
						if (log.isDebugEnabled()) {
							log.debug(dispatcherName + " detects " + getEventName(eventKey.readyOps()) + " event on [" + cinfo + "]");
						}

						// read data
						if (eventKey.isValid() && eventKey.isReadable()) {
							// notify event handler
							dispatcherEventHandler.onHandleReadableEvent(cinfo);
						}

						// write data
						if (eventKey.isValid() && eventKey.isWritable()) {
							// notify event handler
							dispatcherEventHandler.onHandleWriteableEvent(cinfo);
						}			

						// accept connection
						if (eventKey.isValid() && eventKey.isAcceptable()) {
							// notify event handler
							dispatcherEventHandler.onHandleAcceptableEvent(cinfo);
						}			
					}
				}

			} catch (Throwable e) {
				log.warn("exception occured while processing: " + e.getMessage());
			}
		}

		closeDispatcher();
	}

	private String getEventName(int op) {
		StringBuilder sb = new StringBuilder();
		if ((op & SelectionKey.OP_ACCEPT) != 0) sb.append("ACCEPT|");
		if ((op & SelectionKey.OP_CONNECT) != 0) sb.append("CONNECT|");
		if ((op & SelectionKey.OP_READ) != 0) sb.append("READ|");
		if ((op & SelectionKey.OP_WRITE) != 0) sb.append("WRITE|");
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	private void closeDispatcher() {
		if( log.isDebugEnabled() )
			log.debug("closing " + dispatcherName);

		if (selector != null) {
			synchronized (dispatcherThreadGuard) {
				selector.wakeup();

				for (SelectionKey sk : selector.keys()) {
					try {
						ChannelInfo cinfo = (ChannelInfo) sk.attachment();
						dispatcherEventHandler.onHandleDisconnectEvent(cinfo);

					} catch (Exception ignore) {
						// ignore
					}
				}
			}

			try {
				selector.close();
			} catch (Exception e) {
				log.info("error occured by close selector within tearDown: " + e.getMessage());
			}
		}
	}

	public void shutdown() {
		if (isRunning) {
			isRunning = false;

			if (selector != null) {
				// wake up selector, so that isRunning-loop can be terminated
				selector.wakeup();
			}
		}
	}

	public DispatcherPool getDispatcherPool() { return dispatcherPool; }
	public DispatcherEventHandler getDispatcherEventHandler() { return dispatcherEventHandler; }

	public String toString() {
		return dispatcherName + "[" + selector.keys().size() + " registered]" + (selector.isOpen() ? "" : " closed");
	}
}
