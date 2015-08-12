package com.bluescape.model.widget;

import com.bluescape.collaboration.socket.sender.HePositionMessageSender;
import com.bluescape.collaboration.socket.sender.VePositionMessageSender;
import com.bluescape.model.util.Rect;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Model to cast from json Created by Mark Stanford on 1/15/15.
 */
public class MoveModel {
    // region Member Variables
    @SerializedName("rect")
    private float[] mRect = new float[4];

    @SerializedName("order")
    public int mOrder = 1;

    private transient String mModelID;
    // end region Member Variables
    // region Constructor
    public MoveModel() {

    }
    public MoveModel(float[] rect, float order, String modelID) {
        mRect = rect;
        mOrder = (int) order;
        mModelID = modelID;
    }

    public MoveModel(Rect rect, float order, String modelID) {
        mRect = rect.getRect();
        mOrder = (int) order;
        mModelID = modelID;
    }
    // end region Constructor

    // region Getters
    public String getModelID() {
        return mModelID;
    }

    public int getOrder() {
        return mOrder;
    }

    public float[] getRect() {
        return mRect;
    }
    // end region Getters
    // region Setters
    public void setModelID(String modelID) {
        mModelID = modelID;
    }

    public void setOrder(int order) {
        mOrder = order;
    }
    public void setRect(float[] rect) {
        mRect = rect;
    }
    // end region Setters
    // region Abstract Methods
    // Send the Model to Web Socket Server
    public boolean sendHeToWSServer() {
        HePositionMessageSender hePositionMessageSender = new HePositionMessageSender(this);
        hePositionMessageSender.send();
        return true;
    }

    // Send the Model to Web Socket Server
    public boolean sendVeToWSServer() {
        VePositionMessageSender vePositionMessageSender = new VePositionMessageSender(this);
        vePositionMessageSender.send();
        return true;
    }

    @Override
    public String toString() {
        return "MoveModel{" + "mRect=" + Arrays.toString(mRect) + ", mModelID=" + mModelID +

                ", mOrder=" + mOrder + '}';
    }
    // end region Abstract Methods
}
