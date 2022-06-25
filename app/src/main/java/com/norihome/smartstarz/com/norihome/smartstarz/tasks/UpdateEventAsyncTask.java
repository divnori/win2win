package com.norihome.smartstarz.com.norihome.smartstarz.tasks;

/**
 * Created by User on 12/27/2016.
 */

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.norihome.smartstarz.backend.smartStarzApi.SmartStarzApi;
import com.norihome.smartstarz.backend.smartStarzApi.model.EventBean;
import com.norihome.smartstarz.backend.smartStarzApi.model.StudentBean;

import java.io.IOException;

public class UpdateEventAsyncTask extends AsyncTask<Object, Void, Void> {
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
    protected Void doInBackground(Object... params) {

        EventBean eventBean = (EventBean) params[0];
        System.out.println("   UpdateEventAsyncTask called for   " + eventBean.getName());
        if(myApiService == null) {
          initializeAppEngineBackend();
           //initializeBackend(); // local dev app server
        }

        try {
            myApiService.updateEvent(eventBean).execute();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("------------          returning from UpdateEventAsyncTask   --------------------");

        return null;

    }
}