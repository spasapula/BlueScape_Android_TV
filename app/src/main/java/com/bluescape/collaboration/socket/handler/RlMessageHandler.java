package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.CollaboratorModel;
import com.bluescape.model.WorkSpaceModel;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.model.widget.NoteModel;
import com.bluescape.util.ColorQueue;
import com.bluescape.util.ColorUtil;
import com.bluescape.view.ViewUtils;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RlMessageHandler extends BaseMessageHandler {

	private static final String TAG = RlMessageHandler.class.getSimpleName();
	List<CollaboratorModel> membersList;

	public void handleMessage(JSONArray mainMessage) {
		try {
			membersList = new ArrayList<>();
			JSONArray memberJsonArray = (JSONArray) mainMessage.get(AppConstants.WS_ROOM_LIST_EVENT_FROM_SERVER_MESSAGE_FORMAT.ROOM_MEMBERSHIP_LIST
				.ordinal());

			for (int roomMemberCount = 0; roomMemberCount < memberJsonArray.length(); roomMemberCount++) {
				JSONObject memberJsonObj;

				memberJsonObj = (JSONObject) memberJsonArray.get(roomMemberCount);
				CollaboratorModel roomListModel = parseCollaborator(memberJsonObj);
				// Add to member list only if this is not the current clientId
				// from server like the ipad client. That way except current
				// clientID all the other clients are shown
				if (roomListModel != null && !roomListModel.getClientId().equalsIgnoreCase(WorkSpaceState.getInstance().getClientId())) {
					if (!isExistingCollaborator(roomListModel)) membersList.add(roomListModel);
					AppConstants.LOG(AppConstants.VERBOSE, TAG, "Other Room Member adding roomListModel = " + roomListModel.getClientId());

				} else {
					assert roomListModel != null;
					AppConstants.LOG(AppConstants.VERBOSE, TAG, "Current Room Member NOT adding roomListModel = " + roomListModel.getClientId());

					AppConstants.LOG(AppConstants.CRITICAL, TAG, "Current Room Member ADDING TO View PORT = " + roomListModel.getClientId());

					// Add the VC only for the right clientId i.e the current
					// client ignore the others in the workspace
					// with viewPort
					// ["-1","rl",[{"name":"Kris Maganti","device_type":"other","clientType":"ipad","clientId":"54d3bb68b5a05708542db8de","viewPort":[477.3223,-26.36306,1583.954,803.6109]},{"name":"Kris Maganti","device_type":"wall","clientType":"wall","clientId":"552838120044016c7666f255","viewPort":[477.3223,-26.36306,1583.954,803.6109]}]]
					// without viewPort
					// ["-1","rl",[{"name":"Kris Maganti","device_type":"wall","clientType":"wall","clientId":"55283558c17e3df779a8599a"}]]

					// Here the getViewPort returns float[] viewPort = {-1800f,
					// -1800,1800,1800}; by default if none sent
					float[] viewPort = roomListModel.getViewPort();

					if (viewPort != null) {

						float offsetX = (viewPort[Rect.BOTX] + viewPort[Rect.TOPX]) / 2;
						float offsetY = (viewPort[Rect.BOTY] + viewPort[Rect.TOPY]) / 2;
						// update the mZoom so the local zoom level is set for
						// next touch gesture on the workspace
						float mZoom = ViewUtils.getZoomFromVcRect(viewPort[Rect.TOPY], viewPort[Rect.BOTY]);

						// update the ModelTree.mViewPortHistoryModelMap so that
						// only the right drawables are drawn
						// use WorkSpaceState.getInstance().mWorldPosition as
						// default
					    WorkSpaceState.getInstance().setZoomAndOffset(mZoom, offsetX, offsetY);
						WorkSpaceState.getInstance().mHistoryLoadCompleted = true;

					}

				}

			}

			WorkSpaceModel workSpaceModel = WorkSpaceState.getInstance().getWorkSpaceModel();
			workSpaceModel.setRoomMembersList(membersList);
			if (WorkSpaceModel.followingClientId != null)
				if (!WorkSpaceState.getInstance().getWorkSpaceModel().isCollaboratorAvailable()) {
					WorkSpaceModel.followingClientId = null;
					WorkSpaceState.getInstance().getWorkSpaceModel().getWorkspaceUpdateListener().onFollowingUserExit();

				}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	boolean isExistingCollaborator(CollaboratorModel collaboratorModel) {

		for (CollaboratorModel model : membersList) {
			if (model.getClientId().equals(collaboratorModel.getClientId())) { return true; }
		}

		return false;
	}

	private CollaboratorModel parseCollaborator(JSONObject mJsonObject) {
		CollaboratorModel model;
		GsonBuilder builder = new GsonFireBuilder().enableHooks(NoteModel.class).createGsonBuilder();
		Gson gson = builder.create();
		model = gson.fromJson(mJsonObject.toString(), CollaboratorModel.class);
		model.setColor(ColorQueue.getInstance().getColor());
		model.setRgbColor(ColorUtil.getNormalisedColor(ColorUtil.getNativeColor(model.getColor())));
		return model;
	}

}
