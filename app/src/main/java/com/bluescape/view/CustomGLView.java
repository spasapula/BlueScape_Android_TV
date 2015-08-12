package com.bluescape.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.collaboration.socket.sender.HeTsxAppEventGeometryChangedMessageSender;
import com.bluescape.gesture.GestureHandler;
import com.bluescape.gesture.GestureListener;
import com.bluescape.gesture.ScaleListener;
import com.bluescape.model.Group;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.BrowserModel;
import com.bluescape.model.widget.MoveModel;

public class CustomGLView extends GLSurfaceView implements View.OnTouchListener {
	private static final String TAG = "CustomGLView";

	// Attr from xml
	private int mBackGroundColor;
	private int mShapeColor;
	private int mSize;
	int TRACKBALL_SCALE = 10;

	int check=0;
	private float mCurX;
	private float mCurY;
	View trackview;
	// Is renderer set
	// private boolean rendererSet = false;

	// Gesture detector for detecting stuffs
	private GestureDetector mGestureDetector;

	// Gesture detector for scaling motions
	private ScaleGestureDetector mScaleDetector;
	MotionEvent trackmotionevent;
	private Context mContext;
	private final WorkSpaceState wss = WorkSpaceState.getInstance();
	// This is not called
	public CustomGLView(Context context) {
		super(context);

		this.mContext = context;

		// Need to set colors first
		mBackGroundColor = Color.DKGRAY;
		mShapeColor = Color.CYAN;
		mSize = 24;

		// Init gdt
		mGestureDetector = new GestureDetector(getContext(), new GestureListener(context));

		// Add ourselves to the dataholder
		WorkSpaceState.getInstance().setWorkspaceView(this);
		setupOpenGL();
	}

