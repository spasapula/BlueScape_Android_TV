package com.bluescape.model.widget;

import android.graphics.Bitmap;
import android.opengl.Matrix;
import android.util.Log;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.collaboration.socket.sender.HeDeleteMessageSender;
import com.bluescape.collaboration.socket.sender.HeImageCreateMessageSender;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.util.ColorUtil;
import com.bluescape.view.shaders.TextureHelper;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;

public class ImageModel extends TexturedWidgetModel {
	// region Member Variables
	private static final String TAG = ImageModel.class.getSimpleName();

	@SerializedName("baseName")
	private String mBaseName = "";

	// Extension
	@SerializedName("ext")
	public String mExtension = "";

	private PinModel mPinModel = null;

	// do initial allocation of 10 might not be needed or optimize later
	public final  ArrayList<StrokeModel> mExistingStrokes = new ArrayList<>(10);

	public ArrayList<StrokeModel> mRecentlyAddedStrokes = new ArrayList<>(10);

	private Bitmap mTempBitmap = null;

	public boolean mIsImageFetched = false;

	public boolean mDidInitiateImageFetch = false;

	public boolean mIsImageTextured = false;
	// end region Member Variables

	// region Constructor

	// Simple Constructor to send to http server for upload
	public ImageModel(Rect rect) {
		super(rect);
	}
	/**
	 * Null constructor for gson .. Super necessary this causes the base members
	 * like mModelMatrix not to be initialized.. now should be good
	 */
	private ImageModel() {
	}

	private ImageModel(Rect rect, float imageWidth, float imageHieght) {
		super(rect);

		this.mActualWidth = imageWidth;
		this.mActualHeight = imageHieght;

		// Update our rect
		this.mRect = rect;
		this.setRect(rect);
	}
	// end region Constructor

	// region Getters
	public String getBaseName() {
		return mBaseName;
	}
	// end region Getters

	// abstract Getters
	@Override
	public float getHeight() {
		return mActualHeight;
	}

	@Override
	public float getWidth() {
		return mActualWidth;
	}
	// ending Abstract Getters

	// region Setters
	public void setBaseName(String mBaseName) {
		this.mBaseName = mBaseName;
	}
	// end region Setters

	// abstract Setters
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

		Matrix.scaleM(mModelMatrix, 0, scalex, scaleY, scaleZ);

		//Set the BorderMatrix too if ActivityIndicator is set
		if(this.hasActivityIndicator()) {
			setModelBorderMatrix(rect);
            //TODO reorg initialization calls checking.
            if(mInitialsModel != null) {
                mInitialsModel.setModelMatrix(rect);
            }
		}
		if( getParentGroup() != null) {
			setModelBorderMatrix(rect);
		}
	}

	@Override
	public void setRect(Rect rect) {
		setModelMatrix(rect);
		this.mRect = rect;
		mRectArray = rect.rect;
	}
	// end abstract Setters

	// Drawable Methods
	public void createDrawable() {

		initShader();

		AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  @PostDeserialize imagePostDeserialize for ImageModel");
        if(this.mRect == null)
		    this.mRect = new Rect(mRectArray[0], mRectArray[1], mRectArray[2], mRectArray[3]);

		setModelMatrix(mRect);

		super.createDrawable();
		createQuad();

        //TODO is this more appropriate in the BaseWidget Model
        // Initialize the InitialsModel
        mInitialsModel = new InitialsModel(this);


		// Set the bitmap for this drawable
		if (!mIsImageFetched) {
			Bitmap bitmap = TextureHelper.convertResourceToBitmap(R.drawable.image_placeholder);
			initTexture(bitmap, "placeholder", mShader);
		}
		// Set up the texture buffer
		setupModelMVPMatrix();

	}

	public void draw(float[] matrix) {

		// Set the correct shader for our grid object.
		if (mTempBitmap != null) {
			initTexture(mTempBitmap, mTextureKey, mShader);
			mIsImageTextured = true;
			mTempBitmap.recycle();
			mTempBitmap = null;
		}

		//Draw the border
		if(this.hasActivityIndicator()) {
            if(mBorderQuad == null || isDirty()){
                createBorderDrawable();
                setDirty(false);
            }
            mBorderQuad.draw(mBorderMVPMatrix, mBorderShader, mActivityCollaboratorColor);
            //Draw border only if it has activity indicator nad not part of Group
                //Draw the InitialsMModel
                mInitialsModel.draw(matrix);
        }
        //Draw the border
        if( getParentGroup() != null) {
            if(mBorderQuad == null || isDirty()){
                createBorderDrawable();
                setDirty(false);
            }
            //mBorderQuad.draw(mBorderMVPMatrix, mBorderShader, mActivityCollaboratorColor);
            //mBorderQuad.draw(mBorderMVPMatrix, mBorderShader, getParentGroup().getActivityCollaboratorColor());
            mBorderQuad.draw(mBorderMVPMatrix, mBorderShader, ColorUtil.getWhiteColorFloatWithAlpha(0.5f));
        }
		mQuad.draw(mMVPMatrix, mShader, mTextureID);
		if (this.mIsPinned) {

			if (mPinModel == null) {
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
		for(StrokeModel stroke : mExistingStrokes){

			Log.i("stroke.draw(mMVPMatrix)","");

			stroke.draw(mMVPMatrix);
		}
	}

	@Override
	public void preDraw() {
		if (!isModelGLInitialized || pendingImage) {
			createDrawable();
			for(StrokeModel stroke : mExistingStrokes)
				stroke.preDraw();
			isModelGLInitialized = true;
		} else {
			setupModelMVPMatrix();
            setupModelBorderMVPMatrix();
        }
	}
	//Ending Drawable Methods

	// region Public Methods
	public void updateBitmap(String imageUrl, Bitmap bitmap) {
		// Set the bitmap for this drawable
		mTextureKey = imageUrl;
		mTempBitmap = bitmap;
	}
	// end region Public Methods

	// region abstract Methods
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

	// Send the Model to Web Socket Server
	@Override
	public boolean sendToWSServer() {
		HeImageCreateMessageSender heImageCreateMessageSender = new HeImageCreateMessageSender(this);
		heImageCreateMessageSender.send();
		return true;
	}
	@Override
	public boolean sendToWSServerEdit() {
		// Send any newly created strokes
		if (mRecentlyAddedStrokes != null && mRecentlyAddedStrokes.size() > 0) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "In sendToWSServerEdit mRecentlyAddedStrokes.size() =  " + mRecentlyAddedStrokes.size());
			// Add the mPath.moveto and mPath.Draw commands for the x,y
			// co-ordinates here for every stroke on card
			for (int strokesCounter = 0; strokesCounter < mRecentlyAddedStrokes.size(); strokesCounter++) {
				// invalidate();
				StrokeModel strokeModel = WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes.get(strokesCounter);
				strokeModel.sendToWSServer();
			}
		}
		return true;
	}
	@Override
	public boolean sendToWSServerHide() {
		HeDeleteMessageSender heDeleteMessageSender = new HeDeleteMessageSender(this);
		heDeleteMessageSender.send();
		return true;
	}
	@Override
	public void updateOnlyRect(Rect rect) {
		this.mRect = rect;
		mRectArray = rect.rect;
	}
	@Override
	public String toString() {
		return "ImageModel{" + "mRectArray=" + Arrays.toString(mRectArray) + ", mActualWidth=" + mActualWidth + ", mActualHeight=" + mActualHeight
				+ "} " + super.toString();
	}
	// ending Region Public Methods
}