package com.bluescape.collaboration.util;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.model.widget.NoteModel;
import com.bluescape.util.ImageUtils;
import com.bluescape.util.network.HTTPMETHOD;
import com.bluescape.util.network.WebService;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.apache.http.Header;

import java.io.File;
import java.util.Map;

import static android.graphics.Bitmap.createBitmap;

public class NetworkTask {

	/**
	 * Task for retrieving the note jpeg.
	 */
	public static class getNoteImage extends NetworkTask {

		/**
		 * Give the URL of the note Image
		 * 
		 * @param url
		 */
		public getNoteImage(String url) {
			super(url, HTTPMETHOD.GET);
		}
	}

	/**
	 * Task for retreiving the list of note locations.
	 */
	public static class getNoteList extends NetworkTask {

		public getNoteList() {
			super(AppSingleton.getInstance().getApplication()
				.getDataFromSharedPrefs(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS, AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS)
					+ "/card_templates.json", HTTPMETHOD.GET);
			Bundle header = new Bundle();
			header.putString("user-agent",
				AppSingleton.getInstance().getUserAgent());
			setHeader(header);
		}
	}

	public static class sendImageTask extends NetworkTask {

		public sendImageTask() {

			super(AppSingleton.getInstance().getApplication()
				.getDataFromSharedPrefs(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS, AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS)
					+ "/"
					+ AppSingleton.getInstance().getApplication()
						.getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, AppConstants.KEY_WORKSPACE_ID) + "/object/upload", HTTPMETHOD.POST);

			Bundle header = new Bundle();

			header.putString("Cookie", AppSingleton.getInstance().getApplication().getDataFromSharedPrefs(AppConstants.WORKSPACE_COOKIE, ""));
			header.putString("Connection", "keep-alive");
			header.putString("Cache-Control", "no-cache");
			// Delete later testing for Wall Issue is actually set in the
			// SimpleMultipartEntity.java dynamically
			// header.putString("Content-Length", "350000");
			// header.putString("Content-Length", "100000");
			// header.putString("Content-Length", "0");

			header.putString("Accept-Encoding", "gzip, deflate");
			header.putString("Accept", "text/html, */*; q=0.01");
			header.putString("user-agent",
				AppSingleton.getInstance().getUserAgent());

			setHeader(header);

		}
	}

	public static class sendToWallTask extends NetworkTask {

		public sendToWallTask() {
			super(AppSingleton.getInstance().getApplication()
				.getDataFromSharedPrefs(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS, AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS)
					+ "/"
					+ AppSingleton.getInstance().getApplication()
						.getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, AppConstants.KEY_WORKSPACE_ID) + "/send_to_wall", HTTPMETHOD.PUT);

			Bundle header = new Bundle();
			header.putString("Cookie", AppSingleton.getInstance().getApplication().getDataFromSharedPrefs(AppConstants.WORKSPACE_COOKIE, ""));
			header.putString("user-agent",
				AppSingleton.getInstance().getUserAgent());

			setHeader(header);

		}
	}

	private static final String TAG = NetworkTask.class.getSimpleName();

	public static void fetchNoteCardImage(final String url, final NoteModel notecard) {

		NetworkTask imageTask = new NetworkTask.getNoteImage(url);
		WebService webservice = new WebService();
		webservice.makeCall(imageTask, null, new FileAsyncHttpResponseHandler(AppSingleton.getInstance().getApplication().getBaseContext()) {

			@Override
			public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
				AppConstants.LOG(AppConstants.CRITICAL, TAG, "Failed to load note card image");
			}

			@Override
			public void onSuccess(int i, Header[] headers, File file) {

				Bitmap result = ImageUtils.decodeToImage(file);

				// AppConstants.LOG(AppConstants.CRITICAL, TAG,"file: " +
				// file.getAbsolutePath());

				if (result != null) {

					// TODO: This is all debugging code to check the cahce etc.
					LruCache cache = AppSingleton.getInstance().getApplication().getCache();
					cache.put(url, result);

					Map<String, Bitmap> map = cache.snapshot();

					for (String key : map.keySet()) {
						// AppConstants.LOG(AppConstants.CRITICAL,
						// TAG,"Cache key : " + key + " value size " +
						// map.get(key).getByteCount());
					}

					// AppConstants.LOG(AppConstants.INFO,
					// TAG,"Downloaded notecard: " + url + " Decoded size: " +
					// result.getByteCount());

					if (notecard != null) {
						notecard.updateBitmap(url, result);
					}
				} else {
					AppConstants.LOG(AppConstants.ERROR, TAG, "Bitmap failed to generate");
				}
			}

		});
	}

	/**
	 * Returns a bitmap from the baseName or a blank bitmap
	 * 
	 * @param baseName
	 * @return
	 */
	public static Bitmap getBitmapFromBaseName(String baseName, NoteModel notecard) {

		// Error check null baseNames
		if (baseName == null || baseName.equals("")) { return getBlankBitmap(); }

		String color = TemplateHelper.parseBaseNameToColor(baseName);
		String url = TemplateHelper.getUrlFromColor(AppSingleton.getInstance().getApplication().getColorToUrlMap(), color);

		if (url == null || url.equals("")) { return getBlankBitmap(); }

		// AppConstants.LOG(AppConstants.CRITICAL,
		// TAG,String.format("Checking to see if %s is in cache from basename: %s and color %s",
		// url, baseName, color));

		if (TemplateHelper.isUrlInCache(url)) {
			return TemplateHelper.getCachedImage(url);
		} else {
			// We need to fetch it.
			fetchNoteCardImage(url, notecard);
		}

		return getBlankBitmap();
	}

	public static Bitmap getBitmapFromURL(String url, NoteModel notecard) {

		AppConstants.LOG(AppConstants.VERBOSE, TAG, String.format("Checking to see if %s is in cache", url));

		LruCache cache = AppSingleton.getInstance().getApplication().getCache();
		Bitmap bitmap = (Bitmap) cache.get(url);

		if (bitmap != null) {
			return bitmap;
		} else {
			// We need to fetch it.
			fetchNoteCardImage(url, notecard);
		}

		// Create a blank bitmap.
		return getBlankBitmap();
	}

	private static Bitmap getBlankBitmap() {
		int[] colors = new int[500 * 500];

		// Create a blank bitmap.
		for (int i = 0; i < 500 * 500 - 4; i += 4) {
			colors[i] = 255;
			colors[i + 1] = 0;
			colors[i + 2] = 0;
			colors[i + 3] = 255;
		}
		return createBitmap(colors, 500, 500, Bitmap.Config.ARGB_8888);
	}

	private String url;

	private Bundle header;

	private HTTPMETHOD method;

	NetworkTask(String url, HTTPMETHOD method) {
		this.url = url;
		this.method = method;
	}

	public Bundle getHeader() {
		return this.header;
	}

	public HTTPMETHOD getMethod() {
		return method;
	}

	public String getUrl() {
		return url;
	}

	public void setMethod(HTTPMETHOD method) {
		this.method = method;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	void setHeader(Bundle header) {
		this.header = header;
	}

}
