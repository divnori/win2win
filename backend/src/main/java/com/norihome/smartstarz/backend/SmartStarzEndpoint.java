/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.norihome.smartstarz.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.inject.Named;

import static com.google.appengine.api.datastore.TransactionOptions.Builder.withXG;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "smartStarzApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.smartstarz.norihome.com",
                ownerName = "backend.smartstarz.norihome.com",
                packagePath = ""
        )
)
public class SmartStarzEndpoint {

        public static final String NAME = "name";
        public static final String EVENT_DESC = "description";
        public static final String EVENT_DATE = "date";
        public static final String EVENT_ENROLLEDSTUDENTS = "enrolledStudents";
        public static final String EVENT_ID = "id";
        public static final String COMMA = ",";
        public static final String EVENT_LEVEL = "level";
        public static final String EVENT_ALLOWEDSTUDENTS = "allowedStudents";

        public static final String STUDENT_NAME = "StudentName";
        public static final String STUDENT_SMARTSCORE = "SmartScore";
        public static final String STUDENT_PASTCOMPETITIONS = "PastCompetitions";
        public static final String STUDENT_ENROLLEDCOMPETITIONS = "EnrolledCompetitions";
        public static final String STUDENT_GROUP = "StudentGroup";
        public static final String STUDENT_ID = "Id";

        public static final String GROUP_ID = "id";
        public static final String GROUP_NAME = "name";
        public static final String GROUP_MEMBERS = "groupMembers";

        @ApiMethod(name = "storeEvent")
        public void storeEvent(EventBean bean) {
            System.out.println("ENDPOINT -> storeEvent method called with EventName " + bean.getName());
            DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
            Transaction txn = datastoreService.beginTransaction();

            String nextEvent = bean.getName();
            int index = nextEvent.indexOf("-");
            String levelStr = nextEvent.substring(index + 1);
            System.out.println("Level =" + levelStr);
            int level = Integer.parseInt(levelStr);

            if (level > 1 ) {
                // This is a higher level event.
                // Check if the next level event was already created, if so don't create again
                Filter nextEventFilter =
                        new FilterPredicate(NAME, FilterOperator.EQUAL, nextEvent);
                Query nextEventQuery = new Query("EventBean").setFilter(nextEventFilter);
                List<Entity> nextEvents =
                        datastoreService.prepare(nextEventQuery).asList(FetchOptions.Builder.withDefaults());

                if (nextEvents == null || nextEvents.size() == 0 ) {
                    // Next Event is not already created
                    int oldLevel = level - 1;
                    String name = nextEvent.substring(0, index);
                    String oldEvent = name + "-" + oldLevel;
                    System.out.println("Old Event = " + oldEvent);
                    Filter oldEventFilter =
                            new FilterPredicate(NAME, FilterOperator.EQUAL, oldEvent);

                    Query q = new Query("EventBean").setFilter(oldEventFilter);
                    List<Entity> results =
                            datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());

                    System.out.println("Store event found " + results.size() + " number of events");
                    for (Entity entity : results) {
                        EventBean olBean = getEventBean(entity);
                        if (olBean != null) {

                            if (level > olBean.getLevel()) {
                                System.out.println("Reached the max level of this event ");
                                return;
                            }
                            bean.setDate(olBean.getDate());
                            bean.setDescription(olBean.getDescription());
                            // Update old bean desc to say it is now expired.
                            olBean.setDescription("Expired");
                            bean.setLevel(olBean.getLevel());
                        }

                        //put new bean and update old bean in datastore
                        try {
                            Entity newEventBeanEntity = getEventEntity(bean);
                            datastoreService.put(newEventBeanEntity);

                            txn.commit();
                        } finally {
                            if ( txn.isActive()){
                                txn.rollback();
                            }
                        }


                    }
                }
            } else {
                // This is a first level event. We can add it without any checks
                //put new bean in database
                try {
                    Entity eventBeanEntity = getEventEntity(bean);
                    datastoreService.put(eventBeanEntity);
                    txn.commit();
                } finally {
                    if (txn.isActive()) {
                        txn.rollback();
                    }
                }
            }

