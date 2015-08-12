package com.bluescape.model.template;

import com.google.gson.annotations.SerializedName;

public class WidgetTemplate {
	@SerializedName("width")
	private int mWidth = 0;
	@SerializedName("height")
	private int mHeight = 0;

	/**
	 * Constructor for Images. We will use "actualHeight" and "actualWidth" vs.
	 * the rect.width and rect.height to find out how much to matrix.scale with.
	 * 
	 * @param width
	 * @param height
	 */
	WidgetTemplate(int width, int height) {
		mWidth = width;
		mHeight = height;
	}

	public int getHeight() {
		return mHeight;
	}

	public int getWidth() {
		return mWidth;
	}

	public void setHeight(int height) {
		mHeight = height;
	}

	public void setWidth(int width) {
		mWidth = width;
	}

	@Override
	public String toString() {
		return "WidgetTemplate{" + "mWidth=" + mWidth + ", mHeight=" + mHeight + '}';
	}
}
