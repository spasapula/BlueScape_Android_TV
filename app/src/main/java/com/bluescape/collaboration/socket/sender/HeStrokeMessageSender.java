package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.StrokeModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class HeStrokeMessageSender extends BaseMessageSender {

	private static final String TAG = HeStrokeMessageSender.class.getSimpleName();

	// History Event Basic Message Format Enum for sending first 5 values
	// https://github.com/Bluescape/thoughtstream/blob/develop/web-socket-protocol.md#delete
	// server <-- client
	// [client-id, "he", target-id, event-type, event-properties]
	// ["54d3bb68b5a05708542db8de","he","mwLRSw5H3EMryJk6w2r3","stroke",{"color":[0,182,167,1],"brush":1,"locs":[340.5353,-386.1192,350.1682,-373.2754,350.1682,-373.2754,359.801,-360.4317,369.4338,-344.377,372.6447,-337.9551,375.8557,-331.5332,379.0666,-328.3223,382.2775,-325.1114,385.4885,-321.9004,388.6994,-312.2676,388.6994,-305.8457,391.9103,-302.6348,391.9103,-299.4239,395.1213,-296.2129,401.5432,-296.2129],"size":5}]

	// public enum HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT {
	// CLIENT_ID,
	// HE,
	// TARGET_ID,
	// EVENT_TYPE,
	// EVENT_PROPERTIES
	// }

	private JSONArray mWebSocketMessage;

	public HeStrokeMessageSender(StrokeModel strokeModel) {

		try {
			// Send add stroke to web socket server
			mWebSocketMessage = new JSONArray();

			mWebSocketMessage.put(WorkSpaceState.getInstance().getClientId()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
																				// .
																				// CLIENT_ID
			mWebSocketMessage.put("he"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
											// . HE
			mWebSocketMessage.put(strokeModel.getTargetID()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
																// . TARGET_ID
			mWebSocketMessage.put("stroke"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
												// . EVENT_TYPE

			JSONObject eventProperties = new JSONObject();

			// Handle for Eraser and normal stroke
			eventProperties.put("brush", strokeModel.getBrush());
			eventProperties.put("size", strokeModel.getLineWidth());

			// int[] colorIntArray = {0, 182, 167, 1};
			float[] colorIntArray = strokeModel.getColor();
			JSONArray intColorJSONArray = new JSONArray();
			for (float aColorIntArray : colorIntArray) {
				intColorJSONArray.put(aColorIntArray);
			}
			eventProperties.put("color", intColorJSONArray);

			// double[] locsDoubleArray =
			// {340.5353,-386.1192,350.1682,-373.2754,350.1682,-373.2754,359.801,-360.4317,369.4338,-344.377,372.6447,-337.9551,375.8557,-331.5332,379.0666,-328.3223,382.2775,-325.1114,385.4885,-321.9004,388.6994,-312.2676,388.6994,-305.8457,391.9103,-302.6348,391.9103,-299.4239,395.1213,-296.2129,401.5432,-296.2129};
			List locsFloatArray = strokeModel.getArrayLocs();
			JSONArray floatJSONArray = new JSONArray();
			for (int i = 0; i < locsFloatArray.size(); i++) {
				floatJSONArray.put((float) locsFloatArray.get(i));
			}
			eventProperties.put("locs", floatJSONArray);

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
