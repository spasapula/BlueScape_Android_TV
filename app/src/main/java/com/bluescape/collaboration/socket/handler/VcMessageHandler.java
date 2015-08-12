package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.model.WorkSpaceModel;
import com.bluescape.view.ViewUtils;

import org.json.JSONArray;

public class VcMessageHandler extends BaseMessageHandler {

	private static final String TAG = VcMessageHandler.class.getSimpleName();

	// // server <--> client
	// [sender-id, "vc", viewport-rect]

	public void handleMessage(JSONArray mainMessage) {
		WorkSpaceModel workSpaceModel = WorkSpaceState.getInstance().getWorkSpaceModel();
		try {
			JSONArray vcJson = (JSONArray) mainMessage.get(AppConstants.WS_VIEWPORT_CHANGE_EVENT_FROM_SERVER_MESSAGE_FORMAT.VIEWPORT_RECT.ordinal());
			// //VIEWPORT_RECT viewport-rect an array in the form [x1, y1, x2,
			// y2] top left to bottom right representing the section of the
			// workspace viewable on the sending client.
			float[] viewPort = new float[4];
			for (int vcCount = 0; vcCount < vcJson.length(); vcCount++) {
				viewPort[vcCount] = vcJson.getInt(vcCount);
			}
			float offsetX = (viewPort[Rect.BOTX] + viewPort[Rect.TOPX]) / 2;
			float offsetY = (viewPort[Rect.BOTY] + viewPort[Rect.TOPY]) / 2;

			float mZoom = ViewUtils.getZoomFromVcRect(viewPort[Rect.TOPY], viewPort[Rect.BOTY]);

			//WorkSpaceState.getInstance().mHistoryLoadCompleted = true;
			String senderId = (String) mainMessage.get(AppConstants.WS_VIEWPORT_CHANGE_EVENT_FROM_SERVER_MESSAGE_FORMAT.SENDER_ID.ordinal());

			if (WorkSpaceModel.followingClientId != null) {

				if (senderId != null)

					if (senderId.equals(WorkSpaceModel.followingClientId)) {
						WorkSpaceState.getInstance().setZoomAndOffset(mZoom, offsetX, offsetY);
						AppConstants.LOG(AppConstants.CRITICAL, TAG,
							"Received vc message Ignoring until follow me VcMessageHandler" + mainMessage.toString());
					} else {

						workSpaceModel.updateRoomMemberVC(senderId, viewPort);

					}

				workSpaceModel.updateRoomMemberVC(senderId, viewPort);

			} else {
				workSpaceModel.updateRoomMemberVC(senderId, viewPort);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