            System.out.println("---------       storeEvent COMPLETE   ------------------");
        }

    private Entity getEventEntity(EventBean bean) {
        Key eventBeanParentKey = KeyFactory.createKey("EventBeanParent", "root");
        Entity eventBeanEntity = new Entity("EventBean", eventBeanParentKey);
        eventBeanEntity.setProperty(NAME, bean.getName());
        eventBeanEntity.setProperty(EVENT_DESC, bean.getDescription());
        eventBeanEntity.setProperty(EVENT_DATE, bean.getDate());
        eventBeanEntity.setProperty(EVENT_ID,bean.getId());
        eventBeanEntity.setProperty(EVENT_ENROLLEDSTUDENTS, bean.getEnrolledStudents());
        eventBeanEntity.setProperty(EVENT_LEVEL,bean.getLevel());
        eventBeanEntity.setProperty(EVENT_ALLOWEDSTUDENTS, bean.getAllowedStudents());
        return eventBeanEntity;
    }
    private Entity getGroupsEntity(GroupsBean bean) {
        Key groupsBeanParentKey = KeyFactory.createKey("GroupsBeanParent", "root");
        Entity groupsBeanEntity = new Entity("GroupsBean", groupsBeanParentKey);
        groupsBeanEntity.setProperty(GROUP_ID, bean.getId());
        groupsBeanEntity.setProperty(GROUP_NAME, bean.getName());
        groupsBeanEntity.setProperty(GROUP_MEMBERS, bean.getGroupMembers());
        return groupsBeanEntity;
    }
    @ApiMethod(name = "storeStudent")
        public void storeStudent(StudentBean bean) {
           System.out.println("ENDPOINT -> storeStudent called");
           DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
           Transaction txn = datastoreService.beginTransaction();

            Filter propertyFilter =
                new FilterPredicate(STUDENT_NAME, FilterOperator.EQUAL, bean.getName());
            Query q = new Query("StudentBean").setFilter(propertyFilter);
            List<Entity> results =
                datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());

            if (results != null && results.size() > 0) {
                System.out.println("A StudentBean already exists with this name");
            } else {
                try {
                    Entity studentBeanEntity = getStudentEntity(bean);
                    datastoreService.put(studentBeanEntity);
                    txn.commit();
                } finally {
                    if (txn.isActive()) {
                        txn.rollback();
                    }
                }
            }
            System.out.println("ENDPOINT -> storeStudent complete");
        }

    private Entity getStudentEntity(StudentBean bean) {
        Key studentBeanParentKey = KeyFactory.createKey("StudentBeanParent", "root");
        Entity studentBeanEntity = new Entity("StudentBean", studentBeanParentKey);
        studentBeanEntity.setProperty(STUDENT_NAME, bean.getName());
        studentBeanEntity.setProperty(STUDENT_SMARTSCORE, bean.getSmartScore());
        studentBeanEntity.setProperty(STUDENT_PASTCOMPETITIONS, bean.getPastCompetitions());
        studentBeanEntity.setProperty(STUDENT_ENROLLEDCOMPETITIONS, bean.getEnrolledCompetitions());
        studentBeanEntity.setProperty(STUDENT_GROUP, bean.getGroupName());
        studentBeanEntity.setProperty(STUDENT_ID,bean.getId());
        return studentBeanEntity;
    }




        @ApiMethod(name = "getEvents")
        public List<EventBean> getEvents() {
            System.out.println("ENDPOINT -> getEvents method called");
            DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
                Key eventBeanParentKey = KeyFactory.createKey("EventBeanParent", "root");
                Query query = new Query(eventBeanParentKey);
                List<Entity> results = datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults());
                ArrayList<EventBean> events = new ArrayList<EventBean>();
                for (Entity entity : results) {
                    EventBean bean = getEventBean(entity);
                    events.add(bean);
                }

            System.out.println("---------       getEvents COMPLETE   ------------------");
            return events;
        }

    private EventBean getEventBean(Entity entity) {
        EventBean bean = new EventBean();
        bean.setId(entity.getKey().getId());
        bean.setName((String) entity.getProperty(NAME));
        bean.setDescription((String) entity.getProperty(EVENT_DESC));
        bean.setDate((String) entity.getProperty(EVENT_DATE));
        bean.setEnrolledStudents((String) entity.getProperty(EVENT_ENROLLEDSTUDENTS));
        bean.setLevel((Long) entity.getProperty(EVENT_LEVEL));
        bean.setAllowedStudents((String) entity.getProperty(EVENT_ALLOWEDSTUDENTS));
        return bean;
    }

    private StudentBean getStudentBean(Entity entity) {
        StudentBean bean = new StudentBean();
        bean.setId(entity.getKey().getId());
        bean.setName((String) entity.getProperty(STUDENT_NAME));
        if (entity.getProperty(STUDENT_SMARTSCORE) != null)
            bean.setSmartScore((Long) entity.getProperty(STUDENT_SMARTSCORE));
        if (entity.getProperty(STUDENT_PASTCOMPETITIONS) != null)
            bean.setPastCompetitions((String) entity.getProperty(STUDENT_PASTCOMPETITIONS));
        if (entity.getProperty(STUDENT_ENROLLEDCOMPETITIONS) != null)
            bean.setEnrolledCompetitions((String) entity.getProperty(STUDENT_ENROLLEDCOMPETITIONS));
        if (entity.getProperty(STUDENT_GROUP) != null)
            bean.setGroupName((String) entity.getProperty(STUDENT_GROUP));
        return bean;
    }
    private GroupsBean getGroupsBean(Entity entity) {
        GroupsBean groupsBean = new GroupsBean();
        groupsBean.setId(entity.getKey().getId());
        groupsBean.setName((String) entity.getProperty(GROUP_NAME));
        if (entity.getProperty(GROUP_NAME) != null)
            groupsBean.setName((String) entity.getProperty(GROUP_NAME));
        if (entity.getProperty(GROUP_ID) != null)
            groupsBean.setId((Long) entity.getProperty(GROUP_ID));
        if (entity.getProperty(GROUP_MEMBERS) != null)
            groupsBean.setGroupMembers((String) entity.getProperty(GROUP_MEMBERS));
        return groupsBean;
    }

    @ApiMethod(name = "addStudentToEvent")
        public void addStudentToEvent(@Named("eventName") String eventName, @Named("studentName") String studentName) {

            System.out.println("ENDPOINT -> AddStudentToEvent method called with EventName " + eventName + ", StudentName " + studentName);
            DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
            Filter propertyFilter =
                    new FilterPredicate(NAME, FilterOperator.EQUAL, eventName);
            Query q = new Query("EventBean").setFilter(propertyFilter);
            List<Entity> results =
                    datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());

            for (Entity entity : results) {
                EventBean bean = getEventBean(entity);
                if (studentName != null && studentName.length() > 0) {
                    if (bean.getEnrolledStudents() != null && bean.getEnrolledStudents().length() > 0)
                        bean.setEnrolledStudents(bean.getEnrolledStudents() + ", " + studentName);
                    else
                        bean.setEnrolledStudents(studentName);
                }

                datastoreService.delete(entity.getKey());
                Entity newEventEntity = getEventEntity(bean);
                datastoreService.put(newEventEntity);
            }

            // Add the eventName to the Student's list of enrolled events
            Filter studentFilter =
                    new FilterPredicate(STUDENT_NAME, FilterOperator.EQUAL, studentName);
            Query q2 = new Query("StudentBean").setFilter(studentFilter);
            List<Entity> studentResults =
                    datastoreService.prepare(q2).asList(FetchOptions.Builder.withDefaults());

            if (studentResults != null && studentResults.size() > 0) {
                for (Entity entity : studentResults) {
                    StudentBean studentBean = getStudentBean(entity);

                    if (eventName != null && eventName.length() > 0) {
                        if (studentBean.getEnrolledCompetitions() != null && studentBean.getEnrolledCompetitions().length() > 0)
                            studentBean.setEnrolledCompetitions(studentBean.getEnrolledCompetitions() + "," + eventName);
                        else
                            studentBean.setEnrolledCompetitions(eventName);
                        datastoreService.delete(entity.getKey());
                        Entity updatedStudentEntity = getStudentEntity(studentBean);
                        datastoreService.put(updatedStudentEntity);
                    }
                }
            } else {
                // This is the first event that the student is interested in
                // Add a new Student Entity with the name and enrolled events
                StudentBean newStudent = new StudentBean();
                newStudent.setName(studentName);
                newStudent.setEnrolledCompetitions(eventName);
                datastoreService.put(getStudentEntity(newStudent));
            }
            System.out.println("---------       addStudentToEvent COMPLETE   ------------------\n");

        }

    @ApiMethod(name = "getEventsByName")
    public List<EventBean> getEventsByName(@Named("eventName") String eventName) {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Filter propertyFilter =
                new FilterPredicate(NAME, FilterOperator.EQUAL, eventName);
        Query q = new Query("EventBean").setFilter(propertyFilter);
        List<Entity> results =
                datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());
        ArrayList<EventBean> events = new ArrayList<EventBean>();
        for (Entity entity : results) {
            events.add(getEventBean(entity));
        }

        return events;
    }

    @ApiMethod(name = "getStudentByName")
    public StudentBean getStudentByName(@Named("studentName") String studentName) {
        System.out.println("ENDPOINT -> getStudentByName method called with Student Name " + studentName);

        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Filter propertyFilter =
                new FilterPredicate(STUDENT_NAME, FilterOperator.EQUAL, studentName);
        Query q = new Query("StudentBean").setFilter(propertyFilter);
        List<Entity> results =
                datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());
        ArrayList<StudentBean> students = new ArrayList<StudentBean>();
        for (Entity entity : results) {
            students.add(getStudentBean(entity));
        }

        if (students.size() > 0)
            return students.get(0);
        else
            return null;
    }


    @ApiMethod(name = "updateStudent")
    public void updateStudent(@Named("studentName") String studentName, @Named("eventName") String eventName) {
        System.out.println("ENDPOINT -> updateStudent method called with EventName " + eventName
                + ", StudentName " + studentName
                );
        //Getting relevant Student bean
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Filter propertyFilter =
                new FilterPredicate(STUDENT_NAME, FilterOperator.EQUAL, studentName);
        Query q = new Query("StudentBean").setFilter(propertyFilter);
        List<Entity> results =
                datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());

        //updating smartScore and pastCompetitions
        for (Entity entity : results) {
            StudentBean bean = getStudentBean(entity);
            long previousScore = bean.getSmartScore();
            long newScore = previousScore + 10;
            bean.setSmartScore(newScore);
            if (eventName != null && eventName.length() > 0) {
                if (bean.getPastCompetitions() != null && bean.getPastCompetitions().length() > 0)
                    bean.setPastCompetitions(bean.getPastCompetitions() + "," + eventName);
                else
                    bean.setPastCompetitions(eventName);
            }

            datastoreService.delete(entity.getKey());
            Entity newStudentEntity = getStudentEntity(bean);
            datastoreService.put(newStudentEntity);
        }



        System.out.println("---------       updateStudent COMPLETE   ------------------");

    }

    @ApiMethod(name = "removeStudentFromEvent")
    public void removeStudentFromEvent(@Named("eventName") String eventName,
                                       @Named("studentName") String studentName,
                                       @Named("updateStudent") String updateStudentStr) {

        int updateStudent = Integer.parseInt(updateStudentStr);

        System.out.println("ENDPOINT -> removeStudentFromEvent method called with EventName "
                + eventName + ", StudentName " + studentName + ", UpdateStudent flag " + updateStudentStr);
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Filter propertyFilter =
                new FilterPredicate(NAME, FilterOperator.EQUAL, eventName);
        Query q = new Query("EventBean").setFilter(propertyFilter);
        List<Entity> results =
                datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());

        for (Entity entity : results) {
            EventBean anEvent = getEventBean(entity);
            if (studentName != null && studentName.length() > 0) {
                if (anEvent.getEnrolledStudents() != null && anEvent.getEnrolledStudents().length() > 0) {
                    List<String> keepList = new ArrayList<String>();
                    StringTokenizer studentTokenizer = new StringTokenizer(anEvent.getEnrolledStudents(), ",");
                    while (studentTokenizer.hasMoreTokens()) { 
                        String token = studentTokenizer.nextToken();
                        if (!token.equals(studentName)) {
                            keepList.add(token);
                        }
                    } // end while loop
                    // Create a new enrolledStudents Comma separated String
                    String newEnrolledStudents = "";
                    for (String name : keepList) {
                        newEnrolledStudents = newEnrolledStudents + name + ",";
                    }
                    anEvent.setEnrolledStudents(newEnrolledStudents);

                    // Update the current EventBean into Datastore
                    datastoreService.delete(entity.getKey());
                    Entity newEventEntity = getEventEntity(anEvent);
                    datastoreService.put(newEventEntity);
                } 

            }

            // Get the new event and add student to its allowedStudents
            // Only if the student is advancing
            if (updateStudent == 3) {
                String currentEventName = anEvent.getName();
                int index = currentEventName.indexOf("-");
                String levelStr = currentEventName.substring(index + 1);
                System.out.println("Level =" + levelStr);
                int level = Integer.parseInt(levelStr);
                int newLevel = level + 1;
                String name = currentEventName.substring(0, index);
                String newEvent = name + "-" + newLevel;
                Filter eventFilter =
                        new FilterPredicate(NAME, FilterOperator.EQUAL, newEvent);
                Query q2 = new Query("EventBean").setFilter(eventFilter);
                List<Entity> nextEvent =
                        datastoreService.prepare(q2).asList(FetchOptions.Builder.withDefaults());
                for (Entity newEventEntity : nextEvent) {
                    EventBean nextLevelEvent = getEventBean(newEventEntity);
                    if (studentName != null && studentName.length() > 0) {
                        if (nextLevelEvent.getAllowedStudents() != null && nextLevelEvent.getAllowedStudents().length() > 0)
                            nextLevelEvent.setAllowedStudents(nextLevelEvent.getAllowedStudents() + ", " + studentName);
                        else
                            nextLevelEvent.setAllowedStudents(studentName);

                        // Update the EventBean into Datastore
                        datastoreService.delete(newEventEntity.getKey());
                        Entity newNextEventEntity = getEventEntity(nextLevelEvent);
                        datastoreService.put(newNextEventEntity);
                    }
                }
            }

        }

        // For approve and advance paths update student score, past competitions in student bean
        //updating smartScore and pastCompetitions
        if (updateStudent > 1) {

            //Getting relevant Student bean and updating its data
            System.out.println("Updating Student info for " + studentName);
            Filter studentNameFilter =
                    new FilterPredicate(STUDENT_NAME, FilterOperator.EQUAL, studentName.trim());
            Query studentQuery = new Query("StudentBean").setFilter(studentNameFilter);
            List<Entity> studentResults =
                    datastoreService.prepare(studentQuery).asList(FetchOptions.Builder.withDefaults());

            if (studentResults == null || studentResults.size() == 0) {
                System.out.println("Student name not found in datastore");
            } else {
                System.out.println("Found " + studentResults.size() + " number of Student Beans");
                for (Entity entity : studentResults) {
                    StudentBean bean = getStudentBean(entity);
//                    System.out.println("Comparing '" + bean.getName() +"' with '" + studentName + "'");
//                    if (bean.getName().equalsIgnoreCase(studentName)) {
                        System.out.println("Found matching student");
                        long previousScore = bean.getSmartScore();
                        //find level of competition
                        int index = eventName.indexOf("-");
                        String levelStr = eventName.substring(index + 1);
                        System.out.println("Level =" + levelStr);
                        //convert level String to Integer
                        int level = Integer.parseInt(levelStr);

                        long newScore = previousScore + (10 * level);
                        // Events that are not competitions earn the students 5 points
                        if (level == 0)
                            newScore = previousScore + 5;
                        bean.setSmartScore(newScore);
                        if (eventName != null && eventName.length() > 0) {
                            if (bean.getPastCompetitions() != null && bean.getPastCompetitions().length() > 0)
                                bean.setPastCompetitions(bean.getPastCompetitions() + "," + eventName);
                            else
                                bean.setPastCompetitions(eventName);
                        }

                        datastoreService.delete(entity.getKey());
                        Entity newStudentEntity = getStudentEntity(bean);
                        System.out.println("New score = " + newScore);
                        datastoreService.put(newStudentEntity);
//                    }
                }
            }
        }
        System.out.println("---------       removeStudentFromEvent COMPLETE   ------------------\n");

    }

    @ApiMethod(name = "getStudents")
    public List<StudentBean> getStudents() {
        System.out.println("ENDPOINT -> getStudents method called");
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Key studentBeanParentKey = KeyFactory.createKey("StudentBeanParent", "root");
        Query query = new Query(studentBeanParentKey);
        List<Entity> results = datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults());
        ArrayList<StudentBean> students = new ArrayList<StudentBean>();
        for (Entity entity : results) {
            StudentBean bean = getStudentBean(entity);
            students.add(bean);
        }

        return students;
    }

    @ApiMethod(name = "clearStudents")
    public void clearStudents() {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastoreService.beginTransaction();
        try {
            Key studentBeanParentKey = KeyFactory.createKey("StudentBeanParent", "root");
            Query getAllEventsQuery = new Query(studentBeanParentKey);
            List<Entity> results = datastoreService.prepare(getAllEventsQuery).asList(FetchOptions.Builder.withDefaults());
            for (Entity entity : results) {
                datastoreService.delete(entity.getKey());
            }
            txn.commit();
        } finally {
            if ( txn.isActive()){
                txn.rollback();
            }
        }
    }

    @ApiMethod(name = "storeGroup")
    public void storeGroup(GroupsBean Gbean) {
            System.out.println("ENDPOINT -> storeGroup method called with GroupName " + Gbean.getName() + "GroupMembers " + Gbean.getGroupMembers());
            DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
            Transaction txn = datastoreService.beginTransaction();
        try {
            Entity groupsBeanEntity = getGroupsEntity(Gbean);
            datastoreService.put(groupsBeanEntity);
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }

            System.out.println("---------       storeGroup COMPLETE   ------------------");


        }

    @ApiMethod(name = "getGroups")
    public List<GroupsBean> getGroups() {
        System.out.println("ENDPOINT -> getGroups method called");
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Key groupsBeanParentKey = KeyFactory.createKey("GroupsBeanParent", "root");
        Query query = new Query(groupsBeanParentKey);
        List<Entity> results = datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults());
        ArrayList<GroupsBean> groups = new ArrayList<GroupsBean>();
        for (Entity gEntity : results) {
            GroupsBean gBean = getGroupsBean(gEntity);
            groups.add(gBean);
        }

        return groups;
    }

    @ApiMethod(name = "clearGroups")
    public void clearGroups() {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastoreService.beginTransaction();
        try {
            Key groupsBeanParentKey = KeyFactory.createKey("GroupsBeanParent", "root");
            Query getAllEventsQuery = new Query(groupsBeanParentKey);
            List<Entity> results = datastoreService.prepare(getAllEventsQuery).asList(FetchOptions.Builder.withDefaults());
            for (Entity entity : results) {
                datastoreService.delete(entity.getKey());
            }
            txn.commit();
        } finally {
            if ( txn.isActive()){
                txn.rollback();
            }
        }
    }

    @ApiMethod(name = "clearEvents")
    public void clearEvents() {

    }

    @ApiMethod(name = "deleteGroup")
    public void deleteGroup(@Named("groupName") String groupName) {
        System.out.println("ENDPOINT -> deleteGroup method called with GroupName " + groupName);
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastoreService.beginTransaction(withXG(true));

        Filter propertyFilter =
                new FilterPredicate(NAME, FilterOperator.EQUAL, groupName);
        Query q = new Query("GroupsBean").setFilter(propertyFilter);
        List<Entity> results =
                datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());

        for (Entity entity : results) {
            try {
                GroupsBean grp = getGroupsBean(entity);
                // Group is being deleted, remove group name from students in this group
                String groupMembers = grp.getGroupMembers();
                if (groupMembers != null && groupMembers.length() > 0) {
                    StringTokenizer strTok = new StringTokenizer(groupMembers, ",");
                    while (strTok.hasMoreTokens()) {
                        String studentName = strTok.nextToken();
                        Filter propertyFilter2 =
                                new FilterPredicate(STUDENT_NAME, FilterOperator.EQUAL, studentName);
                        Query q2 = new Query("StudentBean").setFilter(propertyFilter2);
                        List<Entity> results2 =
                                datastoreService.prepare(q2).asList(FetchOptions.Builder.withDefaults());

                        //updating group name in Student Bean
                        for (Entity entity2 : results2) {
                            StudentBean bean2 = getStudentBean(entity2);
                            bean2.setGroupName("");
                            datastoreService.put(getStudentEntity(bean2));
                            datastoreService.delete(entity2.getKey());
                        }

                    }
                }

                datastoreService.delete(entity.getKey());

                txn.commit();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        }
        System.out.println("---------       deleteGroup COMPLETE   ------------------");
    }



    @ApiMethod(name = "updateGroup")
    public void updateGroup(GroupsBean oldGroup,
                            @Named("newGroupName") String newGroupName) {
        System.out.println("ENDPOINT -> updateGroup method called with new group name " + newGroupName);
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastoreService.beginTransaction(withXG(true));
        Filter propertyFilter =
                new FilterPredicate(NAME, FilterOperator.EQUAL, oldGroup.getName());
        Query q = new Query("GroupsBean").setFilter(propertyFilter);
        List<Entity> results =
                datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());

        for (Entity entity : results) {
            try {

                GroupsBean newGroup = getGroupsBean(entity);
                newGroup.setName(newGroupName);
                // Update the group name for each of the group members
                String groupMembers = newGroup.getGroupMembers();
                if (groupMembers != null && groupMembers.length() > 0) {
                    StringTokenizer strTok = new StringTokenizer(groupMembers,",");
                    while (strTok.hasMoreTokens()) {
                        String studentName = strTok.nextToken();
                        Filter studentNameFilter =
                                new FilterPredicate(STUDENT_NAME, FilterOperator.EQUAL, studentName.trim());
                        Query studentQuery = new Query("StudentBean").setFilter(studentNameFilter);
                        List<Entity> studentResults =
                                datastoreService.prepare(studentQuery).asList(FetchOptions.Builder.withDefaults());

                        for (Entity entity2 : studentResults) {
                            StudentBean bean = getStudentBean(entity2);
                            bean.setGroupName(newGroup.getName());
                            datastoreService.delete(entity2.getKey());
                            Entity newStudentEntity = getStudentEntity(bean);
                            datastoreService.put(newStudentEntity);
                        }
                    }
                }

                // Update group
                datastoreService.delete(entity.getKey());
                datastoreService.put(getGroupsEntity(newGroup));

                txn.commit();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        }
        System.out.println("---------       updateGroup COMPLETE   ------------------");
    }

    @ApiMethod(name = "updateEvent")
    public void updateEvent(EventBean newEvent) {
        System.out.println("ENDPOINT -> updateEvent method called for event " + newEvent.getName());
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastoreService.beginTransaction();
        Filter propertyFilter =
                new FilterPredicate(NAME, FilterOperator.EQUAL, newEvent.getName());
        Query q = new Query("EventBean").setFilter(propertyFilter);
        List<Entity> results =
                datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());

        for (Entity entity : results) {
            try {

                EventBean anEvent = getEventBean(entity);
                anEvent.setDate(newEvent.getDate());
                anEvent.setDescription(newEvent.getDescription());
                Key eventBeanParentKey = KeyFactory.createKey("EventBeanParent", "root");
                datastoreService.delete(entity.getKey());
                datastoreService.put(getEventEntity(anEvent));

                txn.commit();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        }
        System.out.println("---------       updateEvent COMPLETE   ------------------");
    }


    @ApiMethod(name = "deleteEvent")
    public void deleteEvent(@Named("eventName") String eventName) {
        System.out.println("ENDPOINT -> deleteEvent method called with Event Name " + eventName);
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastoreService.beginTransaction();
        Filter propertyFilter =
                new FilterPredicate(NAME, FilterOperator.EQUAL, eventName);
        Query q = new Query("EventBean").setFilter(propertyFilter);
        List<Entity> results =
                datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());

        for (Entity entity : results) {
            try {

                datastoreService.delete(entity.getKey());

                txn.commit();
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                }
            }
        }
        System.out.println("---------       deleteEvent COMPLETE   ------------------");
    }

    @ApiMethod(name = "addStudentToGroup")
    public void addStudentToGroup(@Named("studentName") String studentName, GroupsBean group) {
        System.out.println("ENDPOINT -> addStudentToGroup called with Student Name " + studentName
                        + ", Group Name " + group.getName());

        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastoreService.beginTransaction(withXG(true));
        try {
        // Update Group - add student name to group members
        Filter propertyFilter =
                new FilterPredicate(NAME, FilterOperator.EQUAL, group.getName());
        Query q = new Query("GroupsBean").setFilter(propertyFilter);
        List<Entity> results =
                datastoreService.prepare(q).asList(FetchOptions.Builder.withDefaults());
            for (Entity entity : results) {
                GroupsBean newGroup = getGroupsBean(entity);
                if (studentName != null && studentName.length() > 0) {
                    if (newGroup.getGroupMembers() != null && newGroup.getGroupMembers().length() > 0)
                        newGroup.setGroupMembers(newGroup.getGroupMembers() + ", " + studentName);
                    else
                        newGroup.setGroupMembers(studentName);
                }
                Key groupsBeanParentKey = KeyFactory.createKey("GroupsBeanParent", "root");
                datastoreService.delete(entity.getKey());
                datastoreService.put(getGroupsEntity(newGroup));
            }
            // Update Student - add group name to student bean
            Filter studentNameFilter =
                    new FilterPredicate(STUDENT_NAME, FilterOperator.EQUAL, studentName.trim());
            Query studentQuery = new Query("StudentBean").setFilter(studentNameFilter);
            List<Entity> studentResults =
                    datastoreService.prepare(studentQuery).asList(FetchOptions.Builder.withDefaults());

            for (Entity entity : studentResults) {
                StudentBean bean = getStudentBean(entity);
                bean.setGroupName(group.getName());
                datastoreService.delete(entity.getKey());
                Entity newStudentEntity = getStudentEntity(bean);
                datastoreService.put(newStudentEntity);
            }
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
        System.out.println("ENDPOINT -> addStudentToGroup complete");
    }
}



