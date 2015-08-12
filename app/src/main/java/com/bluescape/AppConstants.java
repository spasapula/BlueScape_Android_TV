package com.bluescape;

import android.content.pm.ApplicationInfo;
import android.util.Log;

public class AppConstants {

    // Brush = 2 Erase Constants
    public static class EraseBrushColor {
        public static final float[] BRUSH_COLOR_ERASE = {0, 0, 0, 255};
        public static final float[] BRUSH_COLOR_ERASE_SEND = {255, 255, 255, 1};

    }

    // History Event Basic Message Format Enum for Parsing first 5 values
    // https://github.com/Bluescape/thoughtstream/blob/develop/web-socket-protocol.md#delete
    // server <-- client
    // [client-id, "he", target-id, event-type, event-properties]
    public enum HISTORY_EVENT_FROM_CLIENT_MESSAGE_FORMAT {
        CLIENT_ID, HE, TARGET_ID, EVENT_TYPE, EVENT_PROPERTIES
    }

    // History Event Basic Message Format Enum for Parsing first 6 values from
    // server
    // https://github.com/Bluescape/thoughtstream/blob/develop/web-socket-protocol.md#delete
    // server --> client
    // [client-id, "he", target-id, event-id, event-type, event-properties]
    public enum HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT {
        CLIENT_ID, HE, TARGET_ID, EVENT_ID, EVENT_TYPE, EVENT_PROPERTIES
    }

    public enum MarkerColor {
        RED, YELLOW, GREEN, BLUE
    }

    // Stroke Color Selection Constants
    public static class StrokeColor {
        public static final float[] BRUSH_COLOR_WHITE = {222, 182, 222, 1};
        public static final float[] BRUSH_COLOR_RED = {213, 37, 46, 1};
        public static final float[] BRUSH_COLOR_YELLOW = {222, 173, 23, 1};
        public static final float[] BRUSH_COLOR_TEAL = {0, 182, 167, 255};
        public static final float[] BRUSH_COLOR_LIGHT_BLUE = {120, 201, 222, 1};
        public static final float[] BRUSH_COLOR_PURPLE = {131, 98, 173, 1};
        public static final float[] BRUSH_COLOR_BLACK = {67, 67, 67, 1};
    }

    // The server sends this message when the client connects to the socket.
    // Clients are required to store the assigned client ID for use in
    // subsequent socket requests.
    // // server --> client
    // ["-1", "id", client-id]
    // client-id (string) the ID of the newly-joined client
    public enum WS_CLIENT_CONNECT_EVENT_FROM_SERVER_MESSAGE_FORMAT {
        NUM_1, ID_STRING, CLIENT_ID
    }

    // Room Join Response:
    // //server --> client
    // ["-1", "room", [room-id], [databag]]
    public enum WS_JOIN_ROOM_EVENT_FROM_SERVER_MESSAGE_FORMAT {
        NUM_1, ROOM_STRING, ROOM_ID, DATABAG
    }

    // FROM CLIENT MESSAGES
    // WebSocket Message Format Enum for sending values to Server
    // https://github.com/Bluescape/thoughtstream/blob/develop/web-socket-protocol.md#jr-join-room
    // // server <-- client
    // server <-- client
    // [sender-id, "jr", "session", workspace-id]
    public enum WS_JR_SESSION_EVENT_FROM_CLIENT_MESSAGE_FORMAT {
        SENDER_ID, JR, SESSION, WORKSPACE_ID
    }

    // server --> client
    // ["-1", "rl", roomMembershipList]
    public enum WS_ROOM_LIST_EVENT_FROM_SERVER_MESSAGE_FORMAT {
        NUM_1, RL_STRING, ROOM_MEMBERSHIP_LIST
    }

    // View Port Change Response:
    // //server --> client
    // [sender-id, "vc", viewport-rect] vc
    public enum WS_VIEWPORT_CHANGE_EVENT_FROM_SERVER_MESSAGE_FORMAT {
        SENDER_ID, VC_STRING, VIEWPORT_RECT
    }

