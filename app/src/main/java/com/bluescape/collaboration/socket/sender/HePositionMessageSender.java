package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.MoveModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class HePositionMessageSender extends BaseMessageSender {

	private static final String TAG = HePositionMessageSender.class.getSimpleName();

	// History Event Basic Message Format Enum for sending first 5 values
	// https://github.com/Bluescape/thoughtstream/blob/develop/web-socket-protocol.md#delete
	// server <-- client
	// [client-id, "he", target-id, "position", {new-position}]
	// ["54d3bb68b5a05708542db8de","he","54ff3ecd5330744100000000","position",{"rect":[-608.9565,-181.8393,250.9175,309.5177],"order":10095515}]

	// public enum HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT {
	// CLIENT_ID,
	// HE,
	// TARGET_ID,
	// EVENT_TYPE,
	// EVENT_PROPERTIES
	// }

	private JSONArray mWebSocketMessage;

	public HePositionMessageSender(MoveModel moveModel) {

		try {
			// Send add stroke to web socket server
			mWebSocketMessage = new JSONArray();

			mWebSocketMessage.put(WorkSpaceState.getInstance().getClientId()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
																				// .
																				// CLIENT_ID
			mWebSocketMessage.put("he"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
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
			// AppConstants.LOG(AppConstants.CRITICAL, TAG, "Sending toString" +
			// " mWebSocketMessage = " + mWebSocketMessage.toString());
			WorkSpaceState.getInstance().mWebSocketClient.send("" + mWebSocketMessage);
		} catch (Exception ex) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG,
				"Exception in   WorkSpaceState.getInstance().mWebSocketClient.send(\"\" + mWebSocketMessage);" + " mWebSocketMessage = "
						+ mWebSocketMessage.toString());

			ex.printStackTrace();
		}
	}
}
