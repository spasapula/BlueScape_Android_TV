package com.bluescape.model;

import com.google.gson.annotations.SerializedName;

public class CollaboratorModel {

	@SerializedName("name")
	private String name;

	@SerializedName("device_type")
	private String deviceType;

	@SerializedName("clientType")
	private String clientType;

	@SerializedName("clientId")
	private String clientId;

	// Set Default so that in case it doesnt come in the rl message then we show
	// a standard one always
	// note DO NOT DELETE this is the default Zoom of initial workspace
	@SerializedName("viewPort")
	private float[] viewPort = { -1800f, -1800, 1800, 1800 };

	private String color;

	private float[] rgbColor = new float[3];

	public String getClientId() {
		return clientId;
	}

	public String getClientType() {
		return clientType;
	}

	public String getColor() {
		return color;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public String getName() {
		return name;
	}

	public float[] getViewPort() {
		return viewPort;
	}

	public String initials() {
		String words[] = name.split(" ");
		if (words.length == 1) return Character.toString(words[0].charAt(0));
		return Character.toString(words[0].charAt(0)) + Character.toString(words[words.length - 1].charAt(0));
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setViewPort(float[] viewPort) {
		this.viewPort = viewPort;
	}

	public float[] getRgbColor() {
		return rgbColor;
	}

	public void setRgbColor(float[] rgbColor) {
		this.rgbColor = rgbColor;
	}
}
