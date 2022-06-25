package com.norihome.smartstarz;

import android.app.usage.UsageEvents;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class EventFeedFragment extends Fragment {

    String userName = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_event_feed, container, false);
    }


    @Override
    public void onStart() {
        super.onStart();
        Intent intent = this.getActivity().getIntent();
        userName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        System.out.println("EventFeedFragment -- User Name = " + userName);

    }

    public EventBean[] getEvents() {
        EventBean[] eventBeanArray = null;


        GetEventsAsyncTask task = new GetEventsAsyncTask();
        try {
            List<EventBean> list = task.execute().get();

            if (list == null)
                System.out.println("************  EventFeedFragment -> GET EVENTS NULL BEAN COLLECTION   *********");
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
        getView().findViewById(R.id.eventListView).invalidate();
        EventBean[] events = getEvents();
        setUpEventListView(events);
    }

    public void setUpEventListView(EventBean[]  eventBeans) {

        if (eventBeans == null || eventBeans.length ==0) {
            return;
        }

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

        // Remove past events from the student's event feed
        StudentBean student = getStudentBean();
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

            if (student != null ) {
                // Student logged in before and already enrolled in some events
                // Add Level 1 events if not already in past competitions
                // Add highter level events if student is in the allowed list
                if (maxLevel > 1) {
                    if (student.getPastCompetitions() != null &&
                            !student.getPastCompetitions().contains(maxLevelEvent.getName())) {
                            if (maxLevelEvent.getAllowedStudents() != null &&
                                    maxLevelEvent.getAllowedStudents().contains(student.getName())
                                    ) {
                                eventsToShow.add(maxLevelEvent);
                            }
                    }
                } else {
                    if ( student.getPastCompetitions() == null ||
                            (student.getPastCompetitions() != null &&
                            !student.getPastCompetitions().contains(maxLevelEvent.getName()))) {
                        eventsToShow.add(maxLevelEvent);
                    }
                }
            } else {
                // Student logged in first time and not enrolled into any event
                // Only show Level 1 events that are not expired
                if (maxLevel == 1 )
                    eventsToShow.add(maxLevelEvent);
            }
        }


        Collections.sort(eventsToShow, new Comparator<EventBean>() {
            public int compare(EventBean object1, EventBean object2) {
                return object1.getName().compareTo(object2.getName());
            }
        });

        eventBeans = new EventBean[eventsToShow.size()];
        eventsToShow.toArray(eventBeans);

        ListAdapter adapter = new EventListCustomAdapter(this.getActivity(), eventBeans, userName, this);
        ListView myListView;
        myListView = (ListView) getView().findViewById(R.id.eventListView);
        myListView.setAdapter(adapter);

        myListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        EventBean event = (EventBean) (parent.getItemAtPosition(position));
                    }
                });
    }

    public StudentBean getStudentBean() {
        System.out.println("EventFeedFragment ::getting student bean " + userName);
        GetStudentByNameAsyncTask task = new GetStudentByNameAsyncTask();
        StudentBean studentBean = null;
        try {
            studentBean = task.execute(userName).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return studentBean;
    }

}
