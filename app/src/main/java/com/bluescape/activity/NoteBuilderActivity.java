package com.bluescape.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.R;
import com.bluescape.collaboration.util.NetworkTask;
import com.bluescape.collaboration.util.TemplateHelper;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.template.NoteTemplate;
import com.bluescape.model.util.Rect;
import com.bluescape.model.util.StrokeColors;
import com.bluescape.model.util.TextStyle;
import com.bluescape.model.widget.ImageModel;
import com.bluescape.model.widget.NoteModel;
import com.bluescape.util.TextHelper;
import com.bluescape.view.CustomNoteCardView;
import com.bluescape.view.adapter.CreateCardDialogAdapter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoteBuilderActivity extends BaseActivity {
    class GetImageBitmapFromURLTask extends AsyncTask<String, String, Bitmap> {
        Bitmap bmp;
        final ImageModel imageModel;

        GetImageBitmapFromURLTask(ImageModel imageModel) {
            this.imageModel = imageModel;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];

            try {
                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection) ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                backgroundIV.setImageBitmap(bitmap);
                dismissProgressDialog();
            } else {
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "DID NOT Download Image: bitmap is null");
            }
        }
    }

    class GetImageURLTask extends AsyncTask<String, String, String> {
        String ImageUrl;
        final ImageModel imageModel;

        GetImageURLTask(ImageModel imageModel) {
            this.imageModel = imageModel;
        }

        @Override
        protected String doInBackground(String... params) {

            String fullUrl = AppSingleton.getInstance().getApplication()
                    .getDataFromSharedPrefs(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS, AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS)
                    + "/"
                    + AppSingleton.getInstance().getApplication()
                    .getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, AppConstants.KEY_WORKSPACE_ID)
                    + "/asset_url?key="
                    + imageModel.getBaseName()
                    + "-"
                    + getImageKey(AppConstants.MAX_HIGH_RES_AREA, imageModel)
                    + "."
                    + imageModel.mExtension;

            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(fullUrl);
                get.setHeader("Cookie", AppSingleton.getInstance().getApplication().getDataFromSharedPrefs(AppConstants.WORKSPACE_COOKIE, ""));
                HttpResponse responseGet = client.execute(get);
                HttpEntity resEntityGet = responseGet.getEntity();
                if (resEntityGet != null) {
                    JSONObject mJsonObject = new JSONObject(EntityUtils.toString(resEntityGet));
                    Log.d("GET URL ", mJsonObject.get("url").toString());
                    ImageUrl = mJsonObject.get("url").toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ImageUrl;
        }

        @Override
        protected void onPostExecute(String url) {
            super.onPostExecute(url);

            new GetImageBitmapFromURLTask(imageModel).execute(url);

        }
    }

    private static final String TAG = NoteBuilderActivity.class.getSimpleName();
    /**
     * results and codes
     */

    public static final int CODE_OK_NEW = 1;
    public static final int CODE_OK_EDIT = 2;
    public static final int CODE_CANCELLED_NEW = 3;
    public static final int CODE_CANCELLED_EDIT = 4;
    public static final int CODE_OK_EDIT_IMAGE = 5;
    private static final String KEY_RESULT = "com.bluescape.keyResult";
    public static final int REQUEST_CODE = 123;
    /**
     * Keep track of if we are updating an object or creating an object.
     */
    public static final String KEY_IS_NEW = "com.bluescape.newOld";
    public static final String IS_CARD = "com.bluescape.cardImage";

    /**
     */
    public static final String KEY_OBJECT_ID = "com.bluescape.objectID";
    /**
     * Key for the bundle extra.
     */
    public static final String KEY_BUNDLE_EXTRA = "com.bluescape.bundleExtra";
    public Toolbar toolbar;

    private ImageView strokeColorAction, strokeAction;

    private TextView colourWhiteIv, colourRedIv, colourYellowIv, colourTealTv, colourBlueIv, colourPurpleIv, colourBlackIv;

    private StrokeColors selectedStrokeColor = StrokeColors.NO_COLOR;

    // The objects ID
    private String mObjectID;

    // Are we updating a current object or are we creating one
    public boolean mIsNew, mIsCard;

    // The widget if this is not new.
    // public NoteDrawable mDrawable;
    public NoteModel mModel;

    public ImageModel mImageModel;

    // INstance of the View that is created
    private CustomNoteCardView mNoteView;

    // Views
    private EditText et;

    // Template
    private NoteTemplate mTemplate;

    // Current drawable the user has selected to draw

    // FYI..Best is to set the WorkSpaceState.mNoteModel here itself before
    // launching NoteBuilderActivity
    // We can use this in the NoteBuilderActivity and CustomNoteCardView for Add
    // and Edit
    // WorkSpaceState.getInstance().mNoteModel =
    // (NoteModel)baseWidgetDrawable.getModel();

    // Background Bitmap
    public Bitmap mBgBitmap;
    // Text style
    private TextStyle mTextStyle = new TextStyle();

    private String baseName;

    private ImageView backgroundIV;

    public void onClick(View v) {
        int clicked = v.getId();
        switch (clicked) {
            case R.id.cancelActionLL:
                setKeyboardStatus(et, true);
                finish(CODE_CANCELLED_NEW, 0);
                clearingOldStrokes();
                break;
            case R.id.noteAction:
                createCardDialog();
                break;
            case R.id.TextActionIv:
                strokeAction.setImageResource(R.drawable.pen_b);
                mNoteView.mCurrentTool = AppConstants.TOOL_NO_SHAPE;
                if (knowKeyboardStatus()) {
                    setKeyboardStatus(et, false);
                } else {
                    setKeyboardStatus(et, true);
                    et.bringToFront();
                }
                et.setSelection(et.getText().length());
                AppConstants.LOG(AppConstants.INFO, TAG, "Tool changed: TextActionIv");
                break;
            case R.id.strokeColorActionIv:
                AppConstants.LOG(AppConstants.INFO, TAG, "Tool changed: strokeColorActionIv");
                strokeColorDialog();
                break;
            case R.id.strokeActionIv:
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "Tool changed: strokeActionIv");
                setKeyboardStatus(et, true);
                if (mNoteView.mCurrentTool != AppConstants.TOOL_STROKE) {
                    // setting to blue pen explicitly immediately eliminating bus
                    // from menu selection for stroke
                    strokeAction.setImageResource(R.drawable.pen);
                    // Have a Stroke SElection tool local to NoteBuilderActivity and
                    // leave the WorkspaceState for MainActivity
                    mNoteView.mCurrentTool = AppConstants.TOOL_STROKE;
                    // AppConstants.LOG(AppConstants.CRITICAL, TAG, "STROKE SET ");
                    // http://stackoverflow.com/questions/6690530/how-to-show-one-layout-on-top-of-the-other-programmatically-in-my-case
                    mNoteView.bringToFront();
                } else {
                    // setting to grey pen explicitly immediately
                    strokeAction.setImageResource(R.drawable.pen_b);
                    mNoteView.mCurrentTool = AppConstants.TOOL_NO_SHAPE;
                    et.setFocusable(true);
                    et.setClickable(true);
                    et.bringToFront();
                    // AppConstants.LOG(AppConstants.CRITICAL, TAG,
                    // "STROKE UNSET ");
                }

                break;
            case R.id.eraserActionIv:
                AppConstants.LOG(AppConstants.INFO, TAG, "Tool changed: eraserActionIv");
                break;
            case R.id.doneActionTv:
            case R.id.doneActionTLL:
                setKeyboardStatus(et, true);
                if (mIsCard) {
                    // mIsNew means we are building a card
                    if (!mIsNew) {

                        if (baseName != null) {
                            if (!baseName.equals(WorkSpaceState.getInstance().mNoteModel.getBaseName())) {
                                WorkSpaceState.getInstance().mNoteModel.sendToWSServerTemplate();
                            }
                        }

                        // Setting text should flip to updated state in the model.
                        WorkSpaceState.getInstance().mNoteModel.setText(et.getText().toString());

                        finish(CODE_OK_EDIT, 0);

                    } else {

                        // Setting text should flip to updated state in the model.
                        WorkSpaceState.getInstance().mNoteModel.setText(et.getText().toString());

                        // mBus.post(new
                        // ToolSelectionEvent(ToolSelectionEvent.CARD));
                        finish(CODE_OK_NEW, 0);

                    }
                } else {
                    finish(CODE_OK_EDIT_IMAGE, 0);
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_builder);
        init();

        if (this.mIsCard) {// If Card
            // Get the drawable.
            if (!mIsNew) {
                mModel = (NoteModel) WorkSpaceState.getInstance().getModelTree().getModel(mObjectID);
                // TODO kris clean this up and get the mDrawable and its model
                // from the mNoteBuilderActivityContext in CustomNoteCardView
                // mNoteView.setModel((NoteModel) mDrawable.getModel());
                mTemplate = mModel.getNoteTemplate();
                mTextStyle = mModel.getTextStyle();

                Bitmap bgBitmap = NetworkTask.getBitmapFromBaseName(mTemplate.getThumbnail(), WorkSpaceState.getInstance().mNoteModel);
                backgroundIV.setImageBitmap(bgBitmap);

            } else {
                // mDrawable = null;
                mModel = null;

                mTemplate = (NoteTemplate) WorkSpaceState.getInstance().getTemplate();
                mTextStyle = new TextStyle();
                // Here is where we build the card
                Rect rect = new Rect(0, 0, mTemplate.getWidth(), mTemplate.getHeight());
                // There is no et or text yet for a new note card and we are
                // creating the model upfront to use the strokes on model etc.
                // NoteModel noteModel = new NoteModel(rect, mTemplate,
                // et.getText().toString());

                WorkSpaceState.getInstance().mNoteModel = new NoteModel(rect, mTemplate, "");

                Bitmap bgBitmap = NetworkTask.getBitmapFromBaseName(mTemplate.getThumbnail(), WorkSpaceState.getInstance().mNoteModel);
                backgroundIV.setImageBitmap(bgBitmap);

            }

            // Set the edit text bitmap.
            setupTextEdit(et);

        } else {// If Image
            et.setVisibility(View.GONE);
            mImageModel = (ImageModel) WorkSpaceState.getInstance().getModelTree().getModel(mObjectID);
            showProgressDialog("Please Wait.. !");
            new GetImageURLTask(mImageModel).execute();
        }

    }

    private void buildToolbar() {
        toolbar = (Toolbar) findViewById(R.id.noteToolbar);
        strokeColorAction = (ImageView) toolbar.findViewById(R.id.strokeColorActionIv);
        strokeAction = (ImageView) toolbar.findViewById(R.id.strokeActionIv);
        if (!this.mIsCard) {
            toolbar.findViewById(R.id.noteAction).setVisibility(View.GONE);
            toolbar.findViewById(R.id.TextActionIv).setVisibility(View.GONE);
            ((TextView) toolbar.findViewById(R.id.activityName)).setText("Edit Mode");
        }
    }

    private void createCardDialog() {
        final Dialog dialog = customDialog(R.layout.create_card_dialog);

        LinearLayout topLL = (LinearLayout) dialog.findViewById(R.id.topLL);
        LinearLayout bottomLL = (LinearLayout) dialog.findViewById(R.id.bottomLL);
        LinearLayout leftLL = (LinearLayout) dialog.findViewById(R.id.leftLL);
        LinearLayout rightLL = (LinearLayout) dialog.findViewById(R.id.rightLL);

        topLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        bottomLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        rightLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        leftLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        GridView createCardDialogGv = (GridView) dialog.findViewById(R.id.createCardDialogGv);
        createCardDialogGv.setAdapter(new CreateCardDialogAdapter(getApplication()));

        createCardDialogGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Set the template so we have the proper template in the
                // builder.
                WorkSpaceState.getInstance().setTemplate(
                        AppSingleton
                                .getInstance()
                                .getApplication()
                                .getColorToTemplateMap()
                                .get(
                                        TemplateHelper.parseBaseNameToColor(new ArrayList<>(AppSingleton.getInstance().getApplication().getColorToUrlMap()
                                                .values()).get(position))));
                List<String> cardTemplatesList = new ArrayList<>(AppSingleton.getInstance().getApplication().getColorToUrlMap().values());

                WorkSpaceState.getInstance().mNoteModel.setBaseName(cardTemplatesList.get(position));
                Bitmap bgBitmap = NetworkTask.getBitmapFromURL(cardTemplatesList.get(position), WorkSpaceState.getInstance().mNoteModel);
                backgroundIV.setImageBitmap(bgBitmap);
                dialog.dismiss();
            }
        });
    }

    /**
     * finishes the darn thing with the proper result code.
     *
     * @param result
     */
    private void finish(int result, int code) {
        /**
         * TODO: We should really be returning the model here in the intent
         * data. We can serialize the model with Gson and set it as a string key
         * value pair. please no. things have ids.
         */
        Bundle conData = new Bundle();
        conData.putInt(KEY_RESULT, 0);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(result, intent);
        finish();
    }

    private int getImageKey(int pixelCount, ImageModel imageModel) {
        int area = (int) (imageModel.getHeight() * imageModel.getWidth());
        int numberOfStepsDown = 0;
        while (area > pixelCount) {
            area = area / 4;
            numberOfStepsDown++;
        }
        return AppConstants.LARGEST_IMAGE_ZOOM_LEVEL - numberOfStepsDown;
    }

    private void init() {

        // Grab the bundle
        Bundle bundle = getIntent().getBundleExtra(KEY_BUNDLE_EXTRA);

        this.mIsCard = bundle.getBoolean(IS_CARD);

        if (this.mIsCard) this.mIsNew = bundle.getBoolean(KEY_IS_NEW); // Get
        // data
        // from
        // the
        // bundle
        // to
        // build
        // object.

        // Get the objectID
        this.mObjectID = bundle.getString(KEY_OBJECT_ID);

        buildToolbar();
        et = (EditText) findViewById(R.id.editText);
        mNoteView = (CustomNoteCardView) findViewById(R.id.CustomNoteCardView);
        backgroundIV = (ImageView) findViewById(R.id.backgroundIV);

        if (WorkSpaceState.getInstance().mNoteModel != null)
            baseName = WorkSpaceState.getInstance().mNoteModel.getBaseName();

        // Hide keyboard unless editText is selected
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // To select pen by default
        onClick(toolbar.findViewById(R.id.strokeActionIv));
    }

    private void selectPen() {
        if (mNoteView.mCurrentTool != AppConstants.TOOL_STROKE) {
            strokeAction.setImageResource(R.drawable.pen);
            mNoteView.mCurrentTool = AppConstants.TOOL_STROKE;
        }
    }

    private void setStrokeDialogColours(StrokeColors selectedStrokeColor) {

        switch (selectedStrokeColor) {
            case WHITE:
                colourWhiteIv.setBackgroundResource(R.drawable.double_white_circle);
                break;
            case RED:
                colourRedIv.setBackgroundResource(R.drawable.double_red_circle);
                break;
            case YELLOW:
                colourYellowIv.setBackgroundResource(R.drawable.double_yellow_circle);
                break;
            case TEAL:
                colourTealTv.setBackgroundResource(R.drawable.double_teal_circle);
                break;
            case BLUE:
                colourBlueIv.setBackgroundResource(R.drawable.double_blue_circle);
                break;
            case PURPLE:
                colourPurpleIv.setBackgroundResource(R.drawable.double_purple_circle);
                break;
            case BLACK:
                colourBlackIv.setBackgroundResource(R.drawable.double_black_circle);
                break;
            case NO_COLOR:
                colourWhiteIv.setBackgroundResource(R.drawable.white_circle);
                colourRedIv.setBackgroundResource(R.drawable.red_circle);
                colourYellowIv.setBackgroundResource(R.drawable.yellow_circle);
                colourTealTv.setBackgroundResource(R.drawable.teal_circle);
                colourBlueIv.setBackgroundResource(R.drawable.blue_circle);
                colourPurpleIv.setBackgroundResource(R.drawable.purple_circle);
                colourBlackIv.setBackgroundResource(R.drawable.black_circle);
                break;
        }
    }

    /**
     * Sets up the edit text view for editing previously made notes.
     */
    private void setupTextEdit(EditText editText) {

        // Check to see if new, if it is then lets grab the text from it.
        String mText = "";
        if (!mIsNew) {
            mText = mModel.getText();
            if (mModel.getTextStyle().getTextTranform().equals(AppConstants.UPPER_CASE_STR)) {
                editText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
                editText.append(mText.toUpperCase());
            } else {
                editText.append(mText);
            }

        } else {
            mText = "";
            editText.setText(mText);
        }

        Typeface tf = Typeface.create("Dosis-Regular", Typeface.NORMAL);

        editText.setWidth(mTemplate.getWidth());
        // editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setTypeface(tf);
        editText.setTextColor(Color.WHITE);

        float mPaintSize = 42;
        if (!mIsNew) {
            mPaintSize = TextHelper.parseFontSize(mModel.getTextStyle().getFontSize());
        } else {
            mPaintSize = TextHelper.parseFontSize(mTextStyle.getFontSize());
        }

        editText.setTextSize(mPaintSize);
    }

    private void strokeColorDialog() {
        final Dialog dialog = customDialog(R.layout.new_card_stroke_color_dialog);

        LinearLayout topLL = (LinearLayout) dialog.findViewById(R.id.topLL);
        LinearLayout bottomLL = (LinearLayout) dialog.findViewById(R.id.bottomLL);
        LinearLayout leftLL = (LinearLayout) dialog.findViewById(R.id.leftLL);
        LinearLayout rightLL = (LinearLayout) dialog.findViewById(R.id.rightLL);

        colourWhiteIv = (TextView) dialog.findViewById(R.id.colourWhiteIv);
        colourRedIv = (TextView) dialog.findViewById(R.id.colourRedIv);
        colourYellowIv = (TextView) dialog.findViewById(R.id.colourYellowIv);
        colourTealTv = (TextView) dialog.findViewById(R.id.colourTealTv);
        colourBlueIv = (TextView) dialog.findViewById(R.id.colourBlueIv);
        colourPurpleIv = (TextView) dialog.findViewById(R.id.colourPurpleIv);
        colourBlackIv = (TextView) dialog.findViewById(R.id.colourBlackIv);

        topLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        bottomLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        rightLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        leftLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        setStrokeDialogColours(selectedStrokeColor);

        colourWhiteIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                selectedStrokeColor = StrokeColors.WHITE;
                strokeColorAction.setBackgroundResource(R.drawable.white_circle);

                colourWhiteIv.setBackgroundResource(R.drawable.double_white_circle);
                // set the wss color for stroke in case user chooses to draw a
                // stroke
                // Have a Stroke Color Selection tool local to
                // CustomNoteCardView and leave the WorkspaceState for
                // MainActivity
                // wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_WHITE);
                mNoteView.mSelectedStrokeColor = AppConstants.StrokeColor.BRUSH_COLOR_WHITE;
                selectPen();
            }
        });
        colourRedIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                selectedStrokeColor = StrokeColors.RED;

                strokeColorAction.setBackgroundResource(R.drawable.red_circle);

                colourRedIv.setBackgroundResource(R.drawable.double_red_circle);
                // set the wss color for stroke in case user chooses to draw a
                // stroke
                // Have a Stroke Color Selection tool local to
                // CustomNoteCardView and leave the WorkspaceState for
                // MainActivity
                // wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_RED);
                mNoteView.mSelectedStrokeColor = AppConstants.StrokeColor.BRUSH_COLOR_RED;
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "Color Selected : BRUSH_COLOR_RED" + Arrays.toString(mNoteView.mSelectedStrokeColor));

                selectPen();
            }
        });
        colourYellowIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                selectedStrokeColor = StrokeColors.YELLOW;

                strokeColorAction.setBackgroundResource(R.drawable.yellow_circle);

                colourYellowIv.setBackgroundResource(R.drawable.double_yellow_circle);
                // set the wss color for stroke in case user chooses to draw a
                // stroke
                // Have a Stroke Color Selection tool local to
                // CustomNoteCardView and leave the WorkspaceState for
                // MainActivity
                // wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_YELLOW);
                mNoteView.mSelectedStrokeColor = AppConstants.StrokeColor.BRUSH_COLOR_YELLOW;
                AppConstants.LOG(AppConstants.CRITICAL, TAG, "Color Selected : BRUSH_COLOR_YELLOW" + Arrays.toString(mNoteView.mSelectedStrokeColor));
                selectPen();

            }
        });
        colourTealTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                selectedStrokeColor = StrokeColors.TEAL;

                strokeColorAction.setBackgroundResource(R.drawable.teal_circle);

                colourTealTv.setBackgroundResource(R.drawable.double_teal_circle);
                // set the wss color for stroke in case user chooses to draw a
                // stroke
                // Have a Stroke Color Selection tool local to
                // CustomNoteCardView and leave the WorkspaceState for
                // MainActivity
                // wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_TEAL);
                mNoteView.mSelectedStrokeColor = AppConstants.StrokeColor.BRUSH_COLOR_TEAL;
                selectPen();
            }
        });
        colourBlueIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                selectedStrokeColor = StrokeColors.BLUE;

                strokeColorAction.setBackgroundResource(R.drawable.blue_circle);
                colourBlueIv.setBackgroundResource(R.drawable.double_blue_circle);
                // set the wss color for stroke in case user chooses to draw a
                // stroke
                // Have a Stroke Color Selection tool local to
                // CustomNoteCardView and leave the WorkspaceState for
                // MainActivity
                // wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_LIGHT_BLUE);
                mNoteView.mSelectedStrokeColor = AppConstants.StrokeColor.BRUSH_COLOR_LIGHT_BLUE;

                selectPen();
            }
        });
        colourPurpleIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                selectedStrokeColor = StrokeColors.PURPLE;

                strokeColorAction.setBackgroundResource(R.drawable.purple_circle);

                colourPurpleIv.setBackgroundResource(R.drawable.double_purple_circle);
                // set the wss color for stroke in case user chooses to draw a
                // stroke
                // Have a Stroke Color Selection tool local to
                // CustomNoteCardView and leave the WorkspaceState for
                // MainActivity
                // wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_PURPLE);
                mNoteView.mSelectedStrokeColor = AppConstants.StrokeColor.BRUSH_COLOR_PURPLE;
                selectPen();
            }
        });
        colourBlackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                selectedStrokeColor = StrokeColors.BLACK;

                strokeColorAction.setBackgroundResource(R.drawable.black_circle);

                colourBlackIv.setBackgroundResource(R.drawable.double_black_circle);
                // set the wss color for stroke in case user chooses to draw a
                // stroke
                // Have a Stroke Color Selection tool local to
                // CustomNoteCardView and leave the WorkspaceState for
                // MainActivity
                // wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_BLACK);
                mNoteView.mSelectedStrokeColor = AppConstants.StrokeColor.BRUSH_COLOR_BLACK;
                selectPen();

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        clearingOldStrokes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setKeyboardStatus(et,true);
    }

    private void clearingOldStrokes() {
        //Removing Recently Added Strokes on Card
        if (WorkSpaceState.getInstance().mNoteModel != null && WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes != null)
            WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes.removeAll(WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes);
        //Removing Recently Added Strokes on Image
        if (WorkSpaceState.getInstance().mImageModel != null && WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes != null)
            WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes.removeAll(WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes);
    }
}
