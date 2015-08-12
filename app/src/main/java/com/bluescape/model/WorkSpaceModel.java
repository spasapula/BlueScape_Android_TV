package com.bluescape.model;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.activity.WorkspaceUpdateListener;
import com.bluescape.model.util.NormalizedLayerStacker;
import com.bluescape.model.util.Rect;
import com.bluescape.model.widget.LocationMarkerModel;
import com.bluescape.model.widget.TexturedWidgetModel;
import com.bluescape.view.shaders.ShaderHelper;
import com.bluescape.view.shaders.ShaderType;
import com.bluescape.view.shaders.TextureHelper;
import com.bluescape.view.shaders.WorkspaceShaderProgram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

public class WorkSpaceModel extends TexturedWidgetModel {

	private static final String TAG = WorkSpaceModel.class.getSimpleName();

	public static String followingClientId;

	private int[] mColor;
	private Bitmap mBitmap;
	/**
	 * We will use this value in the shader.
	 */
	transient private float mViewPortWidth = 1800f;

	// Adding DataHolders that WebSocket Server and Http Server update
	// Asynchronously
	private List<CollaboratorModel> mRoomMembersList;

	private String mWorkSpaceTitle;

	private WorkspaceUpdateListener mWorkspaceUpdateListener;

	private List<LocationMarkerModel> mMarkersList = new ArrayList<>();

	// Add all the Web Socket Data Elements to the WorkspaceDrawableModel
	private String id;

	private float[] viewPort = new float[4];

	private boolean up;

	private String uid;

	private String name;

	private String shareLink;

	private WorkspaceShaderProgram mShader;

	private float[] mWorldPos = new float[] { -10000, -10000, 10000, 10000 };

	private float mLineWidth = 1000f;

	public WorkSpaceModel(String workspaceID) {
		super(new Rect(-1000000, -1000000, 1000000, 1000000));
		this.setRect(this.mRect);

		this.mColor = new int[] { 0, 0, 0, 0 };
		this.setID(workspaceID);
		// This is now being set in the the Renderer OnSurfaceChanged
		// this.mViewPortWidth = viewportWidth;
		NormalizedLayerStacker.getInstance().reset();

	}

	/**
	 * Default Constructor to hold Web Socket Values
	 */
	private WorkSpaceModel() {

	}

	private WorkSpaceModel(float viewportWidth, String workspaceID) {
		super(new Rect(-1000000, -1000000, 1000000, 1000000));
		this.setRect(this.mRect);

		this.mColor = new int[] { 0, 0, 0, 0 };
		this.setID(workspaceID);
		this.mViewPortWidth = viewportWidth;
	}

	public void clearMarkersList() {
		mMarkersList.clear();
	}

	public void clearRoomMembersList() {
		if (mRoomMembersList != null) mRoomMembersList.clear();
	}

	public void createDrawable() {

		// TODO this is redundant
		this.mViewPortWidth = getViewPortWidth();

		// Get the shader for this shape
		this.mShaderType = ShaderType.Background;

		// Get shader program
		this.mShader = (WorkspaceShaderProgram) ShaderHelper.getInstance().getCompiledShaders().get(mShaderType);

		// set the bitmap for this drawable
		// setBitmap(TextureHelper.convertResourceToBitmap(R.drawable.background_square));
		mBitmap = TextureHelper.convertResourceToBitmap(R.drawable.background_square);

		// Get texture
		// mTextureID = mShader.createTextureID(getBitmap());
		mTextureID = mShader.createTextureID(mBitmap);

		// Callgenerate vertices to set mVertexBuffer
		createQuad();

		// Overridden for Workspace as it is unique compared to other drawables
		// so this method is called in the panAndZoomViewport() in Renderer.java
		// setupModelMVPMatrix();

	}

	public void deleteMarker(String markerId) {
		List<LocationMarkerModel> markersList = getMarkersList();

		for (int count = 0; count < markersList.size(); count++) {
			if (markersList.get(count).getID().equals(markerId)) {
				markersList.remove(count);
				setMarkersList(markersList);
				break;
			}
		}
	}

