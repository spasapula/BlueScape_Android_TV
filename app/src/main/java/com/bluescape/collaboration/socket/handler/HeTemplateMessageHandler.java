package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.change.ChangeTemplateModel;
import com.bluescape.model.widget.NoteModel;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

public class HeTemplateMessageHandler extends BaseMessageHandler {
	private static final String TAG = "com.bluescape.collaboration.socket.handlers.HeTemplateMessageHandler";

	public void handleMessage(JSONArray mainMessage) {
		ChangeTemplateModel changeTemplateModel;

		changeTemplateModel = parseChangeTemplate(mainMessage);

		String targetID = changeTemplateModel.getTargetID();

		String baseName = changeTemplateModel.getmBaseName();

		NoteModel noteModel = (NoteModel) WorkSpaceState.getInstance().getModelTree().getModel(targetID);

		if (noteModel != null) {
			noteModel.setBaseName(baseName);

			// if glInitialized then create again or else we are good
			if (noteModel.isModelGLInitialized) {
				noteModel.mTemplateChanged = true;
			}
		} else {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "Weird Error Could not find parent NoteCard for Template Change in FullHitoryMap");
		}
	}

	public ChangeTemplateModel parseChangeTemplate(JSONArray mJsonArray) {
		ChangeTemplateModel model = null;
		String targetID;

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

			GsonBuilder builder = new GsonFireBuilder().enableHooks(NoteModel.class).createGsonBuilder();
			Gson gson = builder.create();
			model = gson.fromJson(eventJsonString, ChangeTemplateModel.class);
			model.setTargetID(targetID);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return model;
	}
}
