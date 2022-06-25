package com.norihome.smartstarz;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.norihome.smartstarz.backend.smartStarzApi.model.StudentBean;
import com.norihome.smartstarz.com.norihome.smartstarz.beans.RankingsBean;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.GetStudentsAsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class RankingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rankings, container, false);
    }
    @Override
    public void onResume() {
        super.onResume();
        System.out.println("RankingsFragment ::::: onResume() method called");
        getView().findViewById(R.id.RankingsListView).invalidate();
        StudentBean[] students = getStudents();
        setUpRankingsListView(students);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    public StudentBean[] getStudents() {
        StudentBean[] studentBeanArray = null;
        GetStudentsAsyncTask task = new GetStudentsAsyncTask();
        try {
            List<StudentBean> list = task.execute().get();

            if (list == null)
                System.out.println("************  RankingsFragment NULL STUDENT BEAN COLLECTION   *********");
            else {

                studentBeanArray = new StudentBean[list.size()];
                list.toArray(studentBeanArray);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (studentBeanArray != null && studentBeanArray.length > 0)
            System.out.println("RankingsFragment :: getStudents number " + studentBeanArray.length);
        return studentBeanArray;

    }

    public void setUpRankingsListView(StudentBean[]  studentBeans) {

        if(studentBeans == null)
            return;

        List<RankingsBean> rankingsList = new ArrayList<RankingsBean>();
        Map<String, Long> groupScoresMap = new HashMap<String, Long>();

        // Compute Group Scores
        for (StudentBean aStudent : studentBeans) {
            if (aStudent.getGroupName() != null && aStudent.getGroupName().trim().length() > 0) {
                if (groupScoresMap.containsKey(aStudent.getGroupName())) {
                    Long groupScore = groupScoresMap.get(aStudent.getGroupName());
                    groupScore = groupScore + aStudent.getSmartScore();
                    System.out.println("groupScore = " + groupScore);
                    groupScoresMap.put(aStudent.getGroupName(), groupScore);
                } else {
                    groupScoresMap.put(aStudent.getGroupName(), aStudent.getSmartScore());
                }
            }
        }

        SortedMap<Long, List<String>> sortedGroupMap = new TreeMap<Long, List<String>>(Collections.<Long>reverseOrder());
        for (String groupName : groupScoresMap.keySet()) {
            Long groupScore = groupScoresMap.get(groupName);
            List<String> groupsWithThisScore = sortedGroupMap.get(groupScore);
            if (groupsWithThisScore == null) {
                groupsWithThisScore = new ArrayList<String>();
                sortedGroupMap.put(groupScore,groupsWithThisScore);
            }
            groupsWithThisScore.add(groupName);
        }

        int rank = 1;
        // For each group create a ranking bean
        for (Long score : sortedGroupMap.keySet()) {
            List<String> groupsWithSameScore = sortedGroupMap.get(score);
            Collections.sort(groupsWithSameScore);
            for (String groupName : groupsWithSameScore) {
                RankingsBean rankingsBean = new RankingsBean();
                rankingsBean.setRank(rank);
                rankingsBean.setGroupName(groupName);
                rankingsBean.setSmartScore(score);
                rankingsList.add(rankingsBean);
                rank++;
            }
        }

        RankingsBean[] rankingsBeans = new RankingsBean[rankingsList.size()];
        rankingsList.toArray(rankingsBeans);
        ListAdapter adapter = new RankingsCustomAdapter(this.getActivity(), rankingsBeans );
        ListView myListView;
        myListView = (ListView) getView().findViewById(R.id.RankingsListView);
        myListView.setAdapter(adapter);

        myListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        RankingsBean rankingsBean = (RankingsBean) (parent.getItemAtPosition(position));
                    }
                });
    }


}
