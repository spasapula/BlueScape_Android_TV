package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.WorkSpaceModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class RoomMessageHandler extends BaseMessageHandler {

	public void handleMessage(JSONArray mainMessage) {

		try {

			JSONObject roomObject = (JSONObject) mainMessage.get(AppConstants.WS_JOIN_ROOM_EVENT_FROM_SERVER_MESSAGE_FORMAT.DATABAG.ordinal());
			String uid = (String) roomObject.get("uid");
			WorkSpaceModel workSpaceModel = WorkSpaceState.getInstance().getWorkSpaceModel();
			workSpaceModel.setUid(uid);
			String roomName = (String) roomObject.get("name");
			workSpaceModel.setName(roomName);
			String shareLink = (String) roomObject.get("sharing_link");
			workSpaceModel.setShareLink(shareLink);

			workSpaceModel.setWorkSpaceTitle(roomName);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}