package com.bluescape.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.collaboration.util.GetConfiguration;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

public class SplashActivity extends BaseActivity {

    private class ConfigurationResponseHandler extends JsonHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            super.onFailure(statusCode, headers, responseString, throwable);
            // Toast.makeText(getApplicationContext(),
            // "We are unable to process your request. Please try later.",
            // Toast.LENGTH_LONG).show();
            showAlert("", "We are unable to process your request. Please try later.", "TRY AGAIN", tryAgain, null, null, false);
        }

        @Override
        public void onStart() {
            super.onStart();
            showProgressDialog("Please wait..");
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            if (bluescapeApplication.getDataFromSharedPrefs(AppConstants.SESSION_VALUE, null) == null) {
                // if no session available
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                // if session available
                Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private DialogInterface.OnClickListener tryAgain = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            initialCall();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        initialCall();
    }

    private void initialCall() {
        if (isNetworkAvailable()) {
            if (AppConstants.DEBUG) {
                String configuration = bluescapeApplication.getDataFromSharedPrefs(AppConstants.CONFIGURATION, AppConstants.STAGING);
                new GetConfiguration().getConfiguration(configuration, new ConfigurationResponseHandler());
            } else {
                String configuration = bluescapeApplication.getDataFromSharedPrefs(AppConstants.CONFIGURATION, AppConstants.PRODUCTION);
                new GetConfiguration().getConfiguration(configuration, new ConfigurationResponseHandler());
            }
        } else {
            showAlert("", "Network not available.", "TRY AGAIN", tryAgain, null, null, false);
        }
    }
}
