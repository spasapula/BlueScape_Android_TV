package com.bluescape.model.util;

public enum StrokeColors {

	WHITE, RED, YELLOW, TEAL, BLUE, PURPLE, BLACK, NO_COLOR;

	public int getValue() {
		switch (this) {
		case WHITE:
			return 0;
		case RED:
			return 1;
		case YELLOW:
			return 2;
		case TEAL:
			return 3;
		case BLUE:
			return 4;
		case PURPLE:
			return 5;
		case BLACK:
			return 6;
		case NO_COLOR:
			return 7;
		default:
			return 0;
		}
	}
}