    // Volatile Event
    // //server --> client
    // [client-id, "ve", target-id, event-type, event-properties] ve
    public enum WS_VOLATILE_EVENT_FROM_SERVER_MESSAGE_FORMAT {
        CLIENT_ID, VE_STRING, TARGET_ID, EVENT_TYPE, EVENT_PROPERTIES
    }

    // configuration URLs to switch among Staging,Acceptance,Production
    public static final String CONFIGURATION_URLS[] = {
            "https://staging.configuration.bluescape.com/configuration.json",// Staging
            "https://instance2.configuration.bluescape.com/configuration.json",// Instance2
            "https://acceptance.configuration.bluescape.com/configuration.json", // Acceptance
            "https://configuration.bluescape.com/configuration.json" // Production

    };
    // Decision variable - Staging/Acceptance/Production
    public static final String CONFIGURATION = "configuration";
    // configuration URLs
    public static final String PORTAL_URL = "portal_url";

    public static final String HEALTH_URL = "health_url";
    public static final String BROWSE_CLIENT_URL = "browser_client_url";
    public static final String WS_COLLABORATION_SERVICE_ADDRESS = "ws_collaboration_service_address";
    // TODO kris need to change device to android when the WS collab server is
    // updated to accept and send back android as device type in rl message
    // "/socket?device=android"
    public static final String WS_COLLABORATION_SERVICE_URL_SUFFIX = "/socket?device=other";
    public static final String HTTP_COLLABORATION_SERVICE_ADDRESS = "http_collaboration_service_address";

    public static final String AUTHORIZATION_URL = "authentication_url";
    public static final String OAUTH_AUTHORIZATION_URL = "oauth_authorization_url";
    public static final String ASSET_BASE_URL = "assets_base_url";
    public static final String S3 = "s3";

    public static final String S3BUCKET = "bucket";

    public static final String COLLABORATION_SERVICE_PORT = "collaboration_service_port";
    public static final String BLUESCAPE = "bluescape";

    public static final String STAGING = "Staging";
    public static final String ACCEPTANCE = "Acceptance";
    public static final String PRODUCTION = "Production";

    public static final String INSTANCE2 = "Instance2";
    // URL
    public static final String SIGNIN_URL = "/users/sign_in.json";

    public static final String DASH_URL = "/dashboard";
    public static final String SESSION_URL = "sessions_url";
    public static final String FORGOT_PASSWORD = "/users/password/new";
    // Cookie
    public static final String COOKIE = "Cookie";

    // Validation
    public static final String ERR_EMAIL = "Email is empty";
    public static final String ERR_PASSWORD = "Password is empty";
    // User keys
    public static final String USER = "user";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    // Dialog messages
    public static final String MSG_SIGNING_IN = "Signing In...";

    public static final String MSG_SIGNING_OUT = "Signing Out...";

    // Shared preference keys
    public static final String SESSION_KEY = "session_key";

    public static final String SESSION_VALUE = "session_value";

    public static final String KEY_WORKSPACE_ID = "com.bluescape.workspaceID";
    public static final String CENTER_ON_OBJECT_ID = "center_on_objectid";

    public static final String WORKSPACE_COOKIE = "workspace_cookie";

    // History
    public static final String CREATE = "create";

    public static final String DELETE = "delete";

    public static final String POSITION = "position";

    public static final String STROKE = "stroke";
    public static final String PIN = "pin";
    public static final String MARKER_CREATE = "markercreate";
    /**
     * Logging for debugging.
     */
    public static final int VERBOSE = 5;
    public static final int INFO = 4;

    public static final int WARN = 3;

    public static final int ERROR = 2;

    public static final int CRITICAL = 1;

    /**
     * This pulls the debug or release value from the build. Set this in Gradle.
     */
    public static final boolean DEBUG = (0 != (AppSingleton.getInstance().getApplication().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));

    private static final int LOG_LEVEL = CRITICAL;
    public static final int DIALOG_LOADING = 1;

    public static final boolean SAVED = true;
    public static final boolean NOT_SAVED = false;

    // Image popup options
    public static final String CAMERA = "Camera";
    public static final String GALLERY = "Gallery";

    // Side Menu state indicators
    public static final int MENU_OPENED = 1;

    public static final int MENU_CLOSED = 0;

