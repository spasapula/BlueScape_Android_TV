package com.bluescape.collaboration.util;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.BluescapeApplication;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class GetConfiguration {
	private static final String TAG = GetConfiguration.class.getSimpleName();

	public final static AsyncHttpClient client = new AsyncHttpClient();
	private BluescapeApplication bluescapeApplication;

	public void getConfiguration(final String selectedConfig, final JsonHttpResponseHandler handler) {

		bluescapeApplication = AppSingleton.getInstance().getApplication();

		String url;

		switch (selectedConfig) {
		case AppConstants.ACCEPTANCE:
			url = AppConstants.CONFIGURATION_URLS[1];
			break;
		case AppConstants.PRODUCTION:
			url = AppConstants.CONFIGURATION_URLS[2];
			break;
		case AppConstants.INSTANCE2:
			url = AppConstants.CONFIGURATION_URLS[3];
			break;
		default:
			url = AppConstants.CONFIGURATION_URLS[0];
			break;
		}

		client.get(url, new JsonHttpResponseHandler() {

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				handler.onFailure(statusCode, headers, responseString, throwable);
			}

			@Override
			public void onFinish() {
				super.onFinish();
			}

			@Override
			public void onStart() {
				super.onStart();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				// called when response HTTP status is "200 OK"
				AppConstants.LOG(AppConstants.INFO, TAG, "response" + response.toString());
				parseConfigResponce(selectedConfig, response);

				handler.onSuccess(statusCode, headers, response);
			}

		});

	}

	private void parseConfigResponce(String selectedConfig, JSONObject response) {
		try {
			JSONObject s3 = null;
			bluescapeApplication.putDataInSharedPref(AppConstants.CONFIGURATION, selectedConfig);
			if (!response.isNull(AppConstants.PORTAL_URL))
				bluescapeApplication.putDataInSharedPref(AppConstants.PORTAL_URL, response.get(AppConstants.PORTAL_URL).toString());
			if (!response.isNull(AppConstants.HEALTH_URL))
				bluescapeApplication.putDataInSharedPref(AppConstants.HEALTH_URL, response.get(AppConstants.HEALTH_URL).toString());
			if (!response.isNull(AppConstants.BROWSE_CLIENT_URL))
				bluescapeApplication.putDataInSharedPref(AppConstants.BROWSE_CLIENT_URL, response.get(AppConstants.BROWSE_CLIENT_URL).toString());
			if (!response.isNull(AppConstants.WS_COLLABORATION_SERVICE_ADDRESS))
				bluescapeApplication.putDataInSharedPref(AppConstants.WS_COLLABORATION_SERVICE_ADDRESS,
					response.get(AppConstants.WS_COLLABORATION_SERVICE_ADDRESS).toString());
			if (!response.isNull(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS))
				bluescapeApplication.putDataInSharedPref(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS,
					response.get(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS).toString());
			if (!response.isNull(AppConstants.AUTHORIZATION_URL))
				bluescapeApplication.putDataInSharedPref(AppConstants.AUTHORIZATION_URL, response.get(AppConstants.AUTHORIZATION_URL).toString());
			if (!response.isNull(AppConstants.OAUTH_AUTHORIZATION_URL))
				bluescapeApplication.putDataInSharedPref(AppConstants.OAUTH_AUTHORIZATION_URL, response.get(AppConstants.OAUTH_AUTHORIZATION_URL)
					.toString());
			if (!response.isNull(AppConstants.ASSET_BASE_URL))
				bluescapeApplication.putDataInSharedPref(AppConstants.ASSET_BASE_URL, response.get(AppConstants.ASSET_BASE_URL).toString());
			if (!response.isNull(AppConstants.S3)) s3 = (JSONObject) response.get(AppConstants.S3);
			assert s3 != null;
			if (!s3.isNull(AppConstants.S3BUCKET))
				bluescapeApplication.putDataInSharedPref(AppConstants.S3BUCKET, s3.get(AppConstants.S3BUCKET).toString());
			if (!response.isNull(AppConstants.COLLABORATION_SERVICE_PORT))
				bluescapeApplication.putDataInSharedPref(AppConstants.COLLABORATION_SERVICE_PORT,
					response.get(AppConstants.COLLABORATION_SERVICE_PORT).toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

}
