package com.bluescape.model.util;

/**
 * The Y axis is backwards in the JSON from the server. We need to switch the
 * hieght around. Y2 should be above Y1. Created by Mark Stanford on 12/1/14.
 */
public class Rect {

	final public static int TOPX = 0;
	final public static int TOPY = 1;
	final public static int BOTX = 2;
	final public static int BOTY = 3;

	// Useful for Card and image pin/delete pop up on long press
	final public static int MIDTOPX = 4;
	final public static int MIDTOPY = 5;

	final public float[] rect = new float[4];

	public Rect() {

	}

	public Rect(float x, float y, float x2, float y2) {
		rect[TOPX] = x;
		rect[TOPY] = y;
		rect[BOTX] = x2;
		rect[BOTY] = y2;
	}

	public Rect(float[] rect) {
		// noinspection ManualArrayCopy
		System.arraycopy(rect, 0, this.rect, 0, rect.length);
	}

	public float getBOTX() {
		return rect[BOTX];
	}

	public float getBOTY() {
		return rect[BOTY];
	}

	public float getHeight() {
		return rect[BOTY] - rect[TOPY];
	}

	public float[] getRect() {
		return this.rect;
	}

	public float getTOPX() {
		return rect[TOPX];
	}

	public float getTOPY() {
		return rect[TOPY];
	}

	public float getWidth() {
		return rect[BOTX] - rect[TOPX];
	}

	public void setHeight(float height) {
		this.rect[BOTY] = this.rect[TOPY] - height;
	}

	public void setWidth(float width) {
		this.rect[BOTX] = this.rect[TOPX] + width;
	}

	@Override
	public String toString() {
		return "Rect{" + " TOPX=" + rect[TOPX] + ", TOPY=" + rect[TOPY] + ", BOTX=" + rect[BOTX] + ", BOTY=" + rect[BOTY] + "} ";
	}

}
