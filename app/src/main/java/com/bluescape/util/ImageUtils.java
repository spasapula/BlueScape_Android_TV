package com.bluescape.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.File;

public class ImageUtils {

	private static final String TAG = ImageUtils.class.getSimpleName();

	public static Bitmap decodeToImage(byte[] bytes) {
		Bitmap image = null;
		try {
			image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	/**
	 * Decode string to image
	 * 
	 * @param file
	 *            The file to decode
	 * @return decoded image
	 */
	public static Bitmap decodeToImage(File file) {

		Bitmap image = null;
		try {
			image = BitmapFactory.decodeFile(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return image;
	}

	/**
	 * Decode string to image
	 * 
	 * @param imageString
	 *            The string to decode
	 * @return decoded image
	 */
	public static Bitmap decodeToImage(String imageString) {

		Bitmap image = null;
		byte[] imageByte;
		try {
			imageByte = Base64.decode(imageString, Base64.URL_SAFE);
			ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
			image = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	/**
	 * Use this code to merge two bitmaps together.
	 * 
	 * @param bmp1
	 * @param bmp2
	 * @return The merged bitmap.
	 */
	public static Bitmap mergeBitmaps(Bitmap bmp1, Bitmap bmp2) {
		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(bmp1, new Matrix(), null);
		canvas.drawBitmap(bmp2, 0, 0, null);
		return bmOverlay;
	}
}
