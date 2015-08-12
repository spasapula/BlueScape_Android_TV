package com.bluescape.model.widget;

import android.opengl.Matrix;
import android.support.annotation.NonNull;

import com.bluescape.AppConstants;
import com.bluescape.collaboration.socket.sender.HePinMessageSender;
import com.bluescape.collaboration.util.ShowActivityTimerTask;
import com.bluescape.model.IModel;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.google.gson.annotations.SerializedName;

import org.bson.types.ObjectId;
import org.json.JSONArray;

import java.nio.ByteBuffer;
import java.util.Timer;

public abstract class BaseWidgetModel implements IModel, Comparable<BaseWidgetModel> {

    // region Member Variables
    private static final String TAG = BaseWidgetModel.class.getSimpleName();

    protected boolean mIsPinned = false;


    // Target of this widget
    @SerializedName("target-id")
    private String mTargetID = "";

    @SerializedName("rect")
    protected float[] mRectArray;

    @SerializedName("order")
    protected int mOrder = 1;

    @SerializedName("id")
    protected String mID;

    protected Rect mRect;

    protected float[] mModelMatrix = new float[16];
    protected float[] mModelBorderMatrix = new float[16];

    protected InitialsModel mInitialsModel;


    protected boolean mIsDirty = true;

    private String mEventId = null;//#TODO put this in all


    // number of coordinates per vertex in this array
    protected final int mCoordsPerVertex = 3;


    /**
     * Maintining matrices for the draw loop.
     */
    protected float[] mMVPMatrix;
    protected float[] mBorderMVPMatrix;


    // Booleans that allocate and deallocate models for opengl drawLoop to be
    // used in is isInViewPort()
    public boolean isModelGLInitialized = false;

    public boolean isVBOBufferInitialized = false;

    protected float[] tempMVPMatrix = new float[16];
    protected float[] tempBorderMVPMatrix = new float[16];

    public final int COORDS_PER_TEX = 2;

    //Used for Activity Indicator
    protected boolean mActivityIndicator = false;
    protected String mActivityCollaborator;
    //This is color 0,1,2 and 3 is alpha so it can be used in the shader
    protected float[] mActivityCollaboratorColor;
    protected String mActivityInitials;


    //Used to Keep a Reference to the Parent Group for draw() and intersecting etc.
    protected BaseWidgetModel mParentGroup;

    // endregion

    // region Constructor
    public BaseWidgetModel() {
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  BaseWidgetModel() Default");

    }

    /**
     * Call super() so we can generate an ID
     */
    public BaseWidgetModel(Rect rect) {
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  BaseWidgetModel(Rect rect) ");

        this.mRect = rect;
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(this.hashCode());
        this.mID = new ObjectId().toString();
    }

    // endregion

    // compareTo method for Sorting the Widgets in Arrays
    public int compareTo(@NonNull BaseWidgetModel compareBaseWidget) {
        if (this.mOrder > compareBaseWidget.mOrder)
            return 1;
        if (this.mOrder < compareBaseWidget.mOrder)
            return -1;
        if (compareBaseWidget.mEventId == null) {
            if (mEventId == null) return 0;
            return 1;
        }
        if (mEventId == null) {
            if (compareBaseWidget == null) return 0;//already handled before
            return -1;
        }
        return mEventId.compareTo(compareBaseWidget.mEventId);
    }

    // Drawable methods
    public float doesIntersectXY(float x, float y) {
        //Default
        return -1;
    }

    // region Abstract Methods
    public abstract void draw(float[] matrix);

    // endregion

    public int getCoordsPerVertex() {
        return this.mCoordsPerVertex;
    }

    /**
     * Override this if you are using a template or actual height for the scale
     * matrix.
     *
     * @return
     */
    @Override
    public float getHeight() {
        return this.getRect().getHeight();
    }

    @Override
    public String getID() {
        return this.mID;
    }

    @Override
    public float[] getModelMatrix() {
        return this.mModelMatrix;
    }

    public float[] getModelBorderMatrix() {
        return this.mModelBorderMatrix;
    }


    public float[] getMVPMatrix() {
        return mMVPMatrix;
    }

    public float[] getBorderMVPMatrix() {
        return mBorderMVPMatrix;
    }

    public BaseWidgetModel getParentGroup() {
        return mParentGroup;
    }

    public int
    getOrder() {
        return mOrder;
    }

    // endregion

