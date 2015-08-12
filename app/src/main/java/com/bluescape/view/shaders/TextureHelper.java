package com.bluescape.view.shaders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.opengl.GLES20;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;

import java.io.IOException;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLU.gluErrorString;
import static android.opengl.GLUtils.texImage2D;

public class TextureHelper {

	private static final String TAG = TextureHelper.class.getSimpleName();

	/**
	 * Converts a resourceID to a bitmap.
	 * 
	 * @param resourceID
	 * @return
	 */
	public static Bitmap convertResourceToBitmap(int resourceID) {
		// Generate an options class.
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// Don't scale the bitmap
		options.inScaled = false;
		// Decode resource file into a bitmap.
		final Bitmap bitmap = BitmapFactory.decodeResource(WorkSpaceState.getInstance().context.getResources(), resourceID, options);
		if (bitmap == null) {
			AppConstants.LOG(AppConstants.VERBOSE, TAG, "Resource ID " + resourceID + " could not be decoded.");
			return null;
		}
		return bitmap;
	}

	/**
	 * Converts a URI to a bitmap.
	 * 
	 * @param uri
	 * @return
	 */
	public static Bitmap convertUriToBitmap(Uri uri) {
		Bitmap bitmap = null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(WorkSpaceState.getInstance().context.getContentResolver(), uri);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public static Bitmap flip(Bitmap d) {
		Matrix m = new Matrix();
		m.postRotate(180);
		Bitmap dst = Bitmap.createBitmap(d, 0, 0, d.getWidth(), d.getHeight(), m, false);
		dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
		return dst;
	}

	/**
	 * Loads the texture into the openGL context
	 * 
	 * @param bitmap
	 * @return
	 */
	public static int loadTexture(final Bitmap bitmap) {
		final int[] textureObjectIds = new int[1];

		glGenTextures(1, textureObjectIds, 0);

		AppConstants.LOG(AppConstants.VERBOSE, TAG, "TextureID: " + textureObjectIds[0]);

		// Check to see if we got a texture id from opengl
		if (textureObjectIds[0] == 0) {
			AppConstants.LOG(AppConstants.ERROR, TAG, "Could not generate a new OpenGL texture object.");
			return 0;
		}

		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

		// Bind the textureID as 2D.
		glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

		// The filtering for minify and magnify of the texture
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

		AppConstants.LOG(AppConstants.VERBOSE, TAG, "Bitmap size: " + bitmap.getByteCount() + " error: " + gluErrorString(glGetError()));

		glGenerateMipmap(GL_TEXTURE_2D);

		return textureObjectIds[0];
	}
}
