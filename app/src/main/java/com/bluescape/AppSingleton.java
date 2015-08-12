package com.bluescape;

public class AppSingleton {
	private static AppSingleton appSingleton = new AppSingleton();

	public static AppSingleton getInstance() {
		if (appSingleton == null) appSingleton = new AppSingleton();
		return appSingleton;
	}

	private BluescapeApplication application;

	private AppSingleton() {
	}

	public String getUserAgent(){
		return "Bluescape Android " + android.webkit.WebSettings.getDefaultUserAgent(getApplication().getApplicationContext());
	}

	public BluescapeApplication getApplication() {
		return application;
	}

	public void setApplication(BluescapeApplication application) {
		this.application = application;
	}

}
