package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.collaboration.socket.sender.JoinRoomMessageSender;

import org.json.JSONArray;

public class IdMessageHandler extends BaseMessageHandler {
	private static final String TAG = IdMessageHandler.class.getSimpleName();

	public void handleMessage(JSONArray mainMessage) {

		try {
			// Store the client in the workspace state
			String clientId = mainMessage.get(AppConstants.WS_CLIENT_CONNECT_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal()).toString();
			WorkSpaceState.getInstance().setClientId(clientId);
			// Send the join room message handler
			JoinRoomMessageSender joinRoomMessageSender = new JoinRoomMessageSender();
			joinRoomMessageSender.send();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
