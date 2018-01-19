package com.healthme.common.nio;

import java.net.UnknownHostException;

import com.healthme.common.nio.socket.ListenerConfiguration;

public interface ECGListener
{
	void initialize(ListenerConfiguration config, MessageHandler handler)
			throws UnknownHostException;

	String getHost();

	int getPort();

	void start();

	boolean isStarted();

	void stop();
}