package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.model.WorkSpaceModel;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.BrowserModel;
import com.bluescape.model.widget.LocationMarkerModel;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URL;
import java.util.List;

public class HeMarkerMoveMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeMarkerMoveMessageHandler.class.getSimpleName();

	public void handleMessage(JSONArray mainMessage) {
		WorkSpaceModel workSpaceModel = WorkSpaceState.getInstance().getWorkSpaceModel();

		AppConstants.LOG(AppConstants.VERBOSE, TAG, mainMessage.toString());

		LocationMarkerModel locationMarkerModel = parseLocationModel(mainMessage);
		try {
			if (locationMarkerModel != null) {

				BaseWidgetModel baseWidgetModel = WorkSpaceState
						.getInstance()
						.getModelTree()
						.getModel(
								mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString());

				if (baseWidgetModel != null) {

					if (baseWidgetModel instanceof LocationMarkerModel) {

						((LocationMarkerModel) baseWidgetModel).setX(locationMarkerModel.getX());
						((LocationMarkerModel) baseWidgetModel).setY(locationMarkerModel.getY());
						baseWidgetModel.setDirty(true);
						//Set the mAreAllViewPortWidgetsInitialized to false to update scene in Modeltree
						WorkSpaceState
								.getInstance()
								.getModelTree()
								.mAreAllViewPortWidgetsInitialized = false;

					}

					AppConstants.LOG(AppConstants.VERBOSE, TAG, "LocationMarkerModel  Move baseWidgetModel:" + baseWidgetModel.toString());
				} else {
					AppConstants.LOG(AppConstants.CRITICAL, TAG,
							"MarkerMove NOT OK baseWidgetModel is NULL");
				}



			} else {
				AppConstants.LOG(AppConstants.VERBOSE, TAG, "Marker Move Null Marker from Marker Parser");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// @Override
	private LocationMarkerModel parseLocationModel(JSONArray mJsonArray) {

		LocationMarkerModel model = null;
		String targetID;
		String newwidgetid;

//		// server --> client
//		[client-id, "he", marker-id, event-id, "markermove",{
//				"y":1828,
//				"x":-875,
//		}]

		// client-id
		try {

			// client-id
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal());

			// Message Types
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.HE.ordinal());

			// target-id in most cases bit id in LocationModel it is the id and
			// assume the target is the Workspace
			targetID = AppSingleton.getInstance().getApplication().getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, "");
			newwidgetid = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();

			// event-id
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_ID.ordinal());

			// History Event
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal());

			// event Json Object
			String eventJsonString = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal()).toString();

			GsonBuilder builder = new GsonFireBuilder().enableHooks(LocationMarkerModel.class).createGsonBuilder();
			Gson gson = builder.create();
			model = gson.fromJson(eventJsonString, LocationMarkerModel.class);

			model.setTargetID(targetID);
			model.setID(newwidgetid);

			AppConstants.LOG(AppConstants.VERBOSE, TAG, "Marker ID: " + model.getID());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return model;

	}
}
