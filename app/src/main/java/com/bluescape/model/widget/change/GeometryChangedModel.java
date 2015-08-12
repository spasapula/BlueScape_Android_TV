package com.bluescape.model.widget.change;

import com.github.julman99.gsonfire.annotations.PostDeserialize;
import com.google.gson.annotations.SerializedName;

public class GeometryChangedModel {

	private float[] mRect;

	private transient String mModelID;

	@SerializedName("order")
	private int mEventOrder = 1;
	// public float mEventOrder = 1;

	@SerializedName("payload")
	private GeometryPayloadModel geometryPayloadModel = new GeometryPayloadModel();

	private GeometryChangedModel() {

	}

	public int getEventOrder() {
		return mEventOrder;
	}

	public GeometryPayloadModel getGeometryPayloadModel() {
		return geometryPayloadModel;
	}

	public String getModelID() {
		return mModelID;
	}

	public float[] getRect() {
		return mRect;
	}

	public void setEventOrder(float mEventOrder) {
		this.mEventOrder = (int) mEventOrder;
	}

	public void setGeometryPayloadModel(GeometryPayloadModel geometryPayloadModel) {
		this.geometryPayloadModel = geometryPayloadModel;
	}

	public void setModelID(String mModelID) {
		this.mModelID = mModelID;
	}

	@PostDeserialize
	private void geometryChangedDeserialize() {
		float[] mRect = new float[] { geometryPayloadModel.getX(), geometryPayloadModel.getY(),
										geometryPayloadModel.getX() + geometryPayloadModel.getWorldSpaceWidth(),
										geometryPayloadModel.getY() + geometryPayloadModel.getWorldSpaceHeight() };
		setRect(mRect);
		// mEventOrder = geometryPayloadModel.getOrder();
	}

	private void setRect(float[] mRect) {
		this.mRect = mRect;
	}

}
