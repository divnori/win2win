package com.norihome.smartstarz;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;


import com.norihome.smartstarz.backend.smartStarzApi.model.GroupsBean;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.GetGroupsAsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.R.attr.name;

/**
 * A placeholder fragment containing a simple view.
 */
public class GroupsFragment extends Fragment {

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_groups, container, false);
    }



    public GroupsBean[] getGroups() {
        GroupsBean[] groupsBeanArray = null;


        GetGroupsAsyncTask task = new GetGroupsAsyncTask();
        try {
            List<GroupsBean> list = task.execute().get();

            if (list == null)
                System.out.println("************  GroupsFragment -> GET GROUPS NULL BEAN COLLECTION   *********");
            else {
                System.out.println("size = " + list.size());
                groupsBeanArray = new GroupsBean[list.size()];
                list.toArray(groupsBeanArray);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return groupsBeanArray;

    }



    @Override
    public void onResume() {
        super.onResume();
        refreshView();
    }

    public void refreshView() {
        getView().findViewById(R.id.list_groupNames).invalidate();
        GroupsBean[] groups = getGroups();
        setUpGroupsListView(groups);
    }

    public void setUpGroupsListView(GroupsBean[] groupBeans) {

        GroupsBean bean1 = new GroupsBean();
        bean1.setName("");
        ListAdapter adapter = null;
        if (groupBeans == null || groupBeans.length == 0) {
            // If there are no groups then give the admin option to create groups
            GroupsBean bean2 = new GroupsBean();
            bean2.setName("");

            groupBeans = new GroupsBean[2];
            groupBeans[0] = bean1;
            groupBeans[1] = bean2;
            adapter = new GroupsCustomAdapter(this.getActivity(), groupBeans, this);
        } else {
            // If there are some groups already, allow admin to add one more group if needed
            GroupsBean[] groupBeans2 = new GroupsBean[groupBeans.length+1];
            for (int i = 0; i < groupBeans.length; i++)
                groupBeans2[i] = groupBeans[i];
            groupBeans2[groupBeans2.length-1] = bean1;
            adapter = new GroupsCustomAdapter(this.getActivity(), groupBeans2, this);
        }


        ListView myListView;
        myListView = (ListView) getView().findViewById(R.id.list_groupNames);
        myListView.setAdapter(adapter);
    }
}
