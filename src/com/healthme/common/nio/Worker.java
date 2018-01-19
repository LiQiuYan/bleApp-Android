
package com.healthme.common.nio;

import org.apache.log4j.Logger;

import com.healthme.common.nio.socket.ChannelInfo;


public class Worker implements Runnable {
	private static Logger log = Logger.getLogger(Worker.class);
	private ChannelInfo sender;
	private MessageHandler handler;
	private Message message;

	public Worker(ChannelInfo sender, MessageHandler handler, Message message) {

		this.sender = sender;
		this.handler = handler;
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Starting worker thread for "
						+ sender.getRemoteAddress());
			}

			handler.handle(message, sender);

			if (log.isDebugEnabled()) {
				log.debug("Finished worker thread for "
						+ sender.getRemoteAddress());
			}
		} catch (Throwable t) {
			log.error("Worker: Unexpected outer throwable: ", t);
		}
	}
}