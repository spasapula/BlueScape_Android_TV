package com.bluescape.collaboration.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.ImageModel;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by keefe on 6/5/15.
 */
public class ImageFetcher {

	class GetImageTask implements Runnable {
		final ImageModel imageModel;

		private boolean firstRun = true;

		public GetImageTask(ImageModel imageModel) {
			this.imageModel = imageModel;
		}

		public void run() {

			String fullUrl = AppSingleton.getInstance().getApplication()
					.getDataFromSharedPrefs(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS, AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS)
					+ "/"
					+ AppSingleton.getInstance().getApplication()
					.getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, AppConstants.KEY_WORKSPACE_ID)
					+ "/asset_url?key="
					+ imageModel.getBaseName()
					+ "-"
					+ getImageKey(AppConstants.MAX_LOW_RES_AREA, imageModel, firstRun)
					+ "."
					+ imageModel.mExtension;
			AppConstants.LOG(AppConstants.CRITICAL, "ImageFetcher",
					"Let's Get " + fullUrl);
			try {
				// TODO replace deprecated methods
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(fullUrl);
				get.setHeader("Cookie", AppSingleton.getInstance().getApplication().getDataFromSharedPrefs(AppConstants.WORKSPACE_COOKIE, ""));
				HttpResponse responseGet = client.execute(get);
				HttpEntity resEntityGet = responseGet.getEntity();
				if (resEntityGet != null) {
					JSONObject mJsonObject = new JSONObject(EntityUtils.toString(resEntityGet));
					String imageUrl = mJsonObject.get("url").toString();
					// Log.d("GET URL ", imageUrl);
					URL ulrn = new URL(imageUrl);
					HttpURLConnection con = (HttpURLConnection) ulrn.openConnection();
					InputStream is = con.getInputStream();
					Bitmap bmp = BitmapFactory.decodeStream(is);
					imageModel.updateBitmap(imageUrl, bmp);
					imageModel.mIsImageFetched = true;
					imageModel.mDidInitiateImageFetch = false;
					imageModel.setPendingImage(true);
					//imageModel.isModelGLInitialized = false;
					if(firstRun){
						GetImageTask highResTask = new GetImageTask(imageModel);
						highResTask.firstRun = false;
						threadPool.execute(highResTask);
					}
					// AppConstants.LOG(AppConstants.CRITICAL, "ImageFetcher",
					// "Downloaded Image:" + bmp.toString() + "Size : " +
					// bmp.getByteCount());
				} else {
					Log.d("GET URL has a fail " + fullUrl, responseGet.toString());

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static final int KEEP_ALIVE = 10;
	private static final TimeUnit KEEP_ALIVE_UNIT = TimeUnit.SECONDS;
	private final ThreadPoolExecutor threadPool;
	private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
	private Runnable cleaner = null;

	public ImageFetcher(int poolSize, int maxPoolSize) {
		threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, KEEP_ALIVE, KEEP_ALIVE_UNIT, workQueue);
	}

	public void fetchNeededImages(ImageModel imageModel) {
		if (!imageModel.mIsImageFetched && !imageModel.mDidInitiateImageFetch) {// does
			// this
			// only
			// load
			// one
			// image
			// size?
			// yeppers
			imageModel.mDidInitiateImageFetch = true;
			threadPool.execute(new GetImageTask(imageModel));
			if(cleaner==null){
				cleaner = new Runnable() {
					@Override
					public void run() {
						try {
							threadPool.awaitTermination(3600, TimeUnit.SECONDS);
							WorkSpaceState.getInstance().getModelTree().mAreAllViewPortWidgetsInitialized = false;
						} catch (InterruptedException e) {
							e.printStackTrace();
							AppConstants.LOG(AppConstants.CRITICAL, "ImageFetcher",
									"Cleaner has failed");

						}finally{
							cleaner = null;
						}
					}
				};
			}
		}
	}

	private int getImageKey(int pixelCount, ImageModel imageModel, boolean firstRun) {
		if(!firstRun)
			return  AppConstants.LARGEST_IMAGE_ZOOM_LEVEL-1;
		return 5;
	}

}