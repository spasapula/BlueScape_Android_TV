package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.collaboration.util.ShowActivityTimerTask;
import com.bluescape.model.Group;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.ImageModel;
import com.bluescape.model.widget.NoteModel;
import com.bluescape.model.util.Rect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;

public class HePositionMessageHandler extends BaseMessageHandler {
    private static final String TAG = HePositionMessageHandler.class.getSimpleName();

    public void handleMessage(JSONArray mainMessage) {

        try {

            // AppConstants.LOG(AppConstants.VERBOSE, TAG,
            // mainMessage.toString());

            // move he
            // [client-id, "he", target-id, event-id, "position",
            // {new-position}]
            // ["54eb7c3e916593fa15bdba72","he","54eb7b4a45c17e5998000000","54eb7ceb916593fa15bdba7e","position",{"rect":[656,126,936,286],"order":4}]

            boolean myMove = WorkSpaceState.getInstance().getClientId() != null
                    && WorkSpaceState.getInstance() != null
                    && WorkSpaceState.getInstance().mHistoryLoadCompleted
                    && WorkSpaceState
                    .getInstance()
                    .getClientId()
                    .equalsIgnoreCase(
                            mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal()).toString());

            if (!myMove) {
                // AppConstants.LOG(AppConstants.VERBOSE, TAG,
                // "DIFFERENT CLIENT message from Server so processing HePositionMessageHandler  mainMessage: "
                // + mainMessage + "Client-id : " +
                // WorkSpaceState.getInstance().getClientId() );

                // Add logic to update the Note ot image model rect based on new
                // dimensions in move
                BaseWidgetModel baseWidgetModel = WorkSpaceState
                        .getInstance()
                        .getModelTree()
                        .getModel(
                                mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString());

                if (baseWidgetModel != null) {


                    // update the order too
                    JSONObject event = (JSONObject) mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal());

                    baseWidgetModel.setOrder(event.getInt("order"));

                    WorkSpaceState.getInstance().updateOrder(baseWidgetModel.getOrder());
                    // if Already glInitialized then need to prepare and create
                    // again
                    JSONArray coords = event.getJSONArray("rect");

                    //Was already there for maybe Location marker , but needed for GroupNMove anyway
                    //Also this is the case when the baseWidgetModel Rect is not yet created
                    //for history Processing and widgets not in current users Workspace
                    //simplified into single method
                    baseWidgetModel.createRect(coords);

                    //New Code To Handle the Children for Group
                    if (baseWidgetModel instanceof Group) {

                        Group currentGroup = (Group) baseWidgetModel;

                        float currentXDelta = currentGroup.getRect().getTOPX() - currentGroup.getXLastMove();
                        float currentYDelta = currentGroup.getRect().getTOPY() - currentGroup.getYLastMove();


                        if (currentGroup.getChildModels() != null) {
                            for (int childCount = 0; childCount < currentGroup.getChildModels().size(); childCount++) {

                                //BaseWidgetModel childWidget = currentGroup.getChildModels()[childCount];
                                BaseWidgetModel childWidget = currentGroup.getChildModels().get(childCount);

//                                AppConstants.LOG(AppConstants.CRITICAL, TAG,
//                                        "Group n Move id" + currentGroup.getID() + " Processing child: " + childWidget);

                                //Check if childWidget is null or not it is happening for "browser history testing 1.7" worksapce
                                if (childWidget != null) {
                                    //The position[X1,Y1,X2,Y2] is actually the displacement [X,Y,X,Y] for each object from when the group was created
                                    //childWidget.moveRectByXY(currentGroup.getRect().getTOPX(), currentGroup.getRect().getTOPY());
                                    childWidget.moveRectByXY(currentXDelta, currentYDelta);

                                    //Preserving the order among children
                                    childWidget.setOrder(currentGroup.getOrder() + childCount);
                                } else {
                                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                                            "Something Wierd child id is there but cant find the widget  Group n Move id" + currentGroup.getID() + " Processing child: " + childWidget);

                                }

                            }
                            //Now set the order to higher than the last child so that getIntersectingModel picks the group up
                            //currentGroup.setOrder(currentGroup.getOrder()+currentGroup.getChildModels().size()+1);

                        }

                        //store the latest move in mXLastMove and mYLastMove once hte move is processed
                        currentGroup.setXLastMove(currentGroup.getRect().getTOPX());
                        currentGroup.setYLastMove(currentGroup.getRect().getTOPY());

                        Rect newRect = currentGroup.updateGroupRectWithNewChildBounds();
                        if (currentGroup.isModelGLInitialized) {
                            currentGroup.setRect(newRect);
                            currentGroup.setModelMatrix(newRect);
                            currentGroup.setupModelMVPMatrix();
                            currentGroup.setDirty(true);
                        }

                        //Set this so the ve he message updates to the new location
                        WorkSpaceState.getInstance().getModelTree().mAreAllViewPortWidgetsInitialized = false;
                    } else {
                        baseWidgetModel.setActivityHideTimer();
                        if (baseWidgetModel.isModelGLInitialized) {
                            baseWidgetModel.setModelMatrix(baseWidgetModel.getRect());
                            if (baseWidgetModel instanceof NoteModel || baseWidgetModel instanceof ImageModel)
                                baseWidgetModel.setupModelMVPMatrix();
                            WorkSpaceState.getInstance().getModelTree().mAreAllViewPortWidgetsInitialized = false;
                        }

                    }




                } else {
                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                            "MOVE NOT OK baseWidgetModel is NULL probably an Image or position marker move that we are not handling");
                }

            } else {

                AppConstants.LOG(AppConstants.VERBOSE, TAG, "SAME CLIENT message from Server so SKIPPING HePositionMessageHandler  mainMessage: "
                        + mainMessage + "Client-id : " + WorkSpaceState.getInstance().getClientId());

            }

        } catch (Exception e) {
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "Some Exception " + e.getMessage());

            e.printStackTrace();
        }

    }
}
