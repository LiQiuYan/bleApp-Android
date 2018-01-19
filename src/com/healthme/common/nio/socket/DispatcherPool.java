package com.healthme.common.nio.socket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;

public class DispatcherPool {
	public static final int DEFAULT_DISPATCHER_POOL_SIZE = 2;

	private static Logger log = Logger.getLogger(DispatcherPool.class);
	private boolean isRunning = true;

	// worker pool and its memory manager used by dispatcher
	private DispatcherEventHandler dispatcherEventHandler = null;

	// dispatcher management
	private final List<Dispatcher> dispatchers = new CopyOnWriteArrayList<Dispatcher>();
	private int size = 0;

	public DispatcherPool(int size, DispatcherEventHandler dispatcherEventHandler) {
		this.size = size <= 0 ? DEFAULT_DISPATCHER_POOL_SIZE : size;
		this.dispatcherEventHandler = dispatcherEventHandler;
	}

	// normal start, called by Acceptor
	public void run() {
		isRunning = true;
		updateDispatcher();
	}

	public List<Dispatcher> getDispatchers() {
		return dispatchers;
	}

	public int getSize() {
		return size;
	}

	public synchronized void setSize(int size) {
		int oldsize = this.size;
		this.size = size;
		if (oldsize != size) {
			updateDispatcher();
		}
	}

	private synchronized void updateDispatcher() {
		if (isRunning) {
			int currentRunning = dispatchers.size();

			if (currentRunning != size) {
				if (currentRunning > size) {
					for (int i = size; i <  currentRunning; i++) {
						Dispatcher dispatcher = dispatchers.remove(dispatchers.size()-1);
						dispatcher.shutdown();
					}

				} else if ( currentRunning < size) {
					for (int i = currentRunning; i < size; i++) {
						String name = "Dispatcher#" + i;
						Dispatcher dispatcher = new Dispatcher(this, dispatcherEventHandler, name);
						dispatchers.add(dispatcher);

						// start Dispatcher thread
						log.info("Start " + name + " thread(dispatcherEventHandler=" + dispatcherEventHandler + ")");
						Thread t = new Thread(dispatcher);
						t.setDaemon(false);
						t.setName(name);
						t.setPriority(Thread.NORM_PRIORITY + 1);	// priority is higher than worker thread
						t.start();
					}
				}
			}
		}
	}

	/**
	 * shutdown the pool 
	 *
	 */
	void shutdown() {
		isRunning = false;

		log.info("terminate dispatchers");
		for (int i = 0; i < dispatchers.size(); i++) {
			dispatchers.get(i).shutdown();
		}

		dispatchers.clear();
	}

	// get a least-used dispatcher
	public synchronized Dispatcher nextDispatcher() {
		int idx = -1, n;
		int m = Integer.MAX_VALUE;
		for (int i = 0; i < size; i++) {
			n = dispatchers.get(i).getNumOfChannels();
			if (n == 0) {
				idx = i;
				break;
			} else if (n > 0 && n < m) {
				idx = i;
				m = n;
			}
		}

		return idx >= 0 ? dispatchers.get(idx) : null;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("DispatcherPool: " + size + " dispatcher(s)");
		for (int i = 0; i < dispatchers.size(); i++) {
			sb.append("\n\t").append(dispatchers.get(i));
		}
		return sb.toString();
	}
}
