package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.Group;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BaseWidgetModel;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SS_SA on 6/29/15.
 */
public class HeMembershipMessageHandler extends BaseMessageHandler {

    private static final String TAG = HeCreateImageMessageHandler.class.getSimpleName();

    //Sample message
    //["559186c9fcd8ac2605f3f47d","he","55a55c420d28a35ab9000002","55a55c48bb8d15380511c00f","membership",{"children":["55a55bc50d28a334f7000004","55a55bad0d28a334f7000000","55a55baf0d28a334f7000001","55a55bb10d28a334f7000002","55a55bb40d28a334f7000003"]}]

    //region Private Members

    //endregion

    //region Overrides
    public void handleMessage(JSONArray mainMessage) {

        try {

            AppConstants.LOG(AppConstants.VERBOSE, TAG, mainMessage.toString());

            // Check if it is from the same cliend-id and ignore if it is
            boolean myMove = WorkSpaceState.getInstance().getClientId() != null
                    && WorkSpaceState.getInstance() != null
                    && WorkSpaceState.getInstance().mHistoryLoadCompleted
                    && WorkSpaceState
                    .getInstance()
                    .getClientId()
                    .equalsIgnoreCase(
                            mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal()).toString());

            if (!myMove) {

                AppConstants.LOG(AppConstants.VERBOSE, TAG,
                        "DIFFERENT CLIENT message from Server so processing HeMembershipMessageHandler  mainMessage: " + mainMessage + "Client-id : "
                                + WorkSpaceState.getInstance().getClientId());


                // For delete just delete the respective model from the ModelTree
                BaseWidgetModel baseWidgetModel = WorkSpaceState
                        .getInstance()
                        .getModelTree()
                        .getModel(
                                mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString());

                if (baseWidgetModel != null) {


                    //Now Also Check for the children if the target-id is that of a group
                    //New Code To Handle the Children for Group
                    if (baseWidgetModel instanceof Group) {

                        try {

                            Group currentGroup = (Group) baseWidgetModel;

                            JSONObject eventJsonObj = (JSONObject) mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal());

                            if (eventJsonObj.has("children")) {

                                JSONArray children = eventJsonObj.getJSONArray("children");
                                String[] newChildIds = new String[children.length()];


                                for (int i = 0; i < children.length(); i++) {
                                    String currentChildId = children.getString(i);

                                    //Add this child to new ChildIdArray
                                    newChildIds[i] = currentChildId;
                                }

                                //Check and sync the membership before we set new children
                                currentGroup.syncChildMembership(newChildIds);
                                currentGroup.setChildIds(newChildIds);
                                //Create the mChildModels to match the childId's for subsequent move,delete, pin messages
                                currentGroup.groupPostDeserialize();

                            } else {
//                                AppConstants.LOG(AppConstants.CRITICAL, TAG,
//                                        "Group n Move id" + currentGroup.getID() + "Membership NO Children  setting all children to null");
                                currentGroup.setChildIds(null);
                                currentGroup.setChildModels(null);

                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }



                } else {
                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                            "MEMBERSHIP NOT OK baseWidgetModel is NULL probably odd case as the target should be in getModelTree().mWorkspaceModels");
                }


            } else {
                AppConstants.LOG(AppConstants.VERBOSE, TAG, "SAME CLIENT message from Server so SKIPPING HeMembershipMessageHandler  mainMessage: "
                        + mainMessage + "Client-id : " + WorkSpaceState.getInstance().getClientId());
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //endregion

    //region Private Methods

    //endregion
}
