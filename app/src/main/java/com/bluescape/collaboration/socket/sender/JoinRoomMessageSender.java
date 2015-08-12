package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.model.WorkSpaceState;

import org.json.JSONArray;

public class JoinRoomMessageSender extends BaseMessageSender {

	private JSONArray mJrMessage;

	public JoinRoomMessageSender() {

		try {
			// sending join room message [sender-id, "jr", "session",
			// workspace-id]
			mJrMessage = new JSONArray();

			mJrMessage.put(AppConstants.WS_JR_SESSION_EVENT_FROM_CLIENT_MESSAGE_FORMAT.SENDER_ID.ordinal(), WorkSpaceState.getInstance()
				.getClientId());
			mJrMessage.put(AppConstants.WS_JR_SESSION_EVENT_FROM_CLIENT_MESSAGE_FORMAT.JR.ordinal(), "jr");
			mJrMessage.put(AppConstants.WS_JR_SESSION_EVENT_FROM_CLIENT_MESSAGE_FORMAT.SESSION.ordinal(), "session");
			mJrMessage.put(AppConstants.WS_JR_SESSION_EVENT_FROM_CLIENT_MESSAGE_FORMAT.WORKSPACE_ID.ordinal(), AppSingleton.getInstance()
				.getApplication().getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, ""));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void send() {
		WorkSpaceState.getInstance().mWebSocketClient.send("" + mJrMessage);
	}
}
