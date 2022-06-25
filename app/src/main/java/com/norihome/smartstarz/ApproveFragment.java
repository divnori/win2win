package com.norihome.smartstarz;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.norihome.smartstarz.backend.smartStarzApi.model.EventBean;
import com.norihome.smartstarz.com.norihome.smartstarz.beans.ApproveBean;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.GetEventsAsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class ApproveFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_approve, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().findViewById(R.id.approveListView).invalidate();
        EventBean[] events = getEvents();
        setUpApproveListView(events);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public EventBean[] getEvents() {
        EventBean[] eventBeanArray = null;

        GetEventsAsyncTask task = new GetEventsAsyncTask();
        try {
            List<EventBean> list = task.execute().get();

            if (list == null)
                System.out.println("************  Approve Fragment NULL BEAN COLLECTION   *********");
            else {
                System.out.println("size = "  + list.size());
                eventBeanArray = new EventBean[list.size()];
                list.toArray(eventBeanArray);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return eventBeanArray;

    }

    public void setUpApproveListView(EventBean[]  eventBeans) {

        List<ApproveBean> approveList = new ArrayList<ApproveBean>();

        if (eventBeans != null && eventBeans.length > 0) {
            // Convert EventBean to ApproveBeans
            for (EventBean anEvent : eventBeans) {
                String enrolledStudents = anEvent.getEnrolledStudents();
                if (enrolledStudents != null && enrolledStudents.length() > 0) {
                    StringTokenizer stringTokenizer = new StringTokenizer(enrolledStudents, ",");
                    while (stringTokenizer.hasMoreTokens()) {
                        String studentName = stringTokenizer.nextToken();
                        ApproveBean approveBean = new ApproveBean();
                        approveBean.setEventName(anEvent.getName());
                        approveBean.setStudentName(studentName);
                        approveList.add(approveBean);
                    }
                }
            }
        }

        ApproveBean[] approveBeans = new ApproveBean[approveList.size()];
        approveList.toArray(approveBeans);
        ListAdapter adapter = new AdminCustomAdapter(this.getActivity(), approveBeans, this );
        ListView myListView;
        myListView = (ListView) getView().findViewById(R.id.approveListView);
        myListView.setAdapter(adapter);

        myListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ApproveBean approveBean = (ApproveBean) (parent.getItemAtPosition(position));
                    }
                });
    }


}
