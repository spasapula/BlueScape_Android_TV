package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.MoveModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class VePositionMessageSender extends BaseMessageSender {

	private static final String TAG = VePositionMessageSender.class.getSimpleName();

	// Volatile Event Basic Message Format Enum for sending first 5 values
	// https://github.com/Bluescape/thoughtstream/blob/develop/web-socket-protocol.md#delete
	// client sends the following volatile events during the move
	// client->server format is: [<clientId>, <messageType>, <targetId>,
	// <eventType>, <messageProperties>]
	// public enum VOLATILE_EVENT_FROM_CLIENT_MESSAGE_FORMAT {
	// CLIENT_ID,
	// VE,
	// TARGET_ID,
	// EVENT_TYPE,
	// EVENT_PROPERTIES
	// }

	private JSONArray mWebSocketMessage;

	public VePositionMessageSender(MoveModel moveModel) {

		try {
			// Send add stroke to web socket server
			mWebSocketMessage = new JSONArray();

			mWebSocketMessage.put(WorkSpaceState.getInstance().getClientId()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
																				// .
																				// CLIENT_ID
			mWebSocketMessage.put("ve"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
											// . HE

			mWebSocketMessage.put(moveModel.getModelID()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
															// . TARGET_ID
			mWebSocketMessage.put("position"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
												// . EVENT_TYPE

			JSONObject eventProperties = new JSONObject();

			float[] rectFloatArray = moveModel.getRect();
			JSONArray floatJSONArray = new JSONArray();
			for (float aRectFloatArray : rectFloatArray) {
				floatJSONArray.put(aRectFloatArray);
			}
			eventProperties.put("rect", floatJSONArray);

			eventProperties.put("order", moveModel.getOrder());

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
