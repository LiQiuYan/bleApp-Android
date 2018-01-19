package com.healthme.common.nio.socket;

public interface IAppHandlerConnect extends IAppHandler {

	/**
	 * handles a new incomming connection
	 * 
	 * @param cinfo -- the underlying tcp/udp channel
	 * @return true for positive result of handling, false for negative result of handling
	 */
	public boolean onConnect(ChannelInfo cinfo);

	/**
	 * handles disconnecting of a connection
	 * 
	 * @param cinfo -- the underlying tcp/udp channel
	 * @return true for positive result of handling, false for negative result of handling
	 */
	public boolean onDisconnect(ChannelInfo cinfo);

}
