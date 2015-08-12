package com.bluescape.model.widget;

import android.graphics.Bitmap;
import android.opengl.Matrix;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.collaboration.socket.sender.HeTsxAppEventDeleteMessageSender;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.model.util.TextStyle;
import com.bluescape.model.widget.change.PayloadModel;
import com.bluescape.util.ColorUtil;
import com.bluescape.view.shaders.TextureHelper;
import com.github.julman99.gsonfire.annotations.PostDeserialize;
import com.google.gson.annotations.SerializedName;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;

public class BrowserModel extends TexturedWidgetModel {
    // region Member Variables
    private static final String TAG = BrowserModel.class.getSimpleName();

    @SerializedName("targetTsxAppId")
    private String targetTsxAppId;
    @SerializedName("payload")
    private PayloadModel payload = new PayloadModel();
    @SerializedName("messageType")
    private String messageType;

    private TextStyle mTextStyle = new TextStyle();
    private TextModel mTextModel;
    private String mDisplayURL;

    // end region Member Variables

    // region Constructor

    /**
     * Null constructor for gson .. Super necessary this causes the base members
     * like mModelMatrix not to be initialized..
     */
    private BrowserModel() {
    }

    // Simple Constructor to send to http server for upload
    private BrowserModel(Rect rect) {
        super(rect);
    }

    // end region Constructor

    // region Getters

    @Override
    public float getHeight() {
        return mActualHeight;
    }

    public float getActualHeight() {
        return mActualHeight;
    }

    public float getActualWidth() {
        return mActualWidth;
    }

    public String getMessageType() {
        return messageType;
    }

    public float[] getRectArray() {
        return mRectArray;
    }

    public PayloadModel getPayload() {
        return payload;
    }

    public String getTargetTsxAppId() {
        return targetTsxAppId;
    }

    public TextStyle getTextStyle() {
        return mTextStyle;
    }

    @Override
    public float getWidth() {
        return mActualWidth;
    }

    public String getDisplayURL() {
        return mDisplayURL;
    }

    // end region Getters

    // region Setters

    public void setDisplayURL(String displayURL) {
        mDisplayURL = displayURL;
    }


    public void setActualHeight(float mActualHeight) {
        this.mActualHeight = mActualHeight;
    }

    public void setActualWidth(float mActualWidth) {
        this.mActualWidth = mActualWidth;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }


    public void setRectArray(float[] mRectArray) {
        this.mRectArray = mRectArray;
    }

    public void setPayload(PayloadModel payload) {
        this.payload = payload;
    }

    public void setTargetTsxAppId(String targetTsxAppId) {
        this.targetTsxAppId = targetTsxAppId;
    }

    public void setTextStyle(TextStyle textStyle) {
        mTextStyle = textStyle;
    }

    @Override
    public void setRect(Rect rect) {
        setModelMatrix(rect);
        this.mRect = rect;
        mRectArray = rect.rect;
    }

    @Override
    public void setModelMatrix(Rect rect) {
        /**
         * Scale the difference between the actual size of the browser and the
         * rect we are displaying. Rects are updated with move commands, but the
         * matrix is what we use to scale.
         */
        float scalex = (rect.getWidth() / getWidth());
        float scaleY = (rect.getHeight() / getHeight());
        float scaleZ = 1f;

        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(),
                calculateMatrixZorder());

        Matrix.scaleM(mModelMatrix, 0, scalex, scaleY, scaleZ);