    public float[] getParentMVPMatrix() {

        // if(mTargetID != null && mTargetID.equals("") &&
        // !mTargetID.equalsIgnoreCase(WorkSpaceState.getInstance().getModelTree().root.getID())
        // ) {
        if (mTargetID != null && !mTargetID.equals("") && !mTargetID.equalsIgnoreCase(WorkSpaceState.getInstance().getModelTree().root.getID())) {

            BaseWidgetModel parentWidgetModel = WorkSpaceState.getInstance().getModelTree().getModel(mTargetID);
            if (parentWidgetModel == null) {
                parentWidgetModel = WorkSpaceState.getInstance().getModelTree().root;//TODO this is stroke on a deleted object?
            }
            // Assuming hte mMVPMatrix of each node, image or possible parent
            // model is updated in the FullHisotyMap
            return parentWidgetModel.mMVPMatrix;

        } else {

            // default return the root's projection matrix
            return WorkSpaceState.getInstance().getModelTree().root.getMVPMatrix();
        }

    }


    public String getEventId() {
        return mEventId;
    }

    public void setEventId(String mEventId) {
        this.mEventId = mEventId;
    }

    //used for border and pop up of pin/delete
    public float[] getParentViewXYCoords(float parentViewWidth, float parentViewHeight) {

        // return TOPX,TOPY,BOTX,BOTY,MIDTOPX,MIDTOPY
        float[] widgetParentViewPosition = new float[6];
        float[] worldWorkspaceParentViewPosition = new float[4];

        if (WorkSpaceState.getInstance().getWorldPosition() != null && (WorkSpaceState.getInstance().getWorldPosition().length == 4)) {
            float[] worldWorkspaceWindowCoords = WorkSpaceState.getInstance().getWorldPosition();

            float parentViewWorkspaceWidth = worldWorkspaceWindowCoords[Rect.BOTX] - worldWorkspaceWindowCoords[Rect.TOPX];
            float parentViewWorkspaceHeight = worldWorkspaceWindowCoords[Rect.BOTY] - worldWorkspaceWindowCoords[Rect.TOPY];

            // Scale the World in Parent View Window Ratio
            worldWorkspaceParentViewPosition[Rect.TOPX] = (worldWorkspaceWindowCoords[Rect.TOPX] * (parentViewWidth / parentViewWorkspaceWidth));
            worldWorkspaceParentViewPosition[Rect.TOPY] = (worldWorkspaceWindowCoords[Rect.TOPY] * (parentViewHeight / parentViewWorkspaceHeight));

            worldWorkspaceParentViewPosition[Rect.BOTX] = (worldWorkspaceWindowCoords[Rect.BOTX] * (parentViewWidth / parentViewWorkspaceWidth));
            worldWorkspaceParentViewPosition[Rect.BOTY] = (worldWorkspaceWindowCoords[Rect.BOTY] * (parentViewHeight / parentViewWorkspaceHeight));

            // Scale the Widget in Parent View Window Ratio
            widgetParentViewPosition[Rect.TOPX] = (mRect.rect[Rect.TOPX] * (parentViewWidth / parentViewWorkspaceWidth));
            widgetParentViewPosition[Rect.TOPY] = (mRect.rect[Rect.TOPY] * (parentViewHeight / parentViewWorkspaceHeight));

            widgetParentViewPosition[Rect.BOTX] = (mRect.rect[Rect.BOTX] * (parentViewWidth / parentViewWorkspaceWidth));
            widgetParentViewPosition[Rect.BOTY] = (mRect.rect[Rect.BOTY] * (parentViewHeight / parentViewWorkspaceHeight));

            // get the midpoint in X direction
            widgetParentViewPosition[Rect.MIDTOPX] = (widgetParentViewPosition[Rect.TOPX] - worldWorkspaceParentViewPosition[Rect.TOPX] + ((widgetParentViewPosition[Rect.BOTX] - widgetParentViewPosition[Rect.TOPX]) / 2));

            // Get the TOP in Y direction
            widgetParentViewPosition[Rect.MIDTOPY] = (widgetParentViewPosition[Rect.TOPY] - worldWorkspaceParentViewPosition[Rect.TOPY]);

        }

        return widgetParentViewPosition;
    }

    @Override
    public Rect getRect() {
        return this.mRect;
    }

    //This is always populated in the fromJson and should be used if it is possible that mRect is not created yet
    public float[] getRectArray() {
        return this.mRectArray;
    }

    @Override
    public String getTargetID() {
        return mTargetID;
    }

    public int getVertexStride() {
        return mCoordsPerVertex * AppConstants.BYTES_FLOAT;
    }

