package com.bluescape.view.shaders;

import android.content.Context;

import com.bluescape.util.TextResourceReader;

import static android.opengl.GLES20.glUseProgram;

/**
 * This will be the base class for holding shader information. This contains the
 * handles to the uniforms, attributes and matrices in the shaders Created by
 * Mark Stanford on 11/26/14.
 */
public class ShaderProgram {

	/**
	 * Shader locations. Use a background_vertex handle to point to this
	 * location in the shader.
	 */
	// Vertex
	static final String VERTEX_POSITION = "aPosition";
	static final String VERTEX_MATRIX = "u_Matrix";

	// Fragment
	static final String FRAGMENT_COLOR = "uColor";

	private static final String TAG = ShaderProgram.class.getSimpleName();

	// Shader program
	public final int mProgram;

	/**
	 * The super constructor builds the shader and sets the programID
	 *
	 * @param context
	 * @param vertexShaderResourceId
	 * @param fragmentShaderResourceId
	 * @param type
	 */
	ShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId, ShaderType type) {
		// Compile the shaders and link the program.
		mProgram = ShaderHelper.getInstance().buildProgram(TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
			TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId));
	}

	/**
     *
     */
	public void useProgram() {
		// Set the current OpenGL shader program to this program.
		// glUseProgram(program);
		glUseProgram(mProgram);
	}
}