	// This is called
	public CustomGLView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Get the attr from the xml
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomGLView, 0, 0);
		try {
			mBackGroundColor = a.getColor(R.styleable.CustomGLView_color_bg, Color.DKGRAY);
			// boolean mIsFullScreen =
			// a.getBoolean(R.styleable.CustomGLView_full_screen, false);
			mSize = a.getInteger(R.styleable.CustomGLView_size, 24);
			mShapeColor = a.getColor(R.styleable.CustomGLView_color_shape, Color.BLUE);
		} finally {
			a.recycle();
		}

		// Gesture
		mGestureDetector = new GestureDetector(getContext(), new GestureListener(mContext));

		// Scale detector
		mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener(context));

		// Add ourselves to the dataholder
		WorkSpaceState.getInstance().setWorkspaceView(this);
		WorkSpaceState.getInstance().setCurrentSelectedColor(mShapeColor);

		setupOpenGL();
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/**
	 * Standard Android Action Codes
	 * http://developer.android.com/reference/android
	 * /view/MotionEvent.html#ACTION_DOWN ACTION_DOWN = 0 ACTION_UP = 1
	 * ACTION_MOVE = 2 ACTION_CANCEL = 3 ACTION_POINTER_DOWN = 5
	 * ACTION_POINTER_UP = 6 ACTION_POINTER_2_DOWN = 261 e.g for scale [0, 1, 2
	 * x 4, 5, 6 x 2, 261] [0, 1, 2 x 27, 6, 261] 2 finger [0, 1, 2 x 3, 6, 261]
	 */

	@Override
	public boolean onTrackballEvent(MotionEvent event) {

		Log.i("TouchPaint", "Trackball: x=" + event.getAction());

		// return true;


		int fingers = event.getPointerCount();

		if(fingers>0) {

			trackmotionevent = event;
			if (wss.getCurrentTool() == AppConstants.TOOL_ERASER || wss.getCurrentTool() == AppConstants.TOOL_STROKE) {
				TRACKBALL_SCALE = 1;
			} else {
				TRACKBALL_SCALE = 10;
			}

			int N = event.getHistorySize();

			final float scaleX = event.getXPrecision() * TRACKBALL_SCALE;
			final float scaleY = event.getYPrecision() * TRACKBALL_SCALE;
			for (int i = 0; i < N; i++) {
				mCurX += event.getHistoricalX(i) * scaleX;
				mCurY += event.getHistoricalY(i) * scaleY;
				// drawPoint(mCurX, mCurY, 1.0f, 16.0f);
			}
			if (N <= 0) {
				mCurX += event.getX() * scaleX;
				mCurY += event.getY() * scaleY;
			}
			Log.i("TouchPaint", "Trackball: x=" + mCurX
					+ ", y=" + mCurY);

			GestureHandler.getInstance().mGestureMotionEventHistory.put(event.getAction(), event);
			event.setLocation(mCurX, mCurY);

			mScaleDetector.onTouchEvent(event);
			// Send the event out to the gesture detector
			mGestureDetector.onTouchEvent(event);
			// To draw the line

//        if(x>0){
//            width/2*x+width/2;
//        }if(x=0){
//            width/2;
//            320/2=160;
//        }if(x<0){
//            width*x/2-width/2
//        }
//        final int action = event.getActionMasked();
//        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
//            final float X = event.getXPrecision() * TRACKBALL_SCALE;
//            final float Y = event.getYPrecision() * TRACKBALL_SCALE;
//            for (int i = 0; i < N; i++) {
//                moveTrackball(event.getHistoricalX(i) * X,
//                        event.getHistoricalY(i) * Y);
//            }
//            moveTrackball(event.getX()  X, event.getY()  Y);
//        }
//            if (wss.getCurrentTool() == AppConstants.TOOL_ERASER || wss.getCurrentTool() == AppConstants.TOOL_STROKE) {
//                if (mCurX < 0) {
//                    mCurX = 0;
//                }
//                if (mCurY < 0) {
//                    mCurY = 0;
//                }
//            if (mCurX > trackview.getWidth()) {
//                mCurX = trackview.getWidth();
//            }
//            if (mCurY > trackview.getHeight()) {
//                mCurY = trackview.getHeight();
//            }
//            }
			//if(mCurX>0&&mCurX<trackview.getWidth()&&mCurY>0&&mCurY<trackview.getHeight()) {
			if(check==0){
				check=1;
				GestureHandler.getInstance().setGesturePointerNumAndLocation(0, mCurX, mCurY);
				GestureHandler.getInstance().mXYPointerCoordsByIndex.put(0, new float[]{mCurX, mCurY});
				GestureHandler.getInstance().setFingersDown(0);
			}else{
				GestureHandler.getInstance().setGesturePointerNumAndLocation(1, mCurX, mCurY);
				GestureHandler.getInstance().mXYPointerCoordsByIndex.put(1, new float[]{mCurX, mCurY});
				GestureHandler.getInstance().setFingersDown(1);

			}
			GestureHandler.getInstance().drawFromGestureMotionEventHistory();
			// }
			final int action = event.getActionMasked();
			if (action == MotionEvent.ACTION_CANCEL) {
				Log.i("TouchPaint", "ACTION_MOVE");
				mCurX = 0;
				mCurY = 0;
			}
			//drawPoint(mCurX, mCurY, 1.0f, 16.0f);
		}else{
			Log.i("trackball elset", "trackball elseeeeeeeeeeeeeeeeeeee");
		}
		return true;

	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		trackview = view;
		int fingers = motionEvent.getPointerCount();
		GestureHandler.getInstance().setFingersDown(fingers);
		AppConstants.LOG(AppConstants.VERBOSE, TAG, "motionEvent onTouch motionEvent: " + motionEvent);
		AppConstants.LOG(AppConstants.VERBOSE, TAG, "motionEvent Type: " + motionEvent.getAction());

		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			// AppConstants.LOG(AppConstants.CRITICAL, TAG,
			// "Motion is ACTION_DOWN so resetting : GestureHandler.getInstance().resetGestureHandler() action code"
			// + motionEvent.getAction());

			GestureHandler.getInstance().resetGestureHandler();
		}

		GestureHandler.getInstance().mGestureMotionEventHistory.put(motionEvent.getAction(), motionEvent);
		// AppConstants.LOG(AppConstants.VERBOSE, TAG,
		// "mGestureMotionEventHistory: " +
		// GestureHandler.getInstance().mGestureMotionEventHistory);
		// AppConstants.LOG(AppConstants.VERBOSE, TAG,
		// "mGestureMotionEventHistory Keys : " +
		// GestureHandler.getInstance().mGestureMotionEventHistory.keys().size()
		// + "GestureHandler.getInstance().mGestureMotionEventHistory.keys() : "
		// + GestureHandler.getInstance().mGestureMotionEventHistory.keys());
		// AppConstants.LOG(AppConstants.VERBOSE, TAG,
		// "mGestureMotionEventHistory Values: " +
		// GestureHandler.getInstance().mGestureMotionEventHistory.values().size()
		// +
		// "GestureHandler.getInstance().mGestureMotionEventHistory.values() : "
		// + GestureHandler.getInstance().mGestureMotionEventHistory.values());

		for (int i = 0; i < fingers; i++) {

			MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();

			motionEvent.getPointerCoords(i, coords);

			// AppConstants.LOG(AppConstants.VERBOSE, TAG,
			// String.format("Setting Pointer: %d Coords %f:%f", i , coords.x,
			// coords.y));
			GestureHandler.getInstance().setGesturePointerNumAndLocation(i, coords.x, coords.y);
		}

		// new pointerXY coordinates map
		for (int i = 0; i < fingers; i++) {

			MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();

			motionEvent.getPointerCoords(i, coords);

			// AppConstants.LOG(AppConstants.VERBOSE, TAG,
			// String.format("Setting mXYPointerCoordsByIndex Pointer: %d Coords %f:%f",
			// i , coords.x, coords.y));

			// Put adds to the end of List for the list in the multimap for the
			// specific key
			GestureHandler.getInstance().mXYPointerCoordsByIndex.put(i, new float[] { coords.x, coords.y });
		}

		// AppConstants.LOG(AppConstants.VERBOSE, TAG,
		// "mXYPointerCoordsByIndex Keys: " +
		// GestureHandler.getInstance().mXYPointerCoordsByIndex.keys().size() +
		// "GestureHandler.getInstance().mXYPointerCoordsByIndex.keys() : "
		// +GestureHandler.getInstance().mXYPointerCoordsByIndex.keys());

		// AppConstants.LOG(AppConstants.VERBOSE, TAG,
		// "mXYPointerCoordsByIndex Values: " +
		// GestureHandler.getInstance().mXYPointerCoordsByIndex.values().size()
		// + "GestureHandler.getInstance().mXYPointerCoordsByIndex.values() : "
		// + GestureHandler.getInstance().mXYPointerCoordsByIndex.values());

		GestureHandler.getInstance().setFingersDown(fingers);

		// Check to see if we are scaling
		mScaleDetector.onTouchEvent(motionEvent);

		// Send the event out to the gesture detector
		mGestureDetector.onTouchEvent(motionEvent);

		if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
			// AppConstants.LOG(AppConstants.CRITICAL, TAG,
			// "Motion is ACTION_UP so printing  : GestureHandler.getInstance().printMGestureMotionEventHistory() action code"
			// + motionEvent.getAction());

			// GestureHandler.getInstance().printGestureMotionEventHistory();
			GestureHandler.getInstance().drawFromGestureMotionEventHistory();

			if(GestureHandler.getInstance().didWidgetMoveHappen()){
				//Send the He Message for Card, Browser etc.
                //This gets called even on End of OnScale as scroll also happens on scale
				MoveModel moveModel = WorkSpaceState.getInstance().mLastMoveModel;
				if (moveModel != null) {
					BaseWidgetModel baseWidgetModel = WorkSpaceState.getInstance().getModelTree().getModel(moveModel.getModelID());


					//Check the type of BaseWidgetModel if BrowserModel then send TsxAppEvent for everything else just send a PositionChange like before
					if(baseWidgetModel instanceof BrowserModel) {

						HeTsxAppEventGeometryChangedMessageSender heTsxAppEventGeometryChangedMessageSender = new HeTsxAppEventGeometryChangedMessageSender(baseWidgetModel);
						heTsxAppEventGeometryChangedMessageSender.send();

					} else {

						//check if it is a Group Move and set the Move Model Appropriately
						if (baseWidgetModel instanceof Group) {
							//No need to change or special handling as the he should have the same as the last ve
						}
							//Send Single He Message
							moveModel.sendHeToWSServer();

					}

				} else {
					AppConstants.LOG(AppConstants.VERBOSE, TAG, "Null moveModel from MoveModel.fromJSON");
				}
			}
		}

		return true;
	}

	private void setupOpenGL() {

		// Set the context
		setEGLContextClientVersion(2);

		// Assign our renderer.
		setFocusableInTouchMode(true);

		// Initialize the renderer
		com.bluescape.view.renderers.Renderer mRenderer = new com.bluescape.view.renderers.Renderer(this.getContext(), mBackGroundColor);

		setEGLConfigChooser(8 , 8, 8, 8, 16, 0);
		// Add renderer to the dataholder
		WorkSpaceState.getInstance().setRenderer(mRenderer);

		// Set the Renderer for drawing on the GLSurfaceView
		setRenderer(mRenderer);

		// boolean to see if Renderer is set in CustomGLView this is checked in
		// the the onPause and onResume of MainActivity in case notecard add
		// etc. are launched so we
		// dont have to recreate the CustomGLView every time
		WorkSpaceState.getInstance().mIsRendererSet = true;

		// Let use interact with view
		setFocusable(true);

		// Set the onTouchListener
		setOnTouchListener(this);
	}

}
