package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.ModelTree;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.ImageModel;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

public class HeCreateImageMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeCreateImageMessageHandler.class.getSimpleName();

	// TODO is this safe?
	private GsonBuilder builder = new GsonFireBuilder().enableHooks(ImageModel.class).createGsonBuilder();

	private Gson gson = builder.create();

	public void handleMessage(JSONArray mainMessage) {

		try {

			AppConstants.LOG(AppConstants.VERBOSE, TAG, mainMessage.toString());

			ImageModel imageModel = parseImageModel(mainMessage);

			if (imageModel != null) {
				modelTree.add(imageModel);

			} else {
				AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null Image from ImageModel.fromJSON");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ImageModel parseImageModel(JSONArray mJsonArray) {

		ImageModel model = null;

		// client-id
		try {
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal());

			// Message Types
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.HE.ordinal());

			// target-id
			String targetID = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();

			// event-id
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_ID.ordinal());

			// History Event
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal());

			// event Json Object
			String eventJsonString = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal()).toString();
			model = gson.fromJson(eventJsonString, ImageModel.class);
			model.setTargetID(targetID);
			// model.mEventOrder is set according to the order sent in he and ve
			// events
			// now set the global mGlobalWorkSpaceEventOrder to the highest one
			WorkSpaceState.getInstance().updateOrder(model.getOrder());

		} catch (JSONException e) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "ImageModel Exception in GSON Parsing: " + mJsonArray);
			e.printStackTrace();
		}

		return model;
	}
}
