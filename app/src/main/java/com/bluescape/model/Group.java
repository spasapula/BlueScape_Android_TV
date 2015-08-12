package com.bluescape.model;

import android.graphics.Bitmap;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.model.util.Rect;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.InitialsModel;
import com.bluescape.model.widget.TextModel;
import com.bluescape.model.widget.TexturedWidgetModel;
import com.bluescape.view.shaders.ShaderHelper;
import com.bluescape.view.shaders.ShaderType;
import com.bluescape.view.shaders.SimpleShaderProgram;
import com.bluescape.view.shaders.TextureHelper;
import com.bluescape.view.shaders.TextureShaderProgram;
import com.github.julman99.gsonfire.annotations.PostDeserialize;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SS_SA on 6/29/15.
 */
// Extending Base Widget Model/ TexturedWidget Model as the he and ve position processing logic has a BaseWidgetModel assumption
// and properties like id, order and rect in BaseWidgetModel are useful and ModelTree mWorkspaceModels has all BaseWidgetModels
// and we have to create a widget corresponding to a group in the UI when we create anyway
public class Group extends TexturedWidgetModel {
    private static final String TAG = Group.class.getSimpleName();


    //region Member Variables


    @SerializedName("children")
    private String[] mChildIds;

    //Have the ChildWidgets in an Array for Convenience and only one time lookup
    //Using Arrays to do Arrays.sort and maintain the order of the children during and after move
    //private BaseWidgetModel mChildModels[];
    private ArrayList<BaseWidgetModel> mChildModels;


    //Use these variables to compute delta for the current move
    private float mXLastMove;
    private float mYLastMove;


    //Original X1,Y1 of the Group and the Value ot diff by when sending ve and he message
    private float mX1OriginalGroupPosition;
    private float mY1OriginalGroupPosition;

    //Variables to store X1,Y1(top Left), X2,Y2(bottom right) for the GroupNMove
    //that should eventually populate the mRect
    private float mX1 = AppConstants.GROUP_WORKSPACE_INITIAL_BOTTOM_RIGHT;
    private float mY1 = AppConstants.GROUP_WORKSPACE_INITIAL_BOTTOM_RIGHT;
    private float mX2 = AppConstants.GROUP_WORKSPACE_INITIAL_TOP_LEFT;
    private float mY2 = AppConstants.GROUP_WORKSPACE_INITIAL_TOP_LEFT;

    //end region Member Variables

    // region Constructor

    //also write the @PostDeserialize code at the end of the Constructor section
    @PostDeserialize
    public void groupPostDeserialize() {

        if (mChildIds != null && mChildIds.length > 0) {

            //Since this method is called in membership and also group creation check the child delta and
            mChildModels = new ArrayList<BaseWidgetModel>();



            for (int i = 0; i < mChildIds.length; i++) {

                BaseWidgetModel baseWidgetModel = WorkSpaceState
                        .getInstance()
                        .getModelTree()
                        .getModel(
                                mChildIds[i]);
                if (baseWidgetModel != null) {
                    mChildModels.add(baseWidgetModel);
                    setGroupBoundsWithWidget(baseWidgetModel);
                    //Set This Widgets Parent Group so that we can have it for operations later
                    //baseWidgetModel.setDirty(true);
                    baseWidgetModel.setParentGroup(this);
                    if(baseWidgetModel.isModelGLInitialized) {
                        baseWidgetModel.setModelMatrix(baseWidgetModel.getRect());
                        baseWidgetModel.setupModelMVPMatrix();
                    }
                } else {
                  //  AppConstants.LOG(AppConstants.CRITICAL, TAG, "WHOAH Bad Group data groupId = " + mID + " Cant find in Widget in Tree mChildIds[" + i + "] : " + mChildIds[i]);
                }
            }
            //Now after calling  setGroupBoundsWithWidget for all widgets we get the Group Rect
            //This needs to be done before preDraw and  createDrawable
            mRect = new Rect(mX1, mY1, mX2, mY2);
            mRectArray = mRect.getRect();

            //Set the Original Group X1,Y1 Position only for the group create message. Since this groupPostDeserialize is manually invoked in membership
            //setting up a check to not update it in case it is already set. Cehcking by seeing if it is already in model tree and not resetting if it is there
//            private float mX1OriginalGroupPosition;
//            private float mY1OriginalGroupPosition;
            if(mID != null ) {
                BaseWidgetModel currentGroupModel = WorkSpaceState
                        .getInstance()
                        .getModelTree()
                        .getModel(mID);
                if(currentGroupModel == null){
                    setX1OriginalGroupPosition(mX1);
                    setY1OriginalGroupPosition(mY1);
                }
            }




            //Now Sort the children so they are in the right order while processing moves etc.
            //Check the length before sorting cos some workspaces have issues with mChildModels not being present in ModelTree
            if (mChildModels != null && mChildModels.size() > 0) {


                //To maintain the order within the group on move etc..
                Collections.sort(mChildModels);


            //    AppConstants.LOG(AppConstants.CRITICAL, TAG, "groupId = " + mID + " mChildModels.size() : " + mChildModels.size());
            } else {
             //   AppConstants.LOG(AppConstants.CRITICAL, TAG, "groupId = " + mID + "No current Children mChildModels== 0 ");
            }

        } else {
            //AppConstants.LOG(AppConstants.CRITICAL, TAG, "groupId = " + mID + "No current Children mChildIDs== 0 ");
            mChildIds = null;
            mChildModels = null;
        }
    }
    // end region Constructor


    // region Getters

    public String[] getChildIds() {
        return mChildIds;
    }


    public ArrayList<BaseWidgetModel> getChildModels() {
        return mChildModels;
    }

    public float getXLastMove() {
        return mXLastMove;
    }

    public float getYLastMove() {
        return mYLastMove;
    }

    public float getX1() {
        return mX1;
    }

    public float getY1() {
        return mY1;
    }

    public float getX2() {
        return mX2;
    }

    public float getY2() {
        return mY2;
    }

    public float getX1OriginalGroupPosition() {
        return mX1OriginalGroupPosition;
    }

    public float getY1OriginalGroupPosition() {
        return mY1OriginalGroupPosition;
    }

    // end region Getters

    // region Setters

    public void setChildIds(String[] mChildIds) {
        this.mChildIds = mChildIds;
    }


    public void setChildModels(ArrayList<BaseWidgetModel> mChildModels) {
        this.mChildModels = mChildModels;
    }

    public void setXLastMove(float mXLastMove) {
        this.mXLastMove = mXLastMove;
    }

    public void setYLastMove(float mYLastMove) {
        this.mYLastMove = mYLastMove;
    }

    public void setX1(float mX1) {
        this.mX1 = mX1;
    }

    public void setY1(float mY1) {
        this.mY1 = mY1;
    }

    public void setX2(float mX2) {
        this.mX2 = mX2;
    }

    public void setY2(float mY2) {
        this.mY2 = mY2;
    }

    public void setX1OriginalGroupPosition(float mX1OriginalGroupPosition) {
        this.mX1OriginalGroupPosition = mX1OriginalGroupPosition;
    }

    public void setY1OriginalGroupPosition(float mY1OriginalGroupPosition) {
        this.mY1OriginalGroupPosition = mY1OriginalGroupPosition;
    }

    // end region Setters

    // region Public Methods


    @Override
    public void preDraw() {
        if (!isModelGLInitialized) {
            createDrawable();
            isModelGLInitialized = true;
        } else {
            setupModelMVPMatrix();
            setupModelBorderMVPMatrix();
        }

        //Set all the children to dirty to redraw the Borders etc..
        if(getChildModels() != null) {
            for (int childCount = 0; childCount < getChildModels().size(); childCount++) {

                BaseWidgetModel childWidget = getChildModels().get(childCount);

                //update on screen
                if (childWidget.isModelGLInitialized) {
                    //childWidget.setDirty(true);
                    setupModelMVPMatrix();
                    setupModelBorderMVPMatrix();
                }

            }
        }
    }

    public void createDrawable() {

        initShader();

        AppConstants.LOG(AppConstants.VERBOSE, TAG, "Inside  @PostDeserialize imagePostDeserialize for ImageModel");
        if (this.mRect == null)
            this.mRect = new Rect(mRectArray[0], mRectArray[1], mRectArray[2], mRectArray[3]);

        // Initialize the InitialsModel
        mInitialsModel = new InitialsModel(this);


        setModelMatrix(mRect);

        super.createDrawable();
        createQuad();


        // Set the bitmap for this drawable

        Bitmap bitmap = TextureHelper.convertResourceToBitmap(R.drawable.transp_blue_10);
        initTexture(bitmap, "transp_blue_10", mShader);

        // Set up the texture buffer
        setupModelMVPMatrix();


    }