        //Set the BorderMatrix too if ActivityIndicator is set
        if (this.hasActivityIndicator()) {
            setModelBorderMatrix(rect);
            if (mInitialsModel != null)
                mInitialsModel.setModelMatrix(rect);
        }
        if (getParentGroup() != null) {
            setModelBorderMatrix(rect);
        }
    }
    // end region Setters

    // region Public Methods

    public void createDrawable() {
        initShader();
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  createDrawable for BrowserModel");
        if (this.mRect == null)
            this.mRect = new Rect(mRectArray[0], mRectArray[1], mRectArray[2], mRectArray[3]);
        setModelMatrix(mRect);
        super.createDrawable();
        createQuad();
        //TODO is this more appropriate in the BaseWidget Model
        // Initialize the InitialsModel
        mInitialsModel = new InitialsModel(this);
        mTextModel = new TextModel(this);
        Bitmap bitmap = TextureHelper.convertResourceToBitmap(R.drawable.web_img);
        initTexture(bitmap, "webplaceholder", mShader);
        setupModelMVPMatrix();
    }

    @Override
    public float doesIntersectXY(float x, float y) {
        // Lets make things easy to read here.
        float objx, objy, width, height;
        objx = mRect.getTOPX();
        objy = mRect.getTOPY();
        width = mRect.getWidth();
        height = mRect.getHeight();
        if ((x > objx) && (x < (objx + width)) && (y > objy) && (y < (objy + height))) {
            AppConstants.LOG(AppConstants.VERBOSE, TAG,
                    String.format("Intersect is true. Touch x%f:y%f x%f : y%f : w%f : h%f ID: %s", x, y, objx, objy, width, height, mID));
            // return this.mZOrder;
            return mOrder;
        }
        AppConstants.LOG(AppConstants.VERBOSE, TAG, String.format("Intersect is false. Touch x%f:y%f x%f : y%f : w%f : h%f", x, y, objx, objy, width, height));
        return -1;
    }

    public void draw(float[] matrix) {
        //Draw the border
        if (this.hasActivityIndicator()) {
            if (mBorderQuad == null || isDirty()) {
                createBorderDrawable();
            }
            mBorderQuad.draw(mBorderMVPMatrix, mBorderShader, mActivityCollaboratorColor);
            //Draw the InitialsMModel
            mInitialsModel.draw(matrix);
        }

        if (getParentGroup() != null) {
            if (mBorderQuad == null || isDirty()) {
                createBorderDrawable();
                setDirty(false);
            }
            mBorderQuad.draw(mBorderMVPMatrix, mBorderShader, ColorUtil.getWhiteColorFloatWithAlpha(0.5f));
        }


        // Set the correct shader for our grid object.
        mQuad.draw(mMVPMatrix, mShader, mTextureID);
        // Draw the text
        glDisable(GL_DEPTH_TEST);
        mTextModel.mMVPMatrix = mMVPMatrix;
        mTextModel.draw(matrix);
        glEnable(GL_DEPTH_TEST);

    }

    public void updateModelMatrix(Rect rect) {
        /**
         *  Scale the difference between the actual size of the browser and the rect we are displaying.
         *      Rects are updated with move commands, but the matrix is what we use to scale.
         */
        float scalex = (rect.getWidth() / getWidth());
        float scaleY = (rect.getHeight() / getHeight());
        float scaleZ = 1f;
        Matrix.setIdentityM(mModelMatrix, 0);
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  BrowserModel updateModelMatrix mEventOrder= " + mOrder);
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "WorkSpaceState.getInstance().mGlobalWorkSpaceEventOrder= " + WorkSpaceState.getInstance().getGlobalOrder());
        Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(), calculateMatrixZorder());
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  BrowserModel updateModelMatrix -(mEventOrder / AppConstants.ORTHO_PROJECTION_FAR_Z)= " + -(mOrder / AppConstants.ORTHO_PROJECTION_FAR_Z));
        Matrix.scaleM(mModelMatrix, 0, scalex, scaleY, scaleZ);
    }

    // Send the Model to Web Socket Server
    public boolean sendToWSServer() {
        return true;
    }

    public boolean sendToWSServerHide() {
        //This is message needed to delete from Wall, browser is special as it has tsxappevent all others have a regular delete event
        HeTsxAppEventDeleteMessageSender heTsxAppEventDeleteMessageSender = new HeTsxAppEventDeleteMessageSender(this);
        heTsxAppEventDeleteMessageSender.send();
        return true;
    }

    public void updateOnlyRect(Rect rect) {
        this.mRect = rect;
        mRectArray = rect.rect;
    }

    @Override
    public void preDraw() {
        if (!isModelGLInitialized) {
            createDrawable();
            isModelGLInitialized = true;
        } else {
            setupModelMVPMatrix();
            setupModelBorderMVPMatrix();
        }
    }

    @Override
    public String toString() {
        return "BrowserModel{" + "mRectArray=" + Arrays.toString(mRectArray) + ", mActualWidth=" + mActualWidth + ", mActualHeight=" + mActualHeight + "} " + super.toString();
    }
    // end region Public Methods

    //DO NOT DELETE This is called by gson and is where the display url is set
    @PostDeserialize
    private void browsePostDeserialize() {
        //AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  @PostDeserialize browsePostDeserialize for BrowserModel");
        this.mRect = new Rect(payload.getX(), payload.getY(), payload.getX() + payload.getWorldSpaceWidth(), payload.getY() + payload.getWorldSpaceHeight());
        updateModelMatrix(mRect);
        this.mActualWidth = payload.getWorldSpaceWidth();
        this.mActualHeight = payload.getWorldSpaceHeight();
        mRectArray = mRect.rect;

        //Removing unnecessary exceptions. As browsers created in wall can have "" url set only if it is not equal to ""
        if (payload.getUrl() != "") {
            try {
                mDisplayURL = (new URL(payload.getUrl())).getHost();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                mDisplayURL = payload.getUrl();
                if (mDisplayURL == null)
                    mDisplayURL = "this ain't right";//#TODO
            }
        } else {
            mDisplayURL = payload.getUrl();
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "mDisplayURL = NULL browser probably created in wall");
        }
    }

}
