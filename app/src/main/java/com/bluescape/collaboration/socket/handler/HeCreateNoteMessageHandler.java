package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.ModelTree;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.NoteModel;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

public class HeCreateNoteMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeCreateNoteMessageHandler.class.getSimpleName();

	/**
	 * Static factory Method
	 * http://www.drdobbs.com/jvm/creating-and-destroying-java
	 * -objects-par/208403883?pgno=1
	 *
	 * @param mJsonArray
	 * @return
	 */
	private GsonBuilder builder = new GsonFireBuilder().enableHooks(NoteModel.class).createGsonBuilder();

	private Gson gson = builder.create();

	public void handleMessage(JSONArray mainMessage) {

		try {

			AppConstants.LOG(AppConstants.VERBOSE, TAG, mainMessage.toString());

			BaseWidgetModel baseWidgetModel = parseNoteModel(mainMessage);

			if (baseWidgetModel != null) {

				modelTree.add(baseWidgetModel);
			} else {
				AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null note from Note Parser");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public NoteModel parseNoteModel(JSONArray mJsonArray) {

		NoteModel model = null;
		String targetID;

		// client-id
		try {

			// client-id
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal());

			// Message Types
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.HE.ordinal());

			// target-id
			targetID = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();

			// event-id
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_ID.ordinal());

			// History Event
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal());

			// event Json Object
			String eventJsonString = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal()).toString();

			model = gson.fromJson(eventJsonString, NoteModel.class);
			model.setTargetID(targetID);
			// SEt the ZOrder as the Layer Constant
			// model.mEventOrder is set according to the order sent in he and ve
			// events
			// now set the global mGlobalWorkSpaceEventOrder to the highest one
			WorkSpaceState.getInstance().updateOrder(model.getOrder());

			// Check for erroneous scales that set order to -4
			if (model.getOrder() < 0) model.setOrder(1);

			AppConstants.LOG(AppConstants.VERBOSE, TAG, "Note ID: " + model.getID());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return model;

	}
}
