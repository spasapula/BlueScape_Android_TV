package com.bluescape.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.bluescape.AppConstants;
import com.bluescape.activity.NoteBuilderActivity;
import com.bluescape.collaboration.util.GetPDFURLTask;
import com.bluescape.model.Group;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.BrowserModel;
import com.bluescape.model.widget.ImageModel;
import com.bluescape.model.widget.MoveModel;
import com.bluescape.model.widget.NoteModel;
import com.bluescape.model.widget.PDFModel;
import com.bluescape.model.widget.StrokeModel;
import com.bluescape.model.widget.TexturedWidgetModel;
import com.bluescape.view.ViewUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.List;


public class GestureHandler {

    /**
     * Instance holder for our safe lazy instantiation pattern
     * https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
     */
    private static class instanceHolder {
        private static final GestureHandler INSTANCE = new GestureHandler();
    }

    private static final String TAG = "GestureHandler";

    /**
     * Gesture Hierarchy to ignore i.e if Scale has begun then ignore the scroll
     * events as while the scaling is going on you will get the touch //Ignore
     * Scroll and Fling if Scale Starts // Ignore Fling if you started onScroll
     */
    private static boolean mIgnoreOnScale = false;
    private static boolean mIgnoreOnScroll = false;
    private static boolean mIgnoreOnFling = false;

    private static boolean mIgnoreOnDown = false;
    private static boolean mWorkspaceOrWidgetScaleOrScrollStarted = false;

    //Boolean to send he event to server for widget move
    private static boolean mDidWidgetMoveHappen = false;

    /**
     * Returns the singleton
     *
     * @return The GestureHandler singleton reference
     */
    public static GestureHandler getInstance() {
        return instanceHolder.INSTANCE;
    }

    // Dataholder
    private final WorkSpaceState wss = WorkSpaceState.getInstance();

    // Maintain state of gestures
    // private int mLastCommand = NONE;
    private float mPreviousScale = 0.0f;

    private float mPreviousSpanX = 0.0f;

    private float mPreviousSpanY = 0.0f;

    /**
     * Standard Android Action Codes
     * http://developer.android.com/reference/android
     * /view/MotionEvent.html#ACTION_DOWN ACTION_DOWN = 0 ACTION_UP = 1
     * ACTION_MOVE = 2 ACTION_CANCEL = 3 ACTION_POINTER_DOWN = 5
     * ACTION_POINTER_UP = 6 ACTION_POINTER_2_DOWN = 261 e.g for scale [0, 1, 2
     * x 4, 5, 6 x 2, 261] [0, 1, 2 x 27, 6, 261]
     */

    // Multimap to store the history of the Current Gesture starting at first
    // finger down ACTION_DOWN and ending at last finger up ACTION_UP. This will
    // have the entire history
    // of All MotionEvents for the current gesture
    public final ListMultimap<Integer, MotionEvent> mGestureMotionEventHistory = ArrayListMultimap.create();
    // Multimap to store the XY co-ords by Pointer index 0 for first finger 1
    // for second finger. for 0 key the first X,Y will be the XY recorded when
    // the ACTION_DOWN happens
    // This is a convenience MultiMap to replace mPointersToCoordsList which was
    // storing all pointers in the same array and confusing
    public final ListMultimap<Integer, float[]> mXYPointerCoordsByIndex = ArrayListMultimap.create();

    // Store XY co-ords of the first ACTION_DOWN and ACTION_POINTER DOWN to
    // check where the first pointers went down
    // Considering 2 finger only now will deal with 3 finger later
    private float mFinger1ActionDownXY[];
    private float mFinger2ActionPointerDownXY[];

    private BaseWidgetModel mFinger1BaseWidgetModel;

    private BaseWidgetModel mFinger2BaseWidgetModel;

    // boolean to store if we are scaling the workspace
    private boolean mScaleWorkSpace = true;

    // Array to store the stroke x,y and send once the ACTION_UP is detected
    private ArrayList<Float> mStrokeLocsArray = new ArrayList<>();
    /**
     * Maintian the state of the users fingers.
     */
    private int mFingersDown = 0;

    private final List<float[]> mPointersToCoordsList = new ArrayList<>();

    /**
     * Private constructor for our singleton pattern
     */
    private GestureHandler() {
    }

    public void decrementFingersDown() {
        this.mFingersDown--;
    }

    public void drawFromGestureMotionEventHistory() {
    Log.i("drawFromGestureMotionEventHistory","");
        // Draw Stroke if Stroke Tool is selected and there is no hint of a
        // scale, if scale happens then mIgnoreOnScroll is set to true
        if ((wss.getCurrentTool() == AppConstants.TOOL_STROKE || wss.getCurrentTool() == AppConstants.TOOL_ERASER) && !mIgnoreOnScroll) {
            if (mGestureMotionEventHistory.get(MotionEvent.ACTION_UP).size() > 0
                    || mGestureMotionEventHistory.get(MotionEvent.ACTION_CANCEL).size() > 0) {
                // send only if size of the mStrokeLocsArray is > 0 to stop from
                // having empty strokes
                // having exceptions with strokes that have just x,y
                if (mStrokeLocsArray.size() > 1) {
                    // int[] colorIntArray = {0, 182, 167, 1};
                    float[] colorIntArray = wss.getSelectedStrokeColor();

                    StrokeModel strokeModel = new StrokeModel(mStrokeLocsArray, colorIntArray);
                    // Set the target Id to workspace explicitly as we can also
                    // have strokes on cards that use the same send message
                    strokeModel.setTargetID(WorkSpaceState.getInstance().getWorkSpaceModel().getId());
                    Log.i("drawFromGesture", "onScroll" + "sd");
                    // Set right color and mLineWidth and brush for Eraser
                    if (wss.getCurrentTool() == AppConstants.TOOL_ERASER) {



                        strokeModel.setColor(AppConstants.EraseBrushColor.BRUSH_COLOR_ERASE_SEND);
                        strokeModel.setBrush(AppConstants.STROKE_BRUSH_ERASE);
                        strokeModel.reallyJustSetLineWidth(AppConstants.STROKE_BRUSH_ERASE_SIZE);
                    }

                    // Send first and then delete better ux
                    strokeModel.sendToWSServer();

                }
            }
        }

    }


