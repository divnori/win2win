package com.norihome.smartstarz;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.norihome.smartstarz.backend.smartStarzApi.model.EventBean;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.ClearEventsAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.StoreEventAsyncTask;

public class AddEventsFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_add_events, container, false);

    }

    @Override
    public void onStart() {
        super.onStart();
    }


    public void onClearDataButtonClick (View v) {

        ClearEventsAsyncTask task = new ClearEventsAsyncTask();
        task.execute();
        Toast.makeText(this.getActivity(), "Data Cleared", Toast.LENGTH_SHORT).show();
    }

}
