package com.bluescape.model;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.collaboration.socket.sender.VeTsxAppEventGeometryChangedMessageSender;
import com.bluescape.collaboration.util.ImageFetcher;
import com.bluescape.model.util.Rect;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.BrowserModel;
import com.bluescape.model.widget.ImageModel;
import com.bluescape.model.widget.LocationMarkerModel;
import com.bluescape.model.widget.MoveModel;
import com.bluescape.model.widget.StrokeModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.orthoM;

public class ModelTree {

    private static final String TAG = ModelTree.class.getSimpleName();

    // The projection matrix
    private final float[] mProjectionMatrix = new float[16];

    private final ImageFetcher imageFetcher = new ImageFetcher(50, 101);

    private final Map<String, BaseWidgetModel> mWorkspaceModels;

    public WorkSpaceModel root;

    private WorkSpaceState workspace;

    public boolean mAreAllViewPortWidgetsInitialized = false;
    private boolean mFirstLoad = true;
    private BaseWidgetModel scene[];

    public ModelTree(WorkSpaceState workspace) {

        mWorkspaceModels = new HashMap<String, BaseWidgetModel>();
        this.workspace = workspace;
        root = workspace.getWorkSpaceModel();
    }

    public Collection<BaseWidgetModel> allModels() {
        return mWorkspaceModels.values();
    }

    public void setRoot(WorkSpaceModel root) {
        this.root = root;
    }

    public void add(BaseWidgetModel baseWidgetModel) {

        synchronized (mWorkspaceModels) {
            mWorkspaceModels.put(baseWidgetModel.getID(), baseWidgetModel);
        }
        if (workspace.mHistoryLoadCompleted) {
            mAreAllViewPortWidgetsInitialized = false;
        }
    }

    public boolean delete(String ID) {

        // delete it from the full list assume we are calling it because it is
        // already there
        synchronized (mWorkspaceModels) {
            mWorkspaceModels.remove(ID);
        }
        mAreAllViewPortWidgetsInitialized = false;
        return true;
    }

    public BaseWidgetModel getModel(String ID) {
        return mWorkspaceModels.get(ID);
    }

    public void clear() {
        mAreAllViewPortWidgetsInitialized = false;

        synchronized (mWorkspaceModels) {
            mWorkspaceModels.clear();
        }
        mFirstLoad = true;
    }


    public void panAndZoomViewport(float zoom, float offsetX, float offsetY) {
        // original
        synchronized (mWorkspaceModels) {
            orthoM(mProjectionMatrix, 0, -(workspace.getAspectRatio() * zoom) + offsetX,
                    (workspace.getAspectRatio() * zoom) + offsetX, zoom + offsetY, (-zoom + offsetY), -100f, 100f);

            root.setMVPMatrix(mProjectionMatrix);

            // Overridden for Worksapce as it is unique compared to other
            // drawables so this method is called in the updateViewport() in
            // Renderer.java
            root.setupModelMVPMatrix(mProjectionMatrix);

            // Set the workspace background position
            workspace.setWorldPosition(
                    new float[]{-(workspace.getAspectRatio() * zoom) + offsetX, -zoom + offsetY,
                            (workspace.getAspectRatio() * zoom) + offsetX, zoom + offsetY});
            mAreAllViewPortWidgetsInitialized = false;

        }
    }

