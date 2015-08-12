package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;

import org.json.JSONArray;

public class VcMessageSender extends BaseMessageSender {

	private static final String TAG = VcMessageSender.class.getSimpleName();

	// // server <--> client
	// [sender-id, "vc", viewport-rect]
	// ["-1","vc",[-1252.415,-831.1971,1237.496,1036.237]]

	private JSONArray mWebSocketMessage;

	public VcMessageSender() {

	}

	public void send() {
		try {
			// Send add stroke to web socket server
			mWebSocketMessage = new JSONArray();

			mWebSocketMessage.put(WorkSpaceState.getInstance().getClientId()); // sender-id
																				// or
																				// CLIENT_ID
			mWebSocketMessage.put("vc"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
											// . HE
			// mWebSocketMessage.put(WorkSpaceState.getInstance().getWorkSpaceModel().getRect());
			// //HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT . HE

			float[] rectFloatArray = WorkSpaceState.getInstance().getWorldPosition();
			JSONArray floatJSONArray = new JSONArray();
			for (float aRectFloatArray : rectFloatArray) {
				floatJSONArray.put(aRectFloatArray);
			}
			mWebSocketMessage.put(floatJSONArray); // RECT Array

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
