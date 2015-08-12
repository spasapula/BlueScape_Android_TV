package com.bluescape.view.shaders;

import android.opengl.GLES20;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glGetProgramiv;

public class ShaderHelper {

	/**
	 * Instance holder for our safe lazy instantiation pattern
	 * https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
	 */
	private static class instanceHolder {
		private static final ShaderHelper INSTANCE = new ShaderHelper();
	}

	private static final String TAG = "ShaderHelper";

	/**
	 * Returns the singleton
	 * 
	 * @return
	 */
	public static ShaderHelper getInstance() {
		return instanceHolder.INSTANCE;
	}

	// Map of shaderTypes in the shape to the program id int
	private Map<ShaderType, ShaderProgram> compiledShaders = new HashMap<>();

	/**
	 * Private constructor for our singleton pattern
	 */
	private ShaderHelper() {
	}

	public int buildProgram(String vertexSource, String fragmentSource) {

		// Logic for simple shader
		int vertexShader = compileVertexShader(vertexSource);
		int fragmentShader = compileFragmentShader(fragmentSource);

		return attachShader(vertexShader, fragmentShader);
	}

	public Map<ShaderType, ShaderProgram> getCompiledShaders() {
		return compiledShaders;
	}

	public void putCompiledShader(ShaderType type, ShaderProgram program) {
		compiledShaders.put(type, program);
	}

	/**
	 * Links shaders into a program
	 */
	private int attachShader(int vertextShaderID, int fragmentShaderID) {

		int mProgram = GLES20.glCreateProgram(); // create empty OpenGL ES
													// Program
		GLES20.glAttachShader(mProgram, vertextShaderID); // add the vertex
															// shader to program
		GLES20.glAttachShader(mProgram, fragmentShaderID); // add the fragment
															// shader to program
		GLES20.glLinkProgram(mProgram); // creates OpenGL ES program executables

		if (mProgram == 0) {
			Log.w(TAG, "Program creation failed");
			return 0;
		}

		// Checking linking status
		final int[] linkStatus = new int[1];
		glGetProgramiv(mProgram, GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] == 0) {
			// If it failed, delete the program object.
			glDeleteProgram(mProgram);
			Log.w(TAG, "Linking of program failed.");
			return 0;
		}

		return mProgram;
	}

	private int compileFragmentShader(String shaderCode) {
		return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
	}

	private int compileShader(int type, String shaderCode) {
		final int shaderObjectId = glCreateShader(type);

		if (shaderObjectId == 0) {
			Log.w(TAG, "Could not create new shader.");
			return 0;
		}

		GLES20.glShaderSource(shaderObjectId, shaderCode);
		GLES20.glCompileShader(shaderObjectId);

		return shaderObjectId;
	}

	private int compileVertexShader(String shaderCode) {
		return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
	}
}
