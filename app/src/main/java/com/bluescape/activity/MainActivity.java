package com.bluescape.activity;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.R;
import com.bluescape.collaboration.history.HistoryOrganizer;
import com.bluescape.collaboration.history.database.DBHelper;
import com.bluescape.collaboration.socket.SocketHandler;
import com.bluescape.collaboration.socket.SocketManager;
import com.bluescape.collaboration.socket.sender.UndoMessageSender;
import com.bluescape.collaboration.util.HttpHistoryCallBack;
import com.bluescape.collaboration.util.NetworkTask;
import com.bluescape.collaboration.util.TemplateHelper;
import com.bluescape.model.CollaboratorModel;
import com.bluescape.model.Group;
import com.bluescape.model.WorkSpaceModel;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.template.NoteTemplate;
import com.bluescape.model.util.Rect;
import com.bluescape.model.util.StrokeColors;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.BrowserModel;
import com.bluescape.model.widget.ImageModel;
import com.bluescape.model.widget.LocationMarkerModel;
import com.bluescape.model.widget.NoteModel;
import com.bluescape.util.WorkspaceConnectionChangeReceiver;
import com.bluescape.util.network.WebService;
import com.bluescape.view.SlideView;
import com.bluescape.view.ViewUtils;
import com.bluescape.view.adapter.CollaboratorsAdapter;
import com.bluescape.view.adapter.CreateCardDialogAdapter;
import com.bluescape.view.adapter.MarkersAdapter;
import com.bluescape.view.shaders.TextureHelper;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.DataAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MainActivity extends BaseActivity implements NumberPicker.OnValueChangeListener, WorkspaceUpdateListener {

    static class ClickListenerForScrolling implements View.OnClickListener {
        final SlideView scrollView;
        final View menu, app;
        private final View connectedUsersIV;
        private final View locationMarkersIV;

        public ClickListenerForScrolling(SlideView scrollView, View menu, View app, View connectedUsersIV, View locationMarkersIV) {
            super();
            this.scrollView = scrollView;
            this.menu = menu;
            this.app = app;
            this.connectedUsersIV = connectedUsersIV;
            this.locationMarkersIV = locationMarkersIV;

        }

        @Override
        public void onClick(View v) {
            locationMarkersIV.callOnClick();
            connectedUsersIV.callOnClick();
            int menuWidth = menu.getMeasuredWidth();
            int appWidth = app.getMeasuredWidth();
            int sideMenuSize = sideListLL.getMeasuredWidth() + sideOptRL.getMeasuredWidth();
            int finalSize = appWidth - sideMenuSize;
            if (menuStatus == AppConstants.MENU_CLOSED) {
                menuStatus = AppConstants.MENU_OPENED;
            } else if (menuStatus == AppConstants.MENU_OPENED) {
                menuStatus = AppConstants.MENU_CLOSED;
            }

            if (!IS_MENU_OUT) {
                scrollView.scrollTo(finalSize, 0);
                // scrollView.smoothScrollTo(left, 0);
            } else {
                scrollView.scrollTo(menuWidth, 0);
                // scrollView.smoothScrollTo(left, 0);
            }
            menu.setVisibility(View.VISIBLE);

            IS_MENU_OUT = !IS_MENU_OUT;
        }
    }

    static class SizeCallbackForMenu implements SlideView.SizeCallback {
        int btnWidth;
        final View btnSlide;

        public SizeCallbackForMenu(View btnSlide) {
            super();
            this.btnSlide = btnSlide;
        }

        @Override
        public void getViewSize(int idx, int w, int h, int[] dims) {
            dims[0] = w;
            dims[1] = h;
            final int menuIdx = 0;
            if (idx == menuIdx) {
                dims[0] = w - btnWidth;
            }
        }

        @Override
        public void onGlobalLayout() {
            // btnWidth = btnSlide.getMeasuredWidth();
        }
    }

    // TODO move this to historyorganizer

    private class sendToWallCallBack extends DataAsyncHttpResponseHandler {
        final Dialog sendToWallDialogView;
        final ProgressBar progress;
        final TextView infoText;

        sendToWallCallBack(Dialog sendToWallDialogView, ProgressBar progress, TextView infoText) {
            this.sendToWallDialogView = sendToWallDialogView;
            this.progress = progress;
            this.infoText = infoText;
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] responseBody, Throwable throwable) {
            infoText.setText("Oops,Your PIN was incorrect.\nPlease try again.");
            infoText.setTextColor(Color.parseColor("#ff0000"));
            AppConstants.LOG(AppConstants.INFO, TAG, "sendToWallCallBack - Failure responseBody = " + Arrays.toString(responseBody));
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onStart() {
            super.onStart();
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {
            progress.setVisibility(View.GONE);
            AppConstants.LOG(AppConstants.INFO, TAG, "sendToWallCallBack - Success responseBody = " + Arrays.toString(responseBody));
            sendToWallDialogView.dismiss();
            // TODO Kris toast message here for successfully sending to wall ?

        }

    }

    private DialogInterface.OnClickListener clickToDismiss = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            collaboratorDialog.dismiss();
            toolbar.setVisibility(View.VISIBLE);
        }
    };

    private Dialog collaboratorDialog;

    private static boolean isSelectionOn;
    private static final String TAG = MainActivity.class.getSimpleName();

    public static int historyJSONObjSize = -1;
    public static int historyJSONObjCount = 0;
    private static final int REQUEST_CODE_IMAGE_CHOOSER = 101;

    private static final int REQUEST_CODE_IMAGE_CAMERA = 102;

    private static boolean IS_MENU_OUT = false;
    private static int menuStatus = AppConstants.MENU_CLOSED;
    private static RelativeLayout sideOptRL, glSurfaceRL;
    private static LinearLayout sideListLL;

    private Toolbar toolbar;

    private SlideView scrollView;
    private View menu, markerLine, refView;

    // Set it ot the more specific CustomGLView this is actually the
    // RelativeLayout in glsurface_activity.xml resource
    private View app;
    private ImageView strokeColorAction;

    private ImageView strokeAction;

    private ImageView eraserAction, selectionAction;

    private TextView colourWhiteIv, colourRedIv, colourYellowIv, colourTealTv, colourBlueIv, colourPurpleIv, colourBlackIv;

    private ImageView connectedUsersIV;

    private ImageView locationMarkersIV;

    private TextView selectedListTv, addNewMarker;
    private LayoutInflater inflater;

    private ListView collaboratorList, markersList;

    public String getWorkspaceId() {
        return workspaceId;
    }

    private final String workspaceId = bluescapeApplication.getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, "");

    public DBHelper getDbObject() {
        return dbObject;
    }

    private final DBHelper dbObject = bluescapeApplication.getmDBHelper();

    private StrokeColors selectedStrokeColor = StrokeColors.BLUE;
    private final WorkSpaceState wss = WorkSpaceState.getInstance();
    private TextView activityName;
    private LinearLayout sideMenuLL;
    private boolean sendToWallPinEnteredStatus;

    private WorkSpaceModel workSpaceModel;

    private TextView collaboratorName, userNameTv;

    private ImageView memberIv, colorIv;

    private final WorkspaceUpdateListener workspaceUpdateListener = this;

    private final HistoryOrganizer historyOrganizer = new HistoryOrganizer(MainActivity.this, workspaceUpdateListener);

    private final View.OnClickListener addNewMarkerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (menuStatus == AppConstants.MENU_OPENED) {
                sideMenuLL.callOnClick();
            }
            createMarkerDialog();
        }
    };

    private final View.OnClickListener locationMarkersIVListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            markerLine.setVisibility(View.VISIBLE);
            markersList.setVisibility(View.VISIBLE);
            collaboratorList.setVisibility(View.GONE);

            selectedListTv.setText("Location Markers");
            locationMarkersIV.setImageResource(R.drawable.map);
            connectedUsersIV.setImageResource(R.drawable.connected_users_b);
            addNewMarker.setVisibility(View.VISIBLE);
        }
    };

    private final View.OnClickListener connectedUsersIVListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            markerLine.setVisibility(View.GONE);
            markersList.setVisibility(View.GONE);
            addNewMarker.setVisibility(View.GONE);
            collaboratorList.setVisibility(View.VISIBLE);

            locationMarkersIV.setImageResource(R.drawable.map_icon_b);
            connectedUsersIV.setImageResource(R.drawable.connected_users);
            selectedListTv.setText("Connected Users");
        }
    };

    private final View.OnClickListener exitWorkspaceIvListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    private final AdapterView.OnItemClickListener markersListListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LocationMarkerModel marker = workSpaceModel.getMarkersList().get(position);
            float[] offset = new float[2];
            offset[0] = marker.getX();
            offset[1] = marker.getY();
            WorkSpaceState.getInstance().setOffset(offset);
        }
    };

    private final AdapterView.OnItemClickListener collaboratorListListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (menuStatus == AppConstants.MENU_OPENED) {
                CollaboratorModel member = workSpaceModel.getRoomMembersList().get(position);

                sideMenuLL.callOnClick();
                toolbar.setVisibility(View.GONE);
                onSelectCollaboratorDialog();
                collaboratorName.setText(member.getName());

                if (member.getClientType().equals("wall")) {
                    userNameTv.setVisibility(View.INVISIBLE);
                    memberIv.setVisibility(View.VISIBLE);
                } else {
                    userNameTv.setText(member.initials());
                    userNameTv.setVisibility(View.VISIBLE);
                    memberIv.setVisibility(View.INVISIBLE);
                }

                colorIv.setBackgroundColor(Color.parseColor(member.getColor()));

                WorkSpaceModel.followingClientId = member.getClientId();

                float[] viewPort = member.getViewPort();

                float offsetX = (viewPort[Rect.BOTX] + viewPort[Rect.TOPX]) / 2;
                float offsetY = (viewPort[Rect.BOTY] + viewPort[Rect.TOPY]) / 2;

                float mZoom = ViewUtils.getZoomFromVcRect(viewPort[Rect.TOPY], viewPort[Rect.BOTY]);

                WorkSpaceState.getInstance().setZoomAndOffset(mZoom, offsetX, offsetY);
            }

        }

    };

    // =============================< Toolbar >=============================

    @Override
    public void callSocket() {
        SocketManager socketManager = new SocketManager();
        wss.mWebSocketClient = socketManager.createSocket(
                bluescapeApplication.getDataFromSharedPrefs(AppConstants.WS_COLLABORATION_SERVICE_ADDRESS, AppConstants.WS_COLLABORATION_SERVICE_ADDRESS)
                        + "/" + workspaceId + AppConstants.WS_COLLABORATION_SERVICE_URL_SUFFIX,
                bluescapeApplication.getDataFromSharedPrefs(AppConstants.WORKSPACE_COOKIE, ""), new SocketHandler());
        wss.mWebSocketClient.connect();
        // Set the listener in WorkSpaceModel to MainActivity
        WorkSpaceModel workSpaceModel = WorkSpaceState.getInstance().getWorkSpaceModel();
        // First set the WorkspaceId as it is now available after the history
        // call
        workSpaceModel.setId(workspaceId);
    }

    @Override
    public void closeSideMenu() {
        if (menuStatus == AppConstants.MENU_OPENED) {
            sideMenuLL.callOnClick();
        }
    }

    // =============================< Side Menu >=============================

    @Override
    public void createMarkerOnLongPress() {
        addNewMarker.callOnClick();
    }

    @Override
    public void displayProgressDialog() {
        showProgressDialog("Please Wait..");
    }

    @Override
    public void onBackPressed() {

        // TODO kris remove when exit workspace button shows on all devices
        // properly
        // currently not showing well on Sony Experia
        super.onBackPressed();

        // remove previous workspace cookie
        resetCookies();

        // clean up the WorkSpaceState for new Workspace
        wss.cleanUpWorkSpaceStateOnExit();

        Intent dashboardIntent = new Intent(MainActivity.this, DashboardActivity.class);
        dashboardIntent.putExtra(AppConstants.FROM_FLAG, AppConstants.FROM_WORKSPACE);
        startActivity(dashboardIntent);

        workSpaceModel.clearRoomMembersList();
        workSpaceModel.clearMarkersList();

        // For Killing OPENGL Context Explicitly in CustomGLView created in
        // pullViews()
        // this.finish();

    }

    public void onClick(View view) {
        int action = view.getId();
        switch (action) {
            case R.id.camAction:
                if (menuStatus == AppConstants.MENU_OPENED) {
                    sideMenuLL.callOnClick();
                }
                camDialog();
                break;
            case R.id.noteAction:
                if (menuStatus == AppConstants.MENU_OPENED) {
                    sideMenuLL.callOnClick();
                }
                createCardDialog();
                break;
            case R.id.undoAction:

                LayoutInflater layoutInflator = getLayoutInflater();
                View layout = layoutInflator.inflate(R.layout.undo_toast_layout, (ViewGroup) findViewById(R.id.undoToastLL));
                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setView(layout);// setting the view of custom toast layout
                toast.show();

                UndoMessageSender undoMessageSender = new UndoMessageSender();
                undoMessageSender.send();

                break;
            case R.id.sendToWallAction:
                sendToWallDialog();
                break;
            case R.id.selectionAction:
                if (!isSelectionOn) {
                    isSelectionOn = true;
                    selectionAction.setImageResource(R.drawable.select_mode_active);
                } else {
                    isSelectionOn = false;
                    selectionAction.setImageResource(R.drawable.select_mode);
                }
                break;
            case R.id.strokeColorAction:
                strokeColorDialog();
                eraserAction.setImageResource(R.drawable.erase_b);
                break;
            case R.id.strokeAction:
                // Set unset the erase tool have to toggle with erase tool
                if (wss.getCurrentTool() != AppConstants.TOOL_STROKE) {
                    // setting to blue pen explicitly immediately eliminating bus
                    // from menu selection for stroke
                    strokeAction.setImageResource(R.drawable.pen);
                    eraserAction.setImageResource(R.drawable.erase_b);
                    wss.setCurrentTool(AppConstants.TOOL_STROKE);
                    wss.mCurrentSelectedBaseModel = null;
                } else {
                    // setting to grey pen explicitly immediately
                    strokeAction.setImageResource(R.drawable.pen_b);
                    eraserAction.setImageResource(R.drawable.erase_b);
                    wss.setCurrentTool(AppConstants.TOOL_NO_SHAPE);
                    wss.mCurrentSelectedBaseModel = null;
                }
                break;
            case R.id.eraserAction:
                // Set unset the erase tool have to toggle with stroke tool
                if (wss.getCurrentTool() != AppConstants.TOOL_ERASER) {
                    // setting to blue pen explicitly immediately eliminating bus
                    // from menu selection for stroke
                    eraserAction.setImageResource(R.drawable.erase);
                    strokeAction.setImageResource(R.drawable.pen_b);

                    wss.setCurrentTool(AppConstants.TOOL_ERASER);
                    wss.mCurrentSelectedBaseModel = null;
                } else {
                    // setting to grey pen explicitly immediately
                    eraserAction.setImageResource(R.drawable.erase_b);
                    wss.setCurrentTool(AppConstants.TOOL_NO_SHAPE);
                    wss.mCurrentSelectedBaseModel = null;
                }

                break;
        }
    }

    public void onLongPressOptionsPopup() {
        // Close Side menu if it is opened
        if (menuStatus == AppConstants.MENU_OPENED) {
            sideMenuLL.callOnClick();
        }

        float viewWidth = glSurfaceRL.getWidth();
        float viewHeight = glSurfaceRL.getHeight();
        View popupView;
        TextView deleteCardBTN, pinCardBTN = null;
        TextView deselectGroupBTN = null;

        View line = null;
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        if (wss.mCurrentSelectedBaseModel instanceof LocationMarkerModel)
            popupView = layoutInflater.inflate(R.layout.locationmarker_popup_layout, null);
        else if (wss.mCurrentSelectedBaseModel instanceof BrowserModel)
            popupView = layoutInflater.inflate(R.layout.browser_popup_layout, null);
        else if (wss.mCurrentSelectedBaseModel instanceof Group)
            popupView = layoutInflater.inflate(R.layout.group_foreign_popup_layout, null);
        else
            popupView = layoutInflater.inflate(R.layout.widget_options_layout, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        if (wss.mCurrentSelectedBaseModel instanceof LocationMarkerModel) {
            deleteCardBTN = (TextView) popupView.findViewById(R.id.deleteCardBTN);
            ((TextView) popupView.findViewById(R.id.MarkerNameTV)).setText(((LocationMarkerModel) wss.mCurrentSelectedBaseModel).getMarkerName());
        } else if (wss.mCurrentSelectedBaseModel instanceof BrowserModel) {
            deleteCardBTN = (TextView) popupView.findViewById(R.id.deleteCardBTN);
        } else if (wss.mCurrentSelectedBaseModel instanceof Group) {
            deselectGroupBTN = (TextView) popupView.findViewById(R.id.deselectGroupBTN);
            deleteCardBTN = null;
        } else {
            pinCardBTN = (TextView) popupView.findViewById(R.id.pinCardBTN);
            deleteCardBTN = (TextView) popupView.findViewById(R.id.deleteCardBTN);

            if (wss.mCurrentSelectedBaseModel.isPinned()) {
                assert pinCardBTN != null;
                pinCardBTN.setText("Unpin");
            } else {
                assert pinCardBTN != null;
                pinCardBTN.setText("Pin");
            }

            pinCardBTN.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    wss.mCurrentSelectedBaseModel.sendToWSServerPin();
                    popupWindow.dismiss();
                }
            });
        }

        if (deleteCardBTN != null) {
            deleteCardBTN.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    (wss.mCurrentSelectedBaseModel).sendToWSServerHide();
                    WorkSpaceState.getInstance().getModelTree().delete(wss.mCurrentSelectedBaseModel.getID());
                    popupWindow.dismiss();
                }
            });
        }

        if (deselectGroupBTN != null) {
            deselectGroupBTN.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //TODO add the method to handle group unselect
//                    (wss.mCurrentSelectedBaseModel).sendToWSServerHide();
//                    WorkSpaceState.getInstance().getModelTree().delete(wss.mCurrentSelectedBaseModel.getID());
                    popupWindow.dismiss();
                }
            });
        }

        BaseWidgetModel baseWidgetModel = wss.mCurrentSelectedBaseModel;
        baseWidgetModel.getHeight();
        baseWidgetModel.getWidth();

        // Get the Parent Window coordinates from the model itself TODO need to
        // implement for image
        float[] parentViewXYCoords = baseWidgetModel.getParentViewXYCoords(viewWidth, viewHeight);
        // popupWindow.showAtLocation(refView, Gravity.NO_GRAVITY, viewX,
        // viewY);
        // dimensions of R.layout.card_options_layout magic numbers 100 and 10
        // based on dp widths in layout file
        // android:layout_width="50dp"
        // android:layout_height="40dp" />
        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "BEFORE  popupWindow.getWidth()=" + popupWindow.getWidth() +
        // "  popupWindow.getHeight()=" + popupWindow.getHeight());
        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "BEFORE  (int)parentViewXYCoords[]=" +
        // Arrays.toString(parentViewXYCoords));
        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "BEFORE  (int)parentViewXYCoords[0]=" + (int) parentViewXYCoords[0] +
        // "  (int)parentViewXYCoords[1]" + (int) parentViewXYCoords[1]);
        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "BEFORE  (int)parentViewXYCoords[2]=" + (int) parentViewXYCoords[2] +
        // "  (int)parentViewXYCoords[3]" + (int) parentViewXYCoords[3]);
        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "BEFORE  (int)parentViewXYCoords[4]=" + (int) parentViewXYCoords[4] +
        // "  (int)parentViewXYCoords[5]" + (int) parentViewXYCoords[5]);
        if (wss.mCurrentSelectedBaseModel instanceof LocationMarkerModel) {
            popupWindow.showAtLocation(refView, Gravity.NO_GRAVITY,
                    (int) (parentViewXYCoords[Rect.MIDTOPX] - AppConstants.PINDELETE_LOCATION_POPUPX), (int) parentViewXYCoords[Rect.MIDTOPY]
                            - AppConstants.PINDELETE_LOCATION_POPUPY);
        } else {
            popupWindow.showAtLocation(refView, Gravity.NO_GRAVITY, (int) parentViewXYCoords[Rect.MIDTOPX] - AppConstants.PINDELETE_POPUPX,
                    (int) parentViewXYCoords[Rect.MIDTOPY] - AppConstants.PINDELETE_POPUPY);
        }
        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "AFTER  popupView.getWidth()=" + popupView.getWidth() +
        // "  popupView.getHeight()=" + popupView.getHeight());
        // AppConstants.LOG(AppConstants.CRITICAL, TAG,
        // "AFTER  popupWindow.getWidth()=" + popupWindow.getWidth() +
        // "  popupWindow.getHeight()=" + popupWindow.getHeight());
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i2) {
        // Here is the changes
        wss.setCurrentLineWidth(i2 * 500);
    }

    @Override
    public void openBrowser(String url) {
        Intent browserIntent = new Intent(MainActivity.this, BrowserActivity.class);
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        browserIntent.putExtra(AppConstants.URL, url);
        startActivity(browserIntent);
    }

    // =============================< Dialogs >=============================

    // -----------< Cam Dialog >-----------

    @Override
    public void openPDF(String name) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluescape/" + name);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    /**
     * Dialog to pick the size of the line.
     */
    public void showDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Line Size");
        dialog.setContentView(R.layout.dialog);
        final NumberPicker np = (NumberPicker) dialog.findViewById(R.id.numberPicker1);
        np.setMaxValue(10);
        np.setMinValue(1);
        np.setValue((int) wss.getCurrentLineWidth());
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);

        dialog.findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wss.setCurrentLineWidth(np.getValue() * 500);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void stopProgressDialog() {
        dismissProgressDialog();
    }

    // -----------< Create New Note Dialog >-----------

    @Override
    public void updateCollaboratorsList(final List<CollaboratorModel> collaborators) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buildMemberList(collaborators);
            }
        });

    }

    // -----------< Send to wall Dialog >-----------

    @Override
    public void updateMarkersList(final List<LocationMarkerModel> markersList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buildMarkersList(markersList);
            }
        });
    }

    @Override
    public void updateWorkspaceTitle(final String workspaceName) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activityName.setText(workspaceName);
            }
        });
    }

    @Override
    public void onReconnectingToNetwork(String historyURLsString) {
        getJSONHistory(historyURLsString);
    }

    // -----------< Stroke colors Dialog >-----------

    /**
     * Logic for returning an image. This will set the URI in the DSM for later
     * use when building the image widget.
     *
     * @param requestCode
     * @param resultCode
     * @param imageReturnedIntent
     */

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case REQUEST_CODE_IMAGE_CHOOSER:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    wss.setImageUri(selectedImage);
                    wss.setImageRealPath(getRealPathFromURI(selectedImage));
                    buildImage();
                }
                break;
            case REQUEST_CODE_IMAGE_CAMERA:
                if (resultCode == RESULT_OK) {
                    buildImage();

                } else {
                }
                break;
            case NoteBuilderActivity.REQUEST_CODE:

                switch (resultCode) {
                    case NoteBuilderActivity.CODE_OK_NEW:
                        buildCard(wss.mNoteModel);
                        WorkSpaceState.getInstance().mNoteModel = null;
                        break;
                    case NoteBuilderActivity.CODE_OK_EDIT:
                        // We could grab the new text and the object here from the
                        // intent data.
                        // TODO kris Send to server only if things change ?? should be
                        // tracking that
                        WorkSpaceState.getInstance().mNoteModel.sendToWSServerEdit();

                        // Set the recently added strokes to null and free up space as
                        // the newly added strokes will show up in the
                        // existing strokes next time th user clicks
                        WorkSpaceState.getInstance().mNoteModel.mRecentlyAddedStrokes = null;
                        WorkSpaceState.getInstance().mNoteModel = null;

                        break;
                    case NoteBuilderActivity.CODE_CANCELLED_NEW:
                        // We can clear the model, template etc in the DSM
                        WorkSpaceState.getInstance().mNoteModel = null;

                        break;
                    case NoteBuilderActivity.CODE_CANCELLED_EDIT:
                        // Idk what we could do here.
                        WorkSpaceState.getInstance().mNoteModel = null;
                        WorkSpaceState.getInstance().mImageModel = null;
                        break;

                    case NoteBuilderActivity.CODE_OK_EDIT_IMAGE:

                        WorkSpaceState.getInstance().mImageModel.sendToWSServerEdit();

                        WorkSpaceState.getInstance().mImageModel.mRecentlyAddedStrokes = null;
                        WorkSpaceState.getInstance().mImageModel = null;
                        break;
                    case RESULT_CANCELED:
                        // Handle Crash case for Child NoteBuilderActivity
                        break;
                }
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check Heap Size
        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        Log.v("onCreate", "maxMemory:" + Long.toString(maxMemory));
        AppConstants.LOG(AppConstants.CRITICAL, TAG, "maxMemory:" + Long.toString(maxMemory));

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        Log.v("onCreate", "memoryClass:" + Integer.toString(memoryClass));
        AppConstants.LOG(AppConstants.CRITICAL, TAG, "memoryClass:" + Integer.toString(memoryClass));

        int SDK_INT = 9;
        if (Build.VERSION.SDK_INT >= SDK_INT) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Creating the WorkSpaceDrawable and WorkSpaceModel so both the UI
        // thread, Network Background thread and GL Thread can all update it
        workSpaceModel = new WorkSpaceModel(AppSingleton.getInstance().getApplication().getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, ""));
        wss.setWorkSpaceModel(workSpaceModel);
        workSpaceModel.setWorkspaceUpdateListener(this);

        WorkSpaceState.getInstance().currentActivity = this;

        inflater = LayoutInflater.from(this);
        scrollView = (SlideView) inflater.inflate(R.layout.slide_view, null);
        setContentView(scrollView);

        pullViews();

        final View[] children = new View[]{menu, app};

        int SCROLL_OUT_SIDE_MENU = 1;
        scrollView.initViews(children, SCROLL_OUT_SIDE_MENU, new SizeCallbackForMenu(toolbar));

        buildToolbar();

        getJSONHistory(getDataFromBundle());
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case AppConstants.DIALOG_LOADING:
                final Dialog dialog = new Dialog(this, R.style.BluescapeThemeTranslucent);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                // here we set layout of progress dialog
                dialog.setContentView(R.layout.custom_progress_dialog);
                dialog.setCancelable(false);
                return dialog;
            default:
                return null;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        setReceiverState(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        if (WorkSpaceState.getInstance().mIsRendererSet) {
            WorkSpaceState.getInstance().getWorkspaceView().onPause();
        }
    }

    private void setReceiverState(int receiverState) {
        ComponentName receiver = new ComponentName(this, WorkspaceConnectionChangeReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver, receiverState, PackageManager.DONT_KILL_APP);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setReceiverState(PackageManager.COMPONENT_ENABLED_STATE_ENABLED);

        // Don't auto-show the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (WorkSpaceState.getInstance().mIsRendererSet) {
            WorkSpaceState.getInstance().getWorkspaceView().onResume();
        }
        //reconnecting to socket
        if (wss.mWebSocketClient != null)
            wss.mWebSocketClient.connect();
    }

    // Simplified buildCard
    // private void buildCard(BuildWidgetEvent event) {
    private void buildCard(NoteModel noteModel) {

        float topx = WorkSpaceState.getInstance().getOffset()[0];
        float topy = WorkSpaceState.getInstance().getOffset()[1];

        // just checking if the values are the same .. they are
        // float topx =
        // WorkSpaceState.getInstance().getWorldPosition()[Rect.TOPX] + (
        // (WorkSpaceState.getInstance().getWorldPosition()[Rect.BOTX] -
        // WorkSpaceState.getInstance().getWorldPosition()[Rect.TOPX]) / 2 );
        // float topy =
        // WorkSpaceState.getInstance().getWorldPosition()[Rect.TOPY] +
        // ((WorkSpaceState.getInstance().getWorldPosition()[Rect.BOTY] -
        // WorkSpaceState.getInstance().getWorldPosition()[Rect.TOPY]) / 2 );

        float width = noteModel.getWidth();
        float height = noteModel.getHeight();

        Rect rect = new Rect(topx, topy, topx + width, topy + height);

        noteModel.setRect(rect);

        try {
            // Set card target ID to the workspace.
            noteModel.setTargetID(bluescapeApplication.getWorkspaceID());
        } catch (Exception e) {
            noteModel.setTargetID("");
            e.printStackTrace();
        }

        noteModel.sendToWSServer();
    }

    private void buildCardAction() {
        /**
         * Here is the debugging of the template usage. We are setting a
         * template in the dsm. Normally the user would select a color and we
         * would look up color to template in the app class. This map is
         * populated in the network call that gets the templates.
         */
        String defaultTemplate = "{\"id\":\"52f97d502c22897852660f20\",\"width\":560,\"height\":320,\"thumbnail\":\"card_templates/thumbnails/Teal.jpeg\"}";
        Gson gson = new Gson();
        NoteTemplate template = gson.fromJson(defaultTemplate, NoteTemplate.class);
        AppConstants.LOG(AppConstants.VERBOSE, TAG, template.toString());

        launchNoteBuilder();
    }

    /**
     * Simplified image builder at center of workspace
     *
     * @param
     */

    private void buildImage() {
        displayProgressDialog();
        // Get the size of the bitmap
        Bitmap bitmap = TextureHelper.convertUriToBitmap(wss.getImageUri());

        float height, width;
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        float topx = WorkSpaceState.getInstance().getOffset()[0];
        float topy = WorkSpaceState.getInstance().getOffset()[1];

        Rect rect = new Rect(topx, topy, topx + width, topy + height);

        // Build a simpler ImageModel that just has sufficient info to send to
        // server instead of allocating the floats like if we were going to draw
        // it on opengl
        // The draw happens after the successful http upload and subsequent
        // create web-socket message
        ImageModel imageModel = new ImageModel(rect);
        imageModel.sendToWSServer();

    }

    /**
     * Handles logic to build an image card
     */
    private void buildImageChooseImage() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_CODE_IMAGE_CHOOSER);
    }

    /**
     * Handles logic to build an image card
     */
    private void buildImageTakePic() {
//		Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		startActivityForResult(takePicture, REQUEST_CODE_IMAGE_CAMERA);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "bluescape" + (new Random().nextInt()));
        Uri selectedImage = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        wss.setImageUri(selectedImage);
        wss.setImageRealPath(getRealPathFromURI(selectedImage));
        Intent intentPicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentPicture.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
        startActivityForResult(intentPicture, REQUEST_CODE_IMAGE_CAMERA);
    }

    private void buildMarkersList(List<LocationMarkerModel> markers) {
        MarkersAdapter markersAdapter = (MarkersAdapter) markersList.getAdapter();

        if (markersAdapter != null) {
            markersAdapter.updateDealsResult(markers);
        } else {

            markersAdapter = new MarkersAdapter(getApplicationContext(), markers);

            markersList.setAdapter(markersAdapter);
        }
    }

    private void buildMemberList(List<CollaboratorModel> collaborators) {
        CollaboratorsAdapter membersAdapter = (CollaboratorsAdapter) collaboratorList.getAdapter();

        if (membersAdapter != null) {
            membersAdapter.updateDealsResult(collaborators);
        } else {

            membersAdapter = new CollaboratorsAdapter(getApplicationContext(), collaborators);

            collaboratorList.setAdapter(membersAdapter);
        }
    }

    // ================

    private void buildToolbar() {

        // Toolbar
        toolbar = (Toolbar) app.findViewById(R.id.toolbar);

        strokeColorAction = (ImageView) app.findViewById(R.id.strokeColorAction);
        strokeAction = (ImageView) app.findViewById(R.id.strokeAction);
        eraserAction = (ImageView) app.findViewById(R.id.eraserAction);
        selectionAction = (ImageView) app.findViewById(R.id.selectionAction);
        strokeColorAction = (ImageView) toolbar.findViewById(R.id.strokeColorAction);
        ImageView homeIcon = (ImageView) app.findViewById(R.id.sideMenu);
        activityName = (TextView) app.findViewById(R.id.activityName);
        sideMenuLL = (LinearLayout) app.findViewById(R.id.sideMenuLL);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle("");

        homeIcon.setVisibility(View.VISIBLE);

        sideMenuLL.setOnClickListener(new ClickListenerForScrolling(scrollView, menu, app, connectedUsersIV, locationMarkersIV));

    }

    private void camDialog() {

        final Dialog dialog = customDialog(R.layout.cam_dialog);
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

        TextView camera = (TextView) dialog.findViewById(R.id.camera);
        TextView gallery = (TextView) dialog.findViewById(R.id.gallery);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                buildImageTakePic();

            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                buildImageChooseImage();
            }
        });
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
                wss.setTemplate(AppSingleton
                        .getInstance()
                        .getApplication()
                        .getColorToTemplateMap()
                        .get(
                                TemplateHelper.parseBaseNameToColor(new ArrayList<>(AppSingleton.getInstance().getApplication().getColorToUrlMap().values())
                                        .get(position))));
                dialog.dismiss();

                buildCardAction();
            }
        });
    }

    private void createMarkerDialog() {
        final Dialog dialog = customDialog(R.layout.locatiuon_marker_dialog);

        final EditText markerName = (EditText) dialog.findViewById(R.id.markerName);
        TextView cancelMarkerCreation = (TextView) dialog.findViewById(R.id.cancelMarkerCreation);
        TextView createMarker = (TextView) dialog.findViewById(R.id.createMarker);

        LinearLayout rightLL = (LinearLayout) dialog.findViewById(R.id.rightLL);
        LinearLayout leftLL = (LinearLayout) dialog.findViewById(R.id.leftLL);
        LinearLayout topLL = (LinearLayout) dialog.findViewById(R.id.topLL);
        LinearLayout bottomLL = (LinearLayout) dialog.findViewById(R.id.bottomLL);

        cancelMarkerCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboardStatus(markerName, true);
                dialog.dismiss();
            }
        });

        rightLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboardStatus(markerName, true);

                dialog.dismiss();
            }
        });
        markerName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });
        rightLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboardStatus(markerName, true);

                dialog.dismiss();
            }
        });
        leftLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboardStatus(markerName, true);

                dialog.dismiss();

            }
        });
        topLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboardStatus(markerName, true);

                dialog.dismiss();

            }
        });
        bottomLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboardStatus(markerName, true);

                dialog.dismiss();

            }
        });

        createMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                Rect rect = new Rect(WorkSpaceState.getInstance().getOffset()[0], WorkSpaceState.getInstance().getOffset()[1], 0, 0);
                LocationMarkerModel locationMarkerModel = new LocationMarkerModel(rect, calendar.getTimeInMillis() / 1000, markerName.getText()
                        .toString(), 0);
                locationMarkerModel.sendToWSServer();
                setKeyboardStatus(markerName, true);
                dialog.dismiss();
            }
        });
    }

    private String getDataFromBundle() {
        String value = "";
        Bundle extras = getIntent().getExtras();
        WorkSpaceState.getInstance().getModelTree().clear();
        if (extras != null)
            value = extras.getString(AppConstants.HISTORY_URL_ARRAY);
        return value;
    }


    private void getJSONHistory(String historyURLsString) {
        try {
            JSONArray historyURLs = new JSONArray(historyURLsString);

            historyJSONObjCount = 0;
            historyJSONObjSize = historyURLs.length();
            String lastPath = historyURLs.get(historyURLs.length() - 1).toString();
            // If no history object
            if (historyJSONObjSize >= 0) {
                showDialog(AppConstants.DIALOG_LOADING);
            }
            // worth converting and factoring in, probably
            historyOrganizer.reset();
            for (int urlCount = 0; urlCount < historyURLs.length(); urlCount++) {
                historyOrganizer.prepareHistoryLoad(historyURLs.get(urlCount).toString());
            }
            for (int urlCount = 0; urlCount < historyURLs.length(); urlCount++) {

                AppConstants.LOG(AppConstants.INFO, TAG, "Fetching from : " + historyURLs.get(urlCount));

                boolean visitedStatus = dbObject.isAlreadyRetrievedWorkspaceJSON(dbObject, workspaceId, historyURLs.get(urlCount).toString());
                // if workspace is already visited we get history json array
                // form database otherwise we get json by requesting server
                // (last json not going to save in database)
                if (visitedStatus) {// (visitedStatus && urlCount <
                    // historyURLs.length()-1) {
                    AppConstants.LOG(AppConstants.CRITICAL, TAG, "HISTORY LOAD -cached- " + historyURLs.get(urlCount));

                    AppConstants.LOG(AppConstants.INFO, TAG, String.format("visitedStatus - %s%d", true, historyURLs.length()));
                    final String urlHist = historyURLs.get(urlCount).toString();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String dbResponse = dbObject.getVisitedWorkspaceJSON(dbObject, workspaceId, urlHist);
                                // starting history HistoryHandler thread with json
                                // saved in db
                                historyOrganizer.specifyHistory(urlHist, new JSONArray(dbResponse));
                            } catch (Exception e) {
                                AppConstants.LOG(AppConstants.CRITICAL, TAG, "We've got an exception! DEBUG THIS #TODO " + e.getMessage());

                                //   loadHistoryChunk(historyURLs, lastPath, urlCount);
                            }
                        }
                    }).start();

                } else { // if workspace not visited we are making n/w
                    // requesting
                    loadHistoryChunk(historyURLs, lastPath, urlCount);
                }

            }
            historyOrganizer.startedHistoryLoad = System.currentTimeMillis();
            new Thread(historyOrganizer).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void loadHistoryChunk(JSONArray historyURLs, String lastPath, int urlCount) throws JSONException {
        AppConstants.LOG(AppConstants.CRITICAL, TAG, "HISTORY LOAD -nocached- " + historyURLs.get(urlCount));

        AppConstants.LOG(AppConstants.INFO, TAG, "visitedStatus - " + false + historyURLs.length());
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.addHeader("Cookie", "" + bluescapeApplication.getDataFromSharedPrefs(AppConstants.WORKSPACE_COOKIE, ""));
        String userAgent = AppSingleton.getInstance().getUserAgent();
        asyncHttpClient.addHeader("user-agent", userAgent);
        AppConstants.LOG(AppConstants.CRITICAL, TAG, " User Agent " + userAgent);
        HttpHistoryCallBack httphistcallback = new HttpHistoryCallBack(historyURLs.get(urlCount).toString(), lastPath, historyOrganizer, this);
        httphistcallback.startTheClock = System.currentTimeMillis();
        asyncHttpClient.get(
                bluescapeApplication.getDataFromSharedPrefs(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS,
                        AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS) + historyURLs.get(urlCount), httphistcallback);
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        CursorLoader cursorLoader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * Launch the build card dialog.
     * <p/>
     * The dialog should set the model in the DSM. It should set the current
     * tool to note card.
     * <p/>
     * The note will be build when the user presses down on the screen. The
     * notebuilder dialog will set the model in the DSM with everyhting but the
     * coords of the touch which will be set when the touch happens.
     */
    private void launchNoteBuilder() {
        Intent intent = new Intent(this, NoteBuilderActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(NoteBuilderActivity.IS_CARD, true);
        bundle.putBoolean(NoteBuilderActivity.KEY_IS_NEW, true);
        intent.putExtra(NoteBuilderActivity.KEY_BUNDLE_EXTRA, bundle);

        startActivityForResult(intent, NoteBuilderActivity.REQUEST_CODE);
    }


    private void onSelectCollaboratorDialog() {
        collaboratorDialog = customDialog(R.layout.collaborator_dialog);

        LinearLayout topLL = (LinearLayout) collaboratorDialog.findViewById(R.id.topLL);
        LinearLayout bottomLL = (LinearLayout) collaboratorDialog.findViewById(R.id.bottomLL);
        LinearLayout leftLL = (LinearLayout) collaboratorDialog.findViewById(R.id.leftLL);
        LinearLayout rightLL = (LinearLayout) collaboratorDialog.findViewById(R.id.rightLL);

        topLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collaboratorDialog.dismiss();
                toolbar.setVisibility(View.VISIBLE);
                WorkSpaceModel.followingClientId = null;
            }
        });
        bottomLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collaboratorDialog.dismiss();
                toolbar.setVisibility(View.VISIBLE);
                WorkSpaceModel.followingClientId = null;
            }
        });
        rightLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collaboratorDialog.dismiss();
                toolbar.setVisibility(View.VISIBLE);
                WorkSpaceModel.followingClientId = null;
            }
        });
        leftLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collaboratorDialog.dismiss();
                toolbar.setVisibility(View.VISIBLE);
                WorkSpaceModel.followingClientId = null;
            }
        });

        collaboratorName = (TextView) collaboratorDialog.findViewById(R.id.collaboratorName);
        userNameTv = (TextView) collaboratorDialog.findViewById(R.id.userNameTv);
        colorIv = (ImageView) collaboratorDialog.findViewById(R.id.colorIv);
        memberIv = (ImageView) collaboratorDialog.findViewById(R.id.memberIv);

    }

    private void pullViews() {
        menu = inflater.inflate(R.layout.side_menu, null);
        app = inflater.inflate(R.layout.glsurface_activity, null);

        // Set it to true so that when it is resumed on coming back from Note
        // Card add etc. it is good to go
        WorkSpaceState.getInstance().getWorkspaceView().setPreserveEGLContextOnPause(true);

        refView = app.findViewById(R.id.refView);
        glSurfaceRL = (RelativeLayout) app.findViewById(R.id.glSurfaceRL);

        ImageView exitWorkspaceIv = (ImageView) menu.findViewById(R.id.exitWorkspaceIv);
        exitWorkspaceIv.setOnClickListener(exitWorkspaceIvListener);

        selectedListTv = (TextView) menu.findViewById(R.id.selectedListTv);
        selectedListTv.setText("Connected Users");// Initials Name

        collaboratorList = (ListView) menu.findViewById(R.id.collaboratorsList);
        collaboratorList.setOnItemClickListener(collaboratorListListener);

        markerLine = menu.findViewById(R.id.markerLine);

        markersList = (ListView) menu.findViewById(R.id.markersList);
        markersList.setOnItemClickListener(markersListListener);

        addNewMarker = (TextView) menu.findViewById(R.id.addNewMarker);
        addNewMarker.setOnClickListener(addNewMarkerListener);

        sideListLL = (LinearLayout) menu.findViewById(R.id.sideListLL);
        sideOptRL = (RelativeLayout) menu.findViewById(R.id.sideOptRL);

        connectedUsersIV = (ImageView) menu.findViewById(R.id.connectedUsersIV);
        connectedUsersIV.setImageResource(R.drawable.connected_users);
        connectedUsersIV.setOnClickListener(connectedUsersIVListener);

        locationMarkersIV = (ImageView) menu.findViewById(R.id.locationMarkersIV);
        locationMarkersIV.setImageResource(R.drawable.map_icon_b);
        locationMarkersIV.setOnClickListener(locationMarkersIVListener);

    }

    private void selectPen() {
        if (wss.getCurrentTool() != AppConstants.TOOL_STROKE) {
            strokeAction.setImageResource(R.drawable.pen);
            wss.setCurrentTool(AppConstants.TOOL_STROKE);
        }
    }

    private void sendToWallDialog() {

        final Dialog dialog = customDialog(R.layout.send_to_wall_dialog);

        LinearLayout topLL = (LinearLayout) dialog.findViewById(R.id.topLL);
        LinearLayout bottomLL = (LinearLayout) dialog.findViewById(R.id.bottomLL);
        LinearLayout leftLL = (LinearLayout) dialog.findViewById(R.id.leftLL);
        LinearLayout rightLL = (LinearLayout) dialog.findViewById(R.id.rightLL);

        final ProgressBar progress = (ProgressBar) dialog.findViewById(R.id.progress);
        final TextView infoText = (TextView) dialog.findViewById(R.id.infoText);
        final EditText pin = (EditText) dialog.findViewById(R.id.pin);
        final TextView sendToWallText = (TextView) dialog.findViewById(R.id.sendToWallText);
        sendToWallPinEnteredStatus = false;
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                sendToWallText.setTextColor(Color.parseColor("#0000ff"));
                if (pin.getText().toString().length() > 0) sendToWallPinEnteredStatus = true;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        pin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });
        pin.addTextChangedListener(textWatcher);

        sendToWallText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendToWallPinEnteredStatus) {
                    RequestParams requestParams = new RequestParams();
                    requestParams.put("pin", pin.getText().toString());

                    NetworkTask sendToWallTask = new NetworkTask.sendToWallTask();
                    WebService webService = new WebService();
                    webService.makeCall(sendToWallTask, requestParams, new sendToWallCallBack(dialog, progress, infoText));
                }
            }
        });
        topLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboardStatus(pin, true);
                dialog.dismiss();
            }
        });
        bottomLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboardStatus(pin, true);
                dialog.dismiss();
            }
        });
        rightLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboardStatus(pin, true);
                dialog.dismiss();
            }
        });
        leftLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setKeyboardStatus(pin, true);
                dialog.dismiss();
            }
        });
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

    private void strokeColorDialog() {
        final Dialog dialog = customDialog(R.layout.stroke_color_dialog);

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
                wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_WHITE);
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
                wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_RED);

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
                wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_YELLOW);
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
                wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_TEAL);

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
                wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_LIGHT_BLUE);

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
                wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_PURPLE);

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
                wss.setSelectedStrokeColor(AppConstants.StrokeColor.BRUSH_COLOR_BLACK);
                selectPen();

            }
        });
    }

    @Override
    public void onFollowingUserExit() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String exitedUser = collaboratorName.getText().toString();
                showAlert(exitedUser + " has just exited.", "", "Click to dismiss...", clickToDismiss, null, null, false);
            }
        });
    }

}
