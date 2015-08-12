package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.NoteModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class HeTextMessageSender extends BaseMessageSender {

	private static final String TAG = HeTextMessageSender.class.getSimpleName();

	// server <-- client
	// [client-id, "he", target-id, "text", {
	// "text" : "abcdef",
	// "styles" : {"font-size" : "42px","font-weight" : "400","text-transform" :
	// "inherit"}
	// }]
	private JSONArray mWebSocketMessage;

	public HeTextMessageSender(NoteModel noteModel) {
		try {
			mWebSocketMessage = new JSONArray();

			mWebSocketMessage.put(WorkSpaceState.getInstance().getClientId()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
																				// .
																				// CLIENT_ID
			mWebSocketMessage.put("he"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
											// . HE
			mWebSocketMessage.put(noteModel.getID());
			mWebSocketMessage.put("text"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
											// . EVENT_TYPE

			JSONObject eventProperties = new JSONObject();

			eventProperties.put("font-size", noteModel.getTextStyle().getFontSize());
			eventProperties.put("font-weight", noteModel.getTextStyle().getFontWieght());
			eventProperties.put("text-transform", noteModel.getTextStyle().getTextTranform());
			eventProperties.put("text", noteModel.getText())

			;
			mWebSocketMessage.put(eventProperties);
		} catch (Exception e) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, e.getLocalizedMessage());

		}

	}

	@Override
	public void send() {
		super.send();
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