    public void onDoubleTap(MotionEvent e) {

        // AppConstants.LOG(AppConstants.CRITICAL, TAG, "onDoubleTap Motion e ="
        // + e);
        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "onDoubleTap Motion e.getHistorySize =" + e.getHistorySize());

        // TODO krisgesture ignore for now until edit card etc.. is not enabled
        // so we dont get the wierd card edit by mistake
        // if (false) {
        float x, y;
        float scaled[] = ViewUtils.scaleTouchEvent(wss.getWorkspaceView(), e.getX(), e.getY());
        x = scaled[0];
        y = scaled[1];

        // Check to see if we intersect an object here and it's a card, then
        // edit it.
        BaseWidgetModel baseWidgetModel = wss.getModelTree().getIntersectingModel(x, y);

        if (baseWidgetModel != null && baseWidgetModel instanceof NoteModel) {
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "Doubletap on object");

            // Best is to set the WorkSpaceState.mNoteModel here itself before
            // launching NoteBuilderActivity
            // We can use this in the NoteBuilderActivity and CustomNoteCardView
            // for Add and Edit
            WorkSpaceState.getInstance().mNoteModel = (NoteModel) baseWidgetModel;
            Intent intent = new Intent(WorkSpaceState.getInstance().currentActivity, NoteBuilderActivity.class);

            Bundle bundle = new Bundle();
            bundle.putBoolean(NoteBuilderActivity.IS_CARD, true);
            bundle.putBoolean(NoteBuilderActivity.KEY_IS_NEW, false);
            bundle.putString(NoteBuilderActivity.KEY_OBJECT_ID, baseWidgetModel.getID());
            intent.putExtra(NoteBuilderActivity.KEY_BUNDLE_EXTRA, bundle);

