package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.Group;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BrowserModel;
import com.bluescape.model.widget.ImageModel;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by SS_SA on 6/29/15.
 */
public class HeCreateGroupMessageHandler extends BaseMessageHandler {

    private static final String TAG = HeCreateImageMessageHandler.class.getSimpleName();

    //Sample message
    //["559186c9fcd8ac2605f3f47d","he","eUocH9xyeNQrtHz-KPhw","55a3f5931aa674bd05470b1e","create",{"id":"55a3f5940d28a34963000000","type":"group","children":["55a3f5290d28a361d1000003","55a3f52c0d28a361d1000004"]}]

    //region Private Members
    private GsonBuilder builder = new GsonFireBuilder().enableHooks(Group.class).createGsonBuilder();

    private Gson gson = builder.create();
    //endregion

    //region Overrides
    public void handleMessage(JSONArray mainMessage) {

        try {

            AppConstants.LOG(AppConstants.VERBOSE, TAG, mainMessage.toString());

            Group group = parseGroup(mainMessage);

            if (group != null) {

                //Set The Activity Indicator Fields
                group.populateActivityFields(true, mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal()).toString());

                //Add the group to ModelTree
                AppConstants.LOG(AppConstants.VERBOSE, TAG, "Got Group group = " + group);
                modelTree.add(group);

            } else {
                AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null Image from ImageModel.fromJSON");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //endregion

    //region Private Methods
    private Group parseGroup(JSONArray mJsonArray) {

        Group model = null;

        // client-id
        try {
            mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal());

            // Message Types
            mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.HE.ordinal());

            // target-id
            String targetID = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();

            // event-id
            mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_ID.ordinal());

            // History Event
            mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal());

            // event Json Object
            String eventJsonString = mJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal()).toString();

            model = gson.fromJson(eventJsonString, Group.class);

        } catch (JSONException e) {

            e.printStackTrace();
        }

        return model;
    }

    //endregion
}
