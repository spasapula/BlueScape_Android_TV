package com.bluescape.model.util;

/**
 * Created by kvanainc1 on 17/06/15.
 */
public class NormalizedLayerStacker {
	private static NormalizedLayerStacker ourInstance = new NormalizedLayerStacker();

	public static NormalizedLayerStacker getInstance() {
		return ourInstance;
	}

	private int depth;
	private int incrementValue;

	private NormalizedLayerStacker() {
		depth = -1;
		incrementValue = -1;
	}

	public int current() {
		int returnValue = depth;
		depth += incrementValue;
		return returnValue;
	}

	public void reset() {
		reset(-1);
	}

	public void reset(int depth) {
		resetDepthByIncrement(depth, -1);
	}

	public void resetDepthByIncrement(int depth, int incrementValue) {
		this.incrementValue = incrementValue;
		this.depth = depth;
	}
}