	public void draw(float[] matrix) {

		// Use program
		glUseProgram(mShader.mProgram);
		// Set up texture
		mShader.setUniforms(mTextureID);

		// VBO code
		// Pass in the position information
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mQuad.getVertexBufferIndex());
		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mShader.getPositionHandle());
		// Prepare the triangle coordinate data

		glVertexAttribPointer(mShader.getPositionHandle(), getCoordsPerVertex(), GL_FLOAT, false, 0, 0);

		// Clear the currently bound buffer (so future OpenGL calls do not use
		// this buffer).
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

		// GLES20.glUniformMatrix4fv(mShader.getMatrixHandle(), 1, false,
		// mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(mShader.getMatrixHandle(), 1, false, tempMVPMatrix, 0);

		// Send data to fragment shaders, The texture is bound and set in the
		// shader program.
		glUniform4fv(mShader.getFragPosHandle(), 1, mWorldPos, 0); // Set the
																	// position
																	// in the
																	// world
		glUniform1f(mShader.getFragViewPortHandle(), mViewPortWidth); // Width
																		// of
																		// the
																		// viewport
		glUniform1f(mShader.getFragmentLineHandle(), mLineWidth); // Line
																	// separation

		// Draw the triangle
		glDrawArrays(GL_TRIANGLES, 0, mQuad.getVertexCount());

		// Disable vertex array
		glDisableVertexAttribArray(mShader.getPositionHandle());
	}

	//there's a getID in the base, #TODO confusing, refactor
	public String getId() {
		return id;
	}

	public List<LocationMarkerModel> getMarkersList() {
		return mMarkersList;
	}

	public String getName() {
		return name;
	}

	public List<CollaboratorModel> getRoomMembersList() {
		return mRoomMembersList;
	}

	public String getShareLink() {
		return shareLink;
	}

	public String getUid() {
		return uid;
	}

	public float[] getViewPort() {
		return viewPort;
	}

	public String getWorkSpaceTitle() {
		return mWorkSpaceTitle;
	}

	public WorkspaceUpdateListener getWorkspaceUpdateListener() {
		return mWorkspaceUpdateListener;
	}

	public boolean isUp() {
		return up;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMarkersList(List<LocationMarkerModel> mMarkersList) {
		this.mMarkersList = mMarkersList;
		Collections.sort(mMarkersList, new Comparator<LocationMarkerModel>() {
			@Override
			public int compare(LocationMarkerModel p1, LocationMarkerModel p2) {

				return (int) p2.getCreationTime() - (int) p1.getCreationTime();
			}
		});
		mWorkspaceUpdateListener.updateMarkersList(mMarkersList);

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

		Matrix.setIdentityM(mModelMatrix, 0);

		// Shows strokes but no workspace
		Matrix.translateM(mModelMatrix, 0, rect.getTOPX(), rect.getTOPY(), 0.9f);

		Matrix.scaleM(mModelMatrix, 0, scalex, scaleY, scaleZ);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRoomMembersList(List<CollaboratorModel> roomMemberList) {

		if (mRoomMembersList == null)
			mRoomMembersList = roomMemberList;
		else {
			getUpdatedRoomList((ArrayList<CollaboratorModel>) roomMemberList);
		}
		mWorkspaceUpdateListener.updateCollaboratorsList(mRoomMembersList);
	}

	public void setShareLink(String shareLink) {
		this.shareLink = shareLink;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public void setUp(boolean up) {
		this.up = up;
	}

	// Drawable Methods to integrate

	// NOT @Override Passing in the projection here
	public void setupModelMVPMatrix(float[] mProjectionMatrix) {
		// float[] scratch = new float[16];
		Matrix.multiplyMM(tempMVPMatrix, 0, mProjectionMatrix, 0, mModelMatrix, 0);
		// Matrix.multiplyMM(scratch, 0, matrix, 0, mModelMatrix, 0);
		// mMVPMatrix = tempMVPMatrix;
	}

	public void setViewPort(float[] viewPort) {
		this.viewPort = viewPort;
	}

	public void setViewPortWidth(float viewPortWidth) {
		mViewPortWidth = viewPortWidth;
	}

	public void setWorkSpaceTitle(String workSpaceTitle) {
		this.mWorkSpaceTitle = workSpaceTitle;
		mWorkspaceUpdateListener.updateWorkspaceTitle(mWorkSpaceTitle);
	}

	public void setWorkspaceUpdateListener(WorkspaceUpdateListener workspaceUpdateListener) {
		mWorkspaceUpdateListener = workspaceUpdateListener;
	}

	public void setWorldPos(float[] worldPos) {
		// Showing more than 10 squares on each side. Lets make the line width
		// be 5,000.
		if ((worldPos[2] - worldPos[0]) > 10000) {
			mLineWidth = 5000f;
		} else {
			mLineWidth = 1000f;
		}

		mWorldPos = worldPos;
	}

	public void updateRoomMemberVC(String clientId, float[] viewPort) {
		if (mRoomMembersList != null) {
			for (int collaborator = 0; collaborator < mRoomMembersList.size(); collaborator++) {
				if (clientId.equals(mRoomMembersList.get(collaborator).getClientId())) {
					mRoomMembersList.get(collaborator).setViewPort(viewPort);
					return;
				}
			}
		} else {
			// AppConstants.LOG(AppConstants.CRITICAL, TAG, "Inside
			// WorkSpaceModel mRoomMembersList == null:

		}
	}

	private void getUpdatedRoomList(ArrayList<CollaboratorModel> roomMemberList) {

		for (int count = 0; count < mRoomMembersList.size();) {
			CollaboratorModel mCollaboratorModel = mRoomMembersList.get(count);
			boolean flag = false;
			for (int i = 0; i < roomMemberList.size(); i++) {

				CollaboratorModel mNewCollaboratorModel = roomMemberList.get(i);

				if (mCollaboratorModel.getClientId().equals(mNewCollaboratorModel.getClientId())) {
					flag = true;
					roomMemberList.remove(i);
					break;
				}

			}
			if (!flag) {
				mRoomMembersList.remove(count);
			} else {
				count++;
			}

		}
		for (CollaboratorModel mCollaboratorModel : roomMemberList) {
			mRoomMembersList.add(mCollaboratorModel);
		}

	}

	private float getViewPortWidth() {
		return mViewPortWidth;
	}

	public boolean isCollaboratorAvailable() {
		boolean availability = false;
		if (WorkSpaceModel.followingClientId != null && mRoomMembersList != null) {
			for (int collaborator = 0; collaborator < mRoomMembersList.size(); collaborator++) {
				if (WorkSpaceModel.followingClientId.equals(mRoomMembersList.get(collaborator).getClientId())) {
					availability = true;
				}
			}
		}
		return availability;
	}
	public float[] getCollaboratorColor(String currentCollaborator) {
		float color[] = null;
		if (currentCollaborator != null && mRoomMembersList != null)
			for (int collaborator = 0; collaborator < mRoomMembersList.size(); collaborator++) {
				if (currentCollaborator.equals(mRoomMembersList.get(collaborator).getClientId())) {
					color = mRoomMembersList.get(collaborator).getRgbColor();
				}
			}
		return color;
	}

	public String getCollaboratorInitials(String currentCollaborator) {
		String initials = null;
		if (currentCollaborator != null && mRoomMembersList != null)
			for (int collaborator = 0; collaborator < mRoomMembersList.size(); collaborator++) {
				if (currentCollaborator.equals(mRoomMembersList.get(collaborator).getClientId())) {
					initials = mRoomMembersList.get(collaborator).initials();
				}
			}
		return initials;
	}

	// Drawable methods
	public float doesIntersectXY(float x, float y) {
		//return -1;


		AppConstants.LOG(AppConstants.VERBOSE, TAG,
				String.format("Workspace is not a selectable Model"));
		return -1;
	}
}
