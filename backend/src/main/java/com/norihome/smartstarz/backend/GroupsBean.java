package com.norihome.smartstarz.backend;

/**
 * Created by User on 4/4/2017.
 */

public class GroupsBean {
    private long id;
    private String name;
    private String groupMembers;


    public String getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(String groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


}
