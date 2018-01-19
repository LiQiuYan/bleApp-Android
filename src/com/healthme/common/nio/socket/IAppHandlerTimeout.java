package com.healthme.common.nio.socket;

public interface IAppHandlerTimeout extends IAppHandler {

	/**
	 * handles the idle timeout.
	 * 
	 * @param cinfo -- the underlying tcp/udp channel
	 * @return true if the timeout event has been handled (in case of false the connection will be closed by the server)
	 */
	public boolean onTimeout(ChannelInfo cinfo);

}
