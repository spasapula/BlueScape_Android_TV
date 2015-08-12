package com.bluescape.view.util;

import java.util.HashMap;
import java.util.Map;

/**
 * This was inspired by Joe's explanation on why my textures weren't working.
 * Created by Mark Stanford on 12/18/14.
 */
public class TextureManager {

	/**
	 * Instance holder for our safe lazy instantiation pattern
	 * https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
	 */
	private static class instanceHolder {
		private static final TextureManager INSTANCE = new TextureManager();
	}

	/**
	 * Returns the singleton
	 * 
	 * @return
	 */
	public static TextureManager getInstance() {
		return instanceHolder.INSTANCE;
	}

	private Map<String, Integer> mBitmapTextureIDMap;

	private TextureManager() {
		mBitmapTextureIDMap = new HashMap<>();
	}

	public void clearTextureCache() {
		mBitmapTextureIDMap.clear();
	}

	public Integer getTextureID(String path) {
		return mBitmapTextureIDMap.get(path);
	}

	public boolean hasTexture(String path) {
		Integer inty = mBitmapTextureIDMap.get(path);
		return inty != null;
	}

	public void setTextureID(String path, int textureID) {
		mBitmapTextureIDMap.put(path, textureID);
	}

}
