package com.bluescape.model.util;

import android.graphics.PointF;

import com.bluescape.AppConstants;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.StrokeModel;
import com.bluescape.model.widget.TexturedWidgetModel;
import com.bluescape.util.MathHelper;

import java.util.ArrayList;

public class StrokeVertexBuilder {
	private static final String TAG = StrokeVertexBuilder.class.getSimpleName();

	// Incoming Vars from StrokeModel
	private ArrayList<Float> mArrayLocs;
	private float mStrokeModelLineWidth;
	private float mStrokeModelZOrder;

	private transient ArrayList<Float> mVertices;
	private BaseWidgetModel model;
	private transient float[] mVertices_array;
	private ArrayList<PointF> mPoints;

	public StrokeVertexBuilder(ArrayList<Float> mArrayLocs, float mStrokeModelLineWidth, float mStrokeModelZOrder, BaseWidgetModel parentModel) {
		// mStrokeModel = strokeModel;
		this.mArrayLocs = mArrayLocs;
		this.model = parentModel;
		this.mStrokeModelLineWidth = mStrokeModelLineWidth;
		this.mStrokeModelZOrder = mStrokeModelZOrder;
		//AppConstants.LOG(AppConstants.CRITICAL, TAG, "Stroke ZOrder " + mStrokeModelZOrder);

	}

	public void buildVertices() {
		// int nCoordinates = mStrokeModel.getArrayLocs().size();
		int nCoordinates = mArrayLocs.size();

		// Check if stroke model has points else skip building vertices
		// Also writing code in Message Handler to Skip if stroke locs are 0 as
		// this causes null pointer exceptions later in
		// com.bluescape.drawables.BaseWidgetDrawable.setVertexBuffer(BaseWidgetDrawable.java:139)
		if (nCoordinates > 0) {
			mPoints = new ArrayList<>();
			mVertices = new ArrayList<>(nCoordinates * 6);
			for (int i = 0; i < nCoordinates; i += 2) {
				// PointF point = new PointF(mStrokeModel.getArrayLocs().get(i),
				// mStrokeModel.getArrayLocs().get(i + 1));
				float x = mArrayLocs.get(i);
				float y =  mArrayLocs.get(i + 1);
				PointF point = clampPoint(x, y);
				if(point!=null)
					mPoints.add(point);
			}
			convertPointsToVertices();

			mVertices_array = new float[mVertices.size()];
			for (int i = 0; i < mVertices.size(); i++)
				mVertices_array[i] = mVertices.get(i).floatValue();
		} else {
			// AppConstants.LOG(AppConstants.CRITICAL, TAG,
			// "Size of Array Locs in mStrokeModel.getArrayLocs().size()  is 0 Stroke Data issues: "
			// + mStrokeModel.getArrayLocs().size());
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "Size of Array Locs in mStrokeModel.getArrayLocs().size()  is 0 Stroke Data issues: "
															+ mArrayLocs.size());

		}

	}

	protected PointF clampPoint(float x, float y) {
		PointF point;
		if(model!=null){
			float width = 500;
			float height = 300;

			if(model instanceof TexturedWidgetModel){
				TexturedWidgetModel twm = (TexturedWidgetModel) model;
				width = twm.getActualWidth();
				height = twm.getActualHeight();
			}
            if(x<0){
				x =0;
			}
            if(x>width){
				x = width;
			}
            if(y<0) {
				y =0;
			}
            if(y>height) {
				y = height;
			}

        }
		point = new PointF(x, y);
		return point;
	}

	public float[] getVertices() {
		return mVertices_array;
	}

	private void buildVerticesFromThreePoints(PointF middlePoint, PointF leftPoint, PointF rightPoint) {
		PointF leftVector = MathHelper.subtractPointVector(middlePoint, leftPoint);
		PointF rightVector = MathHelper.subtractPointVector(middlePoint, rightPoint);
		// Calculate the right offset
		PointF subtractedVector = MathHelper.subtractPointVector(rightVector, leftVector);
		PointF strokeDirection = MathHelper.normalizePointVector(subtractedVector);
		PointF perpendicularStrokeDirection = MathHelper.perpendicularVector(strokeDirection);
		// PointF offset =
		// MathHelper.vectorMultiplyByScalar(perpendicularStrokeDirection,
		// mStrokeModel.getLineWidth()/2);
		PointF offset = MathHelper.vectorMultiplyByScalar(perpendicularStrokeDirection, mStrokeModelLineWidth / 2);

		createVertices(middlePoint, offset);

	}

	private void convertPointsToVertices() {
		int pointIndex;
		PointF leftPoint, midPoint, rightPoint;
		if (mPoints.size() == 1)
			rightPoint = mPoints.get(0);
		else
			rightPoint = mPoints.get(1);

		midPoint = mPoints.get(0);
		leftPoint = midPoint;
		// Process first point
		buildVerticesFromThreePoints(midPoint, leftPoint, rightPoint);

		// Process middle
		for (pointIndex = 1; pointIndex < mPoints.size() - 1; pointIndex++) {
			midPoint = mPoints.get(pointIndex);
			leftPoint = mPoints.get(pointIndex - 1);
			rightPoint = mPoints.get(pointIndex + 1);
			buildVerticesFromThreePoints(midPoint, leftPoint, rightPoint);
		}

		// Process last point
		if (pointIndex < mPoints.size()) {
			midPoint = mPoints.get(pointIndex);
			rightPoint = midPoint;
			leftPoint = mPoints.get(pointIndex - 1);
			buildVerticesFromThreePoints(midPoint, leftPoint, rightPoint);
		}

	}

	private void createVertices(PointF point, PointF offset) {
		mVertices.add(point.x + offset.x);
		mVertices.add(point.y + offset.y);
		// mVertices.add((float)mStrokeModel.getZOrder());
		mVertices.add(mStrokeModelZOrder);//TODO how does this relate to the matrix multiplication in the draw?
		mVertices.add(point.x - offset.x);
		mVertices.add(point.y - offset.y);
		// mVertices.add((float)mStrokeModel.getZOrder());
		mVertices.add(mStrokeModelZOrder);

	}
}
