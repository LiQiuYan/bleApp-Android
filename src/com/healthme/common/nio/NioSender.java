package com.healthme.common.nio;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.healthme.common.nio.socket.ChannelInfo;

public class NioSender implements Sender<Message> {
	static Logger log = Logger.getLogger(NioSender.class);
	private ChannelInfo cinfo;

	public NioSender(ChannelInfo conn) {
		cinfo = conn;
	}

	public void send(Message message) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("sending message to " + cinfo + ":\n" + message.toString());
		}
		cinfo.send(message);
	}

	public void close() {
		if( log.isDebugEnabled() )
			log.debug("closing cinfo " + cinfo);
		cinfo.close();
	}

	public String getRemoteSocketAddress() {
	    if (cinfo.getChannel().socket() != null && cinfo.getChannel().socket().getRemoteSocketAddress() != null)
            return cinfo.getChannel().socket().getRemoteSocketAddress().toString();
        
        return null;
	}

	public void flush() throws IOException {
		cinfo.flush();
	}

	public Socket getRemoteSocket() {
		return cinfo.getChannel().socket();
	}

	public ChannelInfo getChannelInfo() { return cinfo; }

	public String toString() {
		return "NioSender connected to remote " + cinfo.getRemoteAddress();
	}
}
