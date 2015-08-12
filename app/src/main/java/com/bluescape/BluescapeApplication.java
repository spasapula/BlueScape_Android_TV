package com.bluescape;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.bluescape.collaboration.history.database.DBHelper;
import com.bluescape.model.template.WidgetTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

public class BluescapeApplication extends Application {

	SharedPreferences.Editor editor;
	private SharedPreferences sharedPreferences;
	private LruCache mImageCache;
	private DBHelper mDBHelper;
	/**
	 * Map for colors to urls to download bgs
	 */
	private LinkedHashMap<String, String> colorToUrlMap;
	/**
	 * Map of baseName colors to Templates.
	 */
	private LinkedHashMap<String, WidgetTemplate> mColorToTemplateMap;
	private int colorIndex = 0;

	public void clearDataInSharedPref(String key) {
		(sharedPreferences.edit()).remove(key).commit();
	}

	public LruCache getCache() {
		return mImageCache;
	}

	public Map<String, WidgetTemplate> getColorToTemplateMap() {
		return mColorToTemplateMap;
	}

	public Map<String, String> getColorToUrlMap() {
		return colorToUrlMap;
	}

	public String getDataFromSharedPrefs(String key, String defaultVal) {
		return sharedPreferences.getString(key, defaultVal);
	}

	public String getErrorStringFromResourceCode(int resourceCode) {
		return getResources().getText(resourceCode).toString();
	}

	public DBHelper getmDBHelper() {
		return mDBHelper;
	}

	/**
	 * We may need to have a global reference to the workspace ID.
	 *
	 * @return
	 * @throws Exception
	 */
	public String getWorkspaceID() throws Exception {
		String workspaceID = getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, "");
		if (workspaceID.equals("")) throw new Exception("Bad Workspace.");
		return workspaceID;
	}

	public void incrementColorIndex() {
		colorIndex++;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		AppSingleton.getInstance().setApplication(this);
		init();
	}

	public void putDataInSharedPref(String key, String value) {
		(sharedPreferences.edit()).putString(key, value).commit();
	}

	public void setColorToTemplateMap(LinkedHashMap<String, WidgetTemplate> colorToTemplateMap) {
		mColorToTemplateMap = colorToTemplateMap;
	}

	public void setColorToUrlMap(LinkedHashMap<String, String> colorToUrlMap) {
		this.colorToUrlMap = colorToUrlMap;
	}

	public void setmDBHelper(DBHelper mDBHelper) {
		this.mDBHelper = mDBHelper;
	}

	private void init() {
		sharedPreferences = getApplicationContext().getSharedPreferences(AppConstants.BLUESCAPE, MODE_PRIVATE);

		// Get max available VM memory, exceeding this amount will throw an
		// OutOfMemory exception. Stored in kilobytes as LruCache takes an
		// int in its constructor.
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 8;

		mImageCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};

		colorToUrlMap = new LinkedHashMap<>();
		mColorToTemplateMap = new LinkedHashMap<>();
		// mDrawTree = DrawableTree.getInstance();
		mDBHelper = new DBHelper(getApplicationContext());
		// Register the message handlers

	}

}
