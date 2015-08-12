package com.bluescape.model.util;

import com.google.gson.annotations.SerializedName;

public class TextStyle {

	@SerializedName("font-size")
	private String mFontSize = "42px";
	@SerializedName("font-weight")
	private String mFontWieght = "400";
	@SerializedName("text-transform")
	private String mTextTranform = "inherit";

	public TextStyle() {

	}

	public TextStyle(String fontSize, String fontWieght, String textTranform) {
		mFontSize = fontSize;
		mFontWieght = fontWieght;
		mTextTranform = textTranform;
	}

	public String getFontSize() {
		return mFontSize;
	}

	public String getFontWieght() {
		return mFontWieght;
	}

	public String getTextTranform() {
		return mTextTranform;
	}

	public void setFontSize(String fontSize) {
		mFontSize = fontSize;
	}

	public void setFontWieght(String fontWieght) {
		mFontWieght = fontWieght;
	}

	public void setTextTranform(String textTranform) {
		mTextTranform = textTranform;
	}
}
