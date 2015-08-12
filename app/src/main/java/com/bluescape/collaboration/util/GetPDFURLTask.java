package com.bluescape.collaboration.util;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.bluescape.AppConstants;
import com.bluescape.AppSingleton;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.widget.PDFModel;
import com.bluescape.util.network.FileDownloader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by keefe on 6/24/15.
 */
public class GetPDFURLTask extends AsyncTask<String, String, String> {
    private String pdfUrl;
    final private PDFModel pdfModel;

    public GetPDFURLTask(PDFModel pdfModel) {
        this.pdfModel = pdfModel;
    }

    @Override
    protected String doInBackground(String... params) {

        String fulllUrl = AppSingleton.getInstance().getApplication()
                .getDataFromSharedPrefs(AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS, AppConstants.HTTP_COLLABORATION_SERVICE_ADDRESS)
                + "/"
                + AppSingleton.getInstance().getApplication()
                .getDataFromSharedPrefs(AppConstants.KEY_WORKSPACE_ID, AppConstants.KEY_WORKSPACE_ID)
                + "/asset_url?key="
                + pdfModel.getAssetPath();

        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(fulllUrl);
            get.setHeader("Cookie", AppSingleton.getInstance().getApplication().getDataFromSharedPrefs(AppConstants.WORKSPACE_COOKIE, ""));
            HttpResponse responseGet = client.execute(get);
            HttpEntity resEntityGet = responseGet.getEntity();
            if (resEntityGet != null) {
                JSONObject mJsonObject = new JSONObject(EntityUtils.toString(resEntityGet));
                Log.d("GET URL ", mJsonObject.get("url").toString());
                pdfUrl = mJsonObject.get("url").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pdfUrl != null) {
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File folder = new File(extStorageDirectory, "bluescape");
            boolean mkdir = folder.mkdir();
            File pdfFile = new File(folder, pdfModel.getFilename());

            try {
                pdfFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileDownloader.downloadFile(pdfUrl, pdfFile);
        }
        return "";
    }

    @Override
    protected void onPostExecute(String pdfPath) {
        super.onPostExecute(pdfPath);
        WorkSpaceState.getInstance().getWorkSpaceModel().getWorkspaceUpdateListener().stopProgressDialog();

        WorkSpaceState.getInstance().getWorkSpaceModel().getWorkspaceUpdateListener().openPDF(pdfModel.getFilename());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        WorkSpaceState.getInstance().getWorkSpaceModel().getWorkspaceUpdateListener().displayProgressDialog();

    }

}