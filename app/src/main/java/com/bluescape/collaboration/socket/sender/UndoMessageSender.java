package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;

import org.json.JSONArray;

public class UndoMessageSender extends BaseMessageSender {
	private static final String TAG = UndoMessageSender.class.getSimpleName();

	// server <-- client
	// [sender-id, "un", region-id]

	private JSONArray mWebSocketMessage;

	public UndoMessageSender() {

		mWebSocketMessage = new JSONArray();

		mWebSocketMessage.put(WorkSpaceState.getInstance().getClientId());
		mWebSocketMessage.put("un");
		mWebSocketMessage.put(null);
	}

	@Override
	public void send() {
		try {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "Sending toString" + " mWebSocketMessage = " + mWebSocketMessage.toString());
			WorkSpaceState.getInstance().mWebSocketClient.send("" + mWebSocketMessage);
		} catch (Exception ex) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG,
				"Exception in   WorkSpaceState.getInstance().mWebSocketClient.send(\"\" + mWebSocketMessage);" + " mWebSocketMessage = "
						+ mWebSocketMessage.toString());
			ex.printStackTrace();
		}
	}
}
