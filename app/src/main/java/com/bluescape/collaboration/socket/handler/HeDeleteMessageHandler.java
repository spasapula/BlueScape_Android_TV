package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.model.Group;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BaseWidgetModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class HeDeleteMessageHandler extends BaseMessageHandler {
    private static final String TAG = HeDeleteMessageHandler.class.getSimpleName();

    public void handleMessage(JSONArray mainMessage) {

        try {

            AppConstants.LOG(AppConstants.VERBOSE, TAG, mainMessage.toString());

            // delete he
            // ["54d3bb68b5a05708542db8de","he","551301b0bc397b5200000007","551302f677ff968418b64417","delete",{"hidden":true}]

            //Also Handle GroupNMove delete messages here
            // ["559186c9fcd8ac2605f3f47d","he","55a3f5f60d28a34963000001","55a3f6641aa674bd05470b3b","delete",{"children":["55a3f5290d28a361d1000003","55a3f52c0d28a361d1000004"],"hidden":true}]

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
                        "DIFFERENT CLIENT message from Server so processing HeDeleteMessageHandler  mainMessage: " + mainMessage + "Client-id : "
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

                                for (int i = 0; i < children.length(); i++) {
                                    String currentDeleteChildId = children.getString(i);

                                    //TODO Extra Check make sure it is present in the mChilds
                                    //if(currentGroup.getChildModels().containsKey(currentDeleteChildId)) {
                                    WorkSpaceState
                                            .getInstance()
                                            .getModelTree()
                                            .delete(currentDeleteChildId);
                                    //}
                                }
                            }

                            //Experiment and delete any active members of the group as we are handling the membership messages now
                            //TODO confirm with Keefe and Satish to make sure this is the behaviour we want else comment
                            if (currentGroup.getChildModels() != null) {
                                for (int i = 0; i < currentGroup.getChildModels().size(); i++) {
                                    String currentDeleteChildId = currentGroup.getChildModels().get(i).getID();


                                    WorkSpaceState
                                            .getInstance()
                                            .getModelTree()
                                            .delete(currentDeleteChildId);

                                }
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }


                    WorkSpaceState
                            .getInstance()
                            .getModelTree()
                            .delete(
                                    mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString());
                } else {
                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                            "DELETE NOT OK baseWidgetModel is NULL probably odd case as the target should be in getModelTree().mWorkspaceModels");
                }


            } else {
                AppConstants.LOG(AppConstants.VERBOSE, TAG, "SAME CLIENT message from Server so SKIPPING HeDeleteMessageHandler  mainMessage: "
                        + mainMessage + "Client-id : " + WorkSpaceState.getInstance().getClientId());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
