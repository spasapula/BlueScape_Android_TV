package com.bluescape.collaboration.socket.sender;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.ImageModel;
import com.bluescape.collaboration.util.NetworkTask;
import com.bluescape.util.network.SimpleMultipartEntity;
import com.bluescape.util.network.WebService;
import com.loopj.android.http.DataAsyncHttpResponseHandler;

import org.apache.http.Header;
import org.bson.types.ObjectId;

import java.io.File;
import java.util.Arrays;

public class HeImageCreateMessageSender extends BaseMessageSender {

	private class sendImageCallBack extends DataAsyncHttpResponseHandler {
		@Override
		public void onCancel() {
			WorkSpaceState.getInstance().getWorkSpaceModel().getWorkspaceUpdateListener().stopProgressDialog();
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "from Android sendImageCallBack Request got callback cancel from server.");
		}

		@Override
		public void onFailure(int i, Header[] headers, byte[] responseBody, Throwable throwable) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG,
				"In onFailure responseBody=" + Arrays.toString(responseBody) + " headers =" + Arrays.toString(headers) + " i:" + i + "throwable"
						+ throwable);
			WorkSpaceState.getInstance().getWorkSpaceModel().getWorkspaceUpdateListener().stopProgressDialog();

		}

		@Override
		public void onStart() {
			super.onStart();
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "Calling onStart");
		}

		@Override
		public void onSuccess(int i, Header[] headers, byte[] responseBody) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, new String(responseBody));
			AppConstants.LOG(AppConstants.CRITICAL, TAG, " HTTP RESPONSE: " + i + " In onSuccess responseBody=" + new String(responseBody)
															+ " headers =" + Arrays.toString(headers) + " i:" + i);
			WorkSpaceState.getInstance().getWorkSpaceModel().getWorkspaceUpdateListener().stopProgressDialog();
		}
	}

	// HTTP POST
	// curl -F "file=@photo.JPG" -F "x=236" -F "y=832" -F "x2=361" -F "y2=943"
	// -F "clientId=10" -F "arrayId=10" -F "order=23"
	// "<http_collaboration_service_address>/<workspaceId>/object/upload"
	// Request
	// URL:https://staging.collaboration.bluescape.com/UJf2V-6zJvsBxcm4K_9j/object/upload

	private static final String TAG = HeImageCreateMessageSender.class.getSimpleName();

	final private SimpleMultipartEntity simpleMultipartEntity;

	public HeImageCreateMessageSender(ImageModel imageModel) {

		simpleMultipartEntity = new SimpleMultipartEntity(new sendImageCallBack());

		File imageFile = new File(WorkSpaceState.getInstance().getImageRealPath());

		try {
			simpleMultipartEntity.addPart("file", imageFile, "image/jpeg");
			// simpleMultipartEntity.addPart("file", imageFile,"image/jpg");
			// simpleMultipartEntity.addPart("file",
			// imageFile,"multipart/form-data");

		} catch (Exception e) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "Image File probably not found Exception e:" + e);
		}

		simpleMultipartEntity.addPart("clientId", WorkSpaceState.getInstance().getClientId());
		simpleMultipartEntity.addPart("arrayId", WorkSpaceState.getInstance().getClientId());
		simpleMultipartEntity.addPart("sessionId", WorkSpaceState.getInstance().getWorkSpaceModel().getId());
		simpleMultipartEntity.addPart("id", (new ObjectId().toString()));

		WorkSpaceState.getInstance().updateOrder(WorkSpaceState.getInstance().getGlobalOrder()+3);
		simpleMultipartEntity.addPart("order", Float.toString(WorkSpaceState.getInstance().getGlobalOrder()));

		simpleMultipartEntity.addPart("x", imageModel.getRect().getTOPX() + "");
		simpleMultipartEntity.addPart("y", imageModel.getRect().getTOPY() + "");
		simpleMultipartEntity.addPart("x2", imageModel.getRect().getBOTX() + "");
		simpleMultipartEntity.addPart("y2", imageModel.getRect().getBOTY() + "");

	}

	public void send() {

		NetworkTask sendImageTask = new NetworkTask.sendImageTask();
		WebService webService = new WebService();
		webService.makeMultipartPostCall(sendImageTask, simpleMultipartEntity, new sendImageCallBack());
	}
}
