package com.bluescape.model.template;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

public class NoteTemplate extends WidgetTemplate {

	@SerializedName("thumbnail")
	private String mThumbnail = "";
	@SerializedName("id")
	private String mID = "";

	private transient Bitmap mBitmap;

	public NoteTemplate(String ID, int width, int height, Bitmap mBitmap) {
		super(width, height);
		this.mID = ID;
		this.mBitmap = mBitmap;
	}

	public NoteTemplate(String ID, int width, int height, String thumbnail) {
		super(width, height);
		this.mID = ID;
		this.mThumbnail = thumbnail;
	}

	public String getID() {
		return mID;
	}

	public Bitmap getmBitmap() {
		return mBitmap;
	}

	public String getThumbnail() {
		return mThumbnail;
	}

	public void setID(String ID) {
		mID = ID;
	}

	public void setmBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	public void setThumbnail(String thumbnail) {
		mThumbnail = thumbnail;
	}

	@Override
	public String toString() {
		return "NoteTemplate{" + "mThumbnail='" + mThumbnail + '\'' + ", mID='" + mID + '\'' + "} " + super.toString();
	}
}
