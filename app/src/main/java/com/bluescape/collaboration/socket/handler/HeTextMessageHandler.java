package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.NoteModel;
import com.bluescape.model.widget.TextModel;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

public class HeTextMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeTextMessageHandler.class.getSimpleName();

	public void handleMessage(JSONArray mainMessage) {

		try {

			AppConstants.LOG(AppConstants.VERBOSE, TAG, "he mainMessage = " + mainMessage);

			TextModel textModel = parseTextModel(mainMessage);
			if (textModel != null) {
				AppConstants.LOG(AppConstants.INFO, TAG, "Text Model: " + textModel.toString());

				// Assume Text is only on Note for now.
				NoteModel noteModel = (NoteModel) WorkSpaceState
					.getInstance()
					.getModelTree()
					.getModel(
							mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString());

				if (noteModel != null) {
					noteModel.setText(textModel.getText());
					noteModel.setDirty(true);
					AppConstants.LOG(AppConstants.VERBOSE, TAG, "TEXT DOUBLE OK baseWidgetModel:" + noteModel.toString() + " Awesomesauce ");
				} else {
					AppConstants.LOG(AppConstants.CRITICAL, TAG,
						"TEXT NOT OK baseWidgetModel is NULL probably an Image or position marker move that we are not handling");
				}

			} else {
				AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null textModel from Message");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private TextModel parseTextModel(JSONArray mJsonArray) {

		String targetID;
		TextModel model = null;

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

			GsonBuilder builder = new GsonFireBuilder().enableHooks(TextModel.class).createGsonBuilder();
			Gson gson = builder.create();
			model = gson.fromJson(eventJsonString, TextModel.class);
			model.setTargetID(targetID);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return model;
	}
}
