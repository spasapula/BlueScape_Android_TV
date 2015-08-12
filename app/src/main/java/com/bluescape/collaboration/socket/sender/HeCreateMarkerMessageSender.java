package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.LocationMarkerModel;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

public class HeCreateMarkerMessageSender extends BaseMessageSender {

	private static final String TAG = HeCreateMarkerMessageSender.class.getSimpleName();

	// History Event Basic Message Format Enum for sending first 5 values
	// https://github.com/Bluescape/thoughtstream/blob/develop/web-socket-protocol.md#delete
	// [client-id, "he", target-id, event-type, event-properties]

	// Marker create is different where the element after "he" or 3rd element is
	// the new id not the workspace id as that is inherently assumed
	// server <-- client
	// [client-id, "he", new-widget-id, "markercreate",{
	// "creationTime":1387565966,
	// "name":"my marker",
	// "y":1828,
	// "x":-875,
	// "color":0
	// }]

	private JSONArray mWebSocketMessage;

	public HeCreateMarkerMessageSender(LocationMarkerModel locationMarkerModel) {

		try {
			// Send add stroke to web socket server
			mWebSocketMessage = new JSONArray();

			mWebSocketMessage.put(WorkSpaceState.getInstance().getClientId()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
																				// .
																				// CLIENT_ID

			mWebSocketMessage.put("he"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
											// . HE

			// Marker create is different where the element after "he" or 3rd
			// element is the new id not the workspace id as that is inherently
			// assumed
			mWebSocketMessage.put(new ObjectId()); // The new id to be created
													// new-widget-id

			mWebSocketMessage.put("markercreate"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
													// . EVENT_TYPE

			JSONObject eventProperties = new JSONObject();

			eventProperties.put("creationTime", locationMarkerModel.getCreationTime());
			eventProperties.put("y", locationMarkerModel.getY());
			eventProperties.put("x", locationMarkerModel.getX());
			eventProperties.put("color", locationMarkerModel.getColor());
			eventProperties.put("name", locationMarkerModel.getMarkerName());

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
