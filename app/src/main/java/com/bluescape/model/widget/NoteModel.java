package com.bluescape.model.widget;

import android.graphics.Bitmap;
import android.opengl.Matrix;
import android.util.Log;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.collaboration.socket.sender.HeCardCreateMessageSender;
import com.bluescape.collaboration.socket.sender.HeDeleteMessageSender;
import com.bluescape.collaboration.socket.sender.HeTemplateMessageSender;
import com.bluescape.collaboration.socket.sender.HeTextMessageSender;
import com.bluescape.collaboration.util.NetworkTask;
import com.bluescape.collaboration.util.TemplateHelper;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.template.NoteTemplate;
import com.bluescape.model.util.Rect;
import com.bluescape.model.util.TextStyle;
import com.bluescape.util.ColorUtil;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.bson.types.ObjectId;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;

public class NoteModel extends TexturedWidgetModel {
    // Region Member Variables
    private static final String TAG = NoteModel.class.getSimpleName();

    // Base name, I think this will be the texture to draw
    @SerializedName("baseName")
    private String mBaseName = "";

    // Extension
    @SerializedName("ext")
    final private String mExtension = "";

    @SerializedName("regionId")
    private final String mRegionID = "";

    @SerializedName("hidden")
    private boolean mIsHidden = false;

    @SerializedName("text")
    private String mText = "";

    @SerializedName("styles")
    private TextStyle mTextStyle = new TextStyle();

    private NoteTemplate mNoteTemplate;
    // do initial allocation of 10 might not be needed or optimize later
    public final ArrayList<StrokeModel> mExistingStrokes = new ArrayList<>(10);
    public ArrayList<StrokeModel> mRecentlyAddedStrokes = new ArrayList<>(10);
    private TextModel mTextModel;
    private FloatBuffer mCubeTextureCoordinates;
    public boolean mTemplateChanged = false;

    // End Region Member Variables

    // Region Constructor
    // Called by NoteBuilderActivity on NewNoteCard build
    public NoteModel(Rect rect, NoteTemplate template, String text) {
        super(rect);

        // Set the id here itself so this can later be used for stroke creates
        // on the card for new card add
        ObjectId id = new ObjectId();
        this.setID(id.toString());

        this.mText = text;

        this.mNoteTemplate = template;

        this.mBaseName = template.getThumbnail();

        // Update our rect
        this.mRect = rect;
        this.setRect(rect);
    }

    /**
     * Null constructor for gson
     */
    private NoteModel() {
    }
    // End Region Constructor


    // Region Getters

    public NoteTemplate getNoteTemplate() {
        return mNoteTemplate;
    }

    public String getText() {
        return mText;
    }

    public TextStyle getTextStyle() {
        return mTextStyle;
    }

    public String getBaseName() {
        return mBaseName;
    }

    public boolean isHidden() {
        return mIsHidden;
    }

    // End region Getters

    // Abstract Getters

    @Override
    public float getHeight() {
        return mNoteTemplate.getHeight();
    }

    @Override
    public Rect getRect() {
        return mRect;
    }

    @Override
    public float getWidth() {
        return mNoteTemplate.getWidth();
    }

    @Override
    public void preDraw() {
        if (!isModelGLInitialized) {
            for (StrokeModel stroke : mExistingStrokes)
                stroke.preDraw();
            createDrawable();
            isModelGLInitialized = true;
        } else {
            setupModelMVPMatrix();
            setupModelBorderMVPMatrix();
        }
    }

    // End Abstract Getters

    // Region Setters

    public void setText(String text) {
        mText = text;
        mIsDirty = true;
    }

    public void setTextStyle(TextStyle textStyle) {
        mTextStyle = textStyle;
    }

    public void setHidden(boolean isHidden) {
        mIsHidden = isHidden;
        mIsDirty = true;
    }

    public void setBaseName(String baseName) {
        this.mBaseName = baseName;
    }

    // End region Setters

    // Abstract Setters
    @Override
    public void setHeight(float height) {
        this.mRect.setHeight(height);
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
        // float scaleZ = -mEventOrder;

        // AppConstants.LOG(AppConstants.VERBOSE, TAG, "Note UpdateModelMatrix "
        // + "scalex:" + scalex + "rect.getWidth():" + rect.getWidth() +
        // "getWidth() : " + getWidth());
        // AppConstants.LOG(AppConstants.VERBOSE, TAG, "Note UpdateModelMatrix "
        // + "scalex:" + scalex + "rect.getWidth():" + rect.getWidth() +
        // "getWidth() : " + getWidth());

        Matrix.setIdentityM(mModelMatrix, 0);

        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "Inside  NoteModel setModelMatrix mEventOrder= " + mEventOrder);
        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "WorkSpaceState.getInstance().mGlobalWorkSpaceEventOrder= " +
        // WorkSpaceState.getInstance().mGlobalWorkSpaceEventOrder);

        // Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(),
        // -(mEventOrder / AppConstants.ORTHO_PROJECTION_FAR_Z));
        float zorder = calculateMatrixZorder();
        AppConstants.LOG(AppConstants.CRITICAL, TAG, getID() + " Note ZOrder " + zorder + " from " + mOrder);

        Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(), zorder);

        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "Inside  NoteModel setModelMatrix -(mEventOrder / AppConstants.ORTHO_PROJECTION_FAR_Z)= "
        // + -(mEventOrder / AppConstants.ORTHO_PROJECTION_FAR_Z));

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

    @Override
    public void setRect(Rect rect) {
        this.mRect = rect;
        mRectArray = rect.rect;
        setModelMatrix(rect);
    }

    @Override
    public void setWidth(float width) {
        this.mRect.setWidth(width);
    }

    // end Abstract Setters

    // Region Drawable Methods

    @Override
    public void createDrawable() {

        initShader();

        //MOving towards updating the mRect with the latest status to handle GroupNMove
        //TODO GroupNMove need other Models
        if (mRect == null)
            this.mRect = new Rect(mRectArray[0], mRectArray[1], mRectArray[2], mRectArray[3]);

        String color = TemplateHelper.parseBaseNameToColor(this.mBaseName) + ".jpeg";

        // AppConstants.LOG(AppConstants.CRITICAL, TAG, "Looking for : " + color
        // + " Map size: " +
        // AppSingleton.getInstance().getApplication().getColorToTemplateMap().keySet().size());

        this.mNoteTemplate = (NoteTemplate) AppSingleton.getInstance().getApplication().getColorToTemplateMap().get(color);

        // AppConstants.LOG(AppConstants.CRITICAL, TAG, "Template: " +
        // mNoteTemplate.toString());

        setModelMatrix(mRect);
        super.createDrawable();
        if (!isVBOBufferInitialized) {
            isVBOBufferInitialized = true;

            mTextModel = new TextModel(this);

            // Build the background bitmap.
            Bitmap bgBitmap = NetworkTask.getBitmapFromBaseName(this.getBaseName(), this);

            if (bgBitmap == null) {
                // Maybe we already have the url if we built it locally.
                NetworkTask.getBitmapFromURL(this.getBaseName(), this);
            }

            // Call generate vertices to set VertexBuffer
            createQuad();

            //TODO is this more appropriate in the BaseWidget Model
            // Initialize the InitialsModel
            mInitialsModel = new InitialsModel(this);

            // Set up image texture
            initTexture(bgBitmap, this.getBaseName(), mShader);
            setupModelMVPMatrix();

        } else {
            // release();
            // AppConstants.LOG(AppConstants.CRITICAL, TAG,
            // "Inside NoteModel createDrawable() VBO Buffers already initialized :"
            // );
            setupModelMVPMatrix();

        }

    }

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

        if (mTemplateChanged) {
            mTemplateChanged = false;
            changeTemplate();
        }

        // Set the correct shader for our grid object.

        mQuad.draw(mMVPMatrix, mShader, mTextureID);
        for (StrokeModel stroke : mExistingStrokes) {
            Log.i("stroke.draw(mMVPMatrix)", "");

            stroke.draw(mMVPMatrix);
        }

        glDisable(GL_DEPTH_TEST);
        // Draw the text
        mTextModel.mMVPMatrix = mMVPMatrix;
        mTextModel.draw(matrix);
        glEnable(GL_DEPTH_TEST);

    }

    // Ending Region Drawable Methods

    // Region Public Methods
    public void changeTemplate() {

        // The returned bitmap here maybe an empty bitmap if the texture
        // is not fetched yet
        Bitmap bgBitmap = NetworkTask.getBitmapFromBaseName(this.getBaseName(), this);

        if (bgBitmap == null) {
            NetworkTask.getBitmapFromURL(this.getBaseName(), this);
        }
        mTextureKey = this.getBaseName();
        initTexture(bgBitmap, this.getBaseName(), mShader);

        // Change the Template also as this is used in the NoteBuilderActivity
        String color = TemplateHelper.parseBaseNameToColor(this.mBaseName) + ".jpeg";
        mNoteTemplate = (NoteTemplate) AppSingleton.getInstance().getApplication().getColorToTemplateMap().get(color);

    }

    public void updateBitmap(String imageUrl, Bitmap bitmap) {

        // Set the bitmap for this drawable
        initTexture(bitmap, imageUrl, mShader);
    }

    public String getHeNoteCreateBaseName() {
        // Getting templates from
        // Template URL
        // https://staging.collaboration.bluescape.com:443/card_templates.json
        // card_templates/thumbnails/Yellow.jpeg
        // Expecting "baseName":"sessions/all/Teal",
        if (getBaseName().equalsIgnoreCase("card_templates/thumbnails/Teal.jpeg")) {
            return "sessions/all/Teal";
        } else if (getBaseName().equalsIgnoreCase("card_templates/thumbnails/Beige.jpeg")) {
            return "sessions/all/Beige";
        } else if (getBaseName().equalsIgnoreCase("card_templates/thumbnails/Yellow.jpeg")) {
            return "sessions/all/Yellow";
        } else if (getBaseName().equalsIgnoreCase("card_templates/thumbnails/Gold.jpeg")) {
            return "sessions/all/Gold";
        } else if (getBaseName().equalsIgnoreCase("card_templates/thumbnails/Grey.jpeg")) {
            return "sessions/all/Grey";
        } else if (getBaseName().equalsIgnoreCase("card_templates/thumbnails/Red.jpeg")) {
            return "sessions/all/Red";
        } else if (getBaseName().equalsIgnoreCase("card_templates/thumbnails/Blue.jpeg")) {
            return "sessions/all/Blue";
        } else if (getBaseName().equalsIgnoreCase("card_templates/thumbnails/Yellow.jpeg")) {
            return "sessions/all/Yellow";
        }
        // default
        return "sessions/all/Blue";
    }

    // Ending Region Public Methods

    // Abstract Public Methods
    @Override
    public float doesIntersectXY(float x, float y) {

        // Lets make things easy to read here.
        float objx, objy, width, height;
        objx = mRect.getTOPX();
        objy = mRect.getTOPY();
        width = mRect.getWidth();
        height = mRect.getHeight();

        if ((x > objx) && (x < (objx + width)) && (y > objy) && (y < (objy + height))) {
            //	AppConstants.LOG(AppConstants.VERBOSE, TAG,
            //		String.format("Intersect is true. Touch x%f:y%f x%f : y%f : w%f : h%f ID: %s", x, y, objx, objy, width, height, getID()));
            // return mZOrder;
            return mOrder;
        }

//		AppConstants.LOG(AppConstants.VERBOSE, TAG,
//		String.format("Intersect is false. Touch x%f:y%f x%f : y%f : w%f : h%f", x, y, objx, objy, width, height));
        return -1;
    }


    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        return "NoteModel{" + "mActualWidth=" + mActualWidth + ", mActualHeight=" + mActualHeight + ", mEventOrder='" + mOrder + '\''
                + ", mBaseName='" + mBaseName + '\'' + ", mExtension='" + mExtension + '\'' + ", mRectArray=" + Arrays.toString(mRectArray)
                + ", mRegionID='" + mRegionID + '\'' + ", mIsHidden=" + mIsHidden + ", mText='" + mText + '\'' + ", mTextStyle=" + mTextStyle
                + ", mNoteTemplate=" + mNoteTemplate + "} " + super.toString();
    }

    @Override
    public void updateOnlyRect(Rect rect) {
        this.mRect = rect;
        mRectArray = rect.rect;
    }

    @Override
    public boolean sendToWSServerHide() {
        HeDeleteMessageSender heDeleteMessageSender = new HeDeleteMessageSender(this);
        heDeleteMessageSender.send();
        return true;
    }

    @Override
    public boolean sendToWSServerTemplate() {
        HeTemplateMessageSender heTemplateMessageSender = new HeTemplateMessageSender(this);
        heTemplateMessageSender.send();
        return true;
    }

    @Override
    public boolean sendToWSServerEdit() {
        HeTextMessageSender heTextMessageSender = new HeTextMessageSender(this);
        heTextMessageSender.send();

        // Send any newly created strokes
        if (mRecentlyAddedStrokes != null && mRecentlyAddedStrokes.size() > 0) {

            AppConstants.LOG(AppConstants.CRITICAL, TAG, "In sendToWSServerEdit mRecentlyAddedStrokes.size() =  " + mRecentlyAddedStrokes.size());

            // Add the mPath.moveto and mPath.Draw commands for the x,y
            // co-ordinates here for every stroke on card
            for (int strokesCounter = 0; strokesCounter < mRecentlyAddedStrokes.size(); strokesCounter++) {

                // invalidate();
                StrokeModel strokeModel = WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.get(strokesCounter);
                strokeModel.sendToWSServer();

            }

        }

        return true;
    }

    // Send the Model to Web Socket Server
    public boolean sendToWSServer() {
        HeCardCreateMessageSender heCardCreateMessageSender = new HeCardCreateMessageSender(this);
        heCardCreateMessageSender.send();

        // Send any newly created strokes
        if (mRecentlyAddedStrokes != null && mRecentlyAddedStrokes.size() > 0) {

            AppConstants.LOG(AppConstants.CRITICAL, TAG, "In sendToWSServerEdit mRecentlyAddedStrokes.size() =  " + mRecentlyAddedStrokes.size());

            // Add the mPath.moveto and mPath.Draw commands for the x,y
            // co-ordinates here for every stroke on card
            for (int strokesCounter = 0; strokesCounter < mRecentlyAddedStrokes.size(); strokesCounter++) {

                // invalidate();
                StrokeModel strokeModel = WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.get(strokesCounter);
                strokeModel.sendToWSServer();

            }

        }

        return true;
    }

    // Ending Abstract Public Methods
}
