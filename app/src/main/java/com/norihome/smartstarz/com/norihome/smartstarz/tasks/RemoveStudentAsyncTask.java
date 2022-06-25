package com.norihome.smartstarz.com.norihome.smartstarz.tasks;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.norihome.smartstarz.backend.smartStarzApi.SmartStarzApi;

import java.io.IOException;
import java.util.Map;

/**
 * Created by User on 12/29/2016.
 */

public class RemoveStudentAsyncTask extends AsyncTask<Map<String, String>, Void, Void> {

    public static final String EVENT_NAME = "EventName";
    public static final String STUDENT_NAME = "StudentName";
    public static final String UPDATE_PATH ="UpdatePath";
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
    protected Void doInBackground(Map<String, String>... params) {

        if(myApiService == null) {
         initializeAppEngineBackend();
           //initializeBackend();
        }


        Map<String,String> map = (Map<String,String>)params[0];


        System.out.println("RemoveStudent task called with params " +
                map.get(EVENT_NAME) + ", " + map.get(STUDENT_NAME) + ", " + map.get(UPDATE_PATH));
        try {
            myApiService.removeStudentFromEvent(map.get(EVENT_NAME),
                    map.get(STUDENT_NAME), map.get(UPDATE_PATH)).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
