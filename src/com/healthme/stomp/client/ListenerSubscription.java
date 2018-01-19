package com.healthme.stomp.client;

import java.util.Map;

public interface ListenerSubscription {
	public void onMessage(Map<String, String> headers, String body);
}
