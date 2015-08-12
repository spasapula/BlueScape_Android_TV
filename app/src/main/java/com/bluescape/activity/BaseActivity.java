package com.bluescape.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.BluescapeApplication;
import com.bluescape.R;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private ProgressDialog dialog = null;
    private Dialog noNetworkDialog;// Network BroadcastReceiver
    protected final BluescapeApplication bluescapeApplication = AppSingleton.getInstance().getApplication();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isNetworkAvailable()) {
                if (!isNetworkAvailable()) {
                    if (noNetworkDialog != null) {
                        if (!noNetworkDialog.isShowing()) {
                            noNetworkDialog = customDialog(R.layout.no_network_dialog);
                            noNetworkDialog.setCancelable(false);
                            noNetworkDialog.show();
                        }
                    } else {
                        noNetworkDialog = customDialog(R.layout.no_network_dialog);
                        noNetworkDialog.setCancelable(false);
                        noNetworkDialog.show();
                    }
                } else {
                    if (noNetworkDialog != null)
                        if (noNetworkDialog.isShowing()) noNetworkDialog.dismiss();
                }
            } else {
                if (noNetworkDialog != null)
                    if (noNetworkDialog.isShowing()) noNetworkDialog.dismiss();
            }
        }
    };

    protected Dialog customDialog(int layout) {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setTitle("");
        dialog.setContentView(layout);
        dialog.setCancelable(true);
        dialog.show();
        return dialog;
    }

    protected void dismissProgressDialog() {
        try {
            if (dialog.isShowing()) dialog.dismiss();
        } catch (Exception e) {
            AppConstants.LOG(AppConstants.ERROR, TAG, e.toString());
        }
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager cn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nf = cn.getActiveNetworkInfo();
        return nf != null && nf.isConnected();
    }

    protected boolean knowKeyboardStatus() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isAcceptingText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    protected void resetCookies() {
        (CookieManager.getInstance()).removeAllCookie();
        bluescapeApplication.clearDataInSharedPref(AppConstants.KEY_WORKSPACE_ID);
        bluescapeApplication.clearDataInSharedPref(AppConstants.WORKSPACE_COOKIE);

        // adding session Cookie to Cookies
        (CookieManager.getInstance()).setCookie(
                bluescapeApplication.getDataFromSharedPrefs(AppConstants.PORTAL_URL, AppConstants.CONFIGURATION_URLS[0]),
                bluescapeApplication.getDataFromSharedPrefs(AppConstants.SESSION_KEY, "") + "="
                        + bluescapeApplication.getDataFromSharedPrefs(AppConstants.SESSION_VALUE, ""));
    }

    protected void setKeyboardStatus(EditText editText, boolean status) {
        if (status) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    protected void showAlert(String title, String alertMsg, String positiveActionName, DialogInterface.OnClickListener positiveAction,
                             String negativeActionName, DialogInterface.OnClickListener negativeAction, boolean dual) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(alertMsg);
        builder.setCancelable(false);

        if (positiveActionName != null && positiveAction != null) {
            builder.setPositiveButton(positiveActionName, positiveAction);
        } else {

            if (positiveActionName != null) {
                builder.setPositiveButton(positiveActionName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
            } else {
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
            }
        }

        if (dual) {
            if (negativeActionName != null && negativeAction == null) {
                builder.setNegativeButton(negativeActionName, null);
            } else {
                if (negativeActionName != null) {
                    builder.setNegativeButton(negativeActionName, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                } else {
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                }
            }
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    protected void showProgressDialog(String message) {
        if (dialog == null) dialog = new ProgressDialog(this);
        dialog.setMessage(message);

        dialog.setCancelable(false);
        try {
            if (!dialog.isShowing()) dialog.show();
        } catch (Exception e) {
            AppConstants.LOG(AppConstants.ERROR, TAG, e.toString());
        }
    }

    protected void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    protected void donNotAutoShowKeyboard() {
        // Don't auto-show the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}