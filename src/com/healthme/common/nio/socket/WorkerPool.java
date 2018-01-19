package com.healthme.common.nio.socket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class WorkerPool {
	private static Logger log = Logger.getLogger(WorkerPool.class);

	private static final int DEFAULT_MAX_THREAD_POOL_SIZE = 1024;
	private static final int DEFAULT_KEEP_ALIVE_TIME_SECS	= 60;	// 1 minutes

	private final ThreadPoolExecutor executor;
	private volatile int maxPoolSize;
	private final BlockingQueue<Runnable> theQueue;

	private String name;
	
	/**
	 * constructor for worker pool
	 * @param corePoolSize         core pool size (>=1)
	 * @param maxPoolSize		   max pool size. Default = Integer.MAX_VALUE
	 * 
	 * Worker thread pool creates new thread as needed, but will reuse previously constructed threads when they
	 * are available. The minimum threads are the specified corePoolSize. The idle time for a thread in the pool
	 * is 5 minutes.
	 * 
	 * Worker thread pool policy:
	 * If number of current threads < corePoolSize, create a new thread.
	 * If number of current threads < maxPoolSize and no idle thread available, then create a new thread.
	 * Otherwise, put the task in the queue and wait for a thread to start it.
	 */
	public WorkerPool(int corePoolSize, int maxPoolSize) {
		this("worker",corePoolSize,maxPoolSize);
	}
	
	
	/**
	 * constructor for worker pool
	 * @param corePoolSize         core pool size (>=1)
	 * @param maxPoolSize		   max pool size. Default = Integer.MAX_VALUE
	 * 
	 * Worker thread pool creates new thread as needed, but will reuse previously constructed threads when they
	 * are available. The minimum threads are the specified corePoolSize. The idle time for a thread in the pool
	 * is 5 minutes.
	 * 
	 * Worker thread pool policy:
	 * If number of current threads < corePoolSize, create a new thread.
	 * If number of current threads < maxPoolSize and no idle thread available, then create a new thread.
	 * Otherwise, put the task in the queue and wait for a thread to start it.
	 */
	public WorkerPool(String name,int corePoolSize, int maxPoolSize) {
		if (corePoolSize < 1) {
			corePoolSize = 1;
		}
		if (maxPoolSize < corePoolSize) {
			maxPoolSize =  DEFAULT_MAX_THREAD_POOL_SIZE;
			if (maxPoolSize < corePoolSize) maxPoolSize = corePoolSize;
		}
		this.maxPoolSize = maxPoolSize;

		int keepAliveTime = DEFAULT_KEEP_ALIVE_TIME_SECS;
		log.info("create worker thread pool: corePoolSize=" + corePoolSize + ", maxPoolSize=" + maxPoolSize);
		
		theQueue = new SynchronousQueue<Runnable>();
		//              new LinkedBlockingQueue<Runnable>();
		//				new ThreadPoolBlockingQueue<Runnable>();		// create a new thread if there is no idle thread in the pool
		
		executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize
				, keepAliveTime, TimeUnit.SECONDS
				, theQueue
				, new WorkerThreadFactory(name));
	}
	
	public WorkerPool(String name,int corePoolSize, int maxPoolSize, LinkedBlockingQueue<Runnable> queue) {
		if (corePoolSize < 1) {
			corePoolSize = 1;
		}
		if (maxPoolSize < corePoolSize) {
			maxPoolSize =  DEFAULT_MAX_THREAD_POOL_SIZE;
			if (maxPoolSize < corePoolSize) maxPoolSize = corePoolSize;
		}
		this.maxPoolSize = maxPoolSize;

		int keepAliveTime = DEFAULT_KEEP_ALIVE_TIME_SECS;
		log.info("create worker thread pool: corePoolSize=" + corePoolSize + ", maxPoolSize=" + maxPoolSize);
		
		theQueue = queue;
		
		executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize
				, keepAliveTime, TimeUnit.SECONDS
				, theQueue
				, new WorkerThreadFactory(name));
	}
	public WorkerPool(int corePoolSize) {
		this(corePoolSize, -1);
	}

	/**
	 * submit a Runnable command, wait for executor to run the command
	 */
	public void execute(Runnable command) {
		try {
			executor.execute(command);

		} catch (RejectedExecutionException e) {

			// check to see whether thread pool shutdowns
			if (executor.isShutdown()) {
				Thread t = new Thread(command);
				t.setDaemon(true);
				t.start();

			} else {
				log.warn("couldn't process command " + command + ": " + e.getMessage());
			}
		}
	}

	private static final class WorkerThreadFactory implements ThreadFactory {
		private static int poolCounter = 0;
		private int threadCounter = 0;
		private String namePrefix = null;

		WorkerThreadFactory(String name) {
			namePrefix = name+"-" + (++poolCounter) + "-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName(namePrefix + (++threadCounter));
			t.setDaemon(true);
			t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}

	public void close() {
		executor.shutdownNow();
	}

	public boolean isOpen() { return !executor.isShutdown(); }

	public int getCorePoolSize() { return executor.getCorePoolSize(); }
	public void setCorePoolSize(int size) {
		if (size < 1) {
			size = 1;
		}
		executor.setCorePoolSize(size); 
	}

	// in seconds
	public int getKeepAliveTime() { return (int)executor.getKeepAliveTime(TimeUnit.SECONDS); }
	public void setKeepAliveTime(int secs) { executor.setKeepAliveTime(secs, TimeUnit.SECONDS); }

	public int getMaximumPoolSize() { return executor.getMaximumPoolSize(); }
	public void setMaximumPoolSize(int size) {
		if (size < getCorePoolSize()) {
			size = getCorePoolSize();
		}
		executor.setMaximumPoolSize(size);
		maxPoolSize = size;
	}

	public int getActiveCount() { return executor.getActiveCount(); }
	public int getLargestPoolSize() { return executor.getLargestPoolSize(); }
	public long getTaskCount() { return executor.getTaskCount(); }
	public long getCompleteTaskCount() { return executor.getCompletedTaskCount(); }

	// current number of threads in the pool
	public int getPoolSize() { return executor.getPoolSize(); }

	public String toString() {
		return "Worker thread pool[core size=" + getCorePoolSize() + "]: running=" + getActiveCount() +
		", threads in the pool=" + getPoolSize() + ", tasks in the queue=" + theQueue.size() + "\n" +
		"\tever largest threads=" + getLargestPoolSize() + ", total executed tasks=" + getTaskCount();
	}
	
	class ThreadPoolBlockingQueue<T> extends LinkedBlockingQueue<T> {
		private static final long serialVersionUID = -7197742583556448069L;

		@Override
		public boolean offer(T t) {
			int poolSize = executor.getPoolSize();
			int activeCount = executor.getActiveCount();
			boolean flag = false;
			// put the task in the queue if there is idle thread in the pool or exceed max thread numbers.
			if (activeCount < poolSize || poolSize >= maxPoolSize) {

				flag = super.offer(t);
				if (flag) {
					if (activeCount < poolSize)
						log.debug("@@@Thread pool: use idl thread. " + this);
					else
						log.warn("@@@Thread pool: exceed max pool size and wait in the queue. " + this);
				}
			}
			if (!flag) {
				log.debug("@@@Thread pool: allocate a new thread beyond core size. " + this);
			}
			return flag;
		}
		
		public String toString() {
			int nRunning = executor.getActiveCount();
			int nPoolSize = executor.getPoolSize();
			int nQueueSize = theQueue.size();
			return "core=" + getCorePoolSize() + ", max=" + getMaximumPoolSize() + ", running="  + nRunning +
			", idle=" + (nPoolSize-nRunning) + ", waiting tasks=" + nQueueSize + ".";
		}
	}
}
