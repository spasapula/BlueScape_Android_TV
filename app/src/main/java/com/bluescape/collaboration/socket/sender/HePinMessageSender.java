package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BaseWidgetModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class HePinMessageSender extends BaseMessageSender {
	private static final String TAG = HePinMessageSender.class.getSimpleName();

	// server <-- client
	// [client-id, "he", target-id, "pin", {"pin":true}]
	private JSONArray mWebSocketMessage;

	public HePinMessageSender(BaseWidgetModel baseWidgetModel) {
		try {
			// Send add stroke to web socket server
			mWebSocketMessage = new JSONArray();

			mWebSocketMessage.put(WorkSpaceState.getInstance().getClientId()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
																				// .
																				// CLIENT_ID
			mWebSocketMessage.put("he"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
											// . HE
			mWebSocketMessage.put(baseWidgetModel.getID()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
															// . TARGET_ID
			mWebSocketMessage.put("pin"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
											// . EVENT_TYPE

			JSONObject eventProperties = new JSONObject();
			eventProperties.put("pin", !baseWidgetModel.isPinned());
			mWebSocketMessage.put(eventProperties); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
													// . EVENT_PROPERTIES

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
