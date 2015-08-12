package com.bluescape.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.activity.NoteBuilderActivity;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.ImageModel;
import com.bluescape.model.widget.NoteModel;
import com.bluescape.model.widget.StrokeModel;
import com.bluescape.model.template.NoteTemplate;
import com.bluescape.view.shaders.TextureHelper;

import java.util.ArrayList;
import java.util.List;

public class CustomNoteCardView extends View {

	private static final String TAG = CustomNoteCardView.class.getSimpleName();

	private static Canvas mCanvas = new Canvas(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888));

	private static boolean mEraseMode = false;

	// List of current New Strokes added in this edit or add session This will
	// be populated on
	// touch_up and then iterated on every onDraw
	// private ArrayList<StrokeModel> mCurrentNewStrokes = new
	// ArrayList<StrokeModel>(); ;

	// Use the mRecentlyAddedStrokes in NoteModel instead of a new variable

	private static int mScreenWidth = 700;
	private static int mScreenHeight = 400;
	/**
	 * List of List of points. the points are interleaved with x,y and its a
	 * list of points.
	 */
	private final List<List<Float>> mPointList = new ArrayList<>();
	// Array to store the stroke x,y and send once the ACTION_UP is detected for
	// current stroke
	// private ArrayList<Float> mStrokeLocsArray = new ArrayList<Float>();
	private ArrayList<Float> mStrokeLocsArray;

	private Paint mPaint;
	private Path mPath;
	private Bitmap mBackground;

	// mModel and mDrawable should be retrieved from the getContext Call for
	// cleaner implementation
	private NoteBuilderActivity mNoteBuilderActivityContext;

	// Ideally Access these from mNoteBuilderActivityContext as they are already
	// defined there
	// Have the reference ot the Drawable for ease of use
	private NoteModel mModel;

	private ImageModel mImageModel;

	/**
	 * Get the current tool we have selected.
	 */
	// Have a Stroke Selection tool local to CustomNoteCardView and leave the
	// WorkspaceState for MainActivity
	public int mCurrentTool = AppConstants.TOOL_NO_SHAPE;

	// Have a Stroke Color Selection tool local to CustomNoteCardView and leave
	// the WorkspaceState for MainActivity

	// Toolbar selected color
	public float[] mSelectedStrokeColor = AppConstants.StrokeColor.BRUSH_COLOR_LIGHT_BLUE;

	public CustomNoteCardView(Context c) {
		super(c);
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "In Constructor CustomNoteCardView(Context c) ");
		mPath = new Path();
		mPaint = new Paint(Paint.DITHER_FLAG);
		mPaint.setStrokeWidth(WorkSpaceState.getInstance().getCurrentLineWidth());
	}

	// This is called from NoteBuilderActivity edit and add
	public CustomNoteCardView(Context c, AttributeSet a) {
		super(c, a);
		mNoteBuilderActivityContext = (NoteBuilderActivity) getContext();
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "In Constructor CustomNoteCardView(Context c, AttributeSet a)  setting STROKE Paint Attributes");
		mPath = new Path();
		mPaint = new Paint(Paint.DITHER_FLAG);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(6f);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
	}

	public CustomNoteCardView(Context c, AttributeSet a, int d) {
		super(c, a, d);
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "In Constructor  CustomNoteCardView(Context c, AttributeSet a, int d) ");
		mPath = new Path();
		mPaint = new Paint(Paint.DITHER_FLAG);
		mPaint.setStrokeWidth(WorkSpaceState.getInstance().getCurrentLineWidth());
	}

	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touch_start(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			touch_move(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			touch_up();
			invalidate();
			break;
		}

		return true;
	}

	// This is called for New Card create ONLY not for edit
	public void setTemplate(NoteTemplate template) {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "in CustomNoteCardView.setTemplate(NoteTemplate template) ");

		// this.mTemplate = template;

		// setupScreen(template);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "In onDraw(Canvas canvas)");

		mCanvas = canvas;
		if (mBackground != null) {
			// canvas.drawBitmap(mBackground, 0, 0, mPaint); // Magic numbers,
			// we should figure out the sizes here
			canvas.drawBitmap(mBackground, 0, 0, null); // Magic numbers, we
														// should figure out the
														// sizes here

		}

		// Draw the existing strokes on the card especially for edit card
		if (!mNoteBuilderActivityContext.mIsNew) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "In onDraw(Canvas canvas) NOT NEW SO DRAWING EXISTING STROKES");

			drawExistingStrokes();
		} else {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "In onDraw(Canvas canvas)  NEW SO NOT DRAWING EXISTING STROKES");

		}

		// New Strokes could be added to bot hadd and edit
		drawRecentlyAddedStrokes();

		canvas.drawPath(mPath, mPaint);
	}

	// Overriden method to get the exact width and height of the window so the
	// screen can adjusted in the right proportions
	// for mapping ot the 560 x 320 card dimensions
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		AppConstants.LOG(AppConstants.CRITICAL, TAG, "In onMeasure(..) ");

		int desiredWidth = 560;
		int desiredHeight = 320;

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		// Measure Width
		if (widthMode == MeasureSpec.EXACTLY) {
			// Must be this size
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			// Can't be bigger than...
			width = Math.min(desiredWidth, widthSize);
		} else {
			// Be whatever you want
			width = desiredWidth;
		}

		// Measure Height
		if (heightMode == MeasureSpec.EXACTLY) {
			// Must be this size
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			// Can't be bigger than...
			height = Math.min(desiredHeight, heightSize);
		} else {
			// Be whatever you want
			height = desiredHeight;
		}

		AppConstants.LOG(AppConstants.CRITICAL, TAG, "In onMeasure(..) Final " + " width=" + width + " height=" + height);

		// Set to the right screen dimensions in our view hd screens are higer
		// values like 933 53
		mScreenWidth = width;
		mScreenHeight = height;
		// MUST CALL THIS
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (mNoteBuilderActivityContext.mIsCard) {
			// Differentiate between edit and add
			// set the drawable and the model
			mModel = mNoteBuilderActivityContext.mModel;
			// Get bitmap for template, we need to set bitmap after the template
			mNoteBuilderActivityContext.mBgBitmap = TextureHelper.convertResourceToBitmap(R.drawable.transperent);
			setBackground(mNoteBuilderActivityContext.mBgBitmap);
			if (!mNoteBuilderActivityContext.mIsNew) {
				// This is set in wss in gestureHandler on double tap

				AppConstants.LOG(AppConstants.CRITICAL, TAG, "EDIT ??  mModel != null In onSizeChanged(int w, int h, int oldw, int oldh)" + " w:" + w
																+ " h:" + h + " oldw:" + oldw + " oldh:" + oldh);

				AppConstants.LOG(AppConstants.CRITICAL, TAG, " Before  setupScreen() " + " mScreenWidth:" + mScreenWidth + " mScreenHeight:"
																+ mScreenHeight + " w:" + w + " h:" + h + " oldw:" + oldw + " oldh:" + oldh);

				AppConstants.LOG(AppConstants.CRITICAL, TAG, " After  setupScreen() " + " mScreenWidth:" + mScreenWidth + " mScreenHeight:"
																+ mScreenHeight + " w:" + w + " h:" + h + " oldw:" + oldw + " oldh:" + oldh);
				super.onSizeChanged(mScreenWidth, mScreenHeight, oldw, oldh);
			} else {

				AppConstants.LOG(AppConstants.CRITICAL, TAG, "ADD ?? else  mModel == null In onSizeChanged(int w, int h, int oldw, int oldh)" + " w:"
																+ w + " h:" + h + " oldw:" + oldw + " oldh:" + oldh);

				super.onSizeChanged(mScreenWidth, mScreenHeight, oldw, oldh);
			}
		} else {

			mImageModel = mNoteBuilderActivityContext.mImageModel;
			mNoteBuilderActivityContext.mBgBitmap = TextureHelper.convertResourceToBitmap(R.drawable.transperent);

			setBackground(mNoteBuilderActivityContext.mBgBitmap);
			super.onSizeChanged(mScreenWidth, mScreenHeight, oldw, oldh);
		}

	}

	private void drawExistingStrokes() {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "In drawExistingStrokes() ");

		if (mNoteBuilderActivityContext.mIsCard) {
			if (mModel.mExistingStrokes.size() > 0) {

				AppConstants.LOG(AppConstants.CRITICAL, TAG, "EDIT Card Strokes this.mDrawable.getAllChildren().size()  =  "
																+ mModel.mExistingStrokes.size());
				AppConstants.LOG(AppConstants.CRITICAL, TAG, "EDIT Card Strokes WorkSpaceState.getInstance().mNoteModel.mExistingStrokes.size()  =  "
																+ WorkSpaceState.getInstance().mNoteModel.mExistingStrokes.size());

				AppConstants.LOG(AppConstants.CRITICAL, TAG, "EDIT Card Strokes this.mDrawable getArrayLocs  =  "
																+ mModel.mExistingStrokes.get(0).getArrayLocs().toString());
				AppConstants.LOG(AppConstants.CRITICAL, TAG, "EDIT Card Strokes WorkSpaceState.getInstance().mNoteModel getArrayLocs  =  "
																+ WorkSpaceState.getInstance().mNoteModel.mExistingStrokes.get(0).getArrayLocs());

				// Set the color from the color class
				// http://developer.android.com/reference/android/graphics/Color.html#rgb(int,
				// int, int)
				/**
				 * Return a color-int from red, green, blue components. The
				 * alpha component is implicity 255 (fully opaque). These
				 * component values should be [0..255], but there is no range
				 * check performed, so if they are out of range, the returned
				 * color is undefined.
				 * 
				 * @param red
				 *            Red component [0..255] of the color
				 * @param green
				 *            Green component [0..255] of the color
				 * @param blue
				 *            Blue component [0..255] of the color
				 */
				// public static int rgb(int red, int green, int blue) {
				// return (0xFF << 24) | (red << 16) | (green << 8) | blue;
				// }

				// Add the mPath.moveto and mPath.Draw commands for the x,y
				// co-ordinates here for every stroke on card
				for (int strokesCounter = 0; strokesCounter < mModel.mExistingStrokes.size(); strokesCounter++) {
					// for (int strokesCounter = 0; strokesCounter <
					// this.mDrawable.getAllChildren().size(); strokesCounter++)
					// {

					// SEtup new PAths and Paints for each stroke and draw it on
					// canvas and leave mPath and mPaint for
					// new strokes
					Path currentPath = new Path();
					Paint currentPaint = new Paint(Paint.DITHER_FLAG);

					currentPaint.setAntiAlias(true);
					currentPaint.setStrokeWidth(6f);
					currentPaint.setStyle(Paint.Style.STROKE);
					currentPaint.setStrokeJoin(Paint.Join.ROUND);

					// invalidate();
					// StrokeModel strokeModel = (StrokeModel)
					// this.mDrawable.getAllChildren().get(strokesCounter).getModel();
					StrokeModel strokeModel = mModel.mExistingStrokes.get(strokesCounter);

					currentPaint.setColor(Color.rgb((int) strokeModel.getColor()[0], (int) strokeModel.getColor()[1], (int) strokeModel.getColor()[2]));
					// AppConstants.LOG(AppConstants.CRITICAL, TAG,
					// "In drawExistingStrokes() setting Color:" +
					// Color.rgb(strokeModel.getColor()[0], strokeModel.getColor()[1],
					// strokeModel.getColor()[2]) + "r: " + strokeModel.getColor()[0] +
					// "g: " + strokeModel.getColor()[1] + "b: " +
					// strokeModel.getColor()[2]);

					ArrayList<Float> mStrokeArrayLocs = strokeModel.getArrayLocs();
					// currentPath.moveTo(mStrokeArrayLocs.get(0),
					// mStrokeArrayLocs.get(1));
					currentPath.moveTo(getScaledNoteCardX(mStrokeArrayLocs.get(0)), getScaledNoteCardY(mStrokeArrayLocs.get(1)));

					// invalidate();

					for (int i = 2; i < mStrokeArrayLocs.size(); i += 2) {
						// mNoteView.mPath.lineTo(x, y);
						// currentPath.lineTo(mStrokeArrayLocs.get(i),
						// mStrokeArrayLocs.get(i + 1));
						currentPath.lineTo(getScaledNoteCardX(mStrokeArrayLocs.get(i)), getScaledNoteCardY(mStrokeArrayLocs.get(i + 1)));

						// invalidate();

					}
					mCanvas.drawPath(currentPath, currentPaint);
					// invalidate();

				}

			}
		} else {
			// Add the mPath.moveto and mPath.Draw commands for the x,y
			// co-ordinates here for every stroke on card
			for (int strokesCounter = 0; strokesCounter < mImageModel.mExistingStrokes.size(); strokesCounter++) {
				// for (int strokesCounter = 0; strokesCounter <
				// this.mDrawable.getAllChildren().size(); strokesCounter++) {

				// SEtup new PAths and Paints for each stroke and draw it on
				// canvas and leave mPath and mPaint for
				// new strokes
				Path currentPath = new Path();
				Paint currentPaint = new Paint(Paint.DITHER_FLAG);

				currentPaint.setAntiAlias(true);
				currentPaint.setStrokeWidth(6f);
				currentPaint.setStyle(Paint.Style.STROKE);
				currentPaint.setStrokeJoin(Paint.Join.ROUND);

				// invalidate();
				// StrokeModel strokeModel = (StrokeModel)
				// this.mDrawable.getAllChildren().get(strokesCounter).getModel();
				StrokeModel strokeModel = mImageModel.mExistingStrokes.get(strokesCounter);

				currentPaint.setColor(Color.rgb((int) strokeModel.getColor()[0], (int) strokeModel.getColor()[1], (int) strokeModel.getColor()[2]));
				// AppConstants.LOG(AppConstants.CRITICAL, TAG,
				// "In drawExistingStrokes() setting Color:" +
				// Color.rgb(strokeModel.getColor()[0], strokeModel.getColor()[1],
				// strokeModel.getColor()[2]) + "r: " + strokeModel.getColor()[0] +
				// "g: " + strokeModel.getColor()[1] + "b: " +
				// strokeModel.getColor()[2]);

				ArrayList<Float> mStrokeArrayLocs = strokeModel.getArrayLocs();
				// currentPath.moveTo(mStrokeArrayLocs.get(0),
				// mStrokeArrayLocs.get(1));
				currentPath.moveTo(getScaledNoteCardX(mStrokeArrayLocs.get(0)), getScaledNoteCardY(mStrokeArrayLocs.get(1)));

				// invalidate();

				for (int i = 2; i < mStrokeArrayLocs.size(); i += 2) {
					// mNoteView.mPath.lineTo(x, y);
					// currentPath.lineTo(mStrokeArrayLocs.get(i),
					// mStrokeArrayLocs.get(i + 1));
					currentPath.lineTo(getScaledNoteCardX(mStrokeArrayLocs.get(i)), getScaledNoteCardY(mStrokeArrayLocs.get(i + 1)));

					// invalidate();

				}
				mCanvas.drawPath(currentPath, currentPaint);
				// invalidate();

			}
		}

	}

	private void drawRecentlyAddedStrokes() {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "In drawRecentlyAddedStrokes() ");
		if (mNoteBuilderActivityContext.mIsCard) {

			if (WorkSpaceState.getInstance().mNoteModel != null && WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes != null
				&& WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.size() > 0) {

				AppConstants.LOG(AppConstants.CRITICAL, TAG, "EDIT Card Strokes WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes =  "
																+ WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.size());

				AppConstants.LOG(
					AppConstants.CRITICAL,
					TAG,
					"EDIT Card Strokes WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.get(0).getArrayLocs()  =  "
							+ WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.get(0).getArrayLocs().toString());

				// Add the mPath.moveto and mPath.Draw commands for the x,y
				// co-ordinates here for every stroke on card
				for (int strokesCounter = 0; strokesCounter < WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.size(); strokesCounter++) {

					// SEtup new PAths and Paints for each stroke and draw it on
					// canvas and leave mPath and mPaint for
					// new strokes
					Path currentPath = new Path();
					Paint currentPaint = new Paint(Paint.DITHER_FLAG);

					currentPaint.setAntiAlias(true);
					currentPaint.setStrokeWidth(6f);
					currentPaint.setStyle(Paint.Style.STROKE);
					currentPaint.setStrokeJoin(Paint.Join.ROUND);

					// invalidate();
					StrokeModel strokeModel = WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.get(strokesCounter);
					currentPaint.setColor(Color.rgb((int) strokeModel.getColor()[0], (int) strokeModel.getColor()[1], (int) strokeModel.getColor()[2]));
					// AppConstants.LOG(AppConstants.CRITICAL, TAG,
					// "In drawExistingStrokes() setting Color:" +
					// Color.rgb(strokeModel.getColor()[0], strokeModel.getColor()[1],
					// strokeModel.getColor()[2]) + "r: " + strokeModel.getColor()[0] +
					// "g: " + strokeModel.getColor()[1] + "b: " +
					// strokeModel.getColor()[2]);

					ArrayList<Float> mStrokeArrayLocs = strokeModel.getArrayLocs();
					// currentPath.moveTo(mStrokeArrayLocs.get(0),
					// mStrokeArrayLocs.get(1));
					currentPath.moveTo(getScaledNoteCardX(mStrokeArrayLocs.get(0)), getScaledNoteCardY(mStrokeArrayLocs.get(1)));

					// invalidate();

					for (int i = 2; i < mStrokeArrayLocs.size(); i += 2) {
						// mNoteView.mPath.lineTo(x, y);
						// currentPath.lineTo(mStrokeArrayLocs.get(i),
						// mStrokeArrayLocs.get(i + 1));
						currentPath.lineTo(getScaledNoteCardX(mStrokeArrayLocs.get(i)), getScaledNoteCardY(mStrokeArrayLocs.get(i + 1)));

						// invalidate();

					}
					mCanvas.drawPath(currentPath, currentPaint);
					// invalidate();

				}

			}
		} else {
			if (WorkSpaceState.getInstance().mImageModel != null && WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes != null
				&& WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes.size() > 0) {

				AppConstants.LOG(AppConstants.CRITICAL, TAG, "EDIT Card Strokes WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes =  "
																+ WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes.size());

				AppConstants.LOG(
					AppConstants.CRITICAL,
					TAG,
					"EDIT Card Strokes WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes.get(0).getArrayLocs()  =  "
							+ WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes.get(0).getArrayLocs().toString());

				// Add the mPath.moveto and mPath.Draw commands for the x,y
				// co-ordinates here for every stroke on card
				for (int strokesCounter = 0; strokesCounter < WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes.size(); strokesCounter++) {

					// SEtup new PAths and Paints for each stroke and draw it on
					// canvas and leave mPath and mPaint for
					// new strokes
					Path currentPath = new Path();
					Paint currentPaint = new Paint(Paint.DITHER_FLAG);

					currentPaint.setAntiAlias(true);
					currentPaint.setStrokeWidth(6f);
					currentPaint.setStyle(Paint.Style.STROKE);
					currentPaint.setStrokeJoin(Paint.Join.ROUND);

					// invalidate();
					StrokeModel strokeModel = WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes.get(strokesCounter);
					currentPaint.setColor(Color.rgb((int) strokeModel.getColor()[0], (int) strokeModel.getColor()[1], (int) strokeModel.getColor()[2]));
					// AppConstants.LOG(AppConstants.CRITICAL, TAG,
					// "In drawExistingStrokes() setting Color:" +
					// Color.rgb(strokeModel.getColor()[0], strokeModel.getColor()[1],
					// strokeModel.getColor()[2]) + "r: " + strokeModel.getColor()[0] +
					// "g: " + strokeModel.getColor()[1] + "b: " +
					// strokeModel.getColor()[2]);

					ArrayList<Float> mStrokeArrayLocs = strokeModel.getArrayLocs();
					// currentPath.moveTo(mStrokeArrayLocs.get(0),
					// mStrokeArrayLocs.get(1));
					currentPath.moveTo(getScaledNoteCardX(mStrokeArrayLocs.get(0)), getScaledNoteCardY(mStrokeArrayLocs.get(1)));

					// invalidate();

					for (int i = 2; i < mStrokeArrayLocs.size(); i += 2) {
						// mNoteView.mPath.lineTo(x, y);
						// currentPath.lineTo(mStrokeArrayLocs.get(i),
						// mStrokeArrayLocs.get(i + 1));
						currentPath.lineTo(getScaledNoteCardX(mStrokeArrayLocs.get(i)), getScaledNoteCardY(mStrokeArrayLocs.get(i + 1)));

						// invalidate();

					}
					mCanvas.drawPath(currentPath, currentPaint);
					// invalidate();

				}

			}
		}

	}

	private float getScaledNoteCardX(float unscaledX) {
		// Note
		if (mNoteBuilderActivityContext.mIsCard) {
			return (unscaledX * (mScreenWidth / 560f));
		} else {
			// Image
			return (unscaledX * (mScreenWidth / mImageModel.getWidth()));
		}
	}

	private float getScaledNoteCardY(float unscaledY) {

		if (mNoteBuilderActivityContext.mIsCard) {
			return (unscaledY * (mScreenHeight / 320f));
		} else {
			return (unscaledY * (mScreenHeight / mImageModel.getHeight()));
		}
	}

	private float getUnScaledNoteCardX(float scaledX) {
		if (mNoteBuilderActivityContext.mIsCard) {
			return (scaledX * (560f / mScreenWidth));
		} else {
			return (scaledX * (mImageModel.getWidth() / mScreenWidth));
		}
	}

	private float getUnScaledNoteCardY(float scaledY) {
		if (mNoteBuilderActivityContext.mIsCard) {
			return (scaledY * (320f / mScreenHeight));
		} else {
			return (scaledY * (mImageModel.getHeight() / mScreenHeight));
		}
	}

	// This is called for Edit and Add card
	private void setBackground(Bitmap background) {
		// AppConstants.LOG(AppConstants.CRITICAL, TAG,
		// "in setBackground ScreenWidth mScreenHeight  " + mScreenWidth + " " +
		// mScreenHeight);
		mBackground = Bitmap.createScaledBitmap(background, mScreenWidth, mScreenHeight, true);
	}

	private void touch_move(float x, float y) {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "Touch moved x,y:" + x + ":" + y);

		if (mCurrentTool != AppConstants.TOOL_STROKE) return;
		if (y > 10 && y < mScreenHeight) {
			// Add to the current Stroke Array
			mStrokeLocsArray.add(getUnScaledNoteCardX(x));
			mStrokeLocsArray.add(getUnScaledNoteCardY(y));
			mPath.lineTo(x, y);
		}

	}

	private void touch_start(float x, float y) {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "Touch Started  x,y:" + x + ":" + y);

		if (mCurrentTool != AppConstants.TOOL_STROKE) return;

		// Start a new paint as we dont want the previous strokes in same edit
		// to change to the new color every time.
		mPath = new Path();
		mPaint = new Paint(Paint.DITHER_FLAG);

		mPaint.setColor(Color.rgb((int) mSelectedStrokeColor[0], (int) mSelectedStrokeColor[1], (int) mSelectedStrokeColor[2]));
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(6f);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);

		// Set up a new strokeArray for every new stroke down
		mStrokeLocsArray = new ArrayList<>();
		mStrokeLocsArray.add(getUnScaledNoteCardX(x));
		mStrokeLocsArray.add(getUnScaledNoteCardY(y));

		mPath.moveTo(x, y);
	}

	private void touch_up() {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "Touch up");

		if (mCurrentTool != AppConstants.TOOL_STROKE) return;

		// Adding the Stroke Model
		StrokeModel strokeModel = new StrokeModel(mStrokeLocsArray, mSelectedStrokeColor);

		// Code from Model Tree to add to Workspace to show on screen
		AppConstants.LOG(AppConstants.VERBOSE, TAG, "ADDING Temporary StrokeModel");

		if (mNoteBuilderActivityContext.mIsCard) {
			strokeModel.setTargetID(WorkSpaceState.getInstance().mNoteModel.getID());

			if (WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes == null) {
				// WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.add(strokeModel);
				WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes = new ArrayList<>(10);
			}

			WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.add(strokeModel);

		} else {
			strokeModel.setTargetID(WorkSpaceState.getInstance().mImageModel.getID());

			if (WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes == null) {
				// WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.add(strokeModel);
				WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes = new ArrayList<>(10);
			}

			WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes.add(strokeModel);
		}
	}
}
