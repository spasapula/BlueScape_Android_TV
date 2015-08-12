package com.bluescape.collaboration.socket.handler;

import android.util.Log;

import com.bluescape.AppConstants;
import com.bluescape.model.widget.StrokeModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HeStrokeMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeStrokeMessageHandler.class.getSimpleName();

	public void handleMessage(JSONArray mainMessage) {

		try {

			AppConstants.LOG(AppConstants.VERBOSE, TAG, mainMessage.toString());
			StrokeModel strokeModel = parseStroke(mainMessage);

			if (strokeModel != null) {
				// Draw only if the arrayLocs are greater than 0
				if (strokeModel.getArrayLocs().size() > 0) {
					modelTree.add(strokeModel);
                    if (strokeModel.isDirty())
					  strokeModel.createDrawable();
				} else {
					AppConstants.LOG(AppConstants.CRITICAL, TAG, "Stroke Model Array Locs size is 0 strokeModel.getArrayLocs().size() = "
																	+ strokeModel.getArrayLocs().size() + "For: " + mainMessage);
				}
			} else {
				AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null stroke from Note Parser");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Static factory Method
	 * http://www.drdobbs.com/jvm/creating-and-destroying-java
	 * -objects-par/208403883?pgno=1
	 *
	 * @param mJsonArray
	 * @return
	 */
	public StrokeModel parseStroke(JSONArray mJsonArray) {

		StrokeModel model = null;
		String targetID;

		// client-id
		try {

			// client-id
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal());

			// Message Types
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.HE.ordinal());

			// target-id
			targetID = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();

			// event-id
			String eventId = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_ID.ordinal()).toString();

			// History Event
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal());

			// event Json Object
			JSONObject strokeEvent = (JSONObject) mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal());

			// String eventJsonString =
			// mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal()).toString();

			// AppConstants.LOG(AppConstants.VERBOSE, TAG,
			// "Stroke fromJSON before GsonFireBuilder().enableHooks(StrokeModel.class).createGsonBuilder() ");
			model = new StrokeModel();
			model.setEventId(eventId);
			// GsonBuilder builder = new
			// GsonFireBuilder().registerTypeAdapter(AbstractElement.class, new
			// AbstractElementAdapter()).enableHooks(StrokeModel.class).createGsonBuilder();
			if (strokeEvent.has("strokeId")) model.setStrokeID(strokeEvent.getString("strokeId"));
			model.setBrush(strokeEvent.getInt("brush"));
			JSONArray color = strokeEvent.getJSONArray("color");
			float colora[] = new float[color.length()];
			for (int i = 0; i < color.length(); i++)
				colora[i] = (float) color.getDouble(i);
			model.setColor(colora);
			model.reallyJustSetLineWidth(strokeEvent.getInt("size"));
			JSONArray locs = strokeEvent.getJSONArray("locs");
			model.setArrayLocs(new ArrayList<Float>(locs.length()));
			for (int i = 0; i < locs.length(); i++) {
				model.getArrayLocs().add((float) locs.getDouble(i));
			}
			model.setTargetID(targetID);
			// TODO kris have to differentiate
			// AppConstants.LAYER_STROKES_ON_LOCAL_CARD and
			// AppConstants.LAYER_STROKES_ON_BACKGROUND based on targetID
			//model.initializeVertexBuilder();
			// if stroke is Erase with brush == 2 instead of 1 for normal stroke
			// then set color to black [0,0,0,1] and mlineWidth to 20 as set by
			// he message
			if (model.getBrush() == AppConstants.STROKE_BRUSH_ERASE) {

				Log.i("getBrush","getBrush");

				model.setColor(AppConstants.EraseBrushColor.BRUSH_COLOR_ERASE);
				//AppConstants.LOG(AppConstants.CRITICAL, TAG, "Erase message brush = 2 setting color = EraseBrushColor.BRUSH_COLOR_ERASE");

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return model;
	}
}
