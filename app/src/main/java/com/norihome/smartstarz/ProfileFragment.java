package com.norihome.smartstarz;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.norihome.smartstarz.backend.smartStarzApi.model.GroupsBean;
import com.norihome.smartstarz.backend.smartStarzApi.model.StudentBean;
import com.norihome.smartstarz.com.norihome.smartstarz.QRCodeScannerActivity;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.AddStudentToGroupAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.GetGroupsAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.GetStudentByNameAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.RemoveStudentAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.StoreStudentsAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment  {

    public static final int QR_RESULT_CODE = 1;
    public static final String QR_RESULT = "QR_RESULT";
    public static final String QR_START_PHRASE = "Win2Win";
    String userName = "";
    StudentBean bean = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //this.getActivity().setContentView(R.layout.fragment_profile);

        Intent intent = this.getActivity().getIntent();
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        userName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        System.out.println("ProfileFragment -- User Name = " + userName);
        return view;

    }


    @Override
    public void onStart() {
        super.onStart();
        refreshView();
    }

    public void onResume() {
        super.onResume();
        refreshView();
    }

    private void refreshView() {
        bean = getStudentBean(userName);
        TextView nameView = (TextView) getView().findViewById(R.id.text_profileName);
        nameView.setText("Name:  " + userName);

        if (bean == null) {
            System.out.println("ProfileFragment::refreshView StudentBean is null");
            return;
        }
        addStudentToGroup(bean);

        TextView groupView = (TextView) getView().findViewById(R.id.text_profileGroup);
        groupView.setText("Group: " + bean.getGroupName());

        TextView scoreView = (TextView) getView().findViewById(R.id.text_profileSmartScore);
        if (bean != null && bean.getSmartScore() != null)
            scoreView.setText("Win Score:  " + bean.getSmartScore());

        if (bean != null && bean.getPastCompetitions() != null && !bean.getPastCompetitions().isEmpty()) {
            ListView listView = (ListView) getView().findViewById(R.id.list_pastCompetitions);
            System.out.println("Past comps " + bean.getPastCompetitions());
            String[] pastCompetitions = bean.getPastCompetitions().split(",");

            ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.simple_list_item_1,
                    pastCompetitions
            );

            listView.setAdapter(listViewAdapter);
        }

    }

    public StudentBean getStudentBean(String name) {
        System.out.println("getting student bean " + name);
        GetStudentByNameAsyncTask task = new GetStudentByNameAsyncTask();
        StudentBean studentBean = null;
        try {
            studentBean = task.execute(name).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // If Student Bean is null - create a new Student Bean
        if (studentBean == null) {
            StoreStudentsAsyncTask task2 = new StoreStudentsAsyncTask();
            studentBean = new StudentBean();
            studentBean.setName(name);
            task2.execute(studentBean);
        }

        return studentBean;
    }


    private void addStudentToGroup(StudentBean studentBean) {

        if (studentBean == null) {
            System.out.println("ProfileFragment::addStudentToGroup studentBean is null");
            return;
        }


        // Check if the student has already been assigned to a group
        if (studentBean.getGroupName() == null || studentBean.getGroupName().trim().length() == 0) {
            // Get existing groups and randomly assign student to a group
            GetGroupsAsyncTask grpTask = new GetGroupsAsyncTask();
            List<GroupsBean> groupsBeanList = null;
            try {
                groupsBeanList = grpTask.execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            // If no groups found print a message
            if (groupsBeanList == null || groupsBeanList.size() == 0) {
                System.out.println("No groups found, student not assigned to any group");
            } else {
                // Sort the groups by their sizes
                Map<Integer, List<GroupsBean>> groupSizesMap = new HashMap<Integer, List<GroupsBean>>();
                for (GroupsBean grp : groupsBeanList) {
                    String members = grp.getGroupMembers();
                    if (members != null && members.trim().length() > 0) {
                        StringTokenizer strTok = new StringTokenizer(members, ",");
                        List<GroupsBean> groupsList = groupSizesMap.get(strTok.countTokens());
                        if (groupsList == null) {
                            groupsList = new ArrayList<GroupsBean>();
                            groupSizesMap.put(strTok.countTokens(), groupsList);
                        }
                        groupsList.add(grp);
                    } else {
                        // No group members add group to size 0
                        List<GroupsBean> emptyGroupsList = groupSizesMap.get(0);
                        if (emptyGroupsList == null) {
                            emptyGroupsList = new ArrayList<GroupsBean>();
                            groupSizesMap.put(0, emptyGroupsList);
                        }
                        emptyGroupsList.add(grp);
                    }
                }
                // Pick the groups with least students and assign student to a randomly selected one in those groups
                Set<Integer> groupSizes = groupSizesMap.keySet();
                int minSize = Integer.MAX_VALUE;
                for (Integer aSize : groupSizes) {
                    if (aSize < minSize)
                        minSize = aSize;
                }
                List<GroupsBean> minSizeGroups = groupSizesMap.get(minSize);
                int numOfMinSizeGroups = minSizeGroups.size();

                // Randomly pick one of these groups
                Random random = new Random();
                int randomInt = 0;
                if (numOfMinSizeGroups > 1)
                    random.nextInt(numOfMinSizeGroups - 1);
                GroupsBean grp = minSizeGroups.get(randomInt);

                // Assign student to group
                studentBean.setGroupName(grp.getName());
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
                builder.setTitle("Group Assignment");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setMessage("Hi " + studentBean.getName() +
                        ", You are assigned to group " + grp.getName());
                AlertDialog dialog = builder.create();
                dialog.show();


                // Update student and group data on backend
                Map<String, Object> params = new HashMap<String, Object>();
                params.put(AddStudentToGroupAsyncTask.STUDENT_NAME, studentBean.getName());
                params.put(AddStudentToGroupAsyncTask.GROUP_BEAN, grp);

                AddStudentToGroupAsyncTask addStudentToGroupAsyncTask = new AddStudentToGroupAsyncTask();
                addStudentToGroupAsyncTask.execute(params);
            }
        } else {
            System.out.println("ProfileFragment:: Student " + studentBean.getName() +
                    " already assigned to group " + studentBean.getGroupName());
        }

    }


    public void onScanButtonClick(View v) {

        System.out.println("ProfileFragment :: onScanButtonClick");
        Intent intent = new Intent(this.getActivity(), QRCodeScannerActivity.class);
        try {
            startActivityForResult(intent, QR_RESULT_CODE);
        } catch (RuntimeException re) {
            Toast.makeText(this.getActivity(), "onScnBtnCl " + re.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // Check which request we're responding to
            if (requestCode == QR_RESULT_CODE) {
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {
                    final String qrCode = data.getStringExtra(QR_RESULT); // QR code is the event name

                    // Check that this is a Smart Starz QR code
                    if (qrCode != null && !qrCode.startsWith(QR_START_PHRASE)) {
                        Toast.makeText(this.getActivity(),
                                "This is not a valid Win2Win Event Code"
                                //+ ", Format = " + rawResult.getBarcodeFormat().toString()
                                ,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
                    builder.setCancelable(true);
                    builder.setTitle("Confirmation");
                    final String eventWithLevel = qrCode.substring(qrCode.indexOf(":")+1);
                    String eventName = eventWithLevel.substring(0, eventWithLevel.indexOf("-"));

                    builder.setMessage("Add " + eventWithLevel + ", " + eventName + " to your profile?");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    approveStudent(eventWithLevel);
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
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } catch (RuntimeException re) {
            Toast.makeText(this.getActivity(), "onAcRes " + re.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void approveStudent(String qrCode) {
        // Approve the student for this activity
        Map<String, String> params = new HashMap<String, String>();
        params.put(RemoveStudentAsyncTask.EVENT_NAME, qrCode);
        params.put(RemoveStudentAsyncTask.STUDENT_NAME, userName);
        params.put(RemoveStudentAsyncTask.UPDATE_PATH, "2");

        //Remove student from enrolled students list in event bean and update student
        RemoveStudentAsyncTask task = new RemoveStudentAsyncTask();
        task.execute(params);

        Toast.makeText(this.getActivity(),
                qrCode + " has been added to your profile.",
                Toast.LENGTH_SHORT).show();
    }

}