    /**
     * Override this if you are using a template or actual height for the scale
     * matrix.
     *
     * @return
     */
    @Override
    public float getWidth() {
        return this.getRect().getWidth();
    }

    @Override
    public boolean isDirty() {
        return mIsDirty;
    }

    // Method that tells if the given model is in the view port
    @Override
    public boolean isInViewPort() {
        if (mRectArray == null) {
            return true;
        }

        float workspaceX1 = WorkSpaceState.getInstance().getWorldPosition()[Rect.TOPX];
        float workspaceY1 = WorkSpaceState.getInstance().getWorldPosition()[Rect.TOPY];
        float workspaceX2 = WorkSpaceState.getInstance().getWorldPosition()[Rect.BOTX];
        float workspaceY2 = WorkSpaceState.getInstance().getWorldPosition()[Rect.BOTY];
        float xOverlap = Math.max(0, Math.min(workspaceX2, mRectArray[2]) - Math.max(workspaceX1, mRectArray[0]));
        float yOverlap = Math.max(0, Math.min(workspaceY2, mRectArray[3]) - Math.max(workspaceY1, mRectArray[1]));
        float overlapArea = xOverlap * yOverlap;

        return overlapArea > 0;
    }

    @Override
    public boolean isPinned() {
        return this.mIsPinned;
    }


    public boolean hasActivityIndicator() {
        return mActivityIndicator;
    }

    public String getActivityCollaborator() {
        return mActivityCollaborator;
    }

    public float[] getActivityCollaboratorColor() {
        return mActivityCollaboratorColor;
    }

    public void setActivityCollaboratorColor(float[] mActivityCollaboratorColor) {
        this.mActivityCollaboratorColor = mActivityCollaboratorColor;
    }

    public String getActivityInitials() {
        return mActivityInitials;
    }

    public void setActivityInitials(String mActivityInitials) {
        this.mActivityInitials = mActivityInitials;
    }


    public String getUniqueActivityInitialsWithColor() {
        if (mActivityCollaboratorColor != null && mActivityCollaboratorColor.length >= 3)
            return mActivityInitials + "-" + mActivityCollaboratorColor[0] + "-" + mActivityCollaboratorColor[1] + "-" + mActivityCollaboratorColor[2];
        else
            return mActivityInitials;
    }

