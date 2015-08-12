package com.bluescape.view.shaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.bluescape.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

public class TextShaderProgram extends ShaderProgram {

	private static final String TAG = TextShaderProgram.class.getSimpleName();
	// Vertex
	private static final String VERTEX_COLOR = "aColor";

	private static final String VERTEX_TEXCOORD = "atexCoord";
	// Fragment
	private static final String FRAGMENT_TEXTURE = "uSampler";
	private final int mTextureID;
	private int mPositionHandle;
	private int mMatrixHandle;

	private int mFragTextHandle;
	private int mTexCoordLoc;

	private int mColorHandle;

	/**
	 * Shader for drawing the background.
	 *
	 * @param context
	 */
	public TextShaderProgram(Context context) {
		super(context, R.raw.text_vertex, R.raw.text_fragment, ShaderType.Text);

		// get handle to vertex shader's vPosition member from the shader
		mPositionHandle = glGetAttribLocation(mProgram, VERTEX_POSITION);
		// Get the handle to projection matrix
		mMatrixHandle = glGetUniformLocation(mProgram, VERTEX_MATRIX);
		// Get handle to fragment texture
		mFragTextHandle = glGetUniformLocation(mProgram, FRAGMENT_TEXTURE);
		// Get location of texture coordinates
		mTexCoordLoc = glGetAttribLocation(mProgram, VERTEX_TEXCOORD);
		// Handle for color
		mColorHandle = glGetAttribLocation(mProgram, VERTEX_COLOR);

		mTextureID = TextureHelper.loadTexture(TextureHelper.convertResourceToBitmap(R.drawable.font));

	}

	public int createTextureID(Bitmap bitmap) {
		return TextureHelper.loadTexture(bitmap);
	}

	public int getColorHandle() {
		return mColorHandle;
	}

	public int getFragTextHandle() {
		return mFragTextHandle;
	}

	public int getMatrixHandle() {
		return mMatrixHandle;
	}

	public int getPositionHandle() {
		return mPositionHandle;
	}

	public int getTexCoordLoc() {
		return mTexCoordLoc;
	}

	public int getTextureID() {
		return mTextureID;
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
