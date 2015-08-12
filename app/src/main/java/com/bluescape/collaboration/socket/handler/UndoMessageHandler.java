package com.bluescape.collaboration.socket.handler;

import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.WorkSpaceModel;

import org.json.JSONArray;

public class UndoMessageHandler extends BaseMessageHandler {

	private static final String TAG = UndoMessageHandler.class.getSimpleName();

	// server --> client
	// [client-id, 'undo', target-id, removedEventId]

	public void handleMessage(JSONArray mainMessage) {
		modelTree.performUndo();;
	}
}
