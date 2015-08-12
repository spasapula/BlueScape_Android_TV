package com.bluescape.util;

import android.graphics.PointF;

public class MathHelper {
	public static PointF normalizePointVector(PointF point) {
		PointF returnVal = new PointF();
		float magnitude = android.util.FloatMath.sqrt(((point.x * point.x) + (point.y * point.y)));
		if (magnitude != 0) {
			returnVal.x = point.x / magnitude;
			returnVal.y = point.y / magnitude;
		}
		return returnVal;
	}

	public static PointF perpendicularVector(PointF vector) {
		return new PointF(vector.y, vector.x * -1);
	}

	public static PointF subtractPointVector(PointF vector1, PointF vector2) {
		return new PointF(vector2.x - vector1.x, vector2.y - vector1.y);
	}

	public static PointF vectorMultiplyByScalar(PointF vector, float scalar) {
		return new PointF(vector.x * scalar, vector.y * scalar);
	}
}