    public void moveWidgetOnWorkSpace(MoveModel moveModel) {

        if (moveModel != null) {
            BaseWidgetModel baseWidgetModel = getModel(moveModel.getModelID());

            if (baseWidgetModel != null) {


                //Handle Groups separately
                if (baseWidgetModel instanceof Group) {


                    Group currentGroup = (Group) baseWidgetModel;

//                    float currentXDelta = moveModel.getRect()[0] - currentGroup.getXLastMove();
//                    float currentYDelta = moveModel.getRect()[1] - currentGroup.getYLastMove();

                    float currentXDelta = moveModel.getRect()[0] - currentGroup.getX1();
                    float currentYDelta = moveModel.getRect()[1] - currentGroup.getY1();


                    if (currentGroup.getChildModels() != null) {
                        for (int childCount = 0; childCount < currentGroup.getChildModels().size(); childCount++) {

                            BaseWidgetModel childWidget = currentGroup.getChildModels().get(childCount);
//                            AppConstants.LOG(AppConstants.CRITICAL, TAG,
//                                    "Group n Move id" + currentGroup.getID() + " Processing child: " + childWidget);

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

                    }


                    Rect newRect = currentGroup.updateGroupRectWithNewChildBounds();

                    if (currentGroup.isModelGLInitialized) {
                        currentGroup.setRect(newRect);
                        currentGroup.setModelMatrix(newRect);
                        currentGroup.setupModelMVPMatrix();
                        currentGroup.setDirty(true);
                    } else {
                        //or just ignore ?? TODO need to process the kids
                        //  baseWidgetModel.updateOnlyRect(new Rect(moveModel.getRect()));
                    }

                    //store the latest move in mXLastMove and mYLastMove once hte move is processed
                    currentGroup.setXLastMove(currentXDelta);
                    currentGroup.setYLastMove(currentYDelta);

                    //Now update the MOveModel to send to server the server as it is different for Move in a Group
                    //float[] groupMove = {currentXDelta, currentYDelta, currentXDelta, currentYDelta};

                    float currentOriginalXDelta = moveModel.getRect()[0] - currentGroup.getX1OriginalGroupPosition();
                    float currentOriginalYDelta = moveModel.getRect()[1] - currentGroup.getY1OriginalGroupPosition();
                    float[] groupMove = {currentOriginalXDelta, currentOriginalYDelta, currentOriginalXDelta, currentOriginalYDelta};

                    moveModel.setRect(groupMove);

                    //Set this so the ve he message updates to the new location
                    WorkSpaceState.getInstance().getModelTree().mAreAllViewPortWidgetsInitialized = false;


                } else {
                    baseWidgetModel.setRect(new Rect(moveModel.getRect()));
                    baseWidgetModel.setOrder(moveModel.getOrder());
                    baseWidgetModel.setupModelMVPMatrix();
                    baseWidgetModel.preDraw();
                    mAreAllViewPortWidgetsInitialized = false;
                    //advanceStrokes(baseWidgetModel);
                }


            } else {
                AppConstants.LOG(AppConstants.CRITICAL, TAG,
                        "MOVE NOT OK baseWidgetModel is NULL probably an Image or position marker move that we are not handling");
            }

            //Check the type of BaseWidgetModel if BrowserModel then send TsxAppEvent for everything else just send a PositionChange like before
            if (baseWidgetModel instanceof BrowserModel) {

                //set the right x,y to rect[x1,y1] as that is used to sed the values ot the server
                ((BrowserModel) baseWidgetModel).getPayload().setX(moveModel.getRect()[Rect.TOPX]);
                ((BrowserModel) baseWidgetModel).getPayload().setY(moveModel.getRect()[Rect.TOPY]);

                ((BrowserModel) baseWidgetModel).getPayload().setWorldSpaceWidth(((BrowserModel) baseWidgetModel).getRect().getWidth());
                ((BrowserModel) baseWidgetModel).getPayload().setWorldSpaceHeight(((BrowserModel) baseWidgetModel).getRect().getHeight());

                VeTsxAppEventGeometryChangedMessageSender veTsxAppEventGeometryChangedMessageSender = new VeTsxAppEventGeometryChangedMessageSender(baseWidgetModel);
                veTsxAppEventGeometryChangedMessageSender.send();

            } else {
                if (baseWidgetModel instanceof Group) {
                    moveModel.sendVeToWSServer();

                } else {
                    moveModel.sendVeToWSServer();
                }
            }

        } else {
            AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null moveModel from MoveModel.fromJSON");
        }
    }

    public BaseWidgetModel getIntersectingModel(float x, float y) {
        BaseWidgetModel currentTopBaseWidgetModel = null;
        if (scene == null) return null;
        try {
            long start = System.currentTimeMillis();

            float currentHighestEventOrder = 0;

            for (int i = 0; i < scene.length; i++) {
                BaseWidgetModel baseWidgetModel = scene[i];
                //Also check to make sure it is not part of a Group
                //if (baseWidgetModel.isModelGLInitialized) {
                if (baseWidgetModel.isModelGLInitialized && (baseWidgetModel.getParentGroup() == null)) {

                    float mIntersectingEventOrder = baseWidgetModel.doesIntersectXY(x, y);

                    if (mIntersectingEventOrder > currentHighestEventOrder) {
                        currentHighestEventOrder = mIntersectingEventOrder;
                        currentTopBaseWidgetModel = baseWidgetModel;
                    }
                }
            }
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "getIntersectingDrawable Time " + (System.currentTimeMillis() - start));

        } catch (Exception ex) {
            ex.printStackTrace();
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "Exception in getIntersectingDrawable() ex:" + ex);

        }

