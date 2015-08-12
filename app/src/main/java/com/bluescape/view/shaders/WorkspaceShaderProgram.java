package com.bluescape.view.shaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.bluescape.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

public class WorkspaceShaderProgram extends ShaderProgram {

	private static final String TAG = "BackgroundShader";

	private static final String FRAGMENT_TEXTURE = "uSampler";
	private static final String FRAGMENT_WORLD_POS = "uWorldSpacePos";
	private static final String FRAGMENT_VIEW_PORT_WIDTH = "uViewPortWidth";
	private static final String FRAGMENT_LINE_SEPERATION = "uLineSeparation";
	private final int mPositionHandle;
	private final int mMatrixHandle;

	private final int mFragTextHandle;
	private final int mFragPosHandle;
	private final int mFragViewPortHandle;
	private final int mFragmentLineHandle;

	/**
	 * Shader for drawing the background.
	 *
	 * @param context
	 */
	public WorkspaceShaderProgram(Context context) {
		super(context, R.raw.simple_vertex_shader, R.raw.background_fragment, ShaderType.Background);

		// get handle to vertex shader's vPosition member from the shader
		mPositionHandle = glGetAttribLocation(mProgram, VERTEX_POSITION);
		// Get the handle to projection matrix
		mMatrixHandle = glGetUniformLocation(mProgram, VERTEX_MATRIX);
		// Get handle to fragment texture
		mFragTextHandle = glGetUniformLocation(mProgram, FRAGMENT_TEXTURE);
		// Get handle to position handle
		mFragPosHandle = glGetUniformLocation(mProgram, FRAGMENT_WORLD_POS);
		// Get handle to viewport handle
		mFragViewPortHandle = glGetUniformLocation(mProgram, FRAGMENT_VIEW_PORT_WIDTH);
		// Get handle to fragment line
		mFragmentLineHandle = glGetUniformLocation(mProgram, FRAGMENT_LINE_SEPERATION);

	}

	public int createTextureID(Bitmap bitmap) {
		return TextureHelper.loadTexture(bitmap);
	}

	public int getFragmentLineHandle() {
		return mFragmentLineHandle;
	}

	public int getFragPosHandle() {
		return mFragPosHandle;
	}

	public int getFragTextHandle() {
		return mFragTextHandle;
	}

	public int getFragViewPortHandle() {
		return mFragViewPortHandle;
	}

	public int getMatrixHandle() {
		return mMatrixHandle;
	}

	public int getPositionHandle() {
		return mPositionHandle;
	}

	/**
	 * Use the texture helper to create a texture ID.
	 *
	 * @param textureId
	 */
	public void setUniforms(int textureId) {
		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		// Tell the texture uniform sampler to use this texture in the shader by
		// telling it to read from texture unit 0.
		GLES20.glUniform1i(mFragTextHandle, 0);
	}
}
