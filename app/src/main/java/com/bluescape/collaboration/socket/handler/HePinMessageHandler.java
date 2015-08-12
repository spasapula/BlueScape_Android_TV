package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.ModelTree;
import com.bluescape.model.widget.BaseWidgetModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class HePinMessageHandler extends BaseMessageHandler {

	public void handleMessage(JSONArray mainMessage) {

		try {
			JSONObject eventJsonObj = (JSONObject) mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal());
			boolean isPin = eventJsonObj.getBoolean("pin");
			// target-id
			String targetID = mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();
			updatePinInNoteModel(targetID, isPin);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void updatePinInNoteModel(String ID, boolean isPin) {
		BaseWidgetModel baseWidgetModel = modelTree.getModel(ID);
		if (baseWidgetModel != null)
			baseWidgetModel.setPinned(isPin);
		else
			AppConstants.LOG(AppConstants.CRITICAL, "HePinMessageHandler", "Unable to PIN baseWidgetModel.ID: " + ID + " is null");

	}
}
