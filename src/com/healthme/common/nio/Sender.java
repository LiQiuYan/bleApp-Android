package com.healthme.common.nio;

import java.io.IOException;

public interface Sender<T> {
    void send(T message) throws Exception;
    void close() throws IOException;
    String getRemoteSocketAddress();
}
