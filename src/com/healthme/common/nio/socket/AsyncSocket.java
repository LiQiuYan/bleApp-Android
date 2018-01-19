package com.healthme.common.nio.socket;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

public class AsyncSocket extends Thread{
	Logger log=Logger.getLogger(getClass().getName());
	SocketChannel clientChannel = null;
	long startTime;
	Object lock=new Object();
	private SocketAddress remote;
	private int timeout;
	public AsyncSocket() {
		
	}

	public SocketChannel connect(SocketAddress remote, int timeout) throws IOException{
		this.timeout=timeout;
		this.remote=remote;
		startTime=System.currentTimeMillis();
		this.start();
		try {
			synchronized (lock) {
				if(clientChannel==null||!clientChannel.isConnected())
					lock.wait(timeout);
			}
		} catch (InterruptedException e) {
			throw new IOException("break to connect to "+remote);
		}
		if(clientChannel==null)
			throw new IOException("connect to "+remote+" timeout");
		
		return clientChannel;
	}
	
	public void run() {
		try {
			clientChannel = SocketChannel.open(remote);
			if(System.currentTimeMillis()-startTime>timeout)
				clientChannel.close();
			else{
				synchronized (lock) {
					lock.notify();
				}
			}
		} catch (IOException e) {
			log.error("failed to connect to "+remote);
		}
	}
}