    @Override
    protected void initShader() {
        mShaderType = ShaderType.Texture;
        // Get the shader
        mShader = (TextureShaderProgram) com.bluescape.view.shaders.ShaderHelper.getInstance().getCompiledShaders().get(mShaderType);

        //Initialize the Border Shaders for All TexturedWidgets
        mBorderShaderType = ShaderType.Simple;
        // Get the shader
        mBorderShader = (SimpleShaderProgram) ShaderHelper.getInstance().getCompiledShaders().get(mBorderShaderType);
    }


    public void resetGroupBounds() {
        mX1 = AppConstants.GROUP_WORKSPACE_INITIAL_BOTTOM_RIGHT;
        mY1 = AppConstants.GROUP_WORKSPACE_INITIAL_BOTTOM_RIGHT;
        mX2 = AppConstants.GROUP_WORKSPACE_INITIAL_TOP_LEFT;
        mY2 = AppConstants.GROUP_WORKSPACE_INITIAL_TOP_LEFT;

    }

    public Rect updateGroupRectWithNewChildBounds() {
        if (mChildModels != null && mChildModels.size() > 0) {

            resetGroupBounds();
            for (int i = 0; i < mChildModels.size(); i++) {
                setGroupBoundsWithWidget(mChildModels.get(i));
            }

            mRect = new Rect(mX1, mY1, mX2, mY2);
            mRectArray = mRect.getRect();
          //  AppConstants.LOG(AppConstants.CRITICAL, TAG, "updateGroupRectWithNewChildBounds groupId = " + mID + " mChildModels.size() : " + mChildModels.size());

        } else {
         //   AppConstants.LOG(AppConstants.CRITICAL, TAG, "updateGroupRectWithNewChildBounds groupId = " + mID + "No current Children mChildModels== 0 ");
        }
        return mRect;


    }

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


    // end region Public Methods

    // region Private Methods

    //MEthod that processes a child widget at a time and updates the X1,Y1,X2,Y2
    private void setGroupBoundsWithWidget(BaseWidgetModel baseWidgetModel) {

        if (baseWidgetModel.getRect() != null) {
            if (baseWidgetModel.getRect().getTOPX() < mX1)
                mX1 = baseWidgetModel.getRect().getTOPX();

            if (baseWidgetModel.getRect().getTOPY() < mY1)
                mY1 = baseWidgetModel.getRect().getTOPY();

            if (baseWidgetModel.getRect().getBOTX() > mX2)
                mX2 = baseWidgetModel.getRect().getBOTX();

            if (baseWidgetModel.getRect().getBOTY() > mY2)
                mY2 = baseWidgetModel.getRect().getBOTY();
        }

    }

    public void syncChildMembership(String[] newChildIds){

        if(mChildModels != null) {
            //For now basically set the mParentGroup to null for the ones not in the group Anymore
            for (int i = 0; i < mChildModels.size(); i++) {

                if (Arrays.asList(newChildIds).contains(mChildModels.get(i).getID())) {
                    //membership did not change
                } else {
                    //Gone now so update parentGroup to null
                    mChildModels.get(i).setParentGroup(null);
                }
            }
        }
    }

    // end region Private Methods

    // region Drawable Methods

    public void draw(float[] matrix) {

        //Draw the border
        if (this.hasActivityIndicator()) {
            if (mBorderQuad == null || isDirty()) {
                createBorderDrawable();
                setDirty(false);
            }

//            float[] normalizedColor = {mActivityCollaboratorColor[0], mActivityCollaboratorColor[1], mActivityCollaboratorColor[2], 0.3f};
//            mBorderQuad.draw(mBorderMVPMatrix, mBorderShader, normalizedColor);

            // mBorderQuad.draw(mBorderMVPMatrix, mBorderShader, mActivityCollaboratorColor);
            if (mActivityCollaboratorColor == null) {
                if (WorkSpaceState.getInstance().mHistoryLoadCompleted) {
                    //Draw only if history load is complete and we have the border color from the ve message
                    populateActivityCollaboratorColor(getActivityCollaborator());
                    //final check before draw i.e draw only if color is present and not null
                    if (mActivityCollaboratorColor != null)
                        mBorderQuad.draw(mBorderMVPMatrix, mBorderShader, mActivityCollaboratorColor);

                }
            } else {
                mBorderQuad.draw(mBorderMVPMatrix, mBorderShader, mActivityCollaboratorColor);

            }


            //Draw the InitialsMModel
            mInitialsModel.draw(matrix);
        }
        mQuad.draw(mMVPMatrix, mShader, mTextureID);
    }

    // end region Drawable Methods

    // region Abstract Methods
    // end region Abstract Methods


}
