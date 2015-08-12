package com.bluescape.model.widget;

import android.util.Log;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.collaboration.socket.sender.HeStrokeMessageSender;
import com.bluescape.model.WorkSpaceModel;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.model.util.StrokeVertexBuilder;
import com.bluescape.view.shaders.ShaderHelper;
import com.bluescape.view.shaders.ShaderType;
import com.bluescape.view.shaders.SimpleShaderProgram;
import com.google.gson.annotations.SerializedName;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

public class StrokeModel extends BaseWidgetModel {
    // Region Member Variables
    private static final String TAG = StrokeModel.class.getSimpleName();

    @SerializedName("color")
    private float[] mColor = {213, 37, 46, 1};

    @SerializedName("locs")
    private ArrayList<Float> mArrayLocs = new ArrayList<Float>();

    @SerializedName("size")
    private float mLineWidth = 5f;

    // 1 is the draw brush, while 2 is the erase brush.
    @SerializedName("brush")
    private int mBrush = 1;

    @SerializedName("regionId")
    private String mRegionID = "";

    @SerializedName("strokeId")
    private String mStrokeID = "";

    private StrokeVertexBuilder mStrokeVertexBuilder;

    private String workspaceIdCopy = null;

    private transient SimpleShaderProgram mShader;

    private ShaderType mShaderType;

    // The vertex buffer to pass along to openGL
    private FloatBuffer mVertexBuffer;

    // The coordinates of the drawable
    private float[] shapeCoords;

    private BaseWidgetModel parentModel;

    private float[] normalizedColor = new float[4];

    // End Region Member Variables

    // Region Constructor

    /**
     * Null constructor for gson .. Super necessary this causes the base members
     * like mModelMatrix not to be initialized..
     */
    public StrokeModel() {
        super();
        // Not needed for Stroke
        mModelMatrix = null;
        // Need to set ID as that is used as Key to Draw Elements and pull from
        // ModelTree.mWorkspaceModels to draw
        // Note no id in Stroke History Event
        mID = String.valueOf(this.hashCode());
    }

    // Added for adding strokes from UI to send to Web Socket server
    public StrokeModel(List<Float> locsArray, float[] color) {

        // Need to set ID as that is used as Key to Draw Elements and pull from
        // ModelTree.mWorkspaceModels to draw and delete
        // Note no id in Stroke History Event
        mID = String.valueOf(this.hashCode());
        //Important to set Normalized Color for temporary strokes call setColor method
        this.setColor(color);


        //mArrayLocs = new ArrayList<Float>();
        mArrayLocs = new ArrayList<>(locsArray);
        // have to differentiate AppConstants.LAYER_STROKES_ON_LOCAL_CARD and
        // AppConstants.LAYER_STROKES_ON_BACKGROUND based on targetID
    }

    // End Region Constructor

    // Abstract Getters

    public float[] getColor() {
        return mColor;
    }

    public ArrayList<Float> getArrayLocs() {
        return mArrayLocs;
    }

    public int getBrush() {
        return mBrush;
    }

    public float getLineWidth() {
        return mLineWidth;
    }

    public String getRegionID() {
        return mRegionID;
    }

    public ArrayList<Float> getStrokeArray() {
        return mArrayLocs;
    }

    public String getStrokeID() {
        return mStrokeID;
    }

    public StrokeVertexBuilder getStrokeVertexBuilder() {
        return mStrokeVertexBuilder;
    }

    private float[] getNormalizedColor() {

        return normalizedColor;
    }

    /**
     * Handles logic to create vertices array from the "locs" array
     *
     * @return
     */
    private float[] getVerticesArray() {
        return mStrokeVertexBuilder.getVertices();
    }

    //Ending Abstract Getters

    // Region Setters

    public void setArrayLocs(ArrayList<Float> arrayLocs) {
        mIsDirty = true;

        mArrayLocs = arrayLocs;
    }

    public void setBrush(int mBrush) {
        this.mBrush = mBrush;
    }

    public void setColor(float color[]) {
        mColor = color;
        // Always check if color is not normalized as open gl likes it
        // normalized 0-1 range
        // the server response that is parsed by gson is in the range 1-255
        // outside chance for bug but should be good.
        for (int i = 0; i < mColor.length; i++) {
            if (i <= 2) {
                normalizedColor[i] = mColor[i] / 255.0f;

            } else {
                //only set first 3 RGB 4 is alpha keep it as is i.e 1
                normalizedColor[i] = mColor[i];
            }
        }

    }

    public void setLineWidth(float lineWidth) {
        mIsDirty = true;
        mLineWidth = lineWidth / 100;
    }