    public static final String CARD_TEMPLATES = "cardTemplates";
    public static final String HISTORY_URL_ARRAY = "historyURLArray";

    public static final float ORTHO_PROJECTION_FAR_Z = 250000;

    public static final int INT_ORTHO_PROJECTION_FAR_Z = 250000;
    public static final float LOCATION_MARKER_DEFAULT_ORDER = 250000;

    // Constants for Scaling
    // Observed Constants from iPad
    public static final float SCALE_CARD_MIN_WIDTH = 119.0587f;
    public static final float SCALE_CARD_MIN_HEIGHT = 68.033671f;

    public static final float SCALE_CARD_MAX_WIDTH = 7606.52435f;
    public static final float SCALE_CARD_MAX_HEIGHT = 4346.58537f;
    public static final float SCALE_WIDGET_GROWTH_FACTOR = 0.05f;

    public static final float SCALE_WIDGET_MINIMIZE_THRESHOLD = 1.5f;

    public static final float SCALE_WIDGET_MAXIMIZE_THRESHOLD = 0.85f;

    public static final float SCALE_SCROLL_THRESHOLD = 20.0f;
    // brush prop from he message for stroke to differencitate between stroke
    // and erase
    public static final int STROKE_BRUSH_NORMAL = 1;
    public static final int STROKE_BRUSH_ERASE = 2;

    public static final int STROKE_BRUSH_ERASE_SIZE = 20;

    public static final int MAX_HIGH_RES_AREA = 2048 * 2048;

    public static final int MAX_LOW_RES_AREA = 256 * 256;
    public static final int LARGEST_IMAGE_ZOOM_LEVEL = 11;
    public static final float MARKER_TO_WORKSPACE_STATE_ZOOM_RATIO = 0.17f;
    public static final float BORDER_TO_WORKSPACE_STATE_ZOOM_RATIO = 0.02f;
    public static final float INITIALS_TO_WORKSPACE_STATE_ZOOM_RATIO = 0.12f;

    //Time in Milli seconds to Show Activity Indicator Border Window AND Initials
    public static final long ACTIVITY_INDICATOR_TIME = 5000L;

    public static final float WORKSPACE_STATE_DEFAULT_ZOOM = 1800f;
    public static final String FROM_FLAG = "fromActivityFlag";

    public static final boolean FROM_LOGIN = true;
    public static final boolean FROM_WORKSPACE = false;
    // Constants for Default Card Width nad Height and the scaling factor for
    // strokes on cards and card edit and add window
    public static final int CARD_DEFAULT_WIDTH = 560;

    public static final int CARD_DEFAULT__HEIGHT = 320;
    public static final float CARD_DEFAULT_RATIO = 1.75f; // 560/320 = 1.75f
    // Pin/Delete popup magic numbers
    public static final int PINDELETE_POPUPX = 100;

    public static final int PINDELETE_POPUPY = 10;
    public static final int PINDELETE_LOCATION_POPUPX = 100;

    public static final int PINDELETE_LOCATION_POPUPY = 10;
    // Drawable-Model Constants
    public static final int BYTES_FLOAT = 4;

    public static final String URL = "url";

    // Tool Selection Events from Messages/ToolSelectionEvent
    /**
     * Currently selected Tool
     */
    public static final int TOOL_NO_SHAPE = 3;

    public static final int TOOL_STROKE = 5;
    public static final int TOOL_ERASER = 8;
    public static final int BYTES_PER_FLOAT = 4;

    public static final float PIN_X_LOCATION_START = 0.375f; // Magic Number
    // that puts the
    // Pin at the
    // right
    // location

    public static void LOG(int logLevel, String TAG, String message) {
        if (logLevel <= LOG_LEVEL && DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static final String UPPER_CASE_STR = "uppercase";
    public static final boolean UPPER_CASE = true;
    public static final boolean LOWER_CASE = false;

    public static final int CARD_MARGIN_X = 40;
    public static final int CARD_MARGIN_Y = 60;

    public static final float GROUP_WORKSPACE_INITIAL_BOTTOM_RIGHT = 999999999999f;
    public static final float GROUP_WORKSPACE_INITIAL_TOP_LEFT = -999999999999f;


}
