package com.bluescape.view.renderers;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.WorkSpaceModel;
import com.bluescape.view.shaders.LocationShaderProgram;
import com.bluescape.view.shaders.ShaderHelper;
import com.bluescape.view.shaders.ShaderType;
import com.bluescape.view.shaders.SimpleShaderProgram;
import com.bluescape.view.shaders.TextShaderProgram;
import com.bluescape.view.shaders.TextureShaderProgram;
import com.bluescape.view.shaders.WorkspaceShaderProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_ALWAYS;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_GREATER;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glBlendFunc;

import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;

public class Renderer implements GLSurfaceView.Renderer {

    private static final String TAG = Renderer.class.getSimpleName();

    private static int xxx = 0;

    private Context mContext;

    private long frameStartTimeMs = 0;

    private long startTimeMs;
    private int frameCount;

    public Renderer(Context context, int color) {
        this.mContext = context;
        WorkSpaceState.getInstance().context = context;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        // glClear( GL_DEPTH_BUFFER_BIT);
        // GLES20.glClearDepthf(-11f);

        GLES20.glClearDepthf(-11f);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //	if (WorkSpaceState.getInstance().mHistoryLoadCompleted)
        //		xxx++;

        //	if (WorkSpaceState.getInstance().mHistoryLoadCompleted) { if (xxx <=100 && xxx>50)
        //		 Debug.startMethodTracing("usecase" + xxx, 64000000); }
        //long start = System.currentTimeMillis();
        WorkSpaceState.getInstance().getModelTree().drawFullHistory();
        //AppConstants.LOG(AppConstants.CRITICAL, TAG, "External Draw Loop finishing in " + (System.currentTimeMillis() - start));

        //	 if(xxx<=100 && xxx>50 && WorkSpaceState.getInstance().mHistoryLoadCompleted)
        //	  Debug.stopMethodTracing();

        limitFrameRate(60);
        // logFrameRate();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        WorkSpaceState.getInstance().getModelTree().updateViewport(width, height);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_ALWAYS);//this is using the painter's algorithm, so we must sort by the model zorder and draw in ascending order
        // glDepthFunc(GL_GEQUAL);

        //Added for GroupNMove Color alpha transparency
        glEnable(GL10.GL_BLEND);
        glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        // ios uses this
        // GLES20.glClearDepthf(0f);
        // GLES20.glClearDepthf(-11f);
        // TODO kris enabled culling should look into depth testing also this
        // only looks at winding order
        // not showing strokes if enabled need to figure out
        // glEnable(GL_CULL_FACE);

        // Logic for simple shader, no texture needed.
        SimpleShaderProgram simpleShader = new SimpleShaderProgram(mContext);
        ShaderHelper.getInstance().putCompiledShader(ShaderType.Simple, simpleShader);

        // Logic for the background shader. Texture is passed in using
        // setUniforms and createTextureID
        WorkspaceShaderProgram backgroundShader = new WorkspaceShaderProgram(mContext);
        ShaderHelper.getInstance().putCompiledShader(ShaderType.Background, backgroundShader);

        // Logic for the text shader
        TextShaderProgram textShader = new TextShaderProgram(mContext);
        ShaderHelper.getInstance().putCompiledShader(ShaderType.Text, textShader);

        // Logic for the texture shader
        TextureShaderProgram textureShader = new TextureShaderProgram(mContext);
        ShaderHelper.getInstance().putCompiledShader(ShaderType.Texture, textureShader);

        // Logic for the location shader
        LocationShaderProgram locationShader = new LocationShaderProgram(mContext);
        ShaderHelper.getInstance().putCompiledShader(ShaderType.Location, locationShader);
        /**
         * TODO: set up texture compression.
         * http://stackoverflow.com/questions/9148795
         * /android-opengl-texture-compression
         */
        String s = GLES20.glGetString(GLES20.GL_EXTENSIONS);

        if (s.contains("GL_IMG_texture_compression_pvrtc")) {
            // Use PVR compressed textures
        } else if (s.contains("GL_AMD_compressed_ATC_texture") || s.contains("GL_ATI_texture_compression_atitc")) {
            // Load ATI Textures
        } else if (s.contains("GL_OES_texture_compression_S3TC") || s.contains("GL_EXT_texture_compression_s3tc")) {
            // Use DTX Textures
        } else {
            // Handle no texture compression founded.
        }
    }

    public void updateViewport(float zoom, float offsetX, float offsetY) {
        WorkSpaceState.getInstance().getModelTree().panAndZoomViewport(zoom, offsetX, offsetY);
    }

    private void limitFrameRate(int framesPerSecond) {
        long elapsedFrameTimeMs = SystemClock.elapsedRealtime() - frameStartTimeMs;
        long expectedFrameTimeMs = 1000 / framesPerSecond;
        long timeToSleepMs = expectedFrameTimeMs - elapsedFrameTimeMs;
        if (timeToSleepMs > 0) {
            SystemClock.sleep(timeToSleepMs);
        }
        frameStartTimeMs = SystemClock.elapsedRealtime();
    }

    private void logFrameRate() {
        long elapsedRealtimeMs = SystemClock.elapsedRealtime();
        double elapsedSeconds = (elapsedRealtimeMs - startTimeMs) / 1000.0;
        if (elapsedSeconds >= 1.0) {
            Log.v(TAG, frameCount / elapsedSeconds + "fps");
            startTimeMs = SystemClock.elapsedRealtime();
            frameCount = 0;
        }
        frameCount++;
    }
}
