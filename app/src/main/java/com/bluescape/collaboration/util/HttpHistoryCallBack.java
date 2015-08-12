package com.bluescape.collaboration.util;

import android.util.Log;

import com.bluescape.AppConstants;
import com.bluescape.activity.MainActivity;
import com.bluescape.collaboration.history.HistoryOrganizer;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;

/**
 * Created by keefe on 6/24/15.
 */
public class HttpHistoryCallBack extends JsonHttpResponseHandler {

    private static String TAG = "com.bluescape.collaboration.util.HttpHistoryCallback";
    final String path;
    final String lastPath;
    final HistoryOrganizer organizer;
    private MainActivity activity;

    public HttpHistoryCallBack(String path, String lastPath, HistoryOrganizer organizer, MainActivity activity) {
        this.path = path;
        this.organizer = organizer;
        this.lastPath = lastPath;
        this.activity = activity; //#TODO sort out integration with mainactivity better
    }

    // Ht
    // tpHistoryCallBack
    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        AppConstants.LOG(AppConstants.CRITICAL, TAG, "LOAD FAILED " + path);

        super.onFailure(statusCode, headers, responseString, throwable);
        activity.historyJSONObjCount++; //#TODO noooooo static public access, this must go

        if (activity.historyJSONObjSize == activity.historyJSONObjCount) {
            activity.dismissDialog(1);
        }

        AppConstants.LOG(AppConstants.VERBOSE, TAG, "HttpHistoryCallBack - Failed " + responseString);
    }
    public long startTheClock;

    // HttpHistoryCallBack
    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
        AppConstants.LOG(AppConstants.CRITICAL, TAG, "LOAD COMPLETE " + path + " at " + (System.currentTimeMillis() - startTheClock));
        if (response != null) {
            organizer.specifyHistory(path, response);
        }

        super.onSuccess(statusCode, headers, response);

        if (response != null) {
            final JSONArray responseInside = response;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean visitedStatus = activity.getDbObject().isAlreadyRetrievedWorkspaceJSON(activity.getDbObject(), activity.getWorkspaceId(), path);

                    if (!visitedStatus && !(lastPath.equals(path))) { // restriction
                        // last json
                        // not to
                        // save in
                        // db

                        AppConstants.LOG(AppConstants.INFO, TAG, "Saved - " + false + activity.historyJSONObjCount);
                        activity.getDbObject().insertIntoWorkspacesHistory(activity.getDbObject(), activity.getWorkspaceId(), path, responseInside.toString());
                    }
                    Log.d("Call - " + visitedStatus, activity.historyJSONObjCount + "");
                }
            }).start();
            // new Thread(new HistoryHandler(MainActivity.this,
            // workspaceUpdateListener, response.toString())).start();

        }
    }
}
