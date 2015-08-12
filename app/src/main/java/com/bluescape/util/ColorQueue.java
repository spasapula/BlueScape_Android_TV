package com.bluescape.util;


public class ColorQueue {
	private static class instanceHolder {
		private static final ColorQueue INSTANCE = new ColorQueue();
	}

	public static ColorQueue getInstance() {
		return instanceHolder.INSTANCE;
	}

	private int colorIndex = 0;

	private final String[] colors = { "#0d71ff", "#0b5c00", "#ff641f", "#854707", "#b51515", "#a524c9", "#23b872", "#ff5582", "#ffbc00", "#005175",
										"#a280ff", "#84145b", "#3dbc16", "#ef9d54", "#73adff", "#0f6b62", "#e227b0", "#afbc0f", "#01cfe5", "#302db1",
										"#612f9b", "#44440e", "#ed73ff", "#00aaad", "#5a057a" };

	private ColorQueue() {
	}

	public String getColor() {
		if (colorIndex >= colors.length) colorIndex = 0;
		return colors[colorIndex++];
	}
}
