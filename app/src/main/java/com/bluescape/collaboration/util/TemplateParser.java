package com.bluescape.collaboration.util;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.model.template.NoteTemplate;
import com.bluescape.model.template.WidgetTemplate;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

/**
 * This class parses the json list and color and returns a URL/Bitmap, im not
 * sure yet. Created by Mark Stanford on 1/10/15.
 */
public class TemplateParser {

	private static final String TAG = TemplateParser.class.getSimpleName();

	/**
	 * Pass in the JSON response and then we return a map of color to URL. ex.
	 * <"Teal",
	 * "https://staging.collaboration.bluescape.com/card_templates/thumbnails/Teal.jpeg"
	 * >
	 *
	 * @param response
	 * @return
	 */
	public static void parseJsonResponse(JSONArray response) throws JSONException {

		LinkedHashMap<String, String> colormap = new LinkedHashMap<>();
		LinkedHashMap<String, WidgetTemplate> templateMap = new LinkedHashMap<>();
		Gson gson = new Gson();

		for (int i = 0; i < response.length(); i++) {
			JSONObject obj = (JSONObject) response.get(i);

			if (obj != null) {
				String thumbnail = obj.getString("thumbnail");
				String[] arry = thumbnail.split("/");
				String color = arry[arry.length - 1];
				colormap.put(
					color,
					AppSingleton.getInstance().getApplication()
						.getDataFromSharedPrefs(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS, AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS)
							+ "/" + thumbnail);

				NoteTemplate template = gson.fromJson(obj.toString(), NoteTemplate.class);
				AppConstants.LOG(AppConstants.INFO, TAG, "Template: " + template.toString());
				templateMap.put(color, template);
			}
		}
		AppSingleton.getInstance().getApplication().setColorToUrlMap(colormap);
		AppSingleton.getInstance().getApplication().setColorToTemplateMap(templateMap);
	}

	private TemplateParser() {
	}
}
