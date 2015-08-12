package com.bluescape.collaboration.socket.handler;

import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.WorkSpaceModel;

import org.json.JSONArray;

public class HeMarkerDeleteMessageHandler extends BaseMessageHandler {

	// server --> client
	// [client-id, "he", marker-id, event-id, "markerdelete",{}]

	private static final String TAG = HeMarkerDeleteMessageHandler.class.getSimpleName();

	public void handleMessage(JSONArray mainMessage) {
		WorkSpaceModel workSpaceModel = WorkSpaceState.getInstance().getWorkSpaceModel();

		try {
			String markerId = (String) mainMessage.get(2);

			workSpaceModel.deleteMarker(markerId);
			modelTree.delete(markerId);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
