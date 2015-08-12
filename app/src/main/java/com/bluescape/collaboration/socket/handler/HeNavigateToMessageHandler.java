package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.BrowserModel;
import com.bluescape.model.widget.PDFModel;
import com.bluescape.model.widget.TextModel;
import com.bluescape.model.widget.change.GeometryChangedModel;
import com.bluescape.model.widget.change.PayloadModel;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URL;

public class HeNavigateToMessageHandler extends BaseMessageHandler {
	private static final String TAG = HeNavigateToMessageHandler.class.getSimpleName();


	public void handleMessage(JSONArray mainMessage) {
		try {

            BrowserModel browserModel = parseBrowser(mainMessage);

            if (browserModel != null) {

                BaseWidgetModel baseWidgetModel = WorkSpaceState
							.getInstance()
							.getModelTree()
							.getModel(
									mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString());

					if (baseWidgetModel != null) {


						if (baseWidgetModel instanceof BrowserModel) {
                            if (browserModel.getPayload().getUrl() != "") {
                                ((BrowserModel) baseWidgetModel).getPayload().setUrl(browserModel.getPayload().getUrl());
                                ((BrowserModel) baseWidgetModel).setDisplayURL((new URL(browserModel.getPayload().getUrl())).getHost()) ;

                                baseWidgetModel.setDirty(true);
                                //Set the mAreAllViewPortWidgetsInitialized to false to update scene in Modeltree
                                WorkSpaceState
                                        .getInstance()
                                        .getModelTree()
                                        .mAreAllViewPortWidgetsInitialized = false;

                            }
						}

						AppConstants.LOG(AppConstants.VERBOSE, TAG, "BROWSER  URL Update NavigateTo baseWidgetModel:" + baseWidgetModel.toString());
					} else {
						AppConstants.LOG(AppConstants.CRITICAL, TAG,
								"BROWSER NOT OK baseWidgetModel is NULL probably an Image or position marker move that we are not handling");
					}

            } else {
                AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null browser");
            }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    // @Override
    private BrowserModel parseBrowser(JSONArray mJsonArray) {

        BrowserModel model = null;

        // client-id
        try {
            mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal());

            // Message Types
            mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.HE.ordinal());

            // target-id
            String id = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();

            // event-id
            mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_ID.ordinal());

            // History Event
            mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal());

            // event Json Object
            String eventJsonString = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal()).toString();
            GsonBuilder builder = new GsonFireBuilder().enableHooks(BrowserModel.class).createGsonBuilder();
            Gson gson = builder.create();
            model = gson.fromJson(eventJsonString, BrowserModel.class);


        } catch (JSONException e) {
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "BrowserModel Exception in GSON Parsing: " + mJsonArray);
            e.printStackTrace();
        }

        return model;
    }

}
