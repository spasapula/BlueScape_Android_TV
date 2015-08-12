package com.bluescape.collaboration.history;

import com.bluescape.AppConstants;
import com.bluescape.activity.HistoryListener;
import com.bluescape.model.WorkSpaceState;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;


public class HistoryCallBack extends JsonHttpResponseHandler {
    private static final String TAG = HistoryCallBack.class.getSimpleName();
    private HistoryListener historyListener;

    public HistoryCallBack() {
        this.historyListener = null;
    }

    public HistoryCallBack(HistoryListener historyListener) {
        this.historyListener = historyListener;
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        super.onFailure(statusCode, headers, responseString, throwable);
        AppConstants.LOG(AppConstants.CRITICAL, TAG, "HistoryCallBack - Failure" + responseString);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
        super.onSuccess(statusCode, headers, response);
        if (response != null) {
            if (historyListener == null) {
                AppConstants.LOG(AppConstants.VERBOSE, TAG, "HistoryCallBack - response-> " + response);
                WorkSpaceState.getInstance().getWorkSpaceModel().getWorkspaceUpdateListener().onReconnectingToNetwork(response.toString());
            } else {
                historyListener.onReceivingHistoryURLS(response.toString());
            }
        }
    }
}