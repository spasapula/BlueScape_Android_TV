package com.bluescape.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.R;
import com.bluescape.collaboration.util.GetConfiguration;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.DataAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends BaseActivity {
    private class ConfigurationResponseHandler extends JsonHttpResponseHandler {

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            super.onFailure(statusCode, headers, responseString, throwable);
            dismissProgressDialog();
            Toast.makeText(getApplicationContext(), "We are unable to process your request. Please try later.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            setActionbarTitle();
            dismissProgressDialog();
        }
    }

    private class ContactUsCallBack extends DataAsyncHttpResponseHandler {
        final Dialog contactUsDialogView;

        ContactUsCallBack(Dialog contactUsDialogView) {
            this.contactUsDialogView = contactUsDialogView;
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "ContactUsCallBack - Failure");
            showAlert("Error", "There was an error submitting your request. Please try again.", null, null, null, null, false);
            dismissProgressDialog();
            contactUsDialogView.dismiss();

        }

        @Override
        public void onStart() {
            super.onStart();
            showProgressDialog("Please wait..");
        }

        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            showAlert("Success", "Your support request was successfully submitted.", null, null, null, null, false);
            dismissProgressDialog();
            contactUsDialogView.dismiss();
        }
    }

    private class loginAsyncTask extends AsyncTask<Object, String, String> {

        @Override
        protected String doInBackground(Object... params) {
            String status = null;// login status

            try {
                URI url = URI.create(baseUrl + AppConstants.SIGNIN_URL);

                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);

                // Constuct user object
                Map<String, Map<String, String>> user = new HashMap<>();
                Map<String, String> userParams = new HashMap<>();
                userParams.put(AppConstants.EMAIL, email);
                userParams.put(AppConstants.PASSWORD, password);
                user.put(AppConstants.USER, userParams);

                // Convert the user object to json
                String json = new GsonBuilder().create().toJson(user, Map.class);

                httpPost.setEntity(new StringEntity(json));
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                httpPost.setHeader("user-agent",
                        AppSingleton.getInstance().getUserAgent());

                android.webkit.WebSettings.getDefaultUserAgent(getApplicationContext());

                AppConstants.LOG(AppConstants.CRITICAL, TAG,
                        "getDefaultUserAgent= " + android.webkit.WebSettings.getDefaultUserAgent(getApplicationContext()));

                HttpResponse httpResponse = httpClient.execute(httpPost);

                StatusLine statusLine = httpResponse.getStatusLine();
                int statusCode = statusLine.getStatusCode();

                // Check for a success status code
                if (statusCode == 200 || statusCode == 201) {
                    // making login status success
                    status = "success";
                    // Get cookies
                    List<Cookie> cookiejar = httpClient.getCookieStore().getCookies();

                    Cookie sessionCookie = cookiejar.get(0);

                    // putting data in Cookie
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.setCookie(baseUrl, sessionCookie.getName() + "=" + sessionCookie.getValue());

                    // putting session in SharedPreferences
                    bluescapeApplication.putDataInSharedPref(AppConstants.SESSION_KEY, sessionCookie.getName());
                    bluescapeApplication.putDataInSharedPref(AppConstants.SESSION_VALUE, sessionCookie.getValue());

                } else {
                    Log.e(TAG, "Error signing in - status code: " + statusCode);
                }
                AppConstants.LOG(AppConstants.VERBOSE, TAG, "httpResponse - " + httpResponse.toString());
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
                dismissProgressDialog();
            }

            return status;
        }

        @Override
        protected void onPostExecute(String status) {
            super.onPostExecute(status);

            if (status != null) {
                // Launch DashboardActivity with sessionId cookie
                Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                dashboardIntent.putExtra(AppConstants.FROM_FLAG, AppConstants.FROM_LOGIN);
                startActivity(dashboardIntent);
                // finish();
            } else {
                Toast.makeText(LoginActivity.this, "Error signing in! Check your email and password.", Toast.LENGTH_SHORT).show();
            }

            dismissProgressDialog();
        }
    }

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText emailEt, passwordEt;

    private String email, password;

    private String baseUrl;

    // used in Contact us dialog - type of Issue
    private String type = "Problem";

    private final OnEditorActionListener doneListener = new OnEditorActionListener() {

        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                readLoginCredentials();
                loginAction();
                return true;
            } else {
                return false;
            }
        }
    };

    private final OnClickListener loginBtnListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            readLoginCredentials();
            loginAction();

        }
    };

    private final OnClickListener forgotPasswordTvListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            forgotPassword();
        }
    };

    private final OnClickListener contactUsTvListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            contactUs();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppConstants.DEBUG) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.login, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.acceptance:

                new GetConfiguration().getConfiguration(AppConstants.ACCEPTANCE, new ConfigurationResponseHandler());

                return true;
            case R.id.production:
                bluescapeApplication.putDataInSharedPref(AppConstants.CONFIGURATION, AppConstants.PRODUCTION);
                new GetConfiguration().getConfiguration(AppConstants.PRODUCTION, new ConfigurationResponseHandler());

                return true;
            case R.id.staging:
                bluescapeApplication.putDataInSharedPref(AppConstants.CONFIGURATION, AppConstants.STAGING);
                new GetConfiguration().getConfiguration(AppConstants.STAGING, new ConfigurationResponseHandler());

                return true;
            case R.id.instance2:
                bluescapeApplication.putDataInSharedPref(AppConstants.CONFIGURATION, AppConstants.INSTANCE2);
                new GetConfiguration().getConfiguration(AppConstants.INSTANCE2, new ConfigurationResponseHandler());

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.login_app_bar);
        setSupportActionBar(toolbar);

        // Don't auto-show the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        pullViewsAndSetListeners();

        setActionbarTitle();
    }

    private void contactUs() {
        contactUsDialog();
    }

    private void contactUsDialog() {

        final Dialog contactUsDialogView = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        contactUsDialogView.setTitle("");
        contactUsDialogView.setContentView(R.layout.contact_us_dialog);
        contactUsDialogView.setCancelable(true);

        RelativeLayout contactUSLayout = (RelativeLayout) contactUsDialogView.findViewById(R.id.contactUSLayout);

        final EditText nameEt = (EditText) contactUsDialogView.findViewById(R.id.nameEt);
        final EditText emailEt = (EditText) contactUsDialogView.findViewById(R.id.emailEt);
        final EditText companyEt = (EditText) contactUsDialogView.findViewById(R.id.companyEt);
        final EditText subjectEt = (EditText) contactUsDialogView.findViewById(R.id.subjectEt);
        final EditText descriptionEt = (EditText) contactUsDialogView.findViewById(R.id.descriptionEt);

        final Button problemBtn = (Button) contactUsDialogView.findViewById(R.id.problemBtn);
        final Button featureRequestBtn = (Button) contactUsDialogView.findViewById(R.id.featureRequestBtn);
        final Button questionBtn = (Button) contactUsDialogView.findViewById(R.id.questionBtn);
        final Button kudosBtn = (Button) contactUsDialogView.findViewById(R.id.kudosBtn);

        contactUSLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // To hide keyboard
                setKeyboardStatus(nameEt, true);
            }
        });

        ImageView closeContactUsDialogIv = (ImageView) contactUsDialogView.findViewById(R.id.closeContactUsDialogIv);
        final TextView sendIv = (TextView) contactUsDialogView.findViewById(R.id.sendTv);

        final OnClickListener sendTvListener = new OnClickListener() {
            @Override
            public void onClick(View v) {

                AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

                RequestParams requestParams = new RequestParams();

                requestParams.put("encoding", "UTF-8");
                requestParams.put("00NE0000004zKPw", "Android");
                requestParams.put("orgid", "00DM00000014Doo");
                requestParams.put("retURL", "http://bluescape.com");

                requestParams.put("company", companyEt.getText().toString());
                requestParams.put("email", emailEt.getText().toString());
                requestParams.put("description", descriptionEt.getText().toString());
                requestParams.put("name", nameEt.getText().toString());
                requestParams.put("subject", subjectEt.getText().toString());
                requestParams.put("type", type);

                asyncHttpClient.addHeader("user-agent",
                        AppSingleton.getInstance().getUserAgent());
                asyncHttpClient.post("https://cs7.salesforce.com/servlet/servlet.WebToCase", requestParams,
                        new ContactUsCallBack(contactUsDialogView));

            }
        };
        final OnClickListener nullSendTvListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        };
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((nameEt.getText().toString().length() > 0) && (emailEt.getText().toString().length() > 0)
                        && (companyEt.getText().toString().length() > 0) && (subjectEt.getText().toString().length() > 0)
                        && (descriptionEt.getText().toString().length() > 0)) {
                    sendIv.setTextColor(Color.parseColor("#0077ff"));
                    sendIv.setOnClickListener(sendTvListener);
                } else {
                    sendIv.setOnClickListener(nullSendTvListener);
                    sendIv.setTextColor(Color.parseColor("#cdcdcd"));
                }

            }
        };

        nameEt.addTextChangedListener(textWatcher);
        emailEt.addTextChangedListener(textWatcher);
        companyEt.addTextChangedListener(textWatcher);
        subjectEt.addTextChangedListener(textWatcher);
        descriptionEt.addTextChangedListener(textWatcher);

        problemBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                type = "Problem";

                problemBtn.setBackground(getResources().getDrawable(R.drawable.seg1));
                kudosBtn.setBackground(getResources().getDrawable(R.drawable.seg4));
                questionBtn.setBackground(getResources().getDrawable(R.drawable.seg_white));
                featureRequestBtn.setBackground(getResources().getDrawable(R.drawable.seg_white));

                problemBtn.setTextColor(Color.parseColor("#ffffff"));
                kudosBtn.setTextColor(Color.parseColor("#0077ff"));
                questionBtn.setTextColor(Color.parseColor("#0077ff"));
                featureRequestBtn.setTextColor(Color.parseColor("#0077ff"));

            }
        });
        featureRequestBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                type = "Feature Request";

                problemBtn.setBackground(getResources().getDrawable(R.drawable.seg3));
                kudosBtn.setBackground(getResources().getDrawable(R.drawable.seg4));
                questionBtn.setBackground(getResources().getDrawable(R.drawable.seg_white));
                featureRequestBtn.setBackground(getResources().getDrawable(R.drawable.seg));

                kudosBtn.setTextColor(Color.parseColor("#0077ff"));
                problemBtn.setTextColor(Color.parseColor("#0077ff"));
                questionBtn.setTextColor(Color.parseColor("#0077ff"));
                featureRequestBtn.setTextColor(Color.parseColor("#ffffff"));
            }
        });
        questionBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                type = "Question";

                problemBtn.setBackground(getResources().getDrawable(R.drawable.seg3));
                kudosBtn.setBackground(getResources().getDrawable(R.drawable.seg4));
                questionBtn.setBackground(getResources().getDrawable(R.drawable.seg));
                featureRequestBtn.setBackground(getResources().getDrawable(R.drawable.seg_white));

                kudosBtn.setTextColor(Color.parseColor("#0077ff"));
                problemBtn.setTextColor(Color.parseColor("#0077ff"));
                questionBtn.setTextColor(Color.parseColor("#ffffff"));
                featureRequestBtn.setTextColor(Color.parseColor("#0077ff"));

            }
        });

        kudosBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                type = "Kudos";

                problemBtn.setBackground(getResources().getDrawable(R.drawable.seg3));
                kudosBtn.setBackground(getResources().getDrawable(R.drawable.seg2));
                questionBtn.setBackground(getResources().getDrawable(R.drawable.seg_white));
                featureRequestBtn.setBackground(getResources().getDrawable(R.drawable.seg_white));

                kudosBtn.setTextColor(Color.parseColor("#ffffff"));
                problemBtn.setTextColor(Color.parseColor("#0077ff"));
                questionBtn.setTextColor(Color.parseColor("#0077ff"));
                featureRequestBtn.setTextColor(Color.parseColor("#0077ff"));

            }
        });

        closeContactUsDialogIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contactUsDialogView.dismiss();
            }
        });
        contactUsDialogView.show();
    }

    private void forgotPassword() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(baseUrl + AppConstants.FORGOT_PASSWORD));
        startActivity(browserIntent);
    }

    private void loginAction() {
        // validating login credentials
        if (email.length() == 0) {
            emailEt.setError(AppConstants.ERR_EMAIL);
            return;
        }
        if (password.length() == 0) {
            passwordEt.setError(AppConstants.ERR_PASSWORD);
            return;
        }

        showProgressDialog(AppConstants.MSG_SIGNING_IN);
        // requesting server to get session
        new loginAsyncTask().execute();
    }

    private void pullViewsAndSetListeners() {
        emailEt = (EditText) findViewById(R.id.emailEt);
        passwordEt = (EditText) findViewById(R.id.passwordEt);

        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(loginBtnListener);

        passwordEt.setOnEditorActionListener(doneListener);

        TextView forgotPasswordTv = (TextView) findViewById(R.id.forgotPasswordTv);
        forgotPasswordTv.setOnClickListener(forgotPasswordTvListener);

        TextView contactUsTv = (TextView) findViewById(R.id.contactUsTv);
        contactUsTv.setOnClickListener(contactUsTvListener);

        AssetManager assetMgr = getApplicationContext().getResources().getAssets();

        InputStream fileIn = null;

        try {
            fileIn = assetMgr.open("SourceSansPro-Bold.ttf", AssetManager.ACCESS_STREAMING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        contactUsTv.setOnClickListener(contactUsTvListener);
        if (fileIn != null) {
            forgotPasswordTv.setTypeface(Typeface.createFromAsset(assetMgr, "SourceSansPro-Bold.ttf"));
            contactUsTv.setTypeface(Typeface.createFromAsset(assetMgr, "SourceSansPro-Bold.ttf"));
        }

    }

    private void readLoginCredentials() {
        email = emailEt.getText().toString();
        password = passwordEt.getText().toString();
    }

    private void setActionbarTitle() {
        String version = "";
        getSupportActionBar().setTitle("");
        // Grab the version code
        if (AppConstants.DEBUG) {
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            assert pInfo != null;
            version = String.format("  Version name: %s Version Code : %s", pInfo.versionName, pInfo.versionCode);
            ((TextView) findViewById(R.id.activityName)).setText(bluescapeApplication.getDataFromSharedPrefs(AppConstants.CONFIGURATION,
                    AppConstants.STAGING) + version);
        }
        baseUrl = bluescapeApplication.getDataFromSharedPrefs(AppConstants.PORTAL_URL, AppConstants.CONFIGURATION_URLS[0]);
    }

}
