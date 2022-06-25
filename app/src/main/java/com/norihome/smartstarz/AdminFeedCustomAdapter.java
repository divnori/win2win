package com.norihome.smartstarz;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.norihome.smartstarz.backend.smartStarzApi.model.EventBean;
import com.norihome.smartstarz.backend.smartStarzApi.model.StudentBean;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.AddStudentToEventAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.DeleteEventAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.UpdateEventAsyncTask;

import java.util.HashMap;
import java.util.Map;

import static com.norihome.smartstarz.R.id.button_deleteAD;

import static com.norihome.smartstarz.R.id.button_updateAD;

/**
 * Created by User on 4/2/2017.
 */
class AdminFeedCustomAdapter extends ArrayAdapter<EventBean> {
    String userName;
    AdminFeedFragment fragment;

    AdminFeedCustomAdapter(Context context, EventBean[] events, String userName, AdminFeedFragment a) {
        super(context, R.layout.custom_adminfeed, events);
        this.userName = userName;
        this.fragment = a;

        System.out.println("AdminFeedCustomAdapter started with User Name = " + userName);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.custom_adminfeed, parent, false);

        EventBean singleEvent = getItem(position);
        TextView title = (TextView) customView.findViewById(R.id.text_feedNameAD);
        TextView date = (TextView) customView.findViewById(R.id.editText_feedDateAD);
        TextView details = (TextView) customView.findViewById(R.id.editText_feedDescriptionAD);
        TextView level = (TextView) customView.findViewById(R.id.text_feedLevelAD);
        Button deleteBtn = (Button) customView.findViewById(button_deleteAD);
        Button updateBtn = (Button) customView.findViewById(button_updateAD);
        deleteBtn.setBackgroundColor(deleteBtn.getContext().getResources().getColor(R.color.colorPrimary));
        deleteBtn.setBackgroundColor(updateBtn.getContext().getResources().getColor(R.color.colorPrimary));

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
            AdminFeedCustomAdapter.EventInfoHolder holder = new AdminFeedCustomAdapter.EventInfoHolder();
            holder.eventName = singleEvent.getName();
            holder.deleteBtn = deleteBtn;
            holder.dateView = date;
            holder.descView = details;
            deleteBtn.setTag(holder);
            customView.setTag(holder);
            holder.updateBtn = updateBtn;
            updateBtn.setTag(holder);
            customView.setTag(holder);

            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                    builder.setCancelable(true);
                    builder.setTitle("Confirmation");
                    builder.setMessage("Are you sure you would like to delete this event?");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteEvent(v);
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            });
            holder.updateBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                    builder.setCancelable(true);
                    builder.setTitle("Confirmation");
                    builder.setMessage("Are you sure you would like to update this event?");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateEvent(v);
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            });
        }

        return customView;

    }

    private void updateEvent(View v) {
        Button thisBtn = (Button) v;
        EventInfoHolder holder = (EventInfoHolder) v.getTag();

        EventBean bean = new EventBean();
        bean.setName(holder.eventName);
        String desc = holder.descView.getText().toString();
        if (desc != null)
            bean.setDescription(desc.trim());
        String date = holder.dateView.getText().toString();
        if (date != null)
            bean.setDate(date.trim());

        // Call UpdateEvent task
        UpdateEventAsyncTask task = new UpdateEventAsyncTask();
        task.execute(bean);

        Toast.makeText(fragment.getActivity(), "Event Updated", Toast.LENGTH_SHORT).show();
        fragment.onResume();
    }

    private void deleteEvent(View v) {
        Button deleteBtn = (Button) v;
        EventInfoHolder holder = (EventInfoHolder) v.getTag();

        DeleteEventAsyncTask task = new DeleteEventAsyncTask();
        task.execute(holder.eventName);

        Toast.makeText(fragment.getActivity(), "Event Deleted", Toast.LENGTH_SHORT).show();

        fragment.onResume();
    }

    static class EventInfoHolder {
        String eventName;
        Button deleteBtn;
        Button updateBtn;
        TextView dateView;
        TextView descView;
    }
}