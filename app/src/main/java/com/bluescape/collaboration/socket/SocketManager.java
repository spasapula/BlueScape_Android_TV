package com.bluescape.collaboration.socket;

import com.bluescape.AppSingleton;

import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SocketManager {
	static String TAG = SocketManager.class.getSimpleName();

	public WebSocketClient createSocket(String uri, String cookie, WebSocketClient.Listener listener) {
		List<BasicNameValuePair> extraHeaders = new LinkedList<>();
		extraHeaders.add(new BasicNameValuePair("Cookie", "" + cookie));
		extraHeaders.add(new BasicNameValuePair("user-agent", AppSingleton.getInstance().getUserAgent()));
		// wss://staging.collaboration.bluescape.com/RzissDedmUmWS2UGz9bs/socket?device=other
		return new WebSocketClient(URI.create(uri), listener, extraHeaders);
	}
}
