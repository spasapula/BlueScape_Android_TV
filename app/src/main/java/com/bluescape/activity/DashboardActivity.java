package com.bluescape.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.R;
import com.bluescape.collaboration.history.HistoryCallBack;
import com.bluescape.collaboration.util.NetworkTask;
import com.bluescape.collaboration.util.TemplateParser;
import com.bluescape.util.network.WebService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.bluescape.collaboration.util.NetworkTask.fetchNoteCardImage;

public class DashboardActivity extends BaseActivity implements HistoryListener {
    // region Member Variables
    private static final String TAG = DashboardActivity.class.getSimpleName();
    private static final int DIALOG_LOADING = 1;
    private boolean isWorkspaceOpened = true;// to stop opening workspace multiple times
    private HistoryListener historyListener = this;
    private static final boolean DEBUG = true;
    private WebView dashWebview;
    private String contentIDCookie;
    private String workspaceId;
    private String centerOnObjectId;
    private String flowUrl;

    // Web view client listener
    private final WebViewClient webViewClientListener = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            dismissProgressDialog();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (url.contains(bluescapeApplication.getDataFromSharedPrefs(AppConstants.PORTAL_URL, AppConstants.PORTAL_URL) + AppConstants.DASH_URL)) {
                showProgressDialog("Loading...");
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // showProgressDialog("Please Wait .. ");
            AppConstants.LOG(AppConstants.VERBOSE, TAG, "httpResponse - " + "URL Clicked - " + url);

            String cookies = CookieManager.getInstance().getCookie(url);

            AppConstants.LOG(AppConstants.VERBOSE, TAG, "All the cookies in a string:" + cookies);

            if (url.contains("sign_in")) {
                Intent i = new Intent(DashboardActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                // clear cookies data in SharedPref
                bluescapeApplication.clearDataInSharedPref(AppConstants.SESSION_KEY);
                bluescapeApplication.clearDataInSharedPref(AppConstants.SESSION_VALUE);
                // remove all cookie
                (CookieManager.getInstance()).removeSessionCookie();
                System.exit(0);
                return false;
            } else if (url.contains("oauth/callback")) {
                contentIDCookie = cookies;
                bluescapeApplication.putDataInSharedPref(AppConstants.WORKSPACE_COOKIE, contentIDCookie);
                dismissProgressDialog();
                showDialog(DIALOG_LOADING);
                return false;
            } else if (url.contains(bluescapeApplication.getDataFromSharedPrefs(AppConstants.BROWSE_CLIENT_URL, AppConstants.BROWSE_CLIENT_URL))) {
                // Go to Workspace page on selecting a workspace.
                dismissProgressDialog();
                showDialog(DIALOG_LOADING);
                if (isWorkspaceOpened) {
                    // remove all cookies and put session cookie in cookies
                    if (contentIDCookie == null) {
                        resetCookies();
                    }
                    try {
                        // get workspaceId from url
                        URL urlObj = new URL(url);
                        workspaceId = urlObj.getPath();
                        String query = centerOnObjectId = urlObj.getQuery();
                        if (query != null && query.length() > 0) {
                            String[] params = query.split("&");
                            HashMap<String, String> parameters = new HashMap<>();
                            for (int i = 0; i < params.length; i++) {
                                String[] values = params[i].split("=");
                                parameters.put(values[0], values[1]);

                            }
                            if (parameters.get("objectId") != null) {
                                bluescapeApplication.putDataInSharedPref(AppConstants.CENTER_ON_OBJECT_ID, parameters.get("objectId"));
                            }
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    workspaceId = workspaceId.substring(1, workspaceId.length());
                    AppConstants.LOG(AppConstants.INFO, TAG, "workspaceId - " + workspaceId);
                    bluescapeApplication.putDataInSharedPref(AppConstants.KEY_WORKSPACE_ID, workspaceId);
                    if (contentIDCookie != null) {
                        dismissProgressDialog();
                        showDialog(DIALOG_LOADING);
                        getHistory();
                        isWorkspaceOpened = false;
                        return true;
                    }
                }
            } else if (workspaceId != null) {
                if (!url.contains(workspaceId)) {
                    showProgressDialog("Loading...");
                }
            } else if (url.contains("organizations")) {
                try {
                    flowUrl = (new URL(url)).getPath();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                bluescapeApplication.putDataInSharedPref(AppConstants.SESSION_URL, flowUrl);
            } else {
                showProgressDialog("Loading...");
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        private void getHistory() {
            AppConstants.LOG(AppConstants.INFO, TAG, "GetHistory() called");
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            asyncHttpClient.addHeader("Cookie", "" + contentIDCookie);
            asyncHttpClient.addHeader("user-agent", AppSingleton.getInstance().getUserAgent());
            asyncHttpClient.get(bluescapeApplication.getDataFromSharedPrefs(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS,
                    AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS) + "/" + workspaceId + "/history", new HistoryCallBack(historyListener));
        }

    };
    // end region Member Variables

    private JsonHttpResponseHandler downloadImagesCallBack = new JsonHttpResponseHandler() {
        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            super.onFailure(statusCode, headers, responseString, throwable);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            super.onSuccess(statusCode, headers, response);
            try {
                AppConstants.LOG(AppConstants.VERBOSE, TAG, "NoteList response: " + response.toString());

                TemplateParser.parseJsonResponse(new JSONArray(response.toString()));

                Map<String, String> colorURLMap = bluescapeApplication.getColorToUrlMap();
                for (String url : colorURLMap.values()) {
                    AppConstants.LOG(AppConstants.INFO, TAG, String.format("URL : %s ", url));
                    fetchNoteCardImage(url, null);
                }

            } catch (JSONException e) {
                AppConstants.LOG(AppConstants.ERROR, TAG, "Cards JSON Exception");
                e.printStackTrace();
            }
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_LOADING:
                final Dialog dialog = new Dialog(this, R.style.BluescapeThemeDark);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                // here we set layout of progress dialog
                dialog.setContentView(R.layout.custom_progress_dialog);
                dialog.setCancelable(false);
                return dialog;

            default:
                return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dash_layout);

        donNotAutoShowKeyboard();

        Intent in = getIntent();
        boolean fromFlag = in.getBooleanExtra(AppConstants.FROM_FLAG, AppConstants.FROM_LOGIN);

        // Get list of the note card urls
        NetworkTask noteTask = new NetworkTask.getNoteList();
        WebService webService = new WebService();
        webService.makeCall(noteTask, null, downloadImagesCallBack);

        dashWebview = (WebView) findViewById(R.id.dashWebview);
        WebView.setWebContentsDebuggingEnabled(true);
        final WebSettings settings = dashWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);

        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Default UserAgent= " + android.webkit.WebSettings.getDefaultUserAgent(getApplicationContext()));
        settings.setUserAgentString(android.webkit.WebSettings.getDefaultUserAgent(getApplicationContext()));
        Map<String, String> headerMap = new HashMap<>();
        String cookieString = bluescapeApplication.getDataFromSharedPrefs(AppConstants.SESSION_KEY, "") + "=" + bluescapeApplication.getDataFromSharedPrefs(AppConstants.SESSION_VALUE, "");
        headerMap.put(AppConstants.COOKIE, cookieString);

        dashWebview.setWebViewClient(webViewClientListener);
        dashWebview.setWebChromeClient(new WebChromeClient());

        if (fromFlag) {
            dashWebview.loadUrl(bluescapeApplication.getDataFromSharedPrefs(AppConstants.PORTAL_URL, AppConstants.PORTAL_URL) + AppConstants.DASH_URL, headerMap);
        } else {
            dashWebview.loadUrl(bluescapeApplication.getDataFromSharedPrefs(AppConstants.PORTAL_URL, AppConstants.PORTAL_URL) + bluescapeApplication.getDataFromSharedPrefs(AppConstants.SESSION_URL, AppConstants.DASH_URL), headerMap);
        }
    }

    @Override
    public void onBackPressed() {
        WebBackForwardList mWebBackForwardList = dashWebview.copyBackForwardList();
        if (mWebBackForwardList.getSize() > 0 && mWebBackForwardList.getCurrentIndex() > 0) {
            dashWebview.goBack();
        } else {
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
    @Override
    public void onReceivingHistoryURLS(String historyURLS) {
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "HistoryCallBack - response-> " + historyURLS);
        // Here Navigation
        Intent workspaceIntent = new Intent(DashboardActivity.this, MainActivity.class);
        workspaceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        workspaceIntent.putExtra(AppConstants.HISTORY_URL_ARRAY, historyURLS.toString());
        startActivity(workspaceIntent);
    }
}
