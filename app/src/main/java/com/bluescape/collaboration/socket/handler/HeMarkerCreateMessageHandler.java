package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.model.ModelTree;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.LocationMarkerModel;
import com.bluescape.model.WorkSpaceModel;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class HeMarkerCreateMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeMarkerCreateMessageHandler.class.getSimpleName();

	public void handleMessage(JSONArray mainMessage) {
		WorkSpaceModel workSpaceModel = WorkSpaceState.getInstance().getWorkSpaceModel();

		AppConstants.LOG(AppConstants.VERBOSE, TAG, mainMessage.toString());

		LocationMarkerModel locationMarkerModel = parseLocationModel(mainMessage);
		List<LocationMarkerModel> mMarkersList;
		try {
			if (locationMarkerModel != null) {

				mMarkersList = workSpaceModel.getMarkersList();

				mMarkersList.add(locationMarkerModel);
				workSpaceModel.setMarkersList(mMarkersList);

				modelTree.add(locationMarkerModel);

			} else {
				AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null Marker from Marker Parser");
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

		// Locationmodel is DIFFERENT from NOTE and Image the third element is
		// not the target but the id itself.. I guess the assumption is that it
		// always goes on the workspace
		// 1iosmarker workspace
		// red blue red move
		// redmarker num1 id 551b10c91dd3126100000006
		// [["54d3bb68b5a05708542db8de","he","551b10c91dd3126100000006","551b10d06f1c8ca31b5c3528","markercreate",{"color":0,"x":399.8282,"name":"Num1","creationTime":1427837129,"y":510.2243}],["54f4eb6257bbe71a60acd6f8","he","551b253445c17e17a3000000","551b25346f1c8ca31b5c4388","markercreate",{"name":"bluemarker","color":3,"y":260.8729273918739,"x":-356.3133442332901,"creationTime":1427842356}],["54f4eb6257bbe71a60acd6f8","he","551b10c91dd3126100000006","551b27d66f1c8ca31b5c438a","markermove",{"y":43.86956041939678,"x":224.60821114023602}]]
		// // server <-- client
		// [client-id, "he", new-widget-id, "markercreate",{
		// "creationTime":1387565966,
		// "name":"my marker",
		// "y":1828,
		// "x":-875,
		// "color":0
		// }]
		//
		// // server --> client
		// [client-id, "he", new-widget-id, event-id, "markercreate",{
		// "creationTime":1387565966,
		// "name":"my marker",
		// "y":1828,
		// "x":-875,
		// "color":0
		// }]

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

			// TODO kris delete this why time for id ?
			// model.setID(model.getCreationTime() + "");
			model.setID(newwidgetid);

			model.setOrder(1);
			AppConstants.LOG(AppConstants.VERBOSE, TAG, "Marker ID: " + model.getID());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return model;

	}
}
