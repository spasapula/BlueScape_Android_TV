package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.NoteModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class HeCardCreateMessageSender extends BaseMessageSender {

	private static final String TAG = HeCardCreateMessageSender.class.getSimpleName();

	// History Event Basic Message Format Enum for sending first 5 values
	// https://github.com/Bluescape/thoughtstream/blob/develop/web-socket-protocol.md#delete
	// [client-id, "he", target-id, event-type, event-properties]

	// client --> server
	// [client-id, "he", workspace-id, "create", {
	// "id":"5123e7ebcd18d3ef5e000001",
	// "baseName":"sessions/all/Teal",
	// "ext":"JPEG",
	// "rect":[-1298,-390,-1018,-230],
	// "actualWidth":560,
	// "actualHeight":320,
	// "order":4,
	// "type":"note",
	// "regionId":null,
	// "hidden":false,
	// "text":"some text for the note",
	// "styles": {
	// "font-size" : "42px",
	// "font-weight" : "400",
	// "text-transform" : "inherit"
	// }
	// }]
	//

	private JSONArray mWebSocketMessage;

	public HeCardCreateMessageSender(NoteModel noteModel) {

		try {
			mWebSocketMessage = new JSONArray();

			mWebSocketMessage.put(WorkSpaceState.getInstance().getClientId()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
																				// .
																				// CLIENT_ID
			mWebSocketMessage.put("he"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
											// . HE
			mWebSocketMessage.put(WorkSpaceState.getInstance().getWorkSpaceModel().getId()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
																								// .
																								// TARGET_ID
			mWebSocketMessage.put("create"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
												// . EVENT_TYPE

			JSONObject eventProperties = new JSONObject();

			eventProperties.put("id", noteModel.getID());

			// card_templates/thumbnails/Yellow.jpeg
			// Expecting "baseName":"sessions/all/Teal",
			// eventProperties.put("baseName", noteModel.getBaseName());
			// eventProperties.put("baseName", "sessions/all/Teal");

			eventProperties.put("baseName", noteModel.getHeNoteCreateBaseName());

			float[] rectFloatArray = noteModel.getRect().getRect();
			JSONArray floatJSONArray = new JSONArray();
			for (float aRectFloatArray : rectFloatArray) {
				floatJSONArray.put(aRectFloatArray);
			}
			eventProperties.put("rect", floatJSONArray);

			eventProperties.put("actualWidth", AppConstants.CARD_DEFAULT_WIDTH);
			eventProperties.put("actualHeight", AppConstants.CARD_DEFAULT__HEIGHT);
			WorkSpaceState.getInstance().updateOrder(WorkSpaceState.getInstance().getGlobalOrder() +3);
			eventProperties.put("order", WorkSpaceState.getInstance().getGlobalOrder());
			eventProperties.put("type", "note");
			eventProperties.put("regionId", null);
			eventProperties.put("hidden", false);
			// doesn't show on ipad if ext is not set
			eventProperties.put("ext", "JPEG");
			eventProperties.put("text", noteModel.getText());

			// "styles": {
			// "font-size" : "42px",
			// "font-weight" : "400",
			// "text-transform" : "inherit"
			// }

			JSONObject styles = new JSONObject();
			styles.put("font-size", "42px");
			styles.put("font-weight", "400");
			styles.put("text-transform", "inherit");
			eventProperties.put("styles", styles);

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
