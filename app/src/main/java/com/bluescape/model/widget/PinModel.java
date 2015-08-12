package com.bluescape.model.widget;

import android.graphics.Bitmap;
import android.opengl.Matrix;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.view.shaders.TextureHelper;

import java.util.Arrays;

public class PinModel extends TexturedWidgetModel {
    // Region Member Variables
    private static final String TAG = PinModel.class.getSimpleName();
    private BaseWidgetModel mParentModel;
    // End Region Member Variables

    // Region Constructor

    // Simple Constructor called from TExtModel do not call prepareForDrawable
    // as mRextArray is null
    public PinModel(Rect rect, BaseWidgetModel parentModel) {
        mParentModel = parentModel;

        mRect = rect;
        // mActualWidth =64;
        // mActualHeight =64;
        mActualWidth = mRect.getWidth();
        mActualHeight = mRect.getHeight();

        setModelMatrix(mRect);

    }

    /**
     * Null constructor for gson .. Super necessary this causes the base members
     * like mModelMatrix not to be initialized.. now should be good
     */
    private PinModel() {
    }

    // End Region Constructor

    // Abstract Getters
    @Override
    public float getHeight() {
        return mActualHeight;
    }

    @Override
    public float getWidth() {
        return mActualWidth;
    }
    // End Abstract Getters

    // Abstract Setters

    @Override
    public void setRect(Rect rect) {
        setModelMatrix(rect);
        this.mRect = rect;
        mRectArray = rect.rect;
    }

    // Overriding cos for TextModel this.getParentMVPMatrix() is null so we need
    // to call the corresponding NoteModel's ParentMVPMatrix
    public void setupModelMVPMatrix(float[] matrix) {
        Matrix.multiplyMM(tempMVPMatrix, 0, matrix, 0, mModelMatrix, 0);
        mMVPMatrix = tempMVPMatrix;
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

        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  BrowserModel setModelMatrix mEventOrder= " + mOrder);
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "WorkSpaceState.getInstance().mGlobalWorkSpaceEventOrder= "
                + WorkSpaceState.getInstance().getGlobalOrder());

        Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(),
                mParentModel.calculateMatrixZorder());

        // Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(),
        // -(mEventOrder / AppConstants.ORTHO_PROJECTION_FAR_Z));

        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "Inside  PINModel setModelMatrix -(mEventOrder / AppConstants.ORTHO_PROJECTION_FAR_Z)= "
        // + -(( mParentModel.mEventOrder %
        // AppConstants.INT_ORTHO_PROJECTION_FAR_Z
        // )/AppConstants.ORTHO_PROJECTION_FAR_Z));

        Matrix.scaleM(mModelMatrix, 0, scalex, scaleY, scaleZ);
    }
    //Ending  Abstract Setters


    // Region Drawable Methods
    public void createDrawable() {
        initShader();
        super.createDrawable();
        createQuad();
        Bitmap bitmap = TextureHelper.convertResourceToBitmap(R.drawable.pushpin);
        initTexture(bitmap, "pushpin", mShader);
    }

    public void draw(float[] matrix) {
        mQuad.draw(mMVPMatrix, mShader, mTextureID);
    }

    // End Region Drawable Methods

    // Abstract Public Methods
    @Override
    public String toString() {
        return "PinModel{" + "mRectArray=" + Arrays.toString(mRectArray) + "mRect=" + (mRect) + ", mActualWidth=" + mActualWidth + ", mActualHeight="
                + mActualHeight + "} " + super.toString();
    }

    @Override
    public void updateOnlyRect(Rect rect) {
        this.mRect = rect;
        mRectArray = rect.rect;
    }
    // Ending Abstract Public Methods
}
