package com.bluescape.model.widget;

import android.opengl.Matrix;

import com.bluescape.AppConstants;
import com.bluescape.model.IModel;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.model.util.TextStyle;
import com.bluescape.util.TextHelper;

public class InitialsModel extends TexturedWidgetModel {
    // region Member Variables
    private static final String TAG = InitialsModel.class.getSimpleName();
    private String text;
    private TextStyle styles;
    // This is the reference to the parent Node that is set in the TextDrawable
    private TexturedWidgetModel mParentModel;
    private transient String mTargetID;

    // end region Member Variables
    // region Constructor
    // Needed for fromJSON gson
    private InitialsModel() {
    }

    // This is Similar to createDrawable for other Models
    public InitialsModel(IModel model) {
        mParentModel = (TexturedWidgetModel) model;
        createQuad();
        preDraw();
    }
    // end region Constructor

    // region Getters
    public TextStyle getStyles() {
        return styles;
    }

    public String getTargetID() {
        return mTargetID;
    }

    public String getText() {
        return text;
    }
    // end region Getters

    // abstract Getters
    @Override
    public float getWidth() {
        //return mParentModel.getWidth();
        //return 250;
        return WorkSpaceState.getInstance().getCurrentInitialsWidth();
    }

    @Override
    public float getHeight() {
        //return mParentModel.getHeight();
        //return 150;
        return WorkSpaceState.getInstance().getCurrentInitialsWidth();
    }
    // end Abstract Getters

    // region Setters
    public void setStyles(TextStyle styles) {
        this.styles = styles;
    }

    public void setTargetID(String targetID) {
        mTargetID = targetID;
    }

    // abstract Setters
    // end region Setters
    // Overriding cos for TextModel this.getParentMVPMatrix() is null so we need
    // to call the corresponding NoteModel's ParentMVPMatrix
    @Override
    public void setupModelMVPMatrix() {
        Matrix.multiplyMM(tempMVPMatrix, 0, mParentModel.getParentMVPMatrix(), 0, mModelMatrix, 0);
        mMVPMatrix = tempMVPMatrix;
    }

    //Overridden Method for Initials MOdel
    @Override
    public void setModelMatrix(Rect rect) {

        //Set the border to the actual Width of the the Rect and then don't scale it in setModelBorderMatrix(Rect rect)
        float scaleX = 1f;
        float scaleY = 1f;
        float scaleZ = 1f;

        Matrix.setIdentityM(mModelMatrix, 0);

        //Set the Starting point for the Border
        Matrix.translateM(mModelMatrix, 0, rect.getTOPX() - WorkSpaceState.getInstance().getCurrentBorderWidth(), rect.getTOPY() - WorkSpaceState.getInstance().getCurrentBorderWidth() - WorkSpaceState.getInstance().getCurrentInitialsWidth(), 1f);


        Matrix.scaleM(mModelMatrix, 0, scaleX, scaleY, scaleZ);

        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  setModelBorderMatrix(Rect rect), rect.getTOPX() = " + rect.getTOPX() + " rect.getTOPY() = " + rect.getTOPY() + " WorkSpaceState.getInstance().getCurrentBorderWidth()=" + WorkSpaceState.getInstance().getCurrentBorderWidth());
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  setModelBorderMatrix(Rect rect), rect.getWidth() = " + rect.getWidth() + " getWidth() = " + getWidth() + " rect.getHeight()=" + rect.getHeight() + " getHeight()=" + getHeight());
        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  setModelBorderMatrix(Rect rect), scaleX = " + scaleX + " scaleY = " + scaleY + " scaleZ=" + scaleZ);

    }
    // ending Abstract Setters

    // region Drawable Methods
    public void preDraw() {
        createDrawable();
    }

    public void createDrawable() {
        initShader();
        super.createDrawable();
        if (mParentModel.getActivityInitials() != null && mParentModel.getActivityInitials().length() > 0)
            initTexture(TextHelper.ConvertInitialsToBitmap(mParentModel), mParentModel.getUniqueActivityInitialsWithColor(), mShader);

        setupModelMVPMatrix();
    }

    public void draw(float[] matrix) {
        if (mParentModel.isDirty()) {
            createQuad();
            preDraw();

            mParentModel.setDirty(false);
        }

        mQuad.draw(mMVPMatrix, mShader, mTextureID);
    }
    // end region Drawable Methods

    // region Abstract Methods
    @Override
    public float doesIntersectXY(float x, float y) {
        return -1;
    }

    @Override
    public String toString() {
        return "InitialsBitmapModel{" + "text='" + text + '\'' + ", styles=" + styles + ", mTargetID='" + mTargetID + '\'' + '}';
    }
    // end region Abstract Methods
}
