package com.healthme.common.nio;

import com.healthme.common.nio.socket.ChannelInfo;

public interface MessageHandler
{
	void initialize(HandlerContext context);
	void handle(Message message, ChannelInfo cinfo) throws HandleException;
}
