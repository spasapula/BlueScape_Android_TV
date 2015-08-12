package com.bluescape.activity;

import com.bluescape.model.CollaboratorModel;
import com.bluescape.model.widget.LocationMarkerModel;

import java.util.List;

public interface WorkspaceUpdateListener {
    void callSocket();

    void closeSideMenu();

    void createMarkerOnLongPress();

    void displayProgressDialog();

    void onLongPressOptionsPopup();

    void openBrowser(String url);

    void openPDF(String name);

    void stopProgressDialog();

    void updateCollaboratorsList(List<CollaboratorModel> collaborators);

    void updateMarkersList(List<LocationMarkerModel> markersList);

    void updateWorkspaceTitle(String workspaceName);

    void onReconnectingToNetwork(String historyURLsString);

    void onFollowingUserExit();

}
