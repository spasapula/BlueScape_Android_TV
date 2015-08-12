package com.bluescape.gesture;

import android.content.Context;
import android.view.ScaleGestureDetector;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

	private static final String TAG = "ScaleListener";

	public ScaleListener(Context context) {
		super();
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		GestureHandler.getInstance().onScale(detector);
		return super.onScale(detector);
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		GestureHandler.getInstance().onScaleBegin(detector);
		return super.onScaleBegin(detector);
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		GestureHandler.getInstance().onScaleEnd(detector);
		super.onScaleEnd(detector);
	}
}
