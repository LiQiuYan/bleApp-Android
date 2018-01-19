package com.healthme.common.nio.socket;

import com.healthme.common.nio.Message;

public interface IAppHandler {

	/** onData() event handler
	 * 
	 * processes the incomming data based on the given tcp or udp channel.
	 * This method will be called each time when data in the channel is 
	 * available. Because this depends on the underlying tcp/udp protocol, 
	 * it is not predictable how often and when this method will be call. 
	 * Furthermore the call of this method is independent of the received 
	 * data size. The data event handler is responsible to extract the application 
	 * specific data packages (like HTTP request or SMTP commands) based on
	 * the received data. or, defines IAppHandlerBoundary.checkBoundary()
	 * to decide application specific data packages.
	 * 
	 * One thread is allocated from internal thread pool to execute data
	 * event handler. After finishs, it is returned to the thread pool.
	 * It is unnecessary to handle thread in the even handler.
	 * 
	 * The even handler is responsible for processing any Exception.
	 * 
	 * @param cinfo -- the underlying tcp/udp channel
	 * @param  data -- avaiable data buffer
	 * @return true for positive result of handling, false for negative result of handling
	 */
	
	public boolean onData(ChannelInfo cinfo, Message data);

}