    // This can be used to check all models like Stroke,Note etc. if they are in
    // view port
    public boolean isXYInViewPort(float x, float y) {
        // Lets make things easy to read here.
        float objx, objy, width, height;
        try {

            objx = WorkSpaceState.getInstance().getWorldPosition()[Rect.TOPX];
            objy = WorkSpaceState.getInstance().getWorldPosition()[Rect.TOPY];
            width = WorkSpaceState.getInstance().getWorldPosition()[Rect.BOTX] - WorkSpaceState.getInstance().getWorldPosition()[Rect.TOPX];
            height = WorkSpaceState.getInstance().getWorldPosition()[Rect.BOTY] - WorkSpaceState.getInstance().getWorldPosition()[Rect.TOPY];

            if ((x > objx) && (x < (objx + width)) && (y > objy) && (y < (objy + height))) {
                return true;
            }
        } catch (Exception ex) {
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "World Position Not set Exception in BaseWidgetModel.isXYInViewPort() ex:" + ex);
            ex.printStackTrace();
        }
        return false;
    }

    public void preDraw() {

    }

    @Override
    public void createDrawable() {

    }

    @Override
    public void createBorderDrawable() {

    }

    @Override
    public boolean sendToWSServer() {
        return true;
    }

    public boolean sendToWSServerEdit() {
        return true;
    }

    // region Public Methods

    //Used in Ve and he messages t oshow the activity indicator
    public void populateActivityFields(boolean activityIndicator, String clientId) {
        setActivityIndicator(activityIndicator);
        setActivityCollaborator(clientId);
        //setActivityCollaboratorColor(WorkSpaceState.getInstance().getWorkSpaceModel().getCollaboratorColor(clientId));
        float[] collabColorWithAlpha = new float[4];
        float[] collabColorWithoutAlpha = WorkSpaceState.getInstance().getWorkSpaceModel().getCollaboratorColor(clientId);

        if (collabColorWithoutAlpha != null) {
            collabColorWithAlpha[0] = collabColorWithoutAlpha[0];
            collabColorWithAlpha[1] = collabColorWithoutAlpha[1];
            collabColorWithAlpha[2] = collabColorWithoutAlpha[2];
            collabColorWithAlpha[3] = 0.3f;
            setActivityCollaboratorColor(collabColorWithAlpha);
        } else {
            // This means that this was during http historyLoad and that the actual color
            // will be set later when the web socket RL message hits
            //or also set to white in case of widgets inside group where there is no collaborator for the
        }
        setActivityInitials(WorkSpaceState.getInstance().getWorkSpaceModel().getCollaboratorInitials(clientId));

    }

    //set the collab color for cases where group is active after history load
    public void populateActivityCollaboratorColor(String clientId) {
        float[] collabColorWithAlpha = new float[4];
        float[] collabColorWithoutAlpha = WorkSpaceState.getInstance().getWorkSpaceModel().getCollaboratorColor(clientId);

        //Set initials also
        setActivityInitials(WorkSpaceState.getInstance().getWorkSpaceModel().getCollaboratorInitials(clientId));

        if (collabColorWithoutAlpha != null) {
            collabColorWithAlpha[0] = collabColorWithoutAlpha[0];
            collabColorWithAlpha[1] = collabColorWithoutAlpha[1];
            collabColorWithAlpha[2] = collabColorWithoutAlpha[2];
            collabColorWithAlpha[3] = 0.3f;
            setActivityCollaboratorColor(collabColorWithAlpha);
        } else {
            // This means it is not set and not drawn
        }
    }

    //used in ve and he t ostop showing Box and Initials
    public void setActivityHideTimer() {
        //run after 5 seconds . Somewhat of a hack to account for out of sequence ve nad he events
        Timer t = new Timer();
        ShowActivityTimerTask showActivityTimerTask = new ShowActivityTimerTask(this);
        t.schedule(showActivityTimerTask, AppConstants.ACTIVITY_INDICATOR_TIME);
    }

    public boolean sendToWSServerHide() {
        return true;
    }

    @Override
    public boolean sendToWSServerPin() {
        HePinMessageSender hePinMessageSender = new HePinMessageSender(this);
        hePinMessageSender.send();
        return true;
    }

    public boolean sendToWSServerTemplate() {
        return true;
    }


    //Convenience method to create Rect in case on mRectArray is there
    public Rect createRect() {
        if (mRect == null)
            mRect = new Rect();

        for (int i = 0; i < mRectArray.length; i++) {
            mRect.getRect()[i] = mRectArray[i];
        }
        return mRect;
    }

    public Rect createRect(JSONArray coords) {
        if (mRect == null)
            mRect = new Rect();

        try {
            for (int i = 0; i < coords.length(); i++) {
                mRect.getRect()[i] = (float) coords.getDouble(i);
            }
            //Always keep mRect and mRectArray in sync
            mRectArray = mRect.getRect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mRect;
    }


    public Rect moveRectByXY(float X, float Y) {
        if (mRect == null)
            createRect();

        try {
            mRect.rect[Rect.TOPX] = mRect.rect[Rect.TOPX] + X;
            mRect.rect[Rect.TOPY] = mRect.rect[Rect.TOPY] + Y;
            mRect.rect[Rect.BOTX] = mRect.rect[Rect.BOTX] + X;
            mRect.rect[Rect.BOTY] = mRect.rect[Rect.BOTY] + Y;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //Always keep mRect and mRectArray in sync
        mRectArray = mRect.getRect();

        return mRect;
    }

    @Override
    public void setDirty(boolean isUpdated) {
        this.mIsDirty = isUpdated;
    }

    @Override
    public void setHeight(float height) {
        this.mRect.setHeight(height);
        setModelMatrix(this.mRect);
    }

    @Override
    public void setID(String ID) {
        mID = ID;
    }

    @Override
    public void setModelMatrix(float[] matrix) {
        this.mModelMatrix = matrix;
    }


    @Override
    public void setModelBorderMatrix(float[] matrix) {
        this.mModelBorderMatrix = matrix;
    }

    @Override
    public void setModelMatrix(Rect rect) {

        /**
         * Scale the difference between the template width and the rect width.
         * Rects are updated with move commands, but the matrix is what we use
         * to scale.
         */
        float scalex = (rect.getWidth() / getWidth());
        float scaleY = (rect.getHeight() / getHeight());
        float scaleZ = 1f;

        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(), 1f);

        Matrix.scaleM(mModelMatrix, 0, scalex, scaleY, scaleZ);

        //Set the BorderMatrix too if ActivityIndicator is set
        if (this.hasActivityIndicator()) {
            setModelBorderMatrix(rect);
            mInitialsModel.setModelMatrix(rect);
        }
    }


    @Override
    public void setModelBorderMatrix(Rect rect) {

        //Set the border to the actual Width of the the Rect and then don't scale it in setModelBorderMatrix(Rect rect)
        float scaleX = 1f;
        float scaleY = 1f;
        float scaleZ = 1f;

        Matrix.setIdentityM(mModelBorderMatrix, 0);

        //Set the Starting point for the Border
        Matrix.translateM(mModelBorderMatrix, 0, rect.getTOPX() - WorkSpaceState.getInstance().getCurrentBorderWidth(), rect.getTOPY() - WorkSpaceState.getInstance().getCurrentBorderWidth(), 1f);


        Matrix.scaleM(mModelBorderMatrix, 0, scaleX, scaleY, scaleZ);

        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  setModelBorderMatrix(Rect rect), rect.getTOPX() = " + rect.getTOPX() + " rect.getTOPY() = " + rect.getTOPY() + " WorkSpaceState.getInstance().getCurrentBorderWidth()=" + WorkSpaceState.getInstance().getCurrentBorderWidth());
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  setModelBorderMatrix(Rect rect), rect.getWidth() = " + rect.getWidth() + " getWidth() = " + getWidth() + " rect.getHeight()=" + rect.getHeight() + " getHeight()=" + getHeight());
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  setModelBorderMatrix(Rect rect), scaleX = " + scaleX + " scaleY = " + scaleY + " scaleZ=" + scaleZ);

    }

    public void setMVPMatrix(float[] mvpMatrix) {
        mMVPMatrix = mvpMatrix;
    }

    public void setBorderMVPMatrix(float[] mvpMatrix) {
        mBorderMVPMatrix = mvpMatrix;
    }

    public void setOrder(int order) {
        mOrder = order;
    }

    @Override
    public void setPinned(boolean pinned) {
        this.mIsPinned = pinned;
    }

    public void setActivityIndicator(boolean activityIndicator) {
        this.mActivityIndicator = activityIndicator;
    }

    public void setActivityCollaborator(String activityCollaborator) {
        mActivityCollaborator = activityCollaborator;
    }


    public float calculateMatrixZorder() {
        return calculateMatrixZorder(mOrder);
    }

    public float calculateMatrixZorder(float order) {
        //return -((order % AppConstants.INT_ORTHO_PROJECTION_FAR_Z) / AppConstants.ORTHO_PROJECTION_FAR_Z);
        //	return (float) ((-10.0 * order) / ((double) MAX_Z)) - 0.5f;
        //	double newz = ((double) -10.0*computedOrder) / (250000.0);
        //	AppConstants.LOG(AppConstants.CRITICAL, TAG, this.getClass() + " order "+newz);
        //if(this instanceof StrokeModel)
        return -1;
        //return -1*order;
        //TODO this was a mess of zorder madness.
    }

    @Override
    public void setRect(Rect rect) {
        this.mRect = rect;
        setModelMatrix(rect);
    }


    public void setRectArray(float[] rectArray) {
        this.mRectArray = rectArray;
    }

    public void setParentGroup(BaseWidgetModel mParentGroup) {
        this.mParentGroup = mParentGroup;
    }

    @Override
    public void setTargetID(String targetID) {
        mTargetID = targetID;
    }

    public void setupModelMVPMatrix() {
        Matrix.multiplyMM(tempMVPMatrix, 0, this.getParentMVPMatrix(), 0, mModelMatrix, 0);
        mMVPMatrix = tempMVPMatrix;

        //Setup the borderMVPMatrix if ActivityIndicator is True
        if (this.hasActivityIndicator()) {
            setupModelBorderMVPMatrix();
            mInitialsModel.setupModelMVPMatrix();
        }
        if (this.getParentGroup() != null) {
            setupModelBorderMVPMatrix();
        }
    }


    public void setupModelBorderMVPMatrix() {
        try {
            Matrix.multiplyMM(tempBorderMVPMatrix, 0, this.getParentMVPMatrix(), 0, mModelBorderMatrix, 0);
            mBorderMVPMatrix = tempBorderMVPMatrix;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // endregion

    @Override
    public void setWidth(float width) {
        this.mRect.setWidth(width);
        setModelMatrix(this.mRect);
    }


    @Override
    public void updateOnlyRect(Rect rect) {
        this.mRect = rect;
    }

    // region Private Methods
    // endregion
}