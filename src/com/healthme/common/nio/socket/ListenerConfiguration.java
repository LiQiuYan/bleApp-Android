package com.healthme.common.nio.socket;

public class ListenerConfiguration {
	String host;
	int port;
	String className;
	String handlerClassName;
	int threads;
	String name;
	String netInterface;

	public void setNetInterface(String netInterface) {
		this.netInterface = netInterface;
	}

	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 *            The className to set.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return Returns the port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            The port to set.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return Returns the handlerClassName.
	 */
	public String getHandlerClassName() {
		return handlerClassName;
	}

	/**
	 * @param handlerClassName
	 *            The handlerClassName to set.
	 */
	public void setHandlerClassName(String handlerClassName) {
		this.handlerClassName = handlerClassName;
	}

	/**
	 * @return Returns the threads.
	 */
	public int getThreads() {
		return threads;
	}

	/**
	 * @param threads
	 *            The threads to set.
	 */
	public void setThreads(int threads) {
		this.threads = threads;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getNetInterface() {
		// TODO Auto-generated method stub
		return null;
	}
}
