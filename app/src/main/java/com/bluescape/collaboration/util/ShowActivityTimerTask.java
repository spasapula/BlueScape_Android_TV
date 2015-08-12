package com.bluescape.collaboration.util;


import com.bluescape.model.widget.BaseWidgetModel;

import java.util.TimerTask;

public class ShowActivityTimerTask extends TimerTask {

    private final BaseWidgetModel baseWidgetModel;


    public ShowActivityTimerTask(BaseWidgetModel baseWidgetModel) {
        this.baseWidgetModel = baseWidgetModel;
    }

    public void run() {
        //Do stuff
        baseWidgetModel.setActivityIndicator(false);
        baseWidgetModel.setActivityCollaborator("");
        baseWidgetModel.setActivityInitials("");
        //throws null pointer exception for the draw loop
        //baseWidgetModel.setActivityCollaboratorColor(null);
    }
}