package com.bluescape.view.shaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.bluescape.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

public class TextureShaderProgram extends ShaderProgram {
	private static final String TAG = TextureShaderProgram.class.getSimpleName();

	// Vertex
	private static final String VERTEX_TEXCOORD = "atexCoord";
	// Fragment
	private static final String FRAGMENT_TEXTURE = "uSampler";
	private final int mPositionHandle;
	private final int mMatrixHandle;

	private final int mFragTextHandle;

	private final int mTexCoordLoc;
	private final int mTextureID;

	/**
	 * Shader for drawing the background.
	 *
	 * @param context
	 */
	public TextureShaderProgram(Context context) {
		super(context, R.raw.texture_vertex_shader, R.raw.texture_fragment_shader, ShaderType.Texture);

		// get handle to vertex shader's vPosition member from the shader
		mPositionHandle = glGetAttribLocation(mProgram, VERTEX_POSITION);
		// Get the handle to projection matrix
		mMatrixHandle = glGetUniformLocation(mProgram, VERTEX_MATRIX);
		// Get handle to fragment texture
		mFragTextHandle = glGetUniformLocation(mProgram, FRAGMENT_TEXTURE);
		// Get location of texture coordinates
		mTexCoordLoc = GLES20.glGetAttribLocation(mProgram, VERTEX_TEXCOORD);

		mTextureID = TextureHelper.loadTexture(TextureHelper.convertResourceToBitmap(R.drawable.smiley));

	}

	/**
	 * Use this to create a texture ID when the object is instantiated.
	 * 
	 * @param bitmap
	 * @return
	 */
	public int createTextureID(Bitmap bitmap) {
		return TextureHelper.loadTexture(bitmap);
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
	 * Use the texture helper to create a texture ID. Call this during the draw
	 * pass.
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
