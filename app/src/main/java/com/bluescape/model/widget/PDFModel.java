package com.bluescape.model.widget;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.collaboration.socket.sender.HeDeleteMessageSender;
import com.bluescape.model.util.Rect;
import com.bluescape.model.util.TextStyle;
import com.bluescape.util.ColorUtil;
import com.bluescape.view.shaders.TextureHelper;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;

public class PDFModel extends TexturedWidgetModel {

    // region Member Variables
    private static final String TAG = PDFModel.class.getSimpleName();

    PinModel mPinModel = null;

    @SerializedName("assetPath")
    private String assetPath = "";

    @SerializedName("filename")
    private String filename = "";

    @SerializedName("title")
    private String title = "";

    @SerializedName("pages")
    private String pages = "";

    private TextStyle mTextStyle = new TextStyle();

    private TextModel mTextModel;

    // endregion

    // region Constructor

    /**
     * Null constructor for gson .. Super necessary this causes the base members
     * like mModelMatrix not to be initialized.. now should be good
     */
    private PDFModel() {
    }

    // endregion

    // region Setters/Getters

    public void createDrawable() {

        initShader();
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  PDFModel prepareForDrawable");
        if (this.mRect == null)
            this.mRect = new Rect(mRectArray[0], mRectArray[1], mRectArray[2], mRectArray[3]);
        setModelMatrix(mRect);

        mTextModel = new TextModel(this);

        createQuad();

        //TODO is this more appropriate in the BaseWidget Model
        // Initialize the InitialsModel
        mInitialsModel = new InitialsModel(this);


        super.createDrawable();
        // setBitmap(TextureHelper.convertResourceToBitmap(R.drawable.pdf_img));
        Bitmap bitmap = TextureHelper.convertResourceToBitmap(R.drawable.pdf_img);

        // Setup our texture ID
        initTexture(bitmap, "PDF", mShader);

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
            // return mZOrder;
            return mOrder;

        }

        AppConstants.LOG(AppConstants.VERBOSE, TAG,
                String.format("Intersect is false. Touch x%f:y%f x%f : y%f : w%f : h%f", x, y, objx, objy, width, height));
        return -1;
    }

    // region Abstract Methods
    public void draw(float[] matrix) {
        //Draw the border
        if (this.hasActivityIndicator()) {
            if (mBorderQuad == null || isDirty()) {
                createBorderDrawable();
                setDirty(false);
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

        mQuad.draw(mMVPMatrix, mShader, mTextureID);

        // Draw the pin if the PDF is pinned
        if (this.mIsPinned) {
            if (mPinModel == null) {
                // mPinModel = new PinModel(new Rect(mActualWidth / 2, 0,
                // (mActualWidth / 2) + (mActualWidth / 4), 0 + (mActualHeight /
                // 4)),this);
                // mPinModel = new PinModel(new Rect(225,0, 345,80),this);
                mPinModel = new PinModel(new Rect((getWidth() * AppConstants.PIN_X_LOCATION_START), 0,
                        (getWidth() * AppConstants.PIN_X_LOCATION_START) + getWidth() / 4, getHeight() / 4), this);

                mPinModel.createDrawable();
                mPinModel.setupModelMVPMatrix(mMVPMatrix);
                mPinModel.isModelGLInitialized = true;
            }
            if (mPinModel.isModelGLInitialized) {
                // TODO can call this in the setupModel for the Parent and not
                // do in every draw loop
                mPinModel.setupModelMVPMatrix(mMVPMatrix);
                mPinModel.draw(mMVPMatrix);
            }
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mShader.getPositionHandle());
        GLES20.glDisableVertexAttribArray(mShader.getTexCoordLoc());

        // Draw the text
        glDisable(GL_DEPTH_TEST);
        mTextModel.mMVPMatrix = mMVPMatrix;
        mTextModel.draw(matrix);
        glEnable(GL_DEPTH_TEST);

    }

    // endregion

    public String getAssetPath() {
        return assetPath;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public float getHeight() {
        return mActualHeight;
    }

    public String getPages() {
        return pages;
    }

    public TextStyle getTextStyle() {
        return mTextStyle;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public float getWidth() {
        return mActualWidth;
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

    public boolean sendToWSServer() {

        return true;
    }

    // endregion

    public boolean sendToWSServerHide() {
        HeDeleteMessageSender heDeleteMessageSender = new HeDeleteMessageSender(this);
        heDeleteMessageSender.send();
        return true;
    }

    public void setAssetPath(String assetPath) {
        this.assetPath = assetPath;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public void setModelMatrix(Rect rect) {

        /**
         * Scale the difference between the actual size of the image and the
         * rect we are displaying. Rects are updated with move commands, but the
         * matrix is what we use to scale.
         */
        float scalex = (rect.getWidth() / getWidth());
        float scaleY = (rect.getHeight() / getHeight());
        float scaleZ = 1f;

        Matrix.setIdentityM(mModelMatrix, 0);

        // Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(),
        // 1f);
        Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(),
                calculateMatrixZorder());

        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "Inside  PDFMOdel setModelMatrix -(mEventOrder / AppConstants.ORTHO_PROJECTION_FAR_Z)= "
        // + -((mEventOrder % AppConstants.INT_ORTHO_PROJECTION_FAR_Z) /
        // AppConstants.ORTHO_PROJECTION_FAR_Z));

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

    public void setPages(String pages) {
        this.pages = pages;
    }

    // region public Interface Methods
    @Override
    public void setRect(Rect rect) {
        setModelMatrix(rect);
        this.mRect = rect;
        mRectArray = rect.rect;
    }

    public void setTextStyle(TextStyle textStyle) {
        mTextStyle = textStyle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // endregion

    // region Public Methods
    @Override
    public String toString() {
        return "PDFModel{" + "mRectArray=" + Arrays.toString(mRectArray) + ", mActualWidth=" + mActualWidth + ", mActualHeight=" + mActualHeight
                + "} " + super.toString();
    }

    @Override
    public void updateOnlyRect(Rect rect) {
        this.mRect = rect;
        mRectArray = rect.rect;
    }

    // endregion

    // region Private Methods
    // endregion

}
