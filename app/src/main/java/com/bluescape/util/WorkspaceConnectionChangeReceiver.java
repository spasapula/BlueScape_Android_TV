package com.bluescape.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.BluescapeApplication;
import com.bluescape.collaboration.history.HistoryCallBack;
import com.loopj.android.http.AsyncHttpClient;

public class WorkspaceConnectionChangeReceiver extends BroadcastReceiver {
    private static final String TAG = WorkspaceConnectionChangeReceiver.class.getSimpleName();

    protected final BluescapeApplication bluescapeApplication = AppSingleton.getInstance().getApplication();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (isNetworkAvilable(context)) {
            AppConstants.LOG(AppConstants.INFO, TAG, "GetHistory() called");
            getHistory();
        } else {
            AppConstants.LOG(AppConstants.VERBOSE, TAG, "Network Lost");
        }
    }

    public boolean isNetworkAvilable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in air plan mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private void getHistory() {
        AppConstants.LOG(AppConstants.INFO, TAG, "GetHistory() called");
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.addHeader("Cookie",bluescapeApplication.getDataFromSharedPrefs(AppConstants.WORKSPACE_COOKIE, ""));
        asyncHttpClient.addHeader("user-agent", AppSingleton.getInstance().getUserAgent());
        asyncHttpClient.get(bluescapeApplication.getDataFromSharedPrefs(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS,
                AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS) + "/" + bluescapeApplication.getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, "") + "/history", new HistoryCallBack());
    }


}
