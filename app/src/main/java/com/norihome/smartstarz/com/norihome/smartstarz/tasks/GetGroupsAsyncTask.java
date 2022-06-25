package com.norihome.smartstarz.com.norihome.smartstarz.tasks;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.norihome.smartstarz.backend.smartStarzApi.SmartStarzApi;
import com.norihome.smartstarz.backend.smartStarzApi.model.GroupsBeanCollection;
import com.norihome.smartstarz.backend.smartStarzApi.model.GroupsBean;

import java.io.IOException;
import java.util.List;

/**
 * Created by User on 4/8/2017.
 */

public class GetGroupsAsyncTask extends AsyncTask<Void, Void, List<GroupsBean>> {
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
    public List<GroupsBean> doInBackground(Void... params) {
        System.out.println("------------          get groups task called   --------------------");
        List<GroupsBean> groupsBeanList = null;
        GroupsBeanCollection collection = null;
        SmartStarzApi.GetGroups getGroups = null;
        if(myApiService == null) {
            initializeAppEngineBackend();
            //initializeBackend(); // local dev app server
        }

        try {
            getGroups = myApiService.getGroups();
            collection = getGroups.execute();
            groupsBeanList = collection.getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("------------          returning from getGroups   --------------------");
        return groupsBeanList;
    }

}
