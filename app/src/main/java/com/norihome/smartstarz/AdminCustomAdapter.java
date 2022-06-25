package com.norihome.smartstarz;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.norihome.smartstarz.backend.smartStarzApi.model.EventBean;
import com.norihome.smartstarz.com.norihome.smartstarz.beans.ApproveBean;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.RemoveStudentAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.StoreEventAsyncTask;

import java.util.HashMap;
import java.util.Map;

import static com.norihome.smartstarz.R.id.button_Advance;
import static com.norihome.smartstarz.R.id.button_Approve;
import static com.norihome.smartstarz.R.id.button_Reject;

/**
 * Created by User on 12/28/2016.
 */

class AdminCustomAdapter extends ArrayAdapter<ApproveBean>{

    ApproveBean[] approveBeans = null;
    AdminInfoHolder holder;
    ApproveFragment fragment = null;

    public AdminCustomAdapter(Context context, ApproveBean[] events, ApproveFragment f) {
        super(context, R.layout.custom_adminrow , events);
        approveBeans = events;
        fragment = f;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.custom_adminrow, parent, false);

        ApproveBean approveBean = getItem(position);
        TextView eventName = (TextView) customView.findViewById(R.id.text_eventName);
        final TextView studentName = (TextView) customView.findViewById(R.id.text_studentName);
        ImageButton approveBtn = (ImageButton) customView.findViewById(button_Approve);
        ImageButton rejectBtn = (ImageButton) customView.findViewById(button_Reject);
        final ImageButton advanceBtn = (ImageButton) customView.findViewById(button_Advance);


        eventName.setText(approveBean.getEventName());
        studentName.setText(approveBean.getStudentName());

        AdminInfoHolder holder = new AdminInfoHolder();
        holder.eventName = approveBean.getEventName();
        holder.studentName = approveBean.getStudentName();
        holder.approveBtn = approveBtn;
        approveBtn.setTag(holder);
        holder.rejectBtn = rejectBtn;
        rejectBtn.setTag(holder);
        holder.advanceBtn = advanceBtn;
        advanceBtn.setTag(holder);
        customView.setTag(holder);

        holder.approveBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                builder.setCancelable(true);
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you would like to approve this student?");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                approveStudent(v);
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });

        holder.rejectBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                builder.setCancelable(true);
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you would like to reject this student?");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                rejectStudent(v);

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

        holder.advanceBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                builder.setCancelable(true);
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you would like to advance this student?");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                advanceStudent(v);

                            }
                        }
                       );
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });

        return customView;
    }

    private void advanceStudent(View v) {
        System.out.println("################ADVANCE STUDENT METHOD CALLED######################");
        ImageButton thisBtn = (ImageButton) v;
        AdminInfoHolder holder = (AdminInfoHolder) v.getTag();

        //Call storeEvent to add the next level event

        String currentEvent = holder.eventName;
        int index = currentEvent.indexOf("-");
        String levelStr = currentEvent.substring(index + 1);
        int level = Integer.parseInt(levelStr);
        int newLevel = level +1;
        String name = currentEvent.substring(0, index);
        String newEvent = name + "-" + newLevel;
        StoreEventAsyncTask storeTask = new StoreEventAsyncTask();
        EventBean bean = new EventBean();
        bean.setName(newEvent);
        bean.setLevel((long) newLevel);
        storeTask.execute(bean);

        //call remove student
        Map<String, String> removeEventParams = new HashMap<String, String>();
        removeEventParams.put(RemoveStudentAsyncTask.EVENT_NAME, holder.eventName);
        removeEventParams.put(RemoveStudentAsyncTask.STUDENT_NAME, holder.studentName);
        removeEventParams.put(RemoveStudentAsyncTask.UPDATE_PATH, "3");

        RemoveStudentAsyncTask removeEventTask = new RemoveStudentAsyncTask();
        removeEventTask.execute(removeEventParams);

        fragment.onResume();
    }

    private void rejectStudent(View v) {
        System.out.println("################REJECT STUDENT METHOD CALLED######################");
        ImageButton thisBtn = (ImageButton) v;
        AdminInfoHolder holder = (AdminInfoHolder) v.getTag();
        Map<String, String> params = new HashMap<String, String>();
        params.put(RemoveStudentAsyncTask.EVENT_NAME, holder.eventName);
        params.put(RemoveStudentAsyncTask.STUDENT_NAME, holder.studentName);
        params.put(RemoveStudentAsyncTask.UPDATE_PATH, "1");

        RemoveStudentAsyncTask task = new RemoveStudentAsyncTask();
        task.execute(params);

        fragment.onResume();
    }

    private void approveStudent(View v) {
        ImageButton thisBtn = (ImageButton) v;
        AdminInfoHolder holder = (AdminInfoHolder) v.getTag();
        Map<String, String> params = new HashMap<String, String>();
        params.put(RemoveStudentAsyncTask.EVENT_NAME, holder.eventName);
        params.put(RemoveStudentAsyncTask.STUDENT_NAME, holder.studentName);
        params.put(RemoveStudentAsyncTask.UPDATE_PATH, "2");

        //Remove student from enrolled students list in event bean and update student
        RemoveStudentAsyncTask task = new RemoveStudentAsyncTask();
        task.execute(params);

        fragment.onResume();
    }

    private ApproveBean[] removeApproveBean(String studentName, String eventName) {
        ApproveBean[] newBeanArray = new ApproveBean[approveBeans.length-1];
        int cnt = 0;
        for (int k=0; k < approveBeans.length; k++) {
            ApproveBean aBean = approveBeans[k];
            if (! (aBean.getStudentName().equalsIgnoreCase(studentName) &&
                    aBean.getEventName().equalsIgnoreCase(eventName))) {
                newBeanArray[cnt] = aBean;
            }
        }
        return newBeanArray;

    }

    static class AdminInfoHolder {
        String eventName;
        String studentName;
        ImageButton approveBtn;
        ImageButton rejectBtn;
        ImageButton advanceBtn;
    }
}
