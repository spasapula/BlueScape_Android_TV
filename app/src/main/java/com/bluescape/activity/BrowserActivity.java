package com.bluescape.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluescape.AppConstants;
import com.bluescape.R;

public class BrowserActivity extends BaseActivity {
	private WebView browser;
	private String shareBody;

	private final WebViewClient webViewClientListener = new WebViewClient() {
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			WebBackForwardList mWebBackForwardList = browser.copyBackForwardList();

			if (mWebBackForwardList.getSize() > 0 && mWebBackForwardList.getCurrentIndex() > 0)
				((ImageView) findViewById(R.id.backIV)).setImageResource(R.drawable.back_url_h);
			else
				((ImageView) findViewById(R.id.backIV)).setImageResource(R.drawable.back_url);

			if (mWebBackForwardList.getSize() > 0 && mWebBackForwardList.getSize() - 1 != mWebBackForwardList.getCurrentIndex())
				((ImageView) findViewById(R.id.nextTV)).setImageResource(R.drawable.next_url_h);
			else
				((ImageView) findViewById(R.id.nextTV)).setImageResource(R.drawable.next_url);

			if (mWebBackForwardList.getCurrentIndex() > 0 && mWebBackForwardList.getCurrentIndex() < mWebBackForwardList.getSize() - 1) {
				((ImageView) findViewById(R.id.nextTV)).setImageResource(R.drawable.next_url_h);
				((ImageView) findViewById(R.id.backIV)).setImageResource(R.drawable.back_url_h);
			}
			((TextView)findViewById(R.id.titleTV)).setText(browser.getTitle());
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			shareBody = url;

			return false;
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_layout);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// Don't auto-show the keyboard
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		Intent intent = getIntent();
		String url = intent.getStringExtra(AppConstants.URL);
		browser = (WebView) findViewById(R.id.dashWebview);
		browser.setWebChromeClient(new WebChromeClient());
		browser.setWebViewClient(webViewClientListener);
		browser.loadUrl(url);

		findViewById(R.id.doneTV).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		findViewById(R.id.backIV).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				browser.goBack();
			}
		});
		findViewById(R.id.nextTV).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				browser.goForward();
			}
		});

		findViewById(R.id.reloadTV).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				browser.reload();
			}
		});

		findViewById(R.id.shareTV).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
				startActivity(Intent.createChooser(sharingIntent, ""));
			}
		});
	}

}