    private void setShapeCoords(float[] shapeCoords) {
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "Inside  setShapeCoords(float[] shapeCoords) BaseWidgetModel ");

        this.shapeCoords = shapeCoords;
        setVertexBuffer(shapeCoords);
    }

    // how often does this need to be called?
    private void setVertexBuffer(float[] shapecoords) {

        // Check if shapecoords is null as it causes exception when it is null
        // sometimes
        if (shapecoords != null && shapecoords.length > 0) {
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(shapeCoords.length * AppConstants.BYTES_FLOAT); // (number
            // of
            // coordinate
            // values
            // *
            // 4
            // bytes
            // per
            // float)

            // use the device hardware's native byte order
            bb.order(ByteOrder.nativeOrder());

            // create a floating point buffer from the ByteBuffer
            this.mVertexBuffer = bb.asFloatBuffer();

            // add the coordinates to the FloatBuffer

            this.mVertexBuffer.put(shapeCoords);
            for(int i=0; i<shapeCoords.length;i++) {
                Log.i("shapeCoords"+i, "" + shapeCoords[i]);
            }

            // set the buffer to read the first coordinate
            this.mVertexBuffer.position(0);
        } else {
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "NOT COOL ELSE case for shapecoords != null && shapecoords.length > 0 ");
        }

    }

    public void setRegionID(String mRegionID) {
        this.mRegionID = mRegionID;
    }

    public void setStrokeArray(ArrayList<Float> arrayLocs) {
        mIsDirty = true;
        mArrayLocs = arrayLocs;
    }

    public void setStrokeID(String mStrokeID) {
        this.mStrokeID = mStrokeID;
    }

    @Override
    public void setRect(Rect rect) {
        mIsDirty = true;

        float x = rect.getTOPX();
        float y = rect.getTOPY();

        mArrayLocs.add(x);
        mArrayLocs.add(y);

        this.mRect = rect;
    }
    // End Region Setters

    // Region Drawable Methods

    public void createDrawable() {
        initializeVertexBuilder();
        mIsDirty = true;
        // Get the shader for this shape and the program id where the shader is
        // loaded
        mShaderType = ShaderType.Simple;
        mShader = (SimpleShaderProgram) ShaderHelper.getInstance().getCompiledShaders().get(mShaderType);

        // Call generate vertices to set VertexBuffer
        setShapeCoords(getVerticesArray());
    }

    public void draw(float[] matrix) {
        try {
            Log.i("drawdrawdraw","");

            // Draw only parent MVPMatrix is set.. usually ok for NoteCard but
            // need the check for image as it is not built sometimes
            if (matrix != null) {
                // Add program to OpenGL ES environment
                mShader.useProgram();

                // Prepare the triangle coordinate data
                glVertexAttribPointer(mShader.getPositionHandle(), getCoordsPerVertex(), GL_FLOAT, false, getVertexStride(), mVertexBuffer);

                // Enable the attribute in the shader
                glEnableVertexAttribArray(mShader.getPositionHandle());

                // Set mColor for drawing the triangle
                glUniform4fv(mShader.getColorHandle(), 1, getNormalizedColor(), 0);

                // Send the matrix to the shader
                glUniformMatrix4fv(mShader.getMatrixHandle(), 1, false, matrix, 0);


               // Log.i("mVertexBuffer","mVertexBuffer"+mVertexBuffer);

                // Draw the triangle
                glDrawArrays(GL_TRIANGLE_STRIP, 0, (shapeCoords.length / mCoordsPerVertex));

                // Disable vertex array
                glDisableVertexAttribArray(mShader.getPositionHandle());

            } else {
                AppConstants.LOG(
                        AppConstants.VERBOSE,
                        TAG,
                        "StrokeModel.draw() matrix == null could be Parent Image Model is not built yet parentWidgetModel.mMVPMatrix targetID=  "
                                + this.getTargetID());

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "Exception in StrokeModel.draw " + ex);
        }
    }
    // End Region Drawable Methods

    // Region Public Methods
    public void clearVertexBuilder() {
        mStrokeVertexBuilder = null;
    }

    public void initializeVertexBuilder() {

        Log.i("initializeVertexBuilder","");

        if (workspaceIdCopy == null) {
            workspaceIdCopy = AppSingleton.getInstance().getApplication().getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, "");
        }
        if (mStrokeVertexBuilder == null) {
            if (this.getTargetID().equalsIgnoreCase(workspaceIdCopy)) {
                // Default normal case when stroke is drawn on Workspace
                mStrokeVertexBuilder = new StrokeVertexBuilder(mArrayLocs, mLineWidth, -0.5f, null);
            } else {
                //mStrokeVertexBuilder = new StrokeVertexBuilder(mArrayLocs, mLineWidth, -2f);
                // Since Stroke on CARD comes in as a separate array list item
                // in History

                BaseWidgetModel baseWidgetParentModel = WorkSpaceState.getInstance().getModelTree().getModel(this.getTargetID());


                if (baseWidgetParentModel != null) {
                    mStrokeVertexBuilder = new StrokeVertexBuilder(mArrayLocs, mLineWidth, calculateMatrixZorder(baseWidgetParentModel.getOrder()), baseWidgetParentModel);
                }// else boom NPE below
                else {
                    AppConstants.LOG(AppConstants.CRITICAL, TAG, "Stroke ZOrder Is Improper " + getStrokeID());

                    mStrokeVertexBuilder = new StrokeVertexBuilder(mArrayLocs, mLineWidth, calculateMatrixZorder(1), null);
                }

            }

            if (mRectArray == null)
                mRectArray = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};

            for (int j = 0; j <= mArrayLocs.size() - 1; j += 2) {
                if (mArrayLocs.get(j) < mRectArray[0]) mRectArray[0] = mArrayLocs.get(j);
                if (mArrayLocs.get(j + 1) < mRectArray[1]) mRectArray[1] = mArrayLocs.get(j + 1);
                if (mArrayLocs.get(j) > mRectArray[2]) mRectArray[2] = mArrayLocs.get(j);
                if (mArrayLocs.get(j + 1) > mRectArray[3]) mRectArray[3] = mArrayLocs.get(j + 1);
            }
            if (mStrokeVertexBuilder == null) {
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "jinkeys, stroke vertex builder is still null, is this a stroke on a deleted thing? #TODO");
            } else {
                mStrokeVertexBuilder.buildVertices();
            }
        }
    }

    public void initializeParentModel() {
        if (parentModel != null) return;

        if (!getTargetID().equalsIgnoreCase(WorkSpaceState.getInstance().getWorkSpaceModel().getId())) {
            BaseWidgetModel baseWidgetParentModel = WorkSpaceState.getInstance().getModelTree().getModel(getTargetID());
            if (baseWidgetParentModel != null) {


                Log.i("initializeParentModel",""+this );

                if (baseWidgetParentModel instanceof NoteModel) {
                    if (!((NoteModel) baseWidgetParentModel).mExistingStrokes.contains(this)) {
                        ((NoteModel) baseWidgetParentModel).mExistingStrokes.add(this);
                        Collections.sort(((NoteModel) baseWidgetParentModel).mExistingStrokes);
                    }
                } else if (baseWidgetParentModel instanceof ImageModel) {
                    if (!((ImageModel) baseWidgetParentModel).mExistingStrokes.contains(this)) {
                        ((ImageModel) baseWidgetParentModel).mExistingStrokes.add(this);
                        Collections.sort(((ImageModel) baseWidgetParentModel).mExistingStrokes);
                    }

                }

                //Log.i("initializeParentModel",""+mExistingStroks );

                parentModel = baseWidgetParentModel;
            } else {
                AppConstants.LOG(AppConstants.VERBOSE, TAG, "On Workspace as baseWidgetParentModel == null for strokeModel.getTargetID() =  "
                        + getTargetID());
            }
        } else {
            parentModel = WorkSpaceState.getInstance().getWorkSpaceModel();
        }
    }

    public void reallyJustSetLineWidth(float lineWidth) {
        mLineWidth = lineWidth;
        mIsDirty = true;
    }
    // Ending Region Public Methods

    // Region Abstract Methods
    @Override
    public boolean isInViewPort() {

        if (parentModel == null) {
            initializeParentModel();
        }
//		return true;
        if (parentModel != null && !(parentModel instanceof WorkSpaceModel))
            return parentModel.isInViewPort();

        return super.isInViewPort();
    }

    @Override
    public void preDraw() {
        if (!isModelGLInitialized) {
            createDrawable();
            isModelGLInitialized = true;
        }
    }

    // Send the Model to Web Socket Server
    @Override
    public boolean sendToWSServer() {
        HeStrokeMessageSender heStrokeMessageSender = new HeStrokeMessageSender(this);
        heStrokeMessageSender.send();
        return true;
    }


    @Override
    public String toString() {
        return "StrokeModel{" + "mArrayLocs=" + mArrayLocs + ", mLineWidth=" + mLineWidth + ", mBrush=" + mBrush + ", color="
                + Arrays.toString(mColor) + ", mRegionID=" + mRegionID + ", TargetID=" + getTargetID() + '}';
    }
    // End Region Abstract Methods

}
