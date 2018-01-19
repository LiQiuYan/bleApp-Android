package com.healthme.common.nio.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import com.healthme.common.nio.Message;

public interface ICheckMessage {

	public List<Message> onCheck(ByteBuffer data) throws IOException;
}
