package com.bluescape.collaboration.history;

import android.app.Activity;

import com.bluescape.AppConstants;
import com.bluescape.activity.MainActivity;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.collaboration.socket.HandlerFactory;
import com.bluescape.collaboration.socket.IHandler;
import com.bluescape.activity.WorkspaceUpdateListener;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.StrokeModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by keefe on 6/8/15. The purpose of this class is to maintain proper
 * ordering on history segments.
 */
public class HistoryOrganizer implements Runnable {

	private static final String TAG = HistoryOrganizer.class.getSimpleName();
	public long startedHistoryLoad = 0;
	private Activity context;
	private WorkspaceUpdateListener workspaceUpdateListener;
	private final Lock fullHistoryLock = new ReentrantLock();
	private final Condition historyLoaded = fullHistoryLock.newCondition();//replace with countdownlatch?
	private final Set<String> incomingUrls = new HashSet<String>();
	private final List<String> orderedChunks = new LinkedList<String>();
	private final Map<String, JSONArray> url2history = new HashMap<String, JSONArray>();

	public HistoryOrganizer(Activity context, WorkspaceUpdateListener workspaceUpdateListener) {
		this.context = context;
		this.workspaceUpdateListener = workspaceUpdateListener;
	}

	public void prepareHistoryLoad(String url) {
		fullHistoryLock.lock();
		try {
			incomingUrls.add(url);
			orderedChunks.add(url);
		} finally {
			fullHistoryLock.unlock();
		}
	}

	public void reset() {
		url2history.clear();
		orderedChunks.clear();
		incomingUrls.clear();
	}


	public void run() {
		long start = System.currentTimeMillis();
		fullHistoryLock.lock();
		try {
			while (incomingUrls.size() > 0)
				historyLoaded.await();

			AppConstants.LOG(AppConstants.CRITICAL, TAG, " Total time to load history " + (System.currentTimeMillis() - startedHistoryLoad));

			AppConstants.LOG(AppConstants.CRITICAL, TAG, "Starting Parse, waited on lock : " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			int i = 0;
			JSONArray[] arrays = new JSONArray[orderedChunks.size()];
			Iterator it = orderedChunks.iterator();
			try {
				while (it.hasNext())
					arrays[i++] = url2history.get(it.next());
				Set<String> targetEventTypeFound = new HashSet<String>(500 * arrays.length);
				//TODO KRIS
//				for (i = arrays.length - 1; i >= 0; i--)
//					compressHistory(arrays[i], targetEventTypeFound);
				i = 0;
				for (JSONArray chunk : arrays) {
					parseHistory(chunk);
				}

				Collection<BaseWidgetModel> models = WorkSpaceState.getInstance().getModelTree().allModels();
				for(BaseWidgetModel model : models){
					if(model instanceof StrokeModel){
						((StrokeModel) model).initializeParentModel();
					}
				}
				AppConstants.LOG(AppConstants.CRITICAL, TAG, "ZOrder On Start " + WorkSpaceState.getInstance().getGlobalOrder());

			//} catch (JSONException e) {
			} catch (Exception e) {

				e.printStackTrace();
			}
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "TotalHistoryParseTime - " + (System.currentTimeMillis() - start));
			AppConstants.LOG(AppConstants.CRITICAL, TAG, " Total time to load and parse xhistory " + (System.currentTimeMillis() - startedHistoryLoad));

			workspaceUpdateListener.callSocket();
			context.dismissDialog(AppConstants.DIALOG_LOADING);
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "Time Remove Debug - " + (System.currentTimeMillis() - start));

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			fullHistoryLock.unlock();
		}
	}

	public void specifyHistory(String url, JSONArray history) {
		long start = System.currentTimeMillis();
		fullHistoryLock.lock();
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "Time waiting for specify history lock" + (System.currentTimeMillis() - start));

		try {
			incomingUrls.remove(url);
			url2history.put(url, history);

			if (incomingUrls.size() == 0) historyLoaded.signal();

		} finally {
			fullHistoryLock.unlock();
		}
	}

	private void compressHistory(JSONArray historyChunk, Set<String> targetEventTypeFound) throws JSONException {
		String workspaceId = WorkSpaceState.getInstance().getWorkSpaceModel().getID();
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "TotalHistoryCount - " + historyChunk.length() + " for " + workspaceId);
		long start = System.currentTimeMillis();
		for (int historyCount = historyChunk.length() - 1; historyCount >= 0; historyCount--) {
			JSONArray eventJson = (JSONArray) historyChunk.get(historyCount);
			String targetID = eventJson.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.TARGET_ID.ordinal()).toString();
			if (!workspaceId.equals(targetID)) {
				String type = eventJson.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal()).toString();
				String checks = null;
				if ("position".equals(type) || "template".equals(type)) checks = targetID + "-" + type;

				if ((checks != null && targetEventTypeFound.contains(checks)) || targetEventTypeFound.contains(targetID + "-delete")) {
					historyChunk.put(historyCount, null);
				} else {
					if ("delete".equals(type) || "markerDelete".equals(type)) {
						checks = targetID + "-delete";
						// historyChunk.put(historyCount, null); occasional
						// position objects occur after delete, but why doesn't
						// suppressing create work?
					}
					targetEventTypeFound.add(checks);
				}
			}
		}
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "Compress Time - " + (System.currentTimeMillis() - start));
	}

	private void parseHistory(JSONArray historyJsonArray) {

		try {
			for (int historyCount = 0; historyCount < historyJsonArray.length(); historyCount++) {
				if (!historyJsonArray.isNull(historyCount)) {
					JSONArray eventJsonArray = (JSONArray) historyJsonArray.get(historyCount);
					String event = eventJsonArray.get(AppConstants.HISTORY_EVENT_FROM_SERVER_MESSAGE_FORMAT.EVENT_TYPE.ordinal()).toString();

					// AppConstants.LOG(AppConstants.VERBOSE, TAG, historyCount
					// + "-" + event);
					IHandler handler = HandlerFactory.getInstance().getHandler(event);
					if (handler != null) {
						handler.handleMessage(eventJsonArray);
					}

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		MainActivity.historyJSONObjCount++;
		AppConstants.LOG(AppConstants.INFO, TAG, "historyJSONObjSize: " + MainActivity.historyJSONObjSize + "A");
		AppConstants.LOG(AppConstants.INFO, TAG, "historyJSONObjCount: " + MainActivity.historyJSONObjCount + "A");
		;
	}
}
