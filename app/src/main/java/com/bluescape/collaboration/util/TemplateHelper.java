package com.bluescape.collaboration.util;

import android.graphics.Bitmap;
import com.bluescape.AppSingleton;
import com.bluescape.BluescapeApplication;
import java.util.Map;
import static android.graphics.Bitmap.createBitmap;
import static com.bluescape.collaboration.util.NetworkTask.fetchNoteCardImage;

public class TemplateHelper {
	// TODO get rid of this
	private static BluescapeApplication app = AppSingleton.getInstance().getApplication();

	/**
	 * Retusn the bitmap in the cache.
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getCachedImage(String url) {
		if (isUrlInCache(url)) { return (Bitmap) AppSingleton.getInstance().getApplication().getCache().get(url); }

		fetchNoteCardImage(url, null);

		return createBitmap(500, 500, Bitmap.Config.ARGB_8888);
	}

	/**
	 * Returns the url of the color. Pass in the map and color of the note card.
	 *
	 * @param colorMap
	 * @param color
	 * @return
	 */
	public static String getUrlFromColor(Map<String, String> colorMap, String color) {
		if (!color.contains(".jpeg")) color += ".jpeg";
		for (String key : colorMap.keySet()) {
			if (key.equals(color)) { return colorMap.get(key); }
		}
		return null;
	}

	/**
	 * Checks in the url is in the bitmap cache.
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isUrlInCache(String url) {
		if (url == null || url.equals("")) return false;
		Bitmap bitmap = (Bitmap) app.getCache().get(url);
		return bitmap != null;
	}

	/**
	 * Parses the baseName into a color. EX. mBaseName='sessions/all/Teal'
	 */
	public static String parseBaseNameToColor(String baseName) {
		String[] split = baseName.split("/");
		return split[split.length - 1];
	}
}