            WorkSpaceState.getInstance().currentActivity.startActivityForResult(intent, NoteBuilderActivity.REQUEST_CODE);
        } else if (baseWidgetModel != null && baseWidgetModel instanceof PDFModel) {
            new GetPDFURLTask((PDFModel) baseWidgetModel).execute();
        } else if (baseWidgetModel != null && baseWidgetModel instanceof BrowserModel) {

            BrowserModel browserModel = (BrowserModel) baseWidgetModel;
            WorkSpaceState.getInstance().getWorkSpaceModel().getWorkspaceUpdateListener().openBrowser(browserModel.getPayload().getUrl());

        } else if (baseWidgetModel != null && baseWidgetModel instanceof ImageModel) {

            WorkSpaceState.getInstance().mImageModel = (ImageModel) baseWidgetModel;
            Intent intent = new Intent(WorkSpaceState.getInstance().currentActivity, NoteBuilderActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(NoteBuilderActivity.KEY_OBJECT_ID, baseWidgetModel.getID());
            bundle.putBoolean(NoteBuilderActivity.IS_CARD, false);
            intent.putExtra(NoteBuilderActivity.KEY_BUNDLE_EXTRA, bundle);
            WorkSpaceState.getInstance().currentActivity.startActivityForResult(intent, NoteBuilderActivity.REQUEST_CODE);

        }

    }

    public void onDoubleTapEvent(MotionEvent e) {

        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onDoubleTapEvent Motion e =" + e);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onDoubleTapEvent Motion e.getHistorySize =" + e.getHistorySize());
    }

    public void onDown(MotionEvent e) {

        // AppConstants.LOG(AppConstants.VERBOSE, TAG, "onDown Motion e =" + e);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onDown Motion e.getHistorySize =" + e.getHistorySize());
        if (mIgnoreOnDown) {
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "IGNORING onDown  mIgnoreOnDown == TRUE Motion e =" + e);
            return;
        }

        // Assume only one finger is down and take XY of that only ignoring the
        // second DOWN as that is handled by higher level onScale
        // Store XY co-ords of the first ACTION_DOWN and ACTION_POINTER DOWN to
        // check where the first pointers went down
        // Considering 2 finger only now will deal with 3 finger later
        // mXYPointerCoordsByIndex.get(0).get(0)[0] is the 0 element of the
        // array of the first ACTION_DOWN for finger 1
        // mXYPointerCoordsByIndex.get(0).get(0)[1] is the 1 element of the
        // array of the first ACTION_DOWN for finger 1
        // mXYPointerCoordsByIndex.get(1).get(0)[0] is the 0 element of the
        // array of the first ACTION_POINTER_DOWN for finger 2
        // mXYPointerCoordsByIndex.get(1).get(0)[1] is the 1 element of the
        // array of the first ACTION_POINTER_DOWN for finger 2
        mFinger1ActionDownXY = ViewUtils.scaleTouchEvent(wss.getWorkspaceView(), mXYPointerCoordsByIndex.get(0).get(0)[0], mXYPointerCoordsByIndex
                .get(0).get(0)[1]);

        // new logic now checking that both fingers are on the same widget for
        // it to zoom otherwise just zoom the workspace as currently
        mFinger1BaseWidgetModel = wss.getModelTree().getIntersectingModel(mFinger1ActionDownXY[0], mFinger1ActionDownXY[1]);

        if (mFinger1BaseWidgetModel != null) {
            AppConstants.LOG(AppConstants.VERBOSE, TAG, "mFinger1BaseWidgetDrawable != NULL  mFinger1BaseWidgetModel = " + mFinger1BaseWidgetModel);

            wss.mCurrentSelectedBaseModel = mFinger1BaseWidgetModel;
            // Increment as soon as touched to bring it to the top only if it is
            // not already at the top
            // Dont increment for Location Marker it will return default 1 for
            // Location Marker os that is fine
            AppConstants.LOG(AppConstants.VERBOSE, TAG, "GOing to move " + wss.mCurrentSelectedBaseModel.getID());

            if (wss.mCurrentSelectedBaseModel.getOrder() <= wss.getGlobalOrder()) {
                wss.mCurrentSelectedBaseModel.setOrder(wss.getAndIncrementOrder());
                wss.mCurrentSelectedBaseModel.preDraw();
//				//TODO ugh


            }

        } else {
            AppConstants.LOG(AppConstants.VERBOSE, TAG, "mFinger1BaseWidgetModel == NULL  SO WORKSPACE SCROLL likely ");
            wss.mCurrentSelectedBaseModel = null;
        }

        // Initialize Stroke Models if Stroke is selected
        if (wss.getCurrentTool() == AppConstants.TOOL_STROKE || wss.getCurrentTool() == AppConstants.TOOL_ERASER) {
            // Initialize the TempStroke Array so that we can delete the
            // temporary strokes
            WorkSpaceState.getInstance().mTempStrokeIds = new ArrayList<>();
        }

    }

    private void advanceStrokes(BaseWidgetModel model) {
        if (wss.mCurrentSelectedBaseModel instanceof NoteModel) {
            ArrayList<StrokeModel> strokes = ((NoteModel) model).mExistingStrokes;
            advanceStrokeZorder(strokes);
        } else if (wss.mCurrentSelectedBaseModel instanceof ImageModel) {
            ArrayList<StrokeModel> strokes = ((ImageModel) model).mExistingStrokes;
            advanceStrokeZorder(strokes);
        }
    }

    private void advanceStrokeZorder(ArrayList<StrokeModel> strokes) {
        if (strokes == null) return;
        for (StrokeModel sm : strokes) {
            sm.clearVertexBuilder();
            sm.initializeVertexBuilder();
            sm.preDraw();
        }
    }

    public void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if (mIgnoreOnFling) {
            AppConstants.LOG(AppConstants.VERBOSE, TAG, "IGNORING onFling Motion e1 =" + e1);
            return;
        }

        if (wss.mCurrentSelectedBaseModel instanceof NoteModel || wss.mCurrentSelectedBaseModel instanceof ImageModel
                || wss.mCurrentSelectedBaseModel instanceof PDFModel) {
            if (wss.mCurrentSelectedBaseModel.isPinned()) {
                return;
            }
        }

        // AppConstants.LOG(AppConstants.VERBOSE, TAG, "onFling Motion e1 =" +
        // e1);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onFling Motion e1.getHistorySize =" + e1.getHistorySize());
        // AppConstants.LOG(AppConstants.VERBOSE, TAG, "onFling Motion e2 =" +
        // e2);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // " onFling e2.getHistorySize =" + e2.getHistorySize());

        float x, y;
        float scaled[] = ViewUtils.scaleTouchEvent(wss.getWorkspaceView(), e2.getX(), e2.getY());
        x = scaled[0];
        y = scaled[1];

        // Check to see if we have an up gesture and then calculate the fling
        if (e2.getAction() == MotionEvent.ACTION_UP) {
            // Make sure we have a drawable under our touch
            if (wss.mCurrentSelectedBaseModel != null) {
                AppConstants.LOG(AppConstants.VERBOSE, TAG, String.format("Drawable Fling! x:y %f:%f velx:vely %f:%f", x, y, velocityX, velocityY));

                float topx, topy;
                topx = wss.mCurrentSelectedBaseModel.getRect().getTOPX();
                topy = wss.mCurrentSelectedBaseModel.getRect().getTOPY();

                // Change the x,y of the currently selected drawable
                float width = wss.mCurrentSelectedBaseModel.getRect().getWidth();
                float height = wss.mCurrentSelectedBaseModel.getRect().getHeight();

            } else {

                // TODO krisflingslater evaluate Flings later
                AppConstants.LOG(AppConstants.VERBOSE, TAG, String.format("Scroll Fling! x:y %f:%f velx:vely %f:%f", x, y, velocityX, velocityY));
            }
        }
    }

    public void onLongPress(MotionEvent e) {
        if (wss.mCurrentSelectedBaseModel != null) {

            //Also setting the order to highest at the same time similar to ios once a widget is selected by long press
            if (wss.mCurrentSelectedBaseModel.getOrder() <= wss.getGlobalOrder()) {
                wss.mCurrentSelectedBaseModel.setOrder(wss.getAndIncrementOrder());
                wss.mCurrentSelectedBaseModel.preDraw();
                //to draw widget on top with new order
                wss.getModelTree().mAreAllViewPortWidgetsInitialized = false;
            }

            float x, y;
            float scaled[] = ViewUtils.scaleTouchEvent(wss.getWorkspaceView(), e.getX(), e.getY());
            x = scaled[0];
            y = scaled[1];
            BaseWidgetModel baseWidgetModel = wss.getModelTree().getIntersectingModel(x, y);
            wss.getWorkSpaceModel().getWorkspaceUpdateListener().onLongPressOptionsPopup();

        } else {
            mFinger1ActionDownXY = ViewUtils.scaleTouchEvent(wss.getWorkspaceView(), mXYPointerCoordsByIndex.get(0).get(0)[0],
                    mXYPointerCoordsByIndex.get(0).get(0)[1]);
            WorkSpaceState.getInstance().setOffset(mFinger1ActionDownXY);
            wss.getWorkSpaceModel().getWorkspaceUpdateListener().createMarkerOnLongPress();
        }
    }

    /**
     * I am using the 1st finger and 2nd finger values from the finger to coords
     * list. The list is populated from the View.OnTouch I have. There is some
     * funk to get things working well.
     *
     * @param detector
     */
    public void onScale(ScaleGestureDetector detector) {

        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onScale Motion detector =" + detector);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onScale Motion detector.getCurrentSpan =" +
        // detector.getCurrentSpan());
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onScale Motion detector.getPreviousSpan =" +
        // detector.getPreviousSpan());

        // float x, y, x2, y2;
        AppConstants.LOG(AppConstants.VERBOSE, TAG, String.format("Scale: Event X:Y = %f:%f Pointer List 1 X:Y = %f:%f 2 X:Y %f:%f",
                detector.getFocusX(), detector.getFocusY(), mPointersToCoordsList.get(0)[0], mPointersToCoordsList.get(0)[1],
                mPointersToCoordsList.get(1)[0], mPointersToCoordsList.get(1)[1]));

        /**
         * I am using the values from the list because otherwise I am only given
         * the average between the 2 fingers and that's not accurate enough.
         */

        // Try to do even scaling
        float scaledDifferenceF1[] = ViewUtils.scaleTouchEvent(wss.getWorkspaceView(), mPreviousSpanX, mPreviousSpanY);

        float scaledDifferenceF2[] = ViewUtils.scaleTouchEvent(wss.getWorkspaceView(), detector.getCurrentSpanX(), detector.getCurrentSpanY());

        float current = detector.getCurrentSpan();

        float difference = (mPreviousScale - current);

        float scaledDifference = ViewUtils.scaleZoomGesture(scaledDifferenceF1[0], scaledDifferenceF1[1], scaledDifferenceF2[0],
                scaledDifferenceF2[1], wss.getZoom(), wss.getMaxZoom());

        // RESET the previous metrics for smooth scaling for both cards and
        // workspace
        mPreviousScale = current;
        mPreviousSpanX = detector.getCurrentSpanX();
        mPreviousSpanY = detector.getCurrentSpanY();

        // set up variable true so that strokes are not drawwn once a scale or
        // scroll starts in a gesture
        mWorkspaceOrWidgetScaleOrScrollStarted = true;

        // check for the SCALE_SCROLL_THRESHOLD if scaledDifference in < abs(20)
        // then just scroll instead of scale)
        if (Math.abs(scaledDifference) < AppConstants.SCALE_SCROLL_THRESHOLD) {
            // set scroll to true
            mIgnoreOnScroll = false;
            // return from on scale and dont scale the card or workspace
            AppConstants.LOG(AppConstants.VERBOSE, TAG, "SCALE_SCROLL_THRESHOLD not met for scaledDifference: " + scaledDifference
                    + " So Not scaling but scrolling: ");

            return;
        }

        // Simpler Logic already know in onScaleBegin if mScaleWorkSpace ==
        // false; or not if not start scaling the workspace
        if (mScaleWorkSpace || (mFinger2BaseWidgetModel != null && mFinger2BaseWidgetModel.isPinned())) {

            // Add the difference to the zoom for workspace scale
            AppConstants.LOG(AppConstants.VERBOSE, TAG, "Scale Workspace Zoom Called scaledDifference: " + scaledDifference + " Difference: "
                    + difference);
            wss.addToZoom(scaledDifference);

        } else if (mFinger2BaseWidgetModel != null && !mFinger2BaseWidgetModel.isPinned()) {

            // In this case since mScaleWorkSpace == false means both fingers
            // are on the same widget
            // setting selected drawable to second finger
            wss.mCurrentSelectedBaseModel = mFinger2BaseWidgetModel;
            float ratio, ratioWidth;
            // Keep the ratio of the object
            //ratio = mFinger2BaseWidgetModel.getWidth() / mFinger2BaseWidgetModel.getHeight();
            ratio = ((TexturedWidgetModel)mFinger2BaseWidgetModel).getActualWidth() / ((TexturedWidgetModel)mFinger2BaseWidgetModel).getActualHeight();

            ratioWidth = scaledDifference * ratio;

            // Check for Max ratioWidth similar to Zoom REject so that the card
            // scaling doesnt go crazy
            float currentCardWidth = mFinger2BaseWidgetModel.getRect().getWidth();
            float currentCardHeight = mFinger2BaseWidgetModel.getRect().getHeight();

            // TODO kris refactor into a function in ViewUtils
            // Scale difference and ratioWidth positive minimize case
            if (ratioWidth > 0) {

                if (currentCardWidth <= AppConstants.SCALE_WIDGET_MINIMIZE_THRESHOLD * AppConstants.SCALE_CARD_MIN_WIDTH) {

                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                            " MIN Case currentCardWidth <= AppConstants.SCALE_WIDGET_MINIMIZE_THRESHOLD * AppConstants.SCALE_CARD_MIN_WIDTH"
                                    + "ratioWidth" + ratioWidth + " currentCardHeight " + currentCardWidth);

                    // Post the new position with fixt toppxy and minimum size
                    // old code wiht issue of resizing
//                    Rect rect = new Rect(mFinger2BaseWidgetModel.getRect().getTOPX(), mFinger2BaseWidgetModel.getRect().getTOPY(),
//                            mFinger2BaseWidgetModel.getRect().getTOPX() + AppConstants.SCALE_CARD_MIN_WIDTH, mFinger2BaseWidgetModel.getRect().getTOPY()
//                            + AppConstants.SCALE_CARD_MIN_HEIGHT);


                    Rect rect = new Rect(mFinger2BaseWidgetModel.getRect().getTOPX(), mFinger2BaseWidgetModel.getRect().getTOPY(),
                            mFinger2BaseWidgetModel.getRect().getTOPX() + AppConstants.SCALE_CARD_MIN_WIDTH, mFinger2BaseWidgetModel.getRect().getTOPY()
                            + (AppConstants.SCALE_CARD_MIN_WIDTH / ratio));

                    MoveModel moveModel = new MoveModel(rect, WorkSpaceState.getInstance().getGlobalOrder(), mFinger2BaseWidgetModel.getID());


                    WorkSpaceState.getInstance().getModelTree().moveWidgetOnWorkSpace(moveModel);

                    return;
                }

                if (ratioWidth > currentCardWidth * AppConstants.SCALE_WIDGET_GROWTH_FACTOR) {
                    AppConstants.LOG(AppConstants.VERBOSE, TAG, "LIMITING ratioWidth to .1 so smooth out scaling ratioWidth = " + ratioWidth
                            + " currentCardWidth " + currentCardWidth);

                    ratioWidth = currentCardWidth * AppConstants.SCALE_WIDGET_GROWTH_FACTOR;
                } else {
                    AppConstants.LOG(AppConstants.VERBOSE, TAG, "NOT LIMITING  ratioWidth to .1 so smooth out scaling ratioWidth = " + ratioWidth
                            + " currentCardWidth " + currentCardWidth);
                }

                if (scaledDifference > currentCardHeight * AppConstants.SCALE_WIDGET_GROWTH_FACTOR) {
                    AppConstants.LOG(AppConstants.VERBOSE, TAG, "LIMITING scaledDifference to .1 so smooth out scaling scaledDifference = "
                            + ratioWidth + " currentCardHeight " + currentCardWidth);

                    scaledDifference = currentCardHeight * AppConstants.SCALE_WIDGET_GROWTH_FACTOR;
                } else {
                    AppConstants.LOG(AppConstants.VERBOSE, TAG, "NOT LIMITING  scaledDifference to .1 so smooth out scaling scaledDifference = "
                            + ratioWidth + " currentCardHeight " + currentCardWidth);
                }
            } else if (ratioWidth < 0) {
                // Scale difference and ratioWidth positive maximize zoom/scale
                // out case

                // Check for maximum size case if width is >= 85% of
                // SCALE_CARD_MIN_WIDTH,
                // and set the other rect coords based SCALE_CARD_MAX_WIDTH and
                // SCALE_CARD_MAX_HEIGHT and exit this way size will never go
                // less than min
                if (currentCardWidth >= AppConstants.SCALE_WIDGET_MAXIMIZE_THRESHOLD * AppConstants.SCALE_CARD_MAX_WIDTH) {

                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                            " MAX Case currentCardWidth >= AppConstants.SCALE_WIDGET_MAXIMIZE_THRESHOLD * AppConstants.SCALE_CARD_MAX_WIDTH"
                                    + "ratioWidth" + ratioWidth + " currentCardHeight " + currentCardWidth);

                    // Post the new position with fix topxy and minimum size and
                    //Old with issue of resizing
//                    Rect rect = new Rect(mFinger2BaseWidgetModel.getRect().getTOPX(), mFinger2BaseWidgetModel.getRect().getTOPY(),
//                            mFinger2BaseWidgetModel.getRect().getTOPX() + AppConstants.SCALE_CARD_MAX_WIDTH, mFinger2BaseWidgetModel.getRect().getTOPY()
//                            + AppConstants.SCALE_CARD_MAX_HEIGHT);

                    //TAke the ratio into account set the bigger one to the max

                    Rect rect = new Rect(mFinger2BaseWidgetModel.getRect().getTOPX(), mFinger2BaseWidgetModel.getRect().getTOPY(),
                            mFinger2BaseWidgetModel.getRect().getTOPX() + AppConstants.SCALE_CARD_MAX_WIDTH, mFinger2BaseWidgetModel.getRect().getTOPY()
                            + (AppConstants.SCALE_CARD_MAX_WIDTH / ratio)) ;


                    MoveModel moveModel = new MoveModel(rect, WorkSpaceState.getInstance().getGlobalOrder(), mFinger2BaseWidgetModel.getID());

                    WorkSpaceState.getInstance().getModelTree().moveWidgetOnWorkSpace(moveModel);

                    return;
                }

                if (-ratioWidth > currentCardWidth * AppConstants.SCALE_WIDGET_GROWTH_FACTOR) {
                    AppConstants.LOG(AppConstants.VERBOSE, TAG, "LIMITING ratioWidth to .1 so smooth out scaling ratioWidth = " + ratioWidth
                            + " currentCardWidth " + currentCardWidth);

                    ratioWidth = -currentCardWidth * AppConstants.SCALE_WIDGET_GROWTH_FACTOR;
                } else {
                    AppConstants.LOG(AppConstants.VERBOSE, TAG, "NOT LIMITING  ratioWidth to .1 so smooth out scaling ratioWidth = " + ratioWidth
                            + " currentCardWidth " + currentCardWidth);
                }

                if (-scaledDifference > currentCardHeight * AppConstants.SCALE_WIDGET_GROWTH_FACTOR) {
                    AppConstants.LOG(AppConstants.VERBOSE, TAG, "LIMITING scaledDifference to .1 so smooth out scaling scaledDifference = "
                            + ratioWidth + " currentCardHeight " + currentCardWidth);

                    scaledDifference = -currentCardHeight * AppConstants.SCALE_WIDGET_GROWTH_FACTOR;
                } else {
                    AppConstants.LOG(AppConstants.VERBOSE, TAG, "NOT LIMITING  scaledDifference to .1 so smooth out scaling scaledDifference = "
                            + ratioWidth + " currentCardHeight " + currentCardWidth);
                }
            }

            // Dont post 0 ratioWidth or size change
            if (ratioWidth != 0) {
                // Post the position change
                Rect rect = new Rect(mFinger2BaseWidgetModel.getRect().getTOPX() + (ratioWidth / 2), mFinger2BaseWidgetModel.getRect().getTOPY()
                        + (scaledDifference / 2),
                        mFinger2BaseWidgetModel.getRect().getBOTX() - (ratioWidth / 2), mFinger2BaseWidgetModel.getRect().getBOTY()
                        - (scaledDifference / 2));

                MoveModel moveModel = new MoveModel(rect, WorkSpaceState.getInstance().getGlobalOrder(), mFinger2BaseWidgetModel.getID());

                WorkSpaceState.getInstance().getModelTree().moveWidgetOnWorkSpace(moveModel);

            } else {
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "ratioWidth == 0 so no scaling and no position change event to be posted");

            }

        } else {
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "Odd Case Contact Dev this should not happen");

        }

    }

    public void onScaleBegin(ScaleGestureDetector detector) {

        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onScaleBegin Motion detector =" + detector);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onScaleBegin Motion detector.getCurrentSpan =" +
        // detector.getCurrentSpan());
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onScaleBegin Motion detector.getPreviousSpan =" +
        // detector.getPreviousSpan());
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // String.format("Scale: Event X:Y = %f:%f Pointer List 1 X:Y = %f:%f 2 X:Y %f:%f",
        // detector.getFocusX(), detector.getFocusY(),
        // mPointersToCoordsList.get(0)[0], mPointersToCoordsList.get(0)[1],
        // mPointersToCoordsList.get(1)[0], mPointersToCoordsList.get(1)[1]));

        try {
            // Stop the stroke drawing,scrolling, flinging as Scaling has
            // started
            // mIgnoreOnScroll is proxy for scaling started and to ignore
            // drawing
            mIgnoreOnScroll = true;
            mIgnoreOnFling = true;
            mIgnoreOnDown = true;

            // These are set so that in onScale the values are already populated
            // and helps in scaling things better
            mPreviousSpanX = detector.getCurrentSpanX();
            mPreviousSpanY = detector.getCurrentSpanY();
            mPreviousScale = detector.getCurrentSpan();

            // Store XY co-ords of the first ACTION_DOWN and ACTION_POINTER DOWN
            // to check where the first pointers went down
            // Considering 2 finger only now will deal with 3 finger later
            // mXYPointerCoordsByIndex.get(0).get(0)[0] is the 0 element of the
            // array of the first ACTION_DOWN for finger 1
            // mXYPointerCoordsByIndex.get(0).get(0)[1] is the 1 element of the
            // array of the first ACTION_DOWN for finger 1
            // mXYPointerCoordsByIndex.get(1).get(0)[0] is the 0 element of the
            // array of the first ACTION_POINTER_DOWN for finger 2
            // mXYPointerCoordsByIndex.get(1).get(0)[1] is the 1 element of the
            // array of the first ACTION_POINTER_DOWN for finger 2
            mFinger1ActionDownXY = ViewUtils.scaleTouchEvent(wss.getWorkspaceView(), mXYPointerCoordsByIndex.get(0).get(0)[0],
                    mXYPointerCoordsByIndex.get(0).get(0)[1]);
            mFinger2ActionPointerDownXY = ViewUtils.scaleTouchEvent(wss.getWorkspaceView(), mXYPointerCoordsByIndex.get(1).get(0)[0],
                    mXYPointerCoordsByIndex.get(1).get(0)[1]);

            // new logic now checking that both fingers are on the same widget
            // for it to zoom otherwise just zoom the workspace as currently
            mFinger1BaseWidgetModel = wss.getModelTree().getIntersectingModel(mFinger1ActionDownXY[0], mFinger1ActionDownXY[1]);
            mFinger2BaseWidgetModel = wss.getModelTree().getIntersectingModel(mFinger2ActionPointerDownXY[0], mFinger1ActionDownXY[1]);

            if (mFinger1BaseWidgetModel != null && mFinger2BaseWidgetModel != null) {

                // Check that both fingers are on the same widget then set the
                // wss selected to the
                if (mFinger1BaseWidgetModel.getID().equals(mFinger2BaseWidgetModel.getID())) {
                    mScaleWorkSpace = false;
                } else {
                    // AppConstants.LOG(AppConstants.CRITICAL, TAG,
                    // "Scale WorkSpace Zoom Begin as 1 or both of the fingers are on different drawables. So setting both to null");
                    mFinger1BaseWidgetModel = null;
                    mFinger2BaseWidgetModel = null;
                }
            } else {
                // AppConstants.LOG(AppConstants.CRITICAL, TAG,
                // "Scale WorkSpace Zoom Begin as 1 or both of the fingers are on the workspace ");
                mFinger1BaseWidgetModel = null;
                mFinger2BaseWidgetModel = null;
            }
        } catch (Exception ex) {
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "INVESTIGATE Caught Exception in  onScaleBegin =" + ex);

        }
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onScaleEnd Motion detector =" + detector);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onScaleEnd Motion detector.getCurrentSpan =" +
        // detector.getCurrentSpan());
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onScaleEnd Motion detector.getPreviousSpan =" +
        // detector.getPreviousSpan());

        // once scale ends then scrolling can happen
        mIgnoreOnScroll = false;
        mIgnoreOnFling = false;
    }

    public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        try {

            if (mIgnoreOnScroll) {
                AppConstants.LOG(AppConstants.VERBOSE, TAG, "IGNORING onScroll Motion e1 =" + e1);
                return;
            }


            // Draw strokes only with one finger and in the current gesture
            // there is no widget or workspace scale or scroll like iPad
            // Also used for Eraser action
            if ((wss.getCurrentTool() == AppConstants.TOOL_STROKE || wss.getCurrentTool() == AppConstants.TOOL_ERASER) && mFingersDown < 2
                    && !mWorkspaceOrWidgetScaleOrScrollStarted) {
                for (int i = 0; i < e2.getHistorySize(); i++) {
                    float[] workSpaceXY = ViewUtils.getWorkSpaceXYFromTouch(wss.getWorkspaceView(), e2.getHistoricalX(i), e2.getHistoricalY(i));
                    mStrokeLocsArray.add(workSpaceXY[0]);
                    mStrokeLocsArray.add(workSpaceXY[1]);
                }
                // Start Drawing Temporary Stroke that should not be sent ot
                // server but just displays locally and will be over written by
                // the one from server anyway
                // Copied from below drawFromGestureMotionEventHistory but not
                // sending to WS Server
                // Draw Stroke if Stroke Tool is selected and there is no hint
                // of a scale, if scale happens then mIgnoreOnScroll is set to
                // true

                // send only if size of the mStrokeLocsArray is > 0 to stop from
                // having empty strokes
                // having exceptions with strokes that have just x,y
                if (mStrokeLocsArray.size() > 1) {
                    float[] colorArray = wss.getSelectedStrokeColor();

                    StrokeModel baseWidgetModel = new StrokeModel(mStrokeLocsArray, colorArray);

                    // Code from Model Tree to add to Workspace to show on
                    // screen
                    AppConstants.LOG(AppConstants.VERBOSE, TAG, "ADDING Temporary StrokeModel");

                    baseWidgetModel.setTargetID(WorkSpaceState.getInstance().getWorkSpaceModel().getId());
                    // Set right color and mLineWidth and brush for Eraser
                    if (wss.getCurrentTool() == AppConstants.TOOL_ERASER) {
                        // For Temporary Strokes draw black color instead of
                        // white
                        // baseWidgetModel.mColor =
                        // AppConstants.EraseBrushColor.BRUSH_COLOR_ERASE_SEND;
                        Log.i("onScroll","onScroll"+"onScroll");

                        baseWidgetModel.setColor(AppConstants.EraseBrushColor.BRUSH_COLOR_ERASE);

                        baseWidgetModel.setBrush(AppConstants.STROKE_BRUSH_ERASE);
                        baseWidgetModel.reallyJustSetLineWidth(AppConstants.STROKE_BRUSH_ERASE_SIZE);
                    }

                    WorkSpaceState.getInstance().getModelTree().add(baseWidgetModel);

                    WorkSpaceState.getInstance().mTempStrokeIds.add(baseWidgetModel.getID());
                }

                return;
            }

            // this is for cards etc.
            if (wss.mCurrentSelectedBaseModel != null && !wss.mCurrentSelectedBaseModel.isPinned() && !isAnyGroupItemPinned(wss.mCurrentSelectedBaseModel)) { // this
                // is
                // for
                // cards
                // etc.
                // that
                // are
                // not
                // pinned

                // set up variable true so that strokes are not drawn once a
                // scale or scroll starts in a gesture
                mWorkspaceOrWidgetScaleOrScrollStarted = true;

                //flag to send He Event when ACTION_UP happens for cards, browser etc.
                mDidWidgetMoveHappen = true;

                // Change the x,y of the currently selected drawable
                float width = wss.mCurrentSelectedBaseModel.getRect().getWidth();
                float height = wss.mCurrentSelectedBaseModel.getRect().getHeight();

                float topx, topy, distx, disty;

                topx = wss.mCurrentSelectedBaseModel.getRect().getTOPX();
                topy = wss.mCurrentSelectedBaseModel.getRect().getTOPY();

                distx = ViewUtils.scaleScrollGesture(distanceX, wss.getZoom(), wss.getMaxZoom());
                disty = ViewUtils.scaleScrollGesture(-distanceY, wss.getZoom(), wss.getMaxZoom());

                // moves from gesture point but doesn't scale well at the end of
                // the screen
                Rect rect = new Rect(topx - distx, topy + disty, topx + width - distx, topy + height + disty);

                // SET THE RIGHT mEventOrder
                // AppConstants.LOG(AppConstants.VERBOSE, TAG,
                // "after ++ onScroll WorkSpaceState.getInstance().mGlobalWorkSpaceEventOrder= "
                // + WorkSpaceState.getInstance().mGlobalWorkSpaceEventOrder);
//GO HERE
                //Do not increment here update only once in onDown  SET THE RIGHT mEventOrder
                MoveModel moveModel = new MoveModel(rect, WorkSpaceState.getInstance().getGlobalOrder(), wss.mCurrentSelectedBaseModel.getID());


                wss.mLastMoveModel = moveModel;
                // Update Model and send Ve message to server
                WorkSpaceState.getInstance().getModelTree().moveWidgetOnWorkSpace(moveModel);

            } else { // This is for workspace scroll

                // set up variable true so that strokes are not drawn once a
                // scale or scroll starts in a gesture
                mWorkspaceOrWidgetScaleOrScrollStarted = true;

                // Change the location of the viewport, make sure to scale with
                // zoom and pixels and stuff
                wss.addOffset(new float[]{ViewUtils.scaleScrollGesture(distanceX, wss.getZoom(), wss.getMaxZoom()),
                        ViewUtils.scaleScrollGesture(distanceY, wss.getZoom(), wss.getMaxZoom())});
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppConstants.LOG(AppConstants.CRITICAL, TAG, " INVESTIGATE Caught Exception in  onScroll =" + e);
        }
    }

    public void onShowPress(MotionEvent e) {
        // AppConstants.LOG(AppConstants.VERBOSE, TAG, "onShowPress Motion e ="
        // + e);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onShowPress Motion e.getHistorySize =" + e.getHistorySize());
    }

    public void onSingleTapConfirmed(MotionEvent e) {

        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onSingleTapConfirmed Motion e =" + e);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onSingleTapConfirmed Motion e.getHistorySize =" +
        // e.getHistorySize());
    }

    public void onSingleTapUp(MotionEvent e) {
        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "onSingleTapUp Motion e =" + e);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "onSingleTapUp Motion e.getHistorySize =" + e.getHistorySize());

        wss.mCurrentSelectedBaseModel = null;

        wss.getWorkSpaceModel().getWorkspaceUpdateListener().closeSideMenu();
    }

    // Method used for debugging gesture flow.. can delete
    public void printGestureMotionEventHistory() {

        List<MotionEvent> actionDownMotionEventsList;
        List<MotionEvent> actionUpMotionEventsList;
        List<MotionEvent> actionMoveMotionEventsList;
        List<MotionEvent> actionPointerDownMotionEventsList;
        List<MotionEvent> actionPointerUpMotionEventsList;
        List<MotionEvent> actionPointer2DownMotionEventsList;
        List<MotionEvent> actionCancelMotionEventsList;

        if (GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_DOWN).size() > 0) {
            actionDownMotionEventsList = GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_DOWN);
            // AppConstants.LOG(AppConstants.CRITICAL, TAG,
            // "ACTION_DOWN motionEventsList.size()  : " +
            // actionDownMotionEventsList.size() +
            // "ACTION_DOWN motionEventsList  : " + actionDownMotionEventsList);

            for (int k = 0; k < actionDownMotionEventsList.size(); k++) {
                MotionEvent sMotionEvent = actionDownMotionEventsList.get(k);
                System.out.println(sMotionEvent);
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "MotionEvent ACTION_DOWN Iterating motionEventsList  : " + sMotionEvent);
                for (int i = 0; i < sMotionEvent.getPointerCount(); i++) {
                    MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
                    sMotionEvent.getPointerCoords(i, coords);
                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                            String.format("Printing ACTION_DOWN  Pointer: %d Coords %f:%f", i, coords.x, coords.y));
                }

            }

        }

        if (GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_POINTER_DOWN).size() > 0) {

            actionPointerDownMotionEventsList = GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_POINTER_DOWN);
            // AppConstants.LOG(AppConstants.CRITICAL, TAG,
            // "ACTION_POINTER_DOWN motionEventsList  : " +
            // actionPointerDownMotionEventsList);
            for (int k = 0; k < actionPointerDownMotionEventsList.size(); k++) {
                MotionEvent sMotionEvent = actionPointerDownMotionEventsList.get(k);
                System.out.println(sMotionEvent);
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "MotionEvent ACTION_POINTER_DOWN Iterating motionEventsList  : " + sMotionEvent);
                for (int i = 0; i < sMotionEvent.getPointerCount(); i++) {
                    MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
                    sMotionEvent.getPointerCoords(i, coords);
                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                            String.format("Printing ACTION_POINTER_DOWN  Pointer: %d Coords %f:%f", i, coords.x, coords.y));
                }

            }
        }
        if (GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_POINTER_2_DOWN).size() > 0) {

            actionPointer2DownMotionEventsList = GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_POINTER_2_DOWN);
            // AppConstants.LOG(AppConstants.CRITICAL, TAG,
            // "ACTION_POINTER_2_DOWN motionEventsList  : " +
            // actionPointer2DownMotionEventsList);
            for (int k = 0; k < actionPointer2DownMotionEventsList.size(); k++) {
                MotionEvent sMotionEvent = actionPointer2DownMotionEventsList.get(k);
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "MotionEvent ACTION_POINTER_2_DOWN Iterating motionEventsList  : " + sMotionEvent);
                for (int i = 0; i < sMotionEvent.getPointerCount(); i++) {
                    MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
                    sMotionEvent.getPointerCoords(i, coords);
                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                            String.format("Printing ACTION_POINTER_2_DOWN  Pointer: %d Coords %f:%f", i, coords.x, coords.y));
                }

            }
        }

        if (GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_MOVE).size() > 0) {

            actionMoveMotionEventsList = GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_MOVE);
            // AppConstants.LOG(AppConstants.CRITICAL, TAG,
            // "ACTION_MOVE motionEventsList.size()  : " +
            // actionMoveMotionEventsList.size() +
            // "ACTION_MOVE motionEventsList  : " + actionMoveMotionEventsList);
            for (int k = 0; k < actionMoveMotionEventsList.size(); k++) {
                MotionEvent sMotionEvent = actionMoveMotionEventsList.get(k);

                AppConstants.LOG(AppConstants.CRITICAL, TAG, "actionMoveMotionEventsList k=" + k
                        + " MotionEvent ACTION_MOVE Iterating motionEventsList  : " + sMotionEvent);

                AppConstants.LOG(AppConstants.CRITICAL, TAG, "getPointerCount MotionEvent ACTION_MOVE sMotionEvent.getPointerCount()  : "
                        + sMotionEvent.getPointerCount());

                for (int i = 0; i < sMotionEvent.getPointerCount(); i++) {
                    MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
                    sMotionEvent.getPointerCoords(i, coords);
                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                            String.format("Printing ACTION_MOVE  Pointer Count Pointer: %d Coords %f:%f", i, coords.x, coords.y));
                }
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "getHistorySize MotionEvent ACTION_MOVE sMotionEvent.getHistorySize()  : "
                        + sMotionEvent.getHistorySize());

                for (int i = 0; i < sMotionEvent.getHistorySize(); i++) {
                    AppConstants.LOG(
                            AppConstants.CRITICAL,
                            TAG,
                            String.format("getHistoryXY ACTION_MOVE  HistoricalX: %d Coords %f:%f", i, sMotionEvent.getHistoricalX(i),
                                    sMotionEvent.getHistoricalY(i)));
                }

            }
        }

        if (GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_POINTER_UP).size() > 0) {

            actionPointerUpMotionEventsList = GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_POINTER_UP);
            // AppConstants.LOG(AppConstants.CRITICAL, TAG,
            // "ACTION_POINTER_UP motionEventsList  : " +
            // actionPointerUpMotionEventsList);
            for (int k = 0; k < actionPointerUpMotionEventsList.size(); k++) {
                MotionEvent sMotionEvent = actionPointerUpMotionEventsList.get(k);
                System.out.println(sMotionEvent);
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "MotionEvent ACTION_POINTER_UP Iterating motionEventsList  : " + sMotionEvent);
                for (int i = 0; i < sMotionEvent.getPointerCount(); i++) {
                    MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
                    sMotionEvent.getPointerCoords(i, coords);
                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                            String.format("Printing ACTION_POINTER_UP  Pointer: %d Coords %f:%f", i, coords.x, coords.y));
                }

            }
        }

        if (GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_CANCEL).size() > 0) {

            actionCancelMotionEventsList = GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_CANCEL);
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "ACTION_CANCEL motionEventsList  : " + actionCancelMotionEventsList);
            for (int k = 0; k < actionCancelMotionEventsList.size(); k++) {
                MotionEvent sMotionEvent = actionCancelMotionEventsList.get(k);
                System.out.println(sMotionEvent);
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "MotionEvent ACTION_CANCEL Iterating motionEventsList  : " + sMotionEvent);
                for (int i = 0; i < sMotionEvent.getPointerCount(); i++) {
                    MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
                    sMotionEvent.getPointerCoords(i, coords);
                    AppConstants.LOG(AppConstants.CRITICAL, TAG,
                            String.format("Printing ACTION_CANCEL  Pointer: %d Coords %f:%f", i, coords.x, coords.y));
                }

            }
        }

        if (GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_UP).size() > 0) {

            actionUpMotionEventsList = GestureHandler.getInstance().mGestureMotionEventHistory.get(MotionEvent.ACTION_UP);
            AppConstants.LOG(AppConstants.CRITICAL, TAG, "ACTION_UP motionEventsList  : " + actionUpMotionEventsList);
            for (int k = 0; k < actionUpMotionEventsList.size(); k++) {
                MotionEvent sMotionEvent = actionUpMotionEventsList.get(k);

                System.out.println(sMotionEvent);
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "MotionEvent ACTION_UP Iterating motionEventsList  : " + sMotionEvent);
                for (int i = 0; i < sMotionEvent.getPointerCount(); i++) {
                    MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
                    sMotionEvent.getPointerCoords(i, coords);
                    AppConstants
                            .LOG(AppConstants.CRITICAL, TAG, String.format("Printing ACTION_UP  Pointer: %d Coords %f:%f", i, coords.x, coords.y));
                }

            }
        }

    }

    public void resetGestureHandler() {

        mIgnoreOnScale = false;
        mIgnoreOnScroll = false;
        mIgnoreOnFling = false;
        mIgnoreOnDown = false;
        mGestureMotionEventHistory.clear();
        mXYPointerCoordsByIndex.clear();
        mPointersToCoordsList.clear();
        mStrokeLocsArray = new ArrayList<>();
        mFinger1ActionDownXY = null;
        mFinger2ActionPointerDownXY = null;
        mScaleWorkSpace = true;
        // reset the currently selected drawable on workspace state to get the
        // latest points if something was already selected
        wss.mCurrentSelectedBaseModel = null;
        mWorkspaceOrWidgetScaleOrScrollStarted = false;
        mDidWidgetMoveHappen = false;
        wss.mLastMoveModel = null;
        WorkSpaceState.getInstance().mTempStrokeIds = null;

    }

    public void setFingersDown(int fingersDown) {
        mFingersDown = fingersDown;
    }

    public void setGesturePointerNumAndLocation(int pointerNum, float downX, float downY) {
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "setGesturePointerNumAndLocation pointerNum == " + pointerNum);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "setGesturePointerNumAndLocation downX == " + downX);
        // AppConstants.LOG(AppConstants.VERBOSE, TAG,
        // "setGesturePointerNumAndLocation downY == " + downY);

        mPointersToCoordsList.add(pointerNum, new float[]{downX, downY});
    }

    public boolean didWidgetMoveHappen() {
        return mDidWidgetMoveHappen;
    }

    private boolean isAnyGroupItemPinned(BaseWidgetModel baseWidgetModel) {
        boolean isMoveOk = false;
        if (baseWidgetModel instanceof Group) {
            ArrayList<BaseWidgetModel> mChildModels = ((Group) baseWidgetModel).getChildModels();
            for (int count = 0; count < mChildModels.size(); count++) {
                if (mChildModels.get(count).isPinned())
                    isMoveOk = true;
            }
        }
        return isMoveOk;
    }
}

