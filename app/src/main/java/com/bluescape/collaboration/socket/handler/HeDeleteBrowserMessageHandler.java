package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BaseWidgetModel;

import org.json.JSONArray;

public class HeDeleteBrowserMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeDeleteBrowserMessageHandler.class.getSimpleName();

	public void handleMessage(JSONArray mainMessage) {

		try {

			AppConstants.LOG(AppConstants.VERBOSE, TAG, mainMessage.toString());

			if (!WorkSpaceState.getInstance().mHistoryLoadCompleted
				|| !WorkSpaceState.getInstance().getClientId()
					.equalsIgnoreCase(mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal()).toString())) {
				AppConstants.LOG(AppConstants.VERBOSE, TAG,
					"DIFFERENT CLIENT message from Server so processing HeDeleteMessageHandler  mainMessage: " + mainMessage + "Client-id : "
							+ WorkSpaceState.getInstance().getClientId());

				BaseWidgetModel baseWidgetModel = WorkSpaceState
					.getInstance()
					.getModelTree()
					.getModel(
							mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString());

				if (baseWidgetModel != null) {
					WorkSpaceState
						.getInstance()
						.getModelTree()
						.delete(
								mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString());
				} else {
					AppConstants.LOG(AppConstants.CRITICAL, TAG,
						"DELETE NOT OK baseWidgetModel is NULL probably odd case as the target should be in getModelTree().mWorkspaceModels");
				}

			} else {
				AppConstants.LOG(AppConstants.VERBOSE, TAG, "SAME CLIENT message from Server so SKIPPING HeDeleteMessageHandler  mainMessage: "
															+ mainMessage + "Client-id : " + WorkSpaceState.getInstance().getClientId());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