        return currentTopBaseWidgetModel;
    }


    public void updateViewport(int width, int height) {
        synchronized (mWorkspaceModels) {
            root.setViewPortWidth(width);
            root.createDrawable();

            workspace.setMaxZoom(width);

            float aspectRatio = (float) width / (float) height;
            workspace.setAspectRatio(aspectRatio);

            glViewport(0, 0, width, height);

            panAndZoomViewport(workspace.getZoom(), workspace.getOffset()[0], WorkSpaceState.getInstance()
                    .getOffset()[1]);
        }
    }

    // Draw method called from the renderer
    // This needs to be fast
    private int xyz = 0;

    public void drawFullHistory() {
        try {
            long start = System.currentTimeMillis();
            if (root == null || !workspace.mHistoryLoadCompleted) return;
            if (mFirstLoad) {
                String objectId = AppSingleton.getInstance().getApplication().getDataFromSharedPrefs(AppConstants.CENTER_ON_OBJECT_ID, "");
                if (objectId != null && objectId.length() > 0) {
                    BaseWidgetModel baseWidgetModel = mWorkspaceModels.get(objectId);
                    LocationMarkerModel marker = (LocationMarkerModel) baseWidgetModel;
                    if (marker != null) {
                        float[] offset = new float[2];
                        offset[0] = marker.getX();
                        offset[1] = marker.getY();
                        WorkSpaceState.getInstance().setOffset(offset);
                    }

                    mFirstLoad = false;
                }
            }
//			xyz++;
//			if(xyz==200){
//				Debug.startMethodTracing("methods", 256000000);
//
            if (!mAreAllViewPortWidgetsInitialized) {
                synchronized (mWorkspaceModels) {
                    scene = initializeViewPortWidgets();
                }
                if (scene != null) {
                    AppConstants.LOG(AppConstants.CRITICAL, TAG, "Initialize " + scene.length + " Loop finishing in " + (System.currentTimeMillis() - start));
                    AppConstants.LOG(AppConstants.CRITICAL, TAG, "Total Heap Usage " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
                }
            }
            root.draw(mProjectionMatrix);
            root.setMVPMatrix(mProjectionMatrix);
            start = System.currentTimeMillis();
            synchronized (mWorkspaceModels) {
                if (scene != null) {
                    for (int i = 0; i < scene.length; i++) {
                        if (scene[i] == null) break;
                        drawWidget(scene[i]);
                    }
                }
            }
            //	AppConstants.LOG(AppConstants.CRITICAL, TAG, "Draw Loop finishing in "+ (System.currentTimeMillis() - start));

        } catch (Exception ex) {
            ex.printStackTrace();
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "Exception in drawAll ex: " + ex.toString());
        }
    }

    private void drawWidget(BaseWidgetModel baseWidgetModel) {
        if (!baseWidgetModel.isModelGLInitialized) return;
        try {
            baseWidgetModel.draw(baseWidgetModel.getParentMVPMatrix());
        } catch (Exception e) {
            e.printStackTrace();
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "Exception while drawing " + e.toString() + " " + baseWidgetModel.toString());
        }


    }

    private BaseWidgetModel[] initializeViewPortWidgets() {
        try {
            Iterator<Map.Entry<String, BaseWidgetModel>> iterator = mWorkspaceModels.entrySet().iterator();
            BaseWidgetModel scene[] = new BaseWidgetModel[mWorkspaceModels.size()];
            setCurrentWorkSpaceBorderWidth();
            setCurrentWorkSpaceInitialsWidth();
            int i = 0;
            int strokes = 0;
            LinkedList<StrokeModel> targettedStrokes = new LinkedList<>();
            String workspaceId = root.getId();
            while (iterator.hasNext()) {

                BaseWidgetModel baseWidgetModel = iterator.next().getValue();
                if (baseWidgetModel.isInViewPort()) {
                    initializeSpecialModels(baseWidgetModel);
                    if (baseWidgetModel instanceof StrokeModel && workspaceId != null && baseWidgetModel.getTargetID() != null && !workspaceId.equals(baseWidgetModel.getTargetID())) {
                        targettedStrokes.add((StrokeModel) baseWidgetModel);
                        ((StrokeModel) baseWidgetModel).initializeParentModel();
                    } else {
                        scene[i++] = baseWidgetModel;
                    }
                } else if (baseWidgetModel.isModelGLInitialized) {
                    baseWidgetModel.isModelGLInitialized = false;
                }

            }

            AppConstants.LOG(AppConstants.CRITICAL, TAG, "Initialize total strokes " + strokes);

            scene = Arrays.copyOf(scene, i);
            Arrays.sort(scene);

            for (i = 0; i < scene.length; i++) {
                scene[i].preDraw();
            }
            mAreAllViewPortWidgetsInitialized = true;
            return scene;
        } catch (Exception ex) {
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "Exception in initializeViewPortWidgets ex: " + ex.toString());
            ex.printStackTrace();
        }
        return null;
    }

    private void initializeSpecialModels(BaseWidgetModel baseWidgetModel) {
        if (baseWidgetModel instanceof ImageModel) {
            imageFetcher.fetchNeededImages((ImageModel) baseWidgetModel);
        } else if (baseWidgetModel instanceof LocationMarkerModel) {

            baseWidgetModel.setPinned(true);
            // For Note check if even one of the the 4 edges X1,Y1,X2,Y2 is in
            // the mWorldPosition
            LocationMarkerModel locationMarkerModel = (LocationMarkerModel) baseWidgetModel;
            // Rebuild the Location object with the right dimensions if true
            float locationMarkerWidth = workspace.getZoom() * AppConstants.MARKER_TO_WORKSPACE_STATE_ZOOM_RATIO;

            Rect newLocationRect = new Rect(locationMarkerModel.getX() - (locationMarkerWidth / 2), locationMarkerModel.getY()
                    - (locationMarkerWidth / 2),
                    locationMarkerModel.getX() + (locationMarkerWidth / 2), locationMarkerModel.getY() + (locationMarkerWidth / 2));

            baseWidgetModel.setRect(newLocationRect);
            ((LocationMarkerModel) baseWidgetModel).createDrawable();

        }
    }

    public void performUndo() {
        //TODO fix concurrency
        if (mWorkspaceModels.size() > 0) {
            String key = workspace.getModelTree().getLastBaseWidgetModelIdFromFullHistoryModelMap();
            mWorkspaceModels.remove(key);
            BaseWidgetModel baseWidgetModel = getLastBaseWidgetModelFromFullHistoryModelMap();

            if (baseWidgetModel instanceof LocationMarkerModel) {
                workspace.getWorkSpaceModel().deleteMarker(key);
            }

        }
    }

    public BaseWidgetModel getLastBaseWidgetModelFromFullHistoryModelMap() {
        String myKey = (String) mWorkspaceModels.keySet().toArray()[mWorkspaceModels.size() - 1];
        return mWorkspaceModels.get(myKey);
    }

    public String getLastBaseWidgetModelIdFromFullHistoryModelMap() {
        return (String) mWorkspaceModels.keySet().toArray()[mWorkspaceModels.size() - 1];
    }

    public float setCurrentWorkSpaceBorderWidth() {
        float currentBorderWidth = workspace.getZoom() * AppConstants.BORDER_TO_WORKSPACE_STATE_ZOOM_RATIO;

        workspace.setCurrentBorderWidth(currentBorderWidth);
        return currentBorderWidth;
    }

    public float setCurrentWorkSpaceInitialsWidth() {
        float currentInitialsWidth = workspace.getZoom() * AppConstants.INITIALS_TO_WORKSPACE_STATE_ZOOM_RATIO;
        //float currentInitialsWidth = 800;

        workspace.setCurrentInitialsWidth(currentInitialsWidth);
        return currentInitialsWidth;
    }
}
