package com.healthme.message;

import java.util.List;

public interface MessageParser extends MessageType{

	List<BLEMessage> parse(byte[] data);
}
