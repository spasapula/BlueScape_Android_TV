package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.ModelTree;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BrowserModel;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

public class HeCreateBrowserMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeCreateNoteMessageHandler.class.getSimpleName();

	public void handleMessage(JSONArray mainMessage) {

		try {

			AppConstants.LOG(AppConstants.VERBOSE, TAG, mainMessage.toString());

			BrowserModel browserModel = parseBrowser(mainMessage);

			if (browserModel != null) {
				WorkSpaceState.getInstance().getModelTree().add(browserModel);
				AppConstants.LOG(AppConstants.VERBOSE, TAG, "Not Null browser");
			} else {
				AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null browser");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// @Override
	private BrowserModel parseBrowser(JSONArray mJsonArray) {

		BrowserModel model = null;

		// client-id
		try {
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal());

			// Message Types
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.HE.ordinal());

			// target-id
			String id = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();

			// event-id
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_ID.ordinal());

			// History Event
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal());

			// event Json Object
			String eventJsonString = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal()).toString();
			GsonBuilder builder = new GsonFireBuilder().enableHooks(BrowserModel.class).createGsonBuilder();
			Gson gson = builder.create();
			model = gson.fromJson(eventJsonString, BrowserModel.class);
			model.setTargetID(WorkSpaceState.getInstance().getWorkSpaceModel().getId());
			model.setID(id);
			// model.mEventOrder is set according to the order sent in he and ve
			// events
			// now set the global mGlobalWorkSpaceEventOrder to the highest one
			// since order is inside payload assign mEventOrder = payload.order
			model.setOrder(model.getPayload().getOrder());
			WorkSpaceState.getInstance().updateOrder(model.getOrder());
			// Check for erroneous scales that set order to -4
			if (model.getOrder() < 0) model.setOrder(1);

		} catch (JSONException e) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "BrowserModel Exception in GSON Parsing: " + mJsonArray);
			e.printStackTrace();
		}

		return model;
	}

}
