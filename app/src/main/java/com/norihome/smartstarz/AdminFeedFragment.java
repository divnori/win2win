package com.norihome.smartstarz;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.norihome.smartstarz.backend.smartStarzApi.model.EventBean;
import com.norihome.smartstarz.backend.smartStarzApi.model.StudentBean;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.GetEventsAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.GetStudentByNameAsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * A placeholder fragment containing a simple view.
 */
public class AdminFeedFragment extends Fragment {

    String userName = "";

    public AdminFeedFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_feed, container, false);

    }
        @Override
        public void onStart() {
            super.onStart();
            Intent intent = this.getActivity().getIntent();
            userName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
            System.out.println("AdminFeedFragment -- User Name = " + userName);

        }

        public EventBean[] getEvents() {
            EventBean[] eventBeanArray = null;


            GetEventsAsyncTask task = new GetEventsAsyncTask();
            try {
                List<EventBean> list = task.execute().get();

                if (list == null)
                    System.out.println("************  AdminFeedFragment -> GET EVENTS NULL BEAN COLLECTION   *********");
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

        @Override
        public void onResume() {
            super.onResume();
            refreshView();
        }

    public void refreshView() {
        getView().findViewById(R.id.adminFeed).invalidate();
        EventBean[] events = getEvents();
        setUpAdminFeed(events);
    }

    public void setUpAdminFeed(EventBean[]  eventBeans) {

        if (eventBeans == null || eventBeans.length ==0) {
            return;
        }

        // Sort events of same name into a map
        Map<String, List<EventBean>> eventNamesMap = new HashMap<String, List<EventBean>>();
        for (int z = 0; z < eventBeans.length; z++) {
            EventBean eventBean = eventBeans[z];
            int index = eventBean.getName().indexOf("-");
            String name = eventBean.getName().substring(0, index);
            List<EventBean> eventList = eventNamesMap.get(name);
            if (eventList == null) {
                eventList = new ArrayList<EventBean>();
                eventList.add(eventBean);
                eventNamesMap.put(name, eventList);
            } else
                eventList.add(eventBean);
        }

        List<EventBean> eventsToShow = new ArrayList<EventBean>();
        // Find the max level event in each category and only show current (max level) events
        // Remove the past events (lower than max level events)
        for (String eventName : eventNamesMap.keySet()) {

            List<EventBean> events = eventNamesMap.get(eventName);
            int maxLevel = 1;
            EventBean maxLevelEvent = null;
            if (events.size() > 1) {


                for (EventBean aBean : events) {
                    String currentEventName = aBean.getName();
                    int index = currentEventName.indexOf("-");
                    String levelStr = currentEventName.substring(index + 1);
                    int level = Integer.parseInt(levelStr);
                    if (level > maxLevel) {
                        maxLevel = level;
                        maxLevelEvent = aBean;
                    }
                }
            } else
                maxLevelEvent = events.get(0);
            eventsToShow.add(maxLevelEvent);
        }

        Collections.sort(eventsToShow, new Comparator<EventBean>() {
            public int compare(EventBean object1, EventBean object2) {
                return object1.getName().compareTo(object2.getName());
            }
        });

        eventBeans = new EventBean[eventsToShow.size()];
        eventsToShow.toArray(eventBeans);

        ListAdapter adapter = new AdminFeedCustomAdapter(this.getActivity(), eventBeans, userName, this);
        ListView myListView;
        myListView = (ListView) getView().findViewById(R.id.adminFeed);
        myListView.setAdapter(adapter);

        myListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        EventBean event = (EventBean) (parent.getItemAtPosition(position));
                    }
                });
    }
}


