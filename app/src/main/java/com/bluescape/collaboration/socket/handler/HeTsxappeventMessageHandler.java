package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.collaboration.socket.IHandler;
import com.bluescape.collaboration.socket.HandlerFactory;

import org.json.JSONArray;
import org.json.JSONObject;

public class HeTsxappeventMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeTsxappeventMessageHandler.class.getSimpleName();

	public void handleMessage(JSONArray mainMessage) {

		try {

			AppConstants.LOG(AppConstants.VERBOSE, TAG, mainMessage.toString());
			String type;// createBrowser ot geometryChanged or deleteBrowser
			// event Json Object
			JSONObject eventJsonObj = (JSONObject) mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal());
			if (!eventJsonObj.isNull("messageType")) {
				type = eventJsonObj.get("messageType").toString();
				IHandler handler = HandlerFactory.getInstance().getHandler(type);
				if (handler != null) {
					handler.handleMessage(mainMessage);
				}

			} else {
				AppConstants.LOG(AppConstants.CRITICAL, TAG, "Null type from mainMessage in HeCreateMessageHandler");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
