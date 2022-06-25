package com.norihome.smartstarz;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.norihome.smartstarz.backend.smartStarzApi.model.GroupsBean;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.DeleteGroupAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.StoreGroupsAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.UpdateGroupAsyncTask;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static com.norihome.smartstarz.R.id.button_number;
import static com.norihome.smartstarz.R.id.editText_groupName;



/**
 * Created by User on 4/4/2017.
 */

class GroupsCustomAdapter extends ArrayAdapter<GroupsBean> {

    GroupsInfoHolder holder;
    GroupsBean groupsBean;
    GroupsFragment fragment;

    public GroupsCustomAdapter(Context context, GroupsBean[] groups, GroupsFragment f) {
        super(context, R.layout.custom_groupsrow, groups);
        System.out.println("GroupsCustomAdapter created");
        fragment = f;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        System.out.println("GroupsCustomAdapter :: getView method called for position" + position);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.custom_groupsrow, parent, false);

        GroupsBean singleGroup = getItem(position);
        EditText editGroupName = (EditText) customView.findViewById(editText_groupName);
        editGroupName.setText(singleGroup.getName());

        Button groupSizeButton = (Button) customView.findViewById(button_number);

        int groupSize = 0;
        if (singleGroup.getGroupMembers() != null && singleGroup.getGroupMembers().trim().length() > 0) {
            StringTokenizer groupSizeTokenizer = new StringTokenizer(singleGroup.getGroupMembers().trim(), ",");
            groupSize = groupSizeTokenizer.countTokens();
        }

        groupSizeButton.setText(Html.fromHtml(""+groupSize+"<br/><small><small>Group Size</small></small>"));
        Button createBtn = (Button) customView.findViewById(R.id.button_create);
        if (singleGroup.getName() != null && singleGroup.getName().trim().length() > 0)
            createBtn.setEnabled(false);
        Button updateBtn = (Button) customView.findViewById(R.id.button_update);
        Button deleteBtn = (Button) customView.findViewById(R.id.button_delete);

        /*if (editGroupName.getText().length() == 0) {
            Toast.makeText(fragment.getActivity(), "Please enter a group name", Toast.LENGTH_SHORT).show();
        }*/


        GroupsInfoHolder holder = new GroupsInfoHolder();
        holder.groupName = singleGroup.getName();
        holder.groupNameText = editGroupName;
        holder.button_create = createBtn;
        holder.button_update = updateBtn;
        holder.button_delete = deleteBtn;
        holder.button_groupSize = groupSizeButton;
        holder.groupMembers = singleGroup.getGroupMembers();

        createBtn.setTag(holder);
        updateBtn.setTag(holder);
        deleteBtn.setTag(holder);
        customView.setTag(holder);
        groupSizeButton.setTag(holder);

        holder.button_create.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                GroupsInfoHolder holder = (GroupsInfoHolder) v.getTag();
                EditText editGroupName = holder.groupNameText;
                if (editGroupName.getText() == null || editGroupName.getText().toString().trim().length() == 0) {
                    Toast.makeText(fragment.getActivity(), "Please enter a group name", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                builder.setCancelable(true);
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you would like to create this group?");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createGroup(v);
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

            private void createGroup(View v) {
                Button thisBtn = (Button) v;
                GroupsInfoHolder holder = (GroupsInfoHolder) v.getTag();
                // Create a GroupsBean with the information the user entered
                GroupsBean bean = new GroupsBean();
                EditText editGroupName = holder.groupNameText;
                bean.setName(editGroupName.getText().toString().trim());

                // Create the Store Groups Async Task and execute it
                // This task stores the group information in the Google cloudstore (database)
                StoreGroupsAsyncTask task = new StoreGroupsAsyncTask();
                task.execute(bean);

                Toast.makeText(fragment.getActivity(), "Group Created", Toast.LENGTH_SHORT).show();
                fragment.onResume();
            }

        });

        holder.button_update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                builder.setCancelable(true);
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you would like to update this group?");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateGroup(v);
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

            private void updateGroup(View v) {
                Button thisBtn = (Button) v;
                GroupsInfoHolder holder = (GroupsInfoHolder) v.getTag();

                EditText editGroupName = holder.groupNameText;

                if (editGroupName.getText() == null || editGroupName.getText().toString().trim().length() == 0) {
                    Toast.makeText(fragment.getActivity(), "Please enter a group name", Toast.LENGTH_SHORT).show();
                return;
            }
                Map<String, String> params = new HashMap<String, String>();
                params.put(UpdateGroupAsyncTask.NEW_GROUP_NAME, editGroupName.getText().toString().trim());
                params.put(UpdateGroupAsyncTask.OLD_GROUP_NAME, holder.groupName);

                UpdateGroupAsyncTask task = new UpdateGroupAsyncTask();
                task.execute(params);

                Toast.makeText(fragment.getActivity(), "Group Updated", Toast.LENGTH_SHORT).show();
                fragment.refreshView();
            }

        });

        holder.button_delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                builder.setCancelable(true);
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you would like to delete this group?");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteGroup(v);
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

            private void deleteGroup(View v) {
                Button thisBtn = (Button) v;
                GroupsInfoHolder holder = (GroupsInfoHolder) v.getTag();
                // Create a GroupsBean with the information the user entered
                GroupsBean bean = new GroupsBean();
                EditText editGroupName = holder.groupNameText;

                if (editGroupName.getText() == null || editGroupName.getText().toString().trim().length() == 0) {
                    Toast.makeText(fragment.getActivity(), "Please enter a group name", Toast.LENGTH_SHORT).show();
                    return;
                }
                bean.setName(editGroupName.getText().toString().trim());

                // Create the Store Groups Async Task and execute it
                // This task stores the group information in the Google cloudstore (database)
                DeleteGroupAsyncTask task = new DeleteGroupAsyncTask();
                task.execute(editGroupName.getText().toString());

                Toast.makeText(fragment.getActivity(), "Group Deleted", Toast.LENGTH_SHORT).show();
                fragment.refreshView();
            }

        });

        holder.button_groupSize.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                GroupsInfoHolder holder = (GroupsInfoHolder) v.getTag();
                String[] grpMembersArray = null;
                if (holder.groupMembers != null && holder.groupMembers.length() > 0) {
                    StringTokenizer strtok = new StringTokenizer(holder.groupMembers, ",");
                    grpMembersArray = new String[strtok.countTokens()];
                    int i = 0;
                    while (strtok.hasMoreTokens()) {
                        grpMembersArray[i] = strtok.nextToken();
                        i++;
                    }
                } else {
                    grpMembersArray = new String[1];
                    grpMembersArray[0] = "There are no members in this group";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
                builder.setTitle("Group Members");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setItems(grpMembersArray,null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return customView;
    }

    static class GroupsInfoHolder {
        String groupName;
        EditText groupNameText;
        Button button_create;
        Button button_update;
        Button button_delete;
        Button button_groupSize;
        String groupMembers;
    }
}

