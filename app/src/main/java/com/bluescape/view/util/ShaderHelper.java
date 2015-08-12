package com.bluescape.view.util;

import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

/**
 * A helper class to: 1. Create a new OpenGL shader object. 2. Compile our
 * shader code. 3. Return the shader object for that shader code.
 * 
 * @author benholland
 */
@SuppressWarnings("unused")
class ShaderHelper {

	private static final String TAG = "ShaderHelper";

	public static int compileFragmentShader(String shaderCode) {
		return compileShader(GL_FRAGMENT_SHADER, shaderCode);
	}

	public static int compileVertexShader(String shaderCode) {
		return compileShader(GL_VERTEX_SHADER, shaderCode);
	}

	public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
		final int programObjectId = glCreateProgram();

		if (programObjectId == 0) {
			Log.w(TAG, "Could not create new program");
		}

		// Attach the 2 shaders to the program object
		glAttachShader(programObjectId, vertexShaderId);
		glAttachShader(programObjectId, fragmentShaderId);

		// Join the 2 shaders together
		glLinkProgram(programObjectId);

		// Check if the link failed or succeeded
		final int[] linkStatus = new int[1];
		glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

		// Print the program info log to the Android log output.
		Log.v(TAG, "Results of linking program:\n" + glGetProgramInfoLog(programObjectId));

		// Check the link status: if it is 0 it failed
		if (linkStatus[0] == 0) {
			// If it failed, delete the program object.
			glDeleteProgram(programObjectId);
			Log.w(TAG, "Linking of program failed.");
			return 0;
		}

		return programObjectId;
	}

	/**
	 * Validate program to see if it is valid for the current OpenGL state. Ask
	 * OpenGL if the program is inefficient or failing to run etc. We should
	 * only validate when developing and debugging!
	 * 
	 * @param programObjectId
	 * @return
	 */
	public static boolean validateProgram(int programObjectId) {
		// Validate the program
		glValidateProgram(programObjectId);

		// Check the results
		final int[] validateStatus = new int[1];
		glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);

		Log.v(TAG, "Results of validating program: " + validateStatus[0] + "\nLog:" + glGetProgramInfoLog(programObjectId));

		return validateStatus[0] != 0;
	}

	/**
	 * @param type
	 * @param shaderCode
	 * @return
	 */
	private static int compileShader(int type, String shaderCode) {
		// Create a new shader object
		final int shaderObjectId = glCreateShader(type);

		// Check if the creation was successful
		if (shaderObjectId == 0) {
			Log.w(TAG, "Could not create new shader.");
			return 0;
		}

		// Once we have a valid shader obvject, upload the source code
		glShaderSource(shaderObjectId, shaderCode);

		// Compile the source code that was previously uploaded to
		// shaderObjectId
		glCompileShader(shaderObjectId);

		// Check if OpenGL was able to successfully compile the shader
		final int[] compileStatus = new int[1];
		glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

		// Print the shader info log to the Android log output.
		Log.v(TAG, "Results of compiling source:" + "\n" + shaderCode + "\n:" + glGetShaderInfoLog(shaderObjectId));

		if (compileStatus[0] == 0) {
			// If it failed, delete the shader object.
			glDeleteShader(shaderObjectId);

			Log.w(TAG, "Compilation of shader failed.");

			return 0;
		}

		return shaderObjectId;
	}
}
