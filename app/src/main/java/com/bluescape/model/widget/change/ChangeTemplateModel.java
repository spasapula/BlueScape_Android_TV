package com.bluescape.model.widget.change;

import com.bluescape.model.widget.BaseWidgetModel;
import com.google.gson.annotations.SerializedName;

//TODO why is this a base widget model? all other models implement draw, this is inconsistent.  This is only used for parsing.
public class ChangeTemplateModel extends BaseWidgetModel {

	// Base name, I think this will be the texture to draw
	@SerializedName("baseName")
	private String mBaseName = "";

	// TODO how is this a base widget model?
	public void draw(float[] matrix) {
	}

	public String getmBaseName() {
		return mBaseName;
	}

	public void setmBaseName(String mBaseName) {
		this.mBaseName = mBaseName;
	}

}
