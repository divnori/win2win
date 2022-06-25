package com.norihome.smartstarz.com.norihome.smartstarz.tasks;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.norihome.smartstarz.backend.smartStarzApi.SmartStarzApi;
import com.norihome.smartstarz.backend.smartStarzApi.model.GroupsBean;

import java.io.IOException;
import java.util.Map;

/**
 * Created by User on 4/8/2017.
 */

public class UpdateGroupAsyncTask extends AsyncTask<Map<String, String>, Void, Void> {

    public static final String OLD_GROUP_NAME = "OldGroupName";
    public static final String NEW_GROUP_NAME = "NewGroupName";

    private static SmartStarzApi myApiService = null;
    private void initializeBackend() {
        if(myApiService == null) {  // Only do this once
            SmartStarzApi.Builder builder = new SmartStarzApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl("http://10.0.2.2:8080/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            // end options for devappserver

            myApiService = builder.build();
        }
    }

    private void initializeAppEngineBackend() {
        if(myApiService == null) {  // Only do this once
            SmartStarzApi.Builder builder = new SmartStarzApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    .setRootUrl("https://firebase-smartstarz.appspot.com/_ah/api/");
            myApiService = builder.build();
        }
    }

    @Override
    public Void doInBackground(Map<String, String>... params) {
        System.out.println("------------          update group task called   --------------------");

        if(myApiService == null) {
            initializeAppEngineBackend();
            //initializeBackend(); // local dev app server
        }
        Map<String,String> map = (Map<String,String>)params[0];
        GroupsBean grp1 = new GroupsBean();
        grp1.setName(map.get(UpdateGroupAsyncTask.OLD_GROUP_NAME));
        String newGroupName = map.get(UpdateGroupAsyncTask.NEW_GROUP_NAME);
        try {
            myApiService.updateGroup(newGroupName, grp1).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("------------          returning from delete group task  --------------------");
        return null;
    }
}
