package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.NoteModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class HeTemplateMessageSender extends BaseMessageSender {
	private static final String TAG = HeTemplateMessageSender.class.getSimpleName();

	// server <-- client
	// [client-id, "he", target-id, "pin", {"pin":true}]
	private JSONArray mWebSocketMessage;

	public HeTemplateMessageSender(NoteModel noteModel) {
		try {
			// Send add stroke to web socket server
			mWebSocketMessage = new JSONArray();

			mWebSocketMessage.put(WorkSpaceState.getInstance().getClientId()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
																				// .
																				// CLIENT_ID
			mWebSocketMessage.put("he"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
											// . HE
			mWebSocketMessage.put(noteModel.getID()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
														// . TARGET_ID
			mWebSocketMessage.put("template"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
												// . EVENT_TYPE

			JSONObject eventProperties = new JSONObject();

			String[] urlArray = noteModel.getBaseName().split("/");
			String image = urlArray[urlArray.length - 1];
			String imageName = image.substring(0, image.lastIndexOf("."));
			eventProperties.put("baseName", "sessions/all/" + imageName);

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
