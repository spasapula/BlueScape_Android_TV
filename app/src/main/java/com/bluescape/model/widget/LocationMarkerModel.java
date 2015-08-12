package com.bluescape.model.widget;

import android.graphics.Bitmap;
import android.opengl.Matrix;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.collaboration.socket.sender.HeCreateMarkerMessageSender;
import com.bluescape.collaboration.socket.sender.HeMarkerDeleteSender;
import com.bluescape.model.util.Rect;
import com.bluescape.view.shaders.TextureHelper;
import com.google.gson.annotations.SerializedName;

public class LocationMarkerModel extends TexturedWidgetModel {

    // region Member Variables
    private static final String TAG = LocationMarkerModel.class.getSimpleName();
    @SerializedName("creationTime")
    private double mCreationTime = 0;

    @SerializedName("name")
    private String mMarkerName = "";

    @SerializedName("x")
    private float mX = 0;

    @SerializedName("y")
    private float mY = 0;

    // LocationModel color is an int from 0..3 check the web for values
    // 0 = Red, 1 = yellow, 2 = green, 3 =Blue ios only drawing red for now
    @SerializedName("color")
    private int color = 0;

    private int mWorkSpaceMarkerColor;
    // end region Member Variables

    // region Constructor
    public LocationMarkerModel(Rect rect, double creationTime, String markerName, int color) {
        super(rect);
        mCreationTime = creationTime;
        mMarkerName = markerName;
        mX = rect.getTOPX();
        mY = rect.getTOPY();
        this.color = color;

        //TODO replace if changing off of painters algorithm
        mOrder = Integer.MAX_VALUE;
    }

    /**
     * Null constructor for gson .. Super necessary this causes the base members
     * like mModelMatrix not to be initialized.. now should be good
     */
    private LocationMarkerModel() {
        mOrder = Integer.MAX_VALUE;

    }
    // end region Constructor

    // region Getters
    public int getColor() {
        return color;
    }

    public double getCreationTime() {
        return mCreationTime;
    }

    public String getMarkerName() {
        return mMarkerName;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }
    // end region Getters

    // abstract Getters
    @Override
    public float getHeight() {
        // return super.getHeight();
        // setting to default
        return AppConstants.WORKSPACE_STATE_DEFAULT_ZOOM * AppConstants.MARKER_TO_WORKSPACE_STATE_ZOOM_RATIO;
    }

    @Override
    public float getWidth() {
        // setting to default
        return AppConstants.WORKSPACE_STATE_DEFAULT_ZOOM * AppConstants.MARKER_TO_WORKSPACE_STATE_ZOOM_RATIO;
    }
    // end abstract Getters

    // region Setters
    private void setColor(int color) {
        this.color = color;
    }
    public void setMarkerName(String markerName) {
        mMarkerName = markerName;
    }
    public void setWorkSpaceMarkerColor(int color) {
        this.mWorkSpaceMarkerColor = color;
    }
    public void setX(float x) {
        mX = x;
    }
    public void setY(float y) {
        mY = y;
    }
    // end region Setters

    // abstract Setters
    @Override
    public void setOrder(int order) {
        //ZOrder is fixed at the top
    }
    // end abstract Setters

    // region Drawable Methods
    public void createDrawable() {

        if (!isVBOBufferInitialized) {
            isVBOBufferInitialized = true;

            initShader();
            createQuad();

            int resource;
            Bitmap bitmap;

            switch (color) {
                case 0:
                    resource = R.drawable.workspace_marker_red;
                    bitmap = TextureHelper.convertResourceToBitmap(resource);
                    initTexture(bitmap, "rmarker", mShader);
                    break;
                case 1:
                    resource = R.drawable.workspace_marker_yellow;
                    bitmap = TextureHelper.convertResourceToBitmap(resource);
                    initTexture(bitmap, "ymarker", mShader);
                    break;
                case 2:
                    resource = R.drawable.workspace_marker_green;
                    bitmap = TextureHelper.convertResourceToBitmap(resource);
                    initTexture(bitmap, "gmarker", mShader);
                    break;
                case 3:
                    resource = R.drawable.workspace_marker_blue;
                    bitmap = TextureHelper.convertResourceToBitmap(resource);
                    initTexture(bitmap, "bmarker", mShader);
                    break;
                default:
                    resource = R.drawable.workspace_marker_red;
                    bitmap = TextureHelper.convertResourceToBitmap(resource);
                    initTexture(bitmap, "rmarker", mShader);
                    break;
            }

            setupModelMVPMatrix();

        } else {
            setupModelMVPMatrix();
        }
    }

    public void draw(float[] matrix) {
        mQuad.draw(mMVPMatrix, mShader, mTextureID);
    }

    @Override
    public void preDraw() {
        if (!isModelGLInitialized) {

            createDrawable();
            isModelGLInitialized = true;
        } else {
            setupModelMVPMatrix();
        }
    }

    public void prepareForDrawable() {

    }
    // end region Drawable Methods

    // region Abstract Methods
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

        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Location UpdateModelMatrix " + "scalex:" + scalex + "rect.getWidth():" + rect.getWidth()
                + "getWidth() : " + getWidth());
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Location UpdateModelMatrix " + "scalex:" + scalex + "rect.getWidth():" + rect.getWidth()
                + "getWidth() : " + getWidth());

        Matrix.setIdentityM(mModelMatrix, 0);

        // Location Marker Always on TOP

        Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(), calculateMatrixZorder());//TODO wtf this?
        // Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(),
        // -999999999f);

        Matrix.scaleM(mModelMatrix, 0, scalex, scaleY, scaleZ);
    }

    @Override
    public float doesIntersectXY(float x, float y) {
        // Lets make things easy to read here.
        float topx, topy, width, height;
        topx = mRect.getTOPX();
        topy = mRect.getTOPY();
        width = mRect.getWidth();
        height = mRect.getHeight();

        if ((x > topx) && (x < (topx + width)) && (y > topy) && (y < (topy + height))) {
            return AppConstants.LOCATION_MARKER_DEFAULT_ORDER;

        }
        return -1;
    }

    @Override
    public void setRect(Rect rect) {
        this.mRect = rect;
        // In NoteModel but we dont need in Location as it is not being passed
        // in from server
        // mRectArray = rect.rect;
        setModelMatrix(rect);
    }

    @Override
    public boolean isInViewPort() {
        // Check the X,
        return isXYInViewPort(mX, mY);
    }

    @Override
    // Send the Model to Web Socket Server
    public boolean sendToWSServer() {
        HeCreateMarkerMessageSender heCardCreateMessageSender = new HeCreateMarkerMessageSender(this);
        heCardCreateMessageSender.send();
        return true;
    }

    @Override
    public boolean sendToWSServerHide() {
        HeMarkerDeleteSender heMarkerDeleteSender = new HeMarkerDeleteSender(this);
        heMarkerDeleteSender.send();
        return true;
    }

    @Override
    public String toString() {
        return "LocationModel{" + "mCreationTime=" + mCreationTime + ", mMarkerName='" + mMarkerName + '\'' + ", mX=" + mX + ", mY=" + mY
                + ", mWorkSpaceMarkerColor=" + mWorkSpaceMarkerColor + "} " + super.toString();
    }
    // end region Abstract Methods
}
