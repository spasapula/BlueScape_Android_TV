package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.collaboration.socket.IHandler;
import com.bluescape.collaboration.socket.HandlerFactory;

import org.json.JSONArray;

public class HeMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeMessageHandler.class.getSimpleName();

	public void handleMessage(JSONArray mainMessage) {

		try {

			// [client-id, "he", target-id, event-id, event-type,
			// event-properties]
			// he create
			// ["54d3bb68b5a05708542db8de","he","cK39EC83wjxamMKG-_5R","54e7b225f6137eae4604d491","create",{"id":"54e7b226f042563700000001","actualWidth":560,"baseName":"sessions/all/Yellow","actualHeight":320,"strokes":[],"type":"note","regionId":null,"order":0,"text":"","ext":"JPEG","hidden":false,"rect":[-246.6854,-112,33.31461,48]}]

			String event = mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal()).toString();

			AppConstants.LOG(AppConstants.VERBOSE, TAG, "he mainMessage = " + mainMessage);

			IHandler handler = HandlerFactory.getInstance().getHandler(event);
			if (handler != null) {
				handler.handleMessage(mainMessage);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
