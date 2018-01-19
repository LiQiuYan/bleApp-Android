package com.healthme.common.nio;

import com.healthme.common.nio.socket.ListenerConfiguration;

public class BaseHandlerContext implements HandlerContext {
	protected ListenerConfiguration listenerConfiguration;


	public BaseHandlerContext(ListenerConfiguration config) {
		listenerConfiguration = config;
	}

	/**
	 * @return Returns the config.
	 */
	public ListenerConfiguration getListenerConfiguration() {
		return listenerConfiguration;
	}

	/**
	 * @param config
	 *            The config to set.
	 */
	public void setListenerConfiguration(ListenerConfiguration config) {
		listenerConfiguration = config;
	}
}
