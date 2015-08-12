package com.bluescape.model.widget.change;

import com.google.gson.annotations.SerializedName;

public class GeometryPayloadModel {

	@SerializedName("x")
	private float mX;

	@SerializedName("y")
	private float mY;

	@SerializedName("worldSpaceWidth")
	private float worldSpaceWidth;

	@SerializedName("worldSpaceHeight")
	private float worldSpaceHeight;

	@SerializedName("windowSpaceHeight")
	private float windowSpaceHeight;

	@SerializedName("windowSpaceWidth")
	private float windowSpaceWidth;

	@SerializedName("order")
	int order;

	@SerializedName("version")
	private int version;

	public int getOrder() {
		return order;
	}

	public int getVersion() {
		return version;
	}

	public float getWindowSpaceHeight() {
		return windowSpaceHeight;
	}

	public float getWindowSpaceWidth() {
		return windowSpaceWidth;
	}

	public float getWorldSpaceHeight() {
		return worldSpaceHeight;
	}

	public float getWorldSpaceWidth() {
		return worldSpaceWidth;
	}

	public float getX() {
		return mX;
	}

	public float getY() {
		return mY;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void setWindowSpaceHeight(float windowSpaceHeight) {
		this.windowSpaceHeight = windowSpaceHeight;
	}

	public void setWindowSpaceWidth(float windowSpaceWidth) {
		this.windowSpaceWidth = windowSpaceWidth;
	}

	public void setWorldSpaceHeight(float worldSpaceHeight) {
		this.worldSpaceHeight = worldSpaceHeight;
	}

	public void setWorldSpaceWidth(float worldSpaceWidth) {
		this.worldSpaceWidth = worldSpaceWidth;
	}

	public void setX(float mX) {
		this.mX = mX;
	}

	public void setY(float mY) {
		this.mY = mY;
	}

}
