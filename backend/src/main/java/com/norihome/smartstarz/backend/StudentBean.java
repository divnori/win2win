package com.norihome.smartstarz.backend;

import java.util.List;

/**
 * Created by User on 12/27/2016.
 */

public class StudentBean {

    private long id;
    private String name;
    private long smartScore;
    private String pastCompetitions;
    private String enrolledCompetitions;
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }



    public String getEnrolledCompetitions() {
        return enrolledCompetitions;
    }

    public void setEnrolledCompetitions(String enrolledCompetitions) {
        this.enrolledCompetitions = enrolledCompetitions;
    }



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSmartScore() {
        return smartScore;
    }

    public void setSmartScore(long smartScore) {
        this.smartScore = smartScore;
    }

    public String getPastCompetitions() {
        return pastCompetitions;
    }

    public void setPastCompetitions(String pastCompetitions) {
        this.pastCompetitions = pastCompetitions;
    }




}


