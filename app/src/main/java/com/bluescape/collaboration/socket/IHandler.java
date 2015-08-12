package com.bluescape.collaboration.socket;

import org.json.JSONArray;

public interface IHandler {

	void handleMessage(JSONArray mainMessage);
}
