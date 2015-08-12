package com.bluescape.collaboration.socket.handler;

import com.bluescape.AppConstants;
import com.bluescape.collaboration.util.ShowActivityTimerTask;
import com.bluescape.model.Group;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.LocationMarkerModel;
import com.bluescape.model.widget.MoveModel;
import com.bluescape.model.widget.change.GeometryChangedModel;
import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;

//TODO refactor this class
public class VeMessageHandler extends BaseMessageHandler {
    private static final String TAG = VeMessageHandler.class.getSimpleName();

    public void handleMessage(JSONArray mainMessage) {

        try {

            // server <--> client
            // [client-id, "ve", target-id, event-type, event-properties]
            // ve move card
            // ["54d3bb68b5a05708542db8de","ve","54e7b226f042563700000001","position",{"order":0,"rect":[-329.6854,-132,-49.6854,28]}]

            boolean myMove = WorkSpaceState.getInstance().getClientId() != null
                    && WorkSpaceState.getInstance() != null
                    && WorkSpaceState.getInstance().mHistoryLoadCompleted
                    && WorkSpaceState
                    .getInstance()
                    .getClientId()
                    .equalsIgnoreCase(
                            mainMessage.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal()).toString());

            //Ignore if it is the Ve Message that the current Android Client generates
            if (!myMove) {


                String event = mainMessage.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal()).toString();

                // For card etc..and not non tsxappevent(web , pdf)
                if (event.equals("position")) {
                    MoveModel moveModel = parseMoveModel(mainMessage);

                    if (moveModel != null) {

                        BaseWidgetModel baseWidgetModel = WorkSpaceState.getInstance().getModelTree()
                                .getModel(moveModel.getModelID());

                        if (baseWidgetModel != null) {

                            //Set the ActivityIndicator as it is a move
                            baseWidgetModel.populateActivityFields(true, mainMessage.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal()).toString());


                            if (baseWidgetModel instanceof LocationMarkerModel) {
                                JSONObject eventJsonObj = (JSONObject) mainMessage
                                        .get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal());
                                float mX = (float) eventJsonObj.getDouble("x");
                                float mY = (float) eventJsonObj.getDouble("y");
                                ((LocationMarkerModel) baseWidgetModel).setX(mX);
                                ((LocationMarkerModel) baseWidgetModel).setY(mY);
                                baseWidgetModel.setRect(new Rect(mX, mY, mX + 100, mY + 100));
                            } else {
                                // update the order too
                                baseWidgetModel.setOrder(moveModel.getOrder());


                                //New Code To Handle the Children for Group
                                if (baseWidgetModel instanceof Group) {

//                                    if (baseWidgetModel.isModelGLInitialized) {
//                                        //Doesnt seem right
//                                        baseWidgetModel.setRect(new Rect(moveModel.getRect()));
//
//                                        baseWidgetModel.setupModelMVPMatrix();
//                                        //use this for Border etc..
//                                        baseWidgetModel.setDirty(true);
//                                    } else {
//                                        baseWidgetModel.updateOnlyRect(new Rect(moveModel.getRect()));
//                                    }

                                    Group currentGroup = (Group) baseWidgetModel;
                                    //  cos we moved the setRect to Below
//                                    float currentXDelta = currentGroup.getRect().getTOPX() - currentGroup.getXLastMove();
//                                    float currentYDelta = currentGroup.getRect().getTOPY() - currentGroup.getYLastMove();
                                    float currentXDelta = moveModel.getRect()[0] - currentGroup.getXLastMove();
                                    float currentYDelta = moveModel.getRect()[1] - currentGroup.getYLastMove();

                                    if (currentGroup.getChildModels() != null) {
                                        for (int childCount = 0; childCount < currentGroup.getChildModels().size(); childCount++) {

                                            BaseWidgetModel childWidget = currentGroup.getChildModels().get(childCount);
//                                            AppConstants.LOG(AppConstants.CRITICAL, TAG,
//                                                    "Group n Move id" + currentGroup.getID() + " Processing child: " + childWidget);

                                            //The position[X1,Y1,X2,Y2] is actually the displacement [X,Y,X,Y] for each object from when the group was created
                                            //childWidget.moveRectByXY(currentGroup.getRect().getTOPX(), currentGroup.getRect().getTOPY());
                                            childWidget.moveRectByXY(currentXDelta, currentYDelta);

                                            //Preserving the order among children
                                            childWidget.setOrder(currentGroup.getOrder() + childCount);

                                            //update on screen
                                            if (childWidget.isModelGLInitialized) {
                                                //TODO cleanup  this rect issue so it is elegant
                                                childWidget.setModelMatrix(childWidget.getRect());
                                            }


                                        }
                                        //Now set the order to higher than the last child so that getIntersectingModel picks the group up
                                        //currentGroup.setOrder(currentGroup.getOrder()+currentGroup.getChildModels().size()+1);

                                    }


                                    //TODO Try to get rid of flickering
                                    Rect newRect = currentGroup.updateGroupRectWithNewChildBounds();
                                    //currentGroup.moveRectByXY(currentXDelta, currentYDelta);

                                    if (currentGroup.isModelGLInitialized) {
                                        currentGroup.setRect(newRect);
                                        currentGroup.setModelMatrix(newRect);
                                        currentGroup.setupModelMVPMatrix();
                                        currentGroup.setDirty(true);
                                    }else {
                                        //or just ignore ?? TODO need to process the kids
                                      //  baseWidgetModel.updateOnlyRect(new Rect(moveModel.getRect()));
                                    }

                                    //store the latest move in mXLastMove and mYLastMove once hte move is processed
                                    currentGroup.setXLastMove(moveModel.getRect()[0]);
                                    currentGroup.setYLastMove(moveModel.getRect()[1]);

                                    //Set this so the ve he message updates to the new location
                                    WorkSpaceState.getInstance().getModelTree().mAreAllViewPortWidgetsInitialized = false;



                                } else {
                                    //run after 5 seconds except for Group . Somewhat of a hack to account for out of sequence ve nad he events
                                    baseWidgetModel.setActivityHideTimer();

                                    if (baseWidgetModel.isModelGLInitialized) {
                                        baseWidgetModel.setRect(new Rect(moveModel.getRect()));
                                        baseWidgetModel.setupModelMVPMatrix();
                                        //use this for Border etc..
                                        baseWidgetModel.setDirty(true);
                                    } else {
                                        baseWidgetModel.updateOnlyRect(new Rect(moveModel.getRect()));
                                    }
                                }
                            }

                            if (baseWidgetModel.isInViewPort())
                                WorkSpaceState.getInstance().getModelTree().mAreAllViewPortWidgetsInitialized = false;

                            AppConstants
                                    .LOG(AppConstants.VERBOSE, TAG, "MOVE DOUBLE OK baseWidgetModel:" + baseWidgetModel.toString() + " Awesomesauce ");
                        } else {
                            AppConstants.LOG(AppConstants.VERBOSE, TAG,
                                    "MOVE NOT OK baseWidgetModel is NULL probably an Image or position marker move that we are not handling");
                        }

                    } else {
                        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null moveModel from fromVolatileEventJSON");
                    }
                } else {
                    GeometryChangedModel geometryChangedModel = parseGeometryChanged(mainMessage);

                    if (geometryChangedModel != null) {

                        Rect rect = new Rect(geometryChangedModel.getRect());

                        BaseWidgetModel baseWidgetModel = WorkSpaceState.getInstance().getModelTree()
                                .getModel(geometryChangedModel.getModelID());
                        if (baseWidgetModel != null) {

                            //Set the ActivityIndicator as it is a move or geometryChange
                            baseWidgetModel.populateActivityFields(true, mainMessage.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal()).toString());
                            //TODO no timer here like above ??

                            //Note only if > 0 wall sends "order":0 that will mess up widget selection so only update if greater than 0
                            // ["55930841e7f97624055ca969","he","559319f6fcd8ac2605f543e3","55931b89e7f97624055cc290","tsxappevent",{"messageType":"geometryChanged","payload":{"order":0,"version":1,"windowSpaceHeight":700,"windowSpaceWidth":1020,"worldSpaceHeight":695,"worldSpaceWidth":1019,"x":957,"y":11},"targetTsxAppId":"webbrowser"}]
                            if (geometryChangedModel.getEventOrder() > 0)
                                baseWidgetModel.setOrder(geometryChangedModel.getEventOrder());

                            if (baseWidgetModel.isModelGLInitialized) {
                                baseWidgetModel.setRect(rect);
                                baseWidgetModel.setupModelMVPMatrix();
                                baseWidgetModel.setDirty(true);
                            } else {
                                baseWidgetModel.updateOnlyRect(rect);
                            }
                            if (baseWidgetModel.isInViewPort())
                                WorkSpaceState.getInstance().getModelTree().mAreAllViewPortWidgetsInitialized = false;

                        } else {
                            AppConstants.LOG(AppConstants.CRITICAL, TAG, "Null baseWidgetModel from fromVolatileEventJSON");
                        }

                    } else {
                        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null GeometryChangedModel from fromVolatileEventJSON");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public GeometryChangedModel parseGeometryChanged(JSONArray mJsonArray) {
        GeometryChangedModel model = null;
        String targetID;

        try {

            // client-id
            mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal());

            // Message Types
            mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.VE_STRING.ordinal());

            // target-id
            targetID = mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();

            // event-id
            // mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_ID.ordinal());

            // History Event
            mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal());

            // event Json Object
            String eventJsonString = mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal()).toString();

            GsonBuilder builder = new GsonFireBuilder().enableHooks(GeometryChangedModel.class).createGsonBuilder();
            Gson gson = builder.create();
            model = gson.fromJson(eventJsonString, GeometryChangedModel.class);
            model.setModelID(targetID);

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

    private MoveModel parseMoveModel(JSONArray mJsonArray) {
        MoveModel model = null;
        String targetID;

        try {

            // client-id
            mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.CLIENT_ID.ordinal());

            // Message Types
            mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.VE_STRING.ordinal());

            // target-id
            targetID = mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();

            // event-id
            // mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_ID.ordinal());

            // History Event
            mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal());

            // event Json Object
            String eventJsonString = mJsonArray.get(AppConstants.WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_PROPERTIES.ordinal()).toString();

            Gson gson = new Gson();
            model = gson.fromJson(eventJsonString, MoveModel.class);
            model.setModelID(targetID);
            WorkSpaceState.getInstance().updateOrder(model.getOrder());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return model;
    }
}