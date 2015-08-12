package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.BrowserModel;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

//NOTE this is only used for BrowserModel which has payload all other widgets use the normal position messages
public class VeTsxAppEventGeometryChangedMessageSender extends BaseMessageSender {
	private static final String TAG = VeTsxAppEventGeometryChangedMessageSender.class.getSimpleName();

	//ios message
//	["558d9e00fcd8ac2605f37a6b","ve","559319f6fcd8ac2605f543e3","tsxappevent",{"targetTsxAppId":"webbrowser","payload":{"x":11.90567,"worldSpaceWidth":1018.001,"worldSpaceHeight":700.0001,"y":36.44885,"windowSpaceWidth":1020,"windowSpaceHeight":700,"order":118705,"version":1},"messageType":"geometryChanged"}]
//	["558d9e00fcd8ac2605f37a6b","he","559319f6fcd8ac2605f543e3","tsxappevent",{"targetTsxAppId":"webbrowser","payload":{"x":11.90567,"worldSpaceWidth":1018.001,"worldSpaceHeight":700.0001,"y":36.44885,"windowSpaceWidth":1020,"windowSpaceHeight":700,"order":118705,"version":1},"messageType":"geometryChanged"}]

	private JSONArray mWebSocketMessage;

	public VeTsxAppEventGeometryChangedMessageSender(BaseWidgetModel baseWidgetModel) {
		try {
			// Send add stroke to web socket server
			mWebSocketMessage = new JSONArray();

			mWebSocketMessage.put(WorkSpaceState.getInstance().getClientId()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
																				// .
																				// CLIENT_ID
			mWebSocketMessage.put("ve"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
											// . HE
			mWebSocketMessage.put(baseWidgetModel.getID()); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
															// . TARGET_ID

			mWebSocketMessage.put("tsxappevent"); // HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT
												// . EVENT_TYPE

			JSONObject eventProperties = new JSONObject();

			eventProperties.put("messageType", "geometryChanged");
			eventProperties.put("targetTsxAppId", "webbrowser");

			JSONObject payloadJson = new JSONObject();
			payloadJson.put("x", ((BrowserModel)baseWidgetModel).getPayload().getX());
			payloadJson.put("y", ((BrowserModel)baseWidgetModel).getPayload().getY());

			payloadJson.put("worldSpaceWidth", ((BrowserModel)baseWidgetModel).getPayload().getWorldSpaceWidth());
			payloadJson.put("worldSpaceHeight", ((BrowserModel)baseWidgetModel).getPayload().getWorldSpaceHeight());

			payloadJson.put("windowSpaceWidth", ((BrowserModel)baseWidgetModel).getPayload().getWindowSpaceWidth());
			payloadJson.put("windowSpaceHeight", ((BrowserModel)baseWidgetModel).getPayload().getWindowSpaceHeight());

			payloadJson.put("order", ((BrowserModel)baseWidgetModel).getOrder());
			payloadJson.put("version", 1);

			eventProperties.put("payload", payloadJson);


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
