package com.bluescape.gesture;

import android.content.Context;
import android.view.MotionEvent;

public class GestureListener extends android.view.GestureDetector.SimpleOnGestureListener {

	public static final String TAG = GestureListener.class.getSimpleName();

	public GestureListener(Context context) {
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		GestureHandler.getInstance().onDoubleTap(e);
		return super.onDoubleTap(e);
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		GestureHandler.getInstance().onDoubleTapEvent(e);
		return super.onDoubleTapEvent(e);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		GestureHandler.getInstance().onDown(e);
		return super.onDown(e);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		GestureHandler.getInstance().onFling(e1, e2, velocityX, velocityY);
		return super.onFling(e1, e2, velocityX, velocityY);
	}

	@Override
	public void onLongPress(MotionEvent e) {
		GestureHandler.getInstance().onLongPress(e);
		super.onLongPress(e);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		GestureHandler.getInstance().onScroll(e1, e2, distanceX, distanceY);
		return super.onScroll(e1, e2, distanceX, distanceY);
	}

	@Override
	public void onShowPress(MotionEvent e) {
		GestureHandler.getInstance().onShowPress(e);
		super.onShowPress(e);
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		GestureHandler.getInstance().onSingleTapConfirmed(e);
		return super.onSingleTapConfirmed(e);
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		GestureHandler.getInstance().onSingleTapUp(e);
		return super.onSingleTapUp(e);
	}
}
