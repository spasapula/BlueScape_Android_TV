package com.bluescape.model.widget;

import android.graphics.Bitmap;

import com.bluescape.model.Quad;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.view.shaders.ShaderHelper;
import com.bluescape.view.shaders.ShaderType;
import com.bluescape.view.shaders.SimpleShaderProgram;
import com.bluescape.view.shaders.TextureShaderProgram;
import com.bluescape.view.util.TextureManager;
import com.google.gson.annotations.SerializedName;

/**
 * Created by SS_SA on 6/17/15.
 */
public abstract class TexturedWidgetModel extends BaseWidgetModel {
    private static final String TAG = TexturedWidgetModel.class.getSimpleName();

    // The size of the shape
    @SerializedName("actualWidth")
    protected float mActualWidth;

    @SerializedName("actualHeight")
    protected float mActualHeight;

    protected Quad mQuad = null;

    protected Quad mBorderQuad = null;

    protected String mTextureKey;

    protected int mTextureID;

    protected TextureShaderProgram mShader;

    // The shader type for this shape
    protected ShaderType mShaderType;

    protected transient SimpleShaderProgram mBorderShader;

    protected ShaderType mBorderShaderType;

    protected boolean pendingImage = false;

    // endregion

    // region Constructor
    public TexturedWidgetModel() {

    }

    public TexturedWidgetModel(Rect rect) {
        super(rect);
    }

    // endregion

    // region Abstract Methods
    // endregion

    // region Getters
    public float getActualWidth() {
        return mActualWidth;
    }

    public float getActualHeight() {
        return mActualHeight;
    }
    // end region Getters

    // region Setters
    public void setPendingImage(boolean pendingImage) {
        this.pendingImage = pendingImage;
    }
    // end region Setters

    // region Public Methods
    protected void initTexture(Bitmap bitmap, String textureKey, TextureShaderProgram shader) {
        mTextureKey = textureKey;
        // Check if we loaded the bitmap
        if (bitmap != null && TextureManager.getInstance().hasTexture(textureKey) && !pendingImage) {
            mTextureID = TextureManager.getInstance().getTextureID(textureKey);
        } else {
            mTextureID = shader.createTextureID(bitmap);
            pendingImage = false;
            TextureManager.getInstance().setTextureID(textureKey, mTextureID);
        }
    }

    // region Abstract Methods
    @Override
    public void createDrawable() {

    }

    @Override
    public void createBorderDrawable() {
        createBorderQuad();
        setupModelBorderMVPMatrix();
    }
    // ending Abstract Methods

    // region protected Methods
    protected void initShader() {
        mShaderType = ShaderType.Texture;
        // Get the shader
        mShader = (TextureShaderProgram) com.bluescape.view.shaders.ShaderHelper.getInstance().getCompiledShaders().get(mShaderType);

        //Initialize the Border Shaders for All TexturedWidgets
        mBorderShaderType = ShaderType.Simple;
        // Get the shader
        mBorderShader = (SimpleShaderProgram) ShaderHelper.getInstance().getCompiledShaders().get(mBorderShaderType);
    }

    protected void createQuad() {
        mQuad = new Quad(this.getWidth(), this.getHeight());
    }

    protected void createBorderQuad() {
        try {
            //Set the border to the actual Width of the the Rect and then don't scale it in setModelBorderMatrix(Rect rect)
            mBorderQuad = new Quad(this.getRect().getWidth() + WorkSpaceState.getInstance().getCurrentBorderWidth() * 2, this.getRect().getHeight() + WorkSpaceState.getInstance().getCurrentBorderWidth() * 2);
            //AppConstants.LOG(AppConstants.VERBOSE, TAG, "In createBorderQuad() " + "this.getWidth() = " + this.getWidth() + " this.getHeight() = " + this.getHeight() + " WorkSpaceState.getInstance().getCurrentBorderWidth() = " + WorkSpaceState.getInstance().getCurrentBorderWidth());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    // ending Region protected Methods
}