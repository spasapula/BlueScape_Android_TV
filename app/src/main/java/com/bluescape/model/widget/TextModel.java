package com.bluescape.model.widget;

import android.graphics.Bitmap;
import android.opengl.Matrix;

import com.bluescape.AppConstants;
import com.bluescape.model.IModel;
import com.bluescape.model.util.Rect;
import com.bluescape.model.util.TextStyle;
import com.bluescape.util.TextHelper;

public class TextModel extends TexturedWidgetModel {
    // Region Member Variables
    private static final String TAG = TextModel.class.getSimpleName();
    private PinModel mPinModel = null;
    private String text;
    private TextStyle styles;
    // This is hte reference to the parent Node that is set in the TextDrawable
    private TexturedWidgetModel mParentModel;
    private transient String mTargetID;
    // End Region Member Variables

    // Region Constructor
    // This is Similar to createDrawable for other Models
    public TextModel(IModel model) {
        mParentModel = (TexturedWidgetModel) model;
        createQuad();
        preDraw();
    }

    // Needed for fromJSON gson
    private TextModel() {
    }
    // End Region Constructor

    // Region Getters
    public String getText() {
        return text;
    }

    public TextStyle getStyles() {
        return styles;
    }

    public String getTargetID() {
        return mTargetID;
    }
    //Ending Region Getters

    //Abstract Getters
    @Override
    public float getWidth() {
        return mParentModel.getWidth();
    }

    @Override
    public float getHeight() {
        return mParentModel.getHeight();
    }
    //Ending Abstract Getters

    // Region Setters
    public void setStyles(TextStyle styles) {
        this.styles = styles;
    }

    public void setTargetID(String targetID) {
        mTargetID = targetID;
    }

    // Overriding cos for TextModel this.getParentMVPMatrix() is null so we need
    // to call the corresponding NoteModel's ParentMVPMatrix
    @Override
    public void setupModelMVPMatrix() {
        Matrix.multiplyMM(tempMVPMatrix, 0, mParentModel.getParentMVPMatrix(), 0, mModelMatrix, 0);
        mMVPMatrix = tempMVPMatrix;
    }
    // End Region Setters

    // Region Drawable Methods
    public void preDraw() {
        createDrawable();
    }

    public void createDrawable() {
        initShader();
        super.createDrawable();
        if (mParentModel instanceof NoteModel) {
            if (((NoteModel) mParentModel).getText() != null && ((NoteModel) mParentModel).getText().length() > 0)
                initTexture(TextHelper.ConvertTextToBitmap(mParentModel), ((NoteModel) mParentModel).getText(), mShader);
        } else if (mParentModel instanceof PDFModel) {
            if (((PDFModel) mParentModel).getTitle() != null && ((PDFModel) mParentModel).getTitle().length() > 0)
                initTexture(TextHelper.ConvertTextToBitmap(mParentModel), ((PDFModel) mParentModel).getTitle(), mShader);
        } else if (mParentModel instanceof BrowserModel) {
            if (((BrowserModel) mParentModel).getDisplayURL() != null && ((BrowserModel) mParentModel).getDisplayURL().length() > 0)
                initTexture(TextHelper.ConvertTextToBitmap(mParentModel), ((BrowserModel) mParentModel).getDisplayURL(), mShader);
        }
        setupModelMVPMatrix();
    }

    public void draw(float[] matrix) {
        if (mParentModel instanceof NoteModel) {
            if (mParentModel.isDirty()) {
                // Create a bitmap from the text. This is used when we get a
                // widget model from json.
                Bitmap bitmap = (TextHelper.ConvertTextToBitmap(mParentModel));
                initTexture(bitmap, ((NoteModel) mParentModel).getText(), mShader);
                mParentModel.setDirty(false);
            }
        } else if (mParentModel instanceof PDFModel) {
            if (mParentModel.isDirty()) {
                // Create a bitmap from the text. This is used when we get a
                // widget model from json.
                Bitmap bitmap = (TextHelper.ConvertTextToBitmap(mParentModel));
                initTexture(bitmap, ((PDFModel) mParentModel).getTitle(), mShader);
                mParentModel.setDirty(false);
            }
        } else if (mParentModel instanceof BrowserModel) {
            if (mParentModel.isDirty()) {
                // Create a bitmap from the text. This is used when we get a
                // widget model from json.
                Bitmap bitmap = (TextHelper.ConvertTextToBitmap(mParentModel));
                text = ((BrowserModel) mParentModel).getDisplayURL();
                initTexture(bitmap, text, mShader);
                mParentModel.setDirty(false);
            }
        }

        mQuad.draw(mMVPMatrix, mShader, mTextureID);

        // Draw the pin if the Card is pinned
        if (mParentModel instanceof NoteModel && mParentModel.mIsPinned) {

            if (mPinModel == null) {
                mPinModel = new PinModel(new Rect((mParentModel.getWidth() * AppConstants.PIN_X_LOCATION_START), 0,
                        (mParentModel.getWidth() * AppConstants.PIN_X_LOCATION_START) + mParentModel.getWidth() / 4, mParentModel.getHeight() / 4), this);
                mPinModel.createDrawable();
                mPinModel.setupModelMVPMatrix(mMVPMatrix);
                mPinModel.isModelGLInitialized = true;
            }
            if (mPinModel.isModelGLInitialized) {
                mPinModel.setupModelMVPMatrix(mMVPMatrix);
                mPinModel.draw(mMVPMatrix);
            }

        }
    }

    // End Region Drawable Methods

    //Region Abstract Methods
    @Override
    public float doesIntersectXY(float x, float y) {
        return -1;
    }

    @Override
    public String toString() {
        return "TextModel{" + "text='" + text + '\'' + ", styles=" + styles + ", mTargetID='" + mTargetID + '\'' + '}';
    }
    // Ending Region Abstract Methods
}
