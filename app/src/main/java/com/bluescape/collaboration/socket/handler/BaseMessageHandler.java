package com.bluescape.collaboration.socket.handler;

import com.bluescape.collaboration.socket.IHandler;
import com.bluescape.model.ModelTree;
import com.bluescape.model.WorkSpaceState;

import org.json.JSONArray;

public class BaseMessageHandler implements IHandler {

	protected ModelTree modelTree;

	public BaseMessageHandler(){
		modelTree = WorkSpaceState.getInstance().getModelTree();
	}
	public void handleMessage(JSONArray mainMessage) {

	}

}
