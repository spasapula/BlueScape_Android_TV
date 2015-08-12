package com.bluescape.util.network;

import android.os.Bundle;

import com.bluescape.collaboration.util.NetworkTask;
import com.bluescape.model.WorkSpaceState;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class WebService {

	/**
	 * URLS for the webervice calls
	 */

	public void makeCall(NetworkTask task, RequestParams requestParams, AsyncHttpResponseHandler listener) {

		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		Bundle header = task.getHeader();

		if (header != null) addHeaders(asyncHttpClient, header);

		// Make appropriate call depending on the method.
		switch (task.getMethod()) {
		case GET:
			asyncHttpClient.get(task.getUrl(), listener);
			break;
		case POST:
			asyncHttpClient.post(task.getUrl(), requestParams, listener);
			break;
		case PUT:
			asyncHttpClient.put(task.getUrl(), requestParams, listener);
			break;
		}
	}

	public void makeMultipartPostCall(NetworkTask task, SimpleMultipartEntity simpleMultipartEntity, AsyncHttpResponseHandler listener) {

		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

		Bundle header = task.getHeader();

		if (header != null) addHeaders(asyncHttpClient, header);

		// String tmpEndPoint =
		// "https://staging.collaboration.bluescape.com/w7cJdnK3ywXtKpv1xEWQ/object/upload";
		// asyncHttpClient.post(WorkSpaceState.getInstance().getWorkspaceView().getContext(),
		// tmpEndPoint, simpleMultipartEntity,"multipart/form-data",listener);

		asyncHttpClient.post(WorkSpaceState.getInstance().getWorkspaceView().getContext(), task.getUrl(), simpleMultipartEntity,
			"multipart/form-data", listener);

	}

	/**
	 * This method appends all the headers from the bundle into the async header
	 * 
	 * @param asyncHttpClient
	 * @param header
	 */
	private void addHeaders(AsyncHttpClient asyncHttpClient, Bundle header) {
		for (String key : header.keySet()) {
			asyncHttpClient.addHeader(key, header.getString(key));
		}
	}
}
