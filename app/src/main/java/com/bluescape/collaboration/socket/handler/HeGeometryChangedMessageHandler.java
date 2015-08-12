package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.collaboration.util.ShowActivityTimerTask;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.BrowserModel;
import com.bluescape.model.widget.change.GeometryChangedModel;
import com.bluescape.model.widget.PDFModel;
import com.bluescape.model.util.Rect;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Timer;

public class HeGeometryChangedMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeGeometryChangedMessageHandler.class.getSimpleName();

	public void handleMessage(JSONArray mainMessage) {
		try {
			boolean myMove = WorkSpaceState.getInstance().getClientId() != null
								&& WorkSpaceState.getInstance() != null
								&& WorkSpaceState.getInstance().mHistoryLoadCompleted
								&& WorkSpaceState
									.getInstance()
									.getClientId()
									.equalsIgnoreCase(
										mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal()).toString());
			if (!myMove) {
				GeometryChangedModel geometryChangedModel = parseGeometryChange(mainMessage);

				if (geometryChangedModel != null) {
					BaseWidgetModel baseWidgetModel = WorkSpaceState
						.getInstance()
						.getModelTree()
						.getModel(
								mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString());

					if (baseWidgetModel != null) {
						Timer t = new Timer();
						ShowActivityTimerTask showActivityTimerTask = new ShowActivityTimerTask(baseWidgetModel);
						t.schedule(showActivityTimerTask, AppConstants.ACTIVITY_INDICATOR_TIME);

						baseWidgetModel.setRect(new Rect(geometryChangedModel.getRect()));

						// update the order too
						//Note only if > 0 wall sends "order":0 that will mess up widget selection so only update if greater than 0
						// ["55930841e7f97624055ca969","he","559319f6fcd8ac2605f543e3","55931b89e7f97624055cc290","tsxappevent",{"messageType":"geometryChanged","payload":{"order":0,"version":1,"windowSpaceHeight":700,"windowSpaceWidth":1020,"worldSpaceHeight":695,"worldSpaceWidth":1019,"x":957,"y":11},"targetTsxAppId":"webbrowser"}]
                        if(geometryChangedModel.getEventOrder() > 0 )
                            baseWidgetModel.setOrder(geometryChangedModel.getEventOrder());

						// if Already glInitialized then need to prepare and
						// create again
						if (baseWidgetModel.isModelGLInitialized) {
							if (baseWidgetModel instanceof BrowserModel) {
								// ((BrowserModel)
								// baseWidgetModel).prepareForDrawable();
								// ((BrowserModel)
								// baseWidgetModel).createDrawable();
								baseWidgetModel.setupModelMVPMatrix();
							} else if (baseWidgetModel instanceof PDFModel) {
								// ((PDFModel)
								// baseWidgetModel).prepareForDrawable();
								// ((PDFModel)
								// baseWidgetModel).createDrawable();
								baseWidgetModel.setupModelMVPMatrix();
							}
						}

						AppConstants.LOG(AppConstants.VERBOSE, TAG, "BROWSER  MOVE DOUBLE OK baseWidgetModel:" + baseWidgetModel.toString()
																	+ " Awesomesauce ");
					} else {
						AppConstants.LOG(AppConstants.CRITICAL, TAG,
							"BROWSER NOT OK baseWidgetModel is NULL probably an Image or position marker move that we are not handling");
					}

				} else {
					AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null moveModel from MoveModel.fromJSON");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GeometryChangedModel parseGeometryChange(JSONArray mJsonArray) {
		GeometryChangedModel model = null;
		String id;

		try {

			// client-id
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal());

			// Message Types
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.HE.ordinal());

			// target-id
			id = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();

			// History Event
			mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal());

			// event Json Object
			String eventJsonString = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal()).toString();

			GsonBuilder builder = new GsonFireBuilder().enableHooks(GeometryChangedModel.class).createGsonBuilder();
			Gson gson = builder.create();

			model = gson.fromJson(eventJsonString, GeometryChangedModel.class);
			model.setModelID(id);
			// model.mEventOrder is set according to the order sent in he and ve
			// events
			// now set the global mGlobalWorkSpaceEventOrder to the highest one
			// since order is inside payload assign mEventOrder = payload.order
			model.setEventOrder(model.getGeometryPayloadModel().getOrder());
			WorkSpaceState.getInstance().updateOrder(model.getEventOrder());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return model;
	}

}
