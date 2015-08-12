package com.bluescape.collaboration.socket;

import com.bluescape.AppConstants;

import org.json.JSONArray;

import java.util.Arrays;

public class SocketHandler implements WebSocketClient.Listener {

	private static final String TAG = SocketHandler.class.getSimpleName();

	public SocketHandler() {
	}

	@Override
	public void onConnect() {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "Connected!");

	}

	@Override
	public void onDisconnect(int code, String reason) {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));

	}

	@Override
	public void onError(Exception error) {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, "Error!" + error);

	}

	@Override
	public void onMessage(byte[] data) {
		AppConstants.LOG(AppConstants.CRITICAL, TAG, String.format("Got binary message! %s", Arrays.toString(data)));

	}

	@Override
	public void onMessage(String message) {
		// AppConstants.LOG(AppConstants.CRITICAL, TAG,
		// String.format("Got string message! %s", message));
		handleSocketMessage(message);
	}

	private void handleSocketMessage(String message) {
		try {
			JSONArray mainMessage = new JSONArray(message);
			if (mainMessage.length() > 2) {

				String messageType = (String) mainMessage.get(1);

				IHandler handler = HandlerFactory.getInstance().getHandler(messageType);

				if (handler != null) {
					handler.handleMessage(mainMessage);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}