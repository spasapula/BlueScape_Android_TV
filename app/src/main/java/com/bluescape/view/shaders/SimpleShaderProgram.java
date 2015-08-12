package com.bluescape.view.shaders;

import android.content.Context;

import com.bluescape.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

/**
 * Holds the shader handles for the simple shader Created by Mark Stanford on
 * 11/26/14.
 */
public class SimpleShaderProgram extends ShaderProgram {

	private final int mPositionHandle;
	private final int mColorHandle;
	private final int mMatrixHandle;

	public SimpleShaderProgram(Context context) {
		super(context, R.raw.simple_vertex_shader, R.raw.simple_fragment_shader, ShaderType.Simple);

		// get handle to vertex shader's vPosition member from the shader
		mPositionHandle = glGetAttribLocation(mProgram, VERTEX_POSITION);

		// get handle to fragment shader's vColor member from the shader
		mColorHandle = glGetUniformLocation(mProgram, FRAGMENT_COLOR);

		// Get the handle to projection matrix
		mMatrixHandle = glGetUniformLocation(mProgram, VERTEX_MATRIX);
	}

	public int getColorHandle() {
		return mColorHandle;
	}

	public int getMatrixHandle() {
		return mMatrixHandle;
	}

	public int getPositionHandle() {
		return mPositionHandle;
	}
}
