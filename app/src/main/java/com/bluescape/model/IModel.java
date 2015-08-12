package com.bluescape.model;

import com.bluescape.model.util.Rect;

public interface IModel {

	void createDrawable();

	void createBorderDrawable();

	float getHeight();

	String getID();

	float[] getModelMatrix();

	Rect getRect();

	String getTargetID();

	float getWidth();

	boolean isDirty();

	boolean isInViewPort();

	boolean isPinned();

	void preDraw();

	boolean sendToWSServer();

	boolean sendToWSServerEdit();

	boolean sendToWSServerHide();

	boolean sendToWSServerPin();

	boolean sendToWSServerTemplate();

	void setDirty(boolean dirty);

	void setHeight(float height);

	void setID(String id);

	void setModelMatrix(float[] matrix);

	void setModelBorderMatrix(float[] matrix);


	void setModelMatrix(Rect rect);

	void setModelBorderMatrix(Rect rect);

	void setPinned(boolean pinned);

	void setRect(Rect rect);

	void setTargetID(String targetID);

	void setWidth(float width);

	void updateOnlyRect(Rect rect);

}
