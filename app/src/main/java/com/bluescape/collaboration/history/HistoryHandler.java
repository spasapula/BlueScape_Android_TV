package com.bluescape.collaboration.history;

import android.app.Activity;
import android.os.Looper;

import com.bluescape.AppConstants;
import com.bluescape.activity.MainActivity;
import com.bluescape.collaboration.socket.HandlerFactory;
import com.bluescape.collaboration.socket.IHandler;
import com.bluescape.activity.WorkspaceUpdateListener;

import org.json.JSONArray;
import org.json.JSONException;

public class HistoryHandler implements Runnable {

	private static final String TAG = HistoryHandler.class.getSimpleName();
	/**
	 * Keys for JSON Arrays.
	 */
	public static final int CLIENT_ID = 0;
	public static final int MESSAGE_TYPES = 1;
	public static final int TARGET_ID = 2;
	public static final int EVENT_ID = 3;
	public static final int HISTORY_EVENT = 4;
	public static final int EVENT_OBJECT = 5;
	private final Activity mContext;
	private final String historyJsonString;
	private final WorkspaceUpdateListener workspaceUpdateListener;

	public HistoryHandler(Activity mContext, WorkspaceUpdateListener workspaceUpdateListener, String historyJsonString) {
		this.mContext = mContext;
		this.historyJsonString = historyJsonString;
		this.workspaceUpdateListener = workspaceUpdateListener;
	}

	@Override
	public void run() {
		Looper.prepare();
		parseHistory();
	}

	private void parseHistory() {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "XXJSON: \n" + historyJsonString);

		try {
			JSONArray historyJsonArray = new JSONArray(historyJsonString);

			AppConstants.LOG(AppConstants.CRITICAL, TAG, "XXJSON TotalHistoryCount - " + historyJsonArray.length());

			for (int historyCount = 0; historyCount < historyJsonArray.length(); historyCount++) {

				JSONArray eventJsonArray = (JSONArray) historyJsonArray.get(historyCount);
				String event = eventJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal()).toString();

				AppConstants.LOG(AppConstants.VERBOSE, TAG, historyCount + "-" + event);
				IHandler handler = HandlerFactory.getInstance().getHandler(event);
				if (handler != null) {
					handler.handleMessage(eventJsonArray);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		MainActivity.historyJSONObjCount++;
		AppConstants.LOG(AppConstants.INFO, TAG, "historyJSONObjSize: " + MainActivity.historyJSONObjSize + "A");
		AppConstants.LOG(AppConstants.INFO, TAG, "historyJSONObjCount: " + MainActivity.historyJSONObjCount + "A");
		if (MainActivity.historyJSONObjSize == MainActivity.historyJSONObjCount) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "XXJSON: Complete Out Of This Call : \n" + historyJsonString);

			mContext.dismissDialog(AppConstants.DIALOG_LOADING);
			workspaceUpdateListener.callSocket();
		}
	}
}
