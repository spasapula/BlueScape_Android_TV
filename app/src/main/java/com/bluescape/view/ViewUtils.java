package com.bluescape.view;

import android.view.View;

import com.bluescape.model.WorkSpaceState;

public class ViewUtils {

	private static final String TAG = ViewUtils.class.getSimpleName();

	/**
	 * The coefficent of the zoom math. This is how much zooms will be scaled
	 * down by
	 */

	private static final float ZOOM_FACTOR = 0.5f;

	// private static final float ZOOM_MULTIPLIER = 2.0f;

	/**
	 * Scales a touch event to openGL view.
	 *
	 * @param parentView
	 *            View touch happened in
	 * @param touchX
	 *            X of the touch event from the View
	 * @param touchY
	 *            Y of the touch event from the View
	 * @return float array with x[0] y[1]
	 */
	public static float[] getWorkSpaceXYFromTouch(View parentView, float touchX, float touchY) {

		int parentWidth = parentView.getWidth();
		int parentHeight = parentView.getHeight();

		float ratioToScreenX = (touchX / parentWidth);
		float ratioToScreenY = (parentHeight - touchY) / parentHeight;

		/**
		 * My math for this is: (ratioTouch * (Zoom*2)) - zoom The ratio is
		 * making the touch from pixels into 0,0 bottom left and 1,1 top right.
		 * The zoom is -zoom to + zoom on the workspace, so we need * 2 the
		 * zoom. This is becuase is counts from -zoom to +zoom which is zoom*2
		 * units. We then subtract the zoom so make the number be scaled
		 * properly into the workspace. with 0,0 in the middle. Ex. ratioTouch =
		 * .5,.5 We want this to be 0,0 in a zoom of 1 (.5 * (1*2)) - 1 = 0,0
		 * Ex. ratioTouch = 1,1, We want this to be 2,2 in a zoom of 2 (1 *
		 * (2*2)) - 2 = 2,2 Ex. ratioTouch = .25,.25 We want this to be -.5,-.5
		 * in a zoom of 1 (.25 * (1*2)) - 1 = -.5,-.5 We also need to
		 * incorporate the aspect ratio here. We also need to add or subtract
		 * the offset for scrolling the workspace.
		 */

		float aspectRatio = WorkSpaceState.getInstance().getAspectRatio();
		float zoom = WorkSpaceState.getInstance().getZoom();

		// Magic 2 makes it work because the zoom is -min and max which makes it
		// double since it counts from - to +
		float xRatioWithZoom = (ratioToScreenX * (zoom * 2)) - (zoom);
		float yRatioWithZoom = (ratioToScreenY * (zoom * 2)) - (zoom);

		float ratioToScreenWithAspectX = (xRatioWithZoom * (aspectRatio)); // The
																			// two
																			// is
																			// because
																			// we
																			// split
																			// the
																			// screen
																			// at
																			// 0;

		/**
		 * Put the x,y offset here. It will just be to - or + the offset to the
		 * ratio
		 */

		float ratioWithOffsetX = ratioToScreenWithAspectX + WorkSpaceState.getInstance().getOffset()[0];
		float ratioWithOffsetY = yRatioWithZoom - WorkSpaceState.getInstance().getOffset()[1];

		// The Y is inverted for this project in the server end of things
		return new float[] { ratioWithOffsetX, -ratioWithOffsetY };
	}

	// Function to return the mZoom for WorkSpaceState based on X1 and X2 from
	// the Vc message
	public static float getZoomFromVcRect(float Y1, float Y2) {
		return (Y2 - Y1) * ZOOM_FACTOR;
	}

	/**
	 * @param gesture
	 * @param zoom
	 * @param maxZoom
	 * @return
	 */
	public static float scaleScrollGesture(float gesture, float zoom, float maxZoom) {
		return ((gesture * (zoom / maxZoom)));
	}

	/**
	 * Scales a touch event to openGL view.
	 *
	 * @param parentView
	 *            View touch happened in
	 * @param touchX
	 *            X of the touch event from the View
	 * @param touchY
	 *            Y of the touch event from the View
	 * @return float array with x[0] y[1]
	 */
	public static float[] scaleTouchEvent(View parentView, float touchX, float touchY) {

		int parentWidth = parentView.getWidth();
		int parentHeight = parentView.getHeight();

		float ratioToScreenX = (touchX / parentWidth);
		float ratioToScreenY = (parentHeight - touchY) / parentHeight;

		/**
		 * My math for this is: (ratioTouch * (Zoom*2)) - zoom The ratio is
		 * making the touch from pixels into 0,0 bottom left and 1,1 top right.
		 * The zoom is -zoom to + zoom on the workspace, so we need * 2 the
		 * zoom. This is becuase is counts from -zoom to +zoom which is zoom*2
		 * units. We then subtract the zoom so make the number be scaled
		 * properly into the workspace. with 0,0 in the middle. Ex. ratioTouch =
		 * .5,.5 We want this to be 0,0 in a zoom of 1 (.5 * (1*2)) - 1 = 0,0
		 * Ex. ratioTouch = 1,1, We want this to be 2,2 in a zoom of 2 (1 *
		 * (2*2)) - 2 = 2,2 Ex. ratioTouch = .25,.25 We want this to be -.5,-.5
		 * in a zoom of 1 (.25 * (1*2)) - 1 = -.5,-.5 We also need to
		 * incorporate the aspect ratio here. We also need to add or subtract
		 * the offset for scrolling the workspace.
		 */

		float aspectRatio = WorkSpaceState.getInstance().getAspectRatio();
		float zoom = WorkSpaceState.getInstance().getZoom();

		// Magic 2 makes it work because the zoom is -min and max which makes it
		// double since it counts from - to +
		float xRatioWithZoom = (ratioToScreenX * (zoom * 2)) - (zoom);
		float yRatioWithZoom = (ratioToScreenY * (zoom * 2)) - (zoom);

		float ratioToScreenWithAspectX = (xRatioWithZoom * (aspectRatio)); // The
																			// two
																			// is
																			// because
																			// we
																			// split
																			// the
																			// screen
																			// at
																			// 0;

		/**
		 * Put the x,y offset here. It will just be to - or + the offset to the
		 * ratio
		 */

		float ratioWithOffsetX = ratioToScreenWithAspectX + WorkSpaceState.getInstance().getOffset()[0];
		float ratioWithOffsetY = yRatioWithZoom - WorkSpaceState.getInstance().getOffset()[1];

		// The Y is inverted for this project in the server end of things
		return new float[] { ratioWithOffsetX, -ratioWithOffsetY };
	}

	public static float scaleZoomGesture(float gestureX1, float gestureY1, float gestureX2, float gestureY2, float zoom, float maxZoom) {
		float xDiff = gestureX1 - gestureX2;
		float yDiff = gestureY1 - gestureY2;

		float averageDiff = (xDiff + yDiff) / 2; // Divide by two to get the
													// average.

		return averageDiff / ZOOM_FACTOR; // Magic number is best number.

	}
}
