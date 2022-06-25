package com.norihome.smartstarz.backend;

import java.util.Comparator;
import java.util.List;

/**
 * The object model for the data we are sending through endpoints
 */
public class EventBean implements Comparator<EventBean>{

    private long id;
    private String name;
    private String date;
    private String description;
    private String enrolledStudents;
    private long level;
    private String allowedStudents;

    public String getAllowedStudents() {
        return allowedStudents;
    }

    public void setAllowedStudents(String allowedStudents) {
        this.allowedStudents = allowedStudents;
    }



    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }




    public String getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(String enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public int compare(EventBean eventBean, EventBean t1) {
        if (eventBean == null || t1 == null)
            return 0;
        else {
            return eventBean.getName().compareToIgnoreCase(t1.getName());
        }
    }
}