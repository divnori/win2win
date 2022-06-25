package com.norihome.smartstarz;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.norihome.smartstarz.backend.smartStarzApi.model.EventBean;
import com.norihome.smartstarz.backend.smartStarzApi.model.StudentBean;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.AddStudentToEventAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.GetStudentByNameAsyncTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.norihome.smartstarz.R.color.colorDisabled;
import static com.norihome.smartstarz.R.id.button_iAmInterested;

/**
 * Created by User on 12/28/2016.
 */

class EventListCustomAdapter extends ArrayAdapter<EventBean> {

    String userName;
    EventInfoHolder holder;
    StudentBean studentBean;
    EventFeedFragment fragment;

    EventListCustomAdapter(Context context, EventBean[] events, String userName, EventFeedFragment f) {
        super(context, R.layout.custom_row , events);
        this.userName = userName;
        this.fragment = f;

        System.out.println("EventListCustomAdapter started with User Name = " + userName);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.custom_row, parent, false);

        EventBean singleEvent = getItem(position);
        TextView title = (TextView) customView.findViewById(R.id.text_feedName);
        TextView date = (TextView) customView.findViewById(R.id.text_feedDate);
        TextView details = (TextView) customView.findViewById(R.id.text_feedDescription);
        TextView level = (TextView) customView.findViewById(R.id.text_feedLevel);

        Button iAmIntBtn = (Button) customView.findViewById(button_iAmInterested);
        iAmIntBtn.setBackgroundColor(iAmIntBtn.getContext().getResources().getColor(R.color.colorPrimary));

        // Disable the iAmIntBtn if the student already enrolled in this event
        if (singleEvent != null &&
                singleEvent.getEnrolledStudents() != null &&
                        singleEvent.getEnrolledStudents().contains(userName)) {
            iAmIntBtn.setEnabled(false);
            iAmIntBtn.setBackgroundColor(Color.GRAY);
        }

    if (singleEvent != null) {
        String name = singleEvent.getName();
        int index = name.indexOf("-");
        String levelStr = name.substring(index + 1);
        System.out.println("Level =" + levelStr);
        String titleStr = name.substring(0, index);
        System.out.println("Event Name:  " + titleStr);
        title.setText(titleStr);
        date.setText(singleEvent.getDate());
        details.setText(singleEvent.getDescription());
        level.setText("Level:  " + levelStr);


        EventInfoHolder holder = new EventInfoHolder();
        holder.eventName = singleEvent.getName();
        holder.iAmInterestedBtn = iAmIntBtn;
        iAmIntBtn.setTag(holder);
        customView.setTag(holder);

        holder.iAmInterestedBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Button thisBtn = (Button) v;
                EventInfoHolder holder = (EventInfoHolder) v.getTag();
                Map<String, String> params = new HashMap<String, String>();
                params.put(AddStudentToEventAsyncTask.EVENT_NAME, holder.eventName);
                params.put(AddStudentToEventAsyncTask.STUDENT_NAME, userName);

                AddStudentToEventAsyncTask task = new AddStudentToEventAsyncTask();
                task.execute(params);

                fragment.onResume();
            }
        });
    }

        return customView;
    }

    static class EventInfoHolder {
        String eventName;
        Button iAmInterestedBtn;
    }


}


