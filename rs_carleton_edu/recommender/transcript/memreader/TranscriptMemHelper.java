package transcript.memreader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import transcript.data.Entry;
import transcript.utilities.Pair;
import cern.colt.list.IntArrayList;
import cern.colt.list.ObjectArrayList;
import cern.colt.map.OpenIntObjectHashMap;

/**
 * Handles all the data retrieval/manipulation for a TranscriptMemReader.
 * 
 * Essentially, this is the layer between the data and the algorithms.
 */
public class TranscriptMemHelper {

    // The "database" for this TranscriptMemHelper
    private TranscriptMemReader tmr;

    /**
     * Creates a new TranscriptMemHelper, using a serialized
     * TranscriptMemReader file.
     * 
     * @param fileName a serialized TranscriptMemReader file.
     */
    public TranscriptMemHelper(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fis);
            tmr = (TranscriptMemReader) in.readObject();
        }
        catch (ClassNotFoundException e) {
            System.out.println("Can't find class");
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }
    }
    
    /**
     * @return the underlying database.
     */
    public TranscriptMemReader getTranscriptMemReader() {
        return tmr;
    }

    /**
     * @return studToCourse directly from the TranscriptMemReader
     */
    public OpenIntObjectHashMap getStudToCourse() {
        return tmr.studToCourse;
    }

    /**
     * @return courseToStud directly from the TranscriptMemReader
     */
    public HashMap<String, ObjectArrayList> getCourseToStud() {
        return tmr.courseToStud;
    }

    /**
     * Gets a grade for a student/course.
     * 
     * Returned as an int for convenience.
     * 
     * @param sid the student id
     * @param course the course (full course name, please)
     * @return the grade as an int
     */
    public int getGrade(int sid, String course) {
        ObjectArrayList list = (ObjectArrayList) tmr.studToCourse.get(sid);

        //The student was not found
        if (list == null)
            return -1;
        
        int index = binarySearch(list, course);

        if (index >= 0)
            return ((Entry) list.getQuick(index)).getGradeAsInt();
        else
            return -1;
    }

    /**
     * @param sid the student id
     * @return the average grade that student recieved
     */
    public double getAvgForStud(int sid) {
        if (tmr.sumByStud.containsKey(sid))
            return (double) tmr.sumByStud.get(sid)
                    / (double) ((ObjectArrayList) tmr.studToCourse.get(sid))
                            .size();
        else
            return -1;
    }

    /**
     * @param course the course (full course name, please)
     * @return the average grade for people taking the course
     */
    public double getAvgForCourse(String course) {
        if (tmr.sumByCourse.containsKey(course))
            return (double) tmr.sumByCourse.get(course)
                    / (double) tmr.courseToStud.get(course).size();
        else
            return -1;
    }

    /**
     * Calculates the standard deviation for a student
     * 
     * @param sid the student id
     * @return the student's standard deviation
     */
    public double getStandardDeviationForStud(int sid) {
        double avg = getAvgForStud(sid), sd = 0;
        ObjectArrayList courses = getCoursesTakenByStud(sid);        
     
        for(int i = 0; i < courses.size(); i++)
            sd += Math.pow((double)((Entry)courses.getQuick(i)).getGradeAsInt() - avg, 2);
        
        if(courses.size() == 1) 
            return Math.sqrt(sd);
        else
            return Math.sqrt(sd / (courses.size() - 1.0));
    }
    
    /**
     * Calculates the standard deviation for a course.
     * 
     * @param course the course (full course name, please)
     * @return its standard deviation
     */
    public double getStandardDeviationForCourse(String course) {
        double avg = getAvgForCourse(course), sd = 0;
        ObjectArrayList studs = getStudsTakingCourse(course);
        
        for(int i = 0; i < studs.size(); i++)
            sd += Math.pow((double)((Entry)studs.getQuick(i)).getGradeAsInt() - avg, 2);
            
        if(studs.size() == 1)
            return Math.sqrt(sd);
        else
            return Math.sqrt(sd / (studs.size() - 1.0));
    }
    
    /**
     * @param sid the student id
     * @return the sum of all grades of a student
     */
    public double getGradeSumForStud(int sid) {
        //Check that the sid is in the database
        if (!tmr.sumByStud.containsKey(sid))
            return -1;

        return tmr.sumByStud.get(sid);
    }

    /**
     * @param sid the student id
     * @return the number of courses that student has taken
     */
    public int getNumberOfCoursesTaken(int sid) {
        //Check that the sid is in the database
        if (!tmr.studToCourse.containsKey(sid))
            return -1;

        return ((ObjectArrayList) tmr.studToCourse.get(sid)).size();
    }

    /**
     * @param course the course (full course name, please)
     * @return the number of students who have taken this course
     */
    public int getNumberOfStudsTakingCourse(String course) {
        //Check that the course is in the database
        if (!tmr.courseToStud.containsKey(course))
            return -1;

        return tmr.courseToStud.get(course).size();
    }

    /**
     * @return the number of students in the database
     */
    public int getNumberOfStuds() {
        return tmr.studToCourse.size();
    }

    /**
     * @return the number of courses in the database
     */
    public int getNumberOfCourses() {
        return tmr.courseToStud.size();
    }

    /**
     * @param sid the student id
     * @return the list of courses that student has taken (sorted)
     */
    public ObjectArrayList getCoursesTakenByStud(int sid) {
        return (ObjectArrayList) tmr.studToCourse.get(sid);
    }

    /**
     * @param course the course (full course name, please)
     * @return the list of students who have taken this course (sorted)
     */
    public ObjectArrayList getStudsTakingCourse(String course) {
        return (ObjectArrayList) tmr.courseToStud.get(course);
    }

    /**
     * @return a set of all student ids
     */
    public IntArrayList getListOfStuds() {
        return tmr.studToCourse.keys();
    }

    /**
     * @return a set of all courses (Full course)
     */
    public Set<String> getListOfCourses() {
        return tmr.courseToStud.keySet();
    }

    /**
     * Performs an inner join between two different SIDs.  The results
     * of the join are the courses common between the two people.
     * 
     * @param studOne the first sid
     * @param studTwo the second sid
     * @return the courses they have in common
     */
    public ArrayList<Pair> innerJoin(int studOne, int studTwo) {
        Entry a = new Entry(studOne, 0, "-1", "-1", 0, "-1", "-1", "-1");
        Entry b = new Entry(studTwo, 0, "-1", "-1", 0, "-1", "-1", "-1");

        return join(a, b, true, false);
    }

    /**
     * Performs an inner join between two different courses.  The results
     * of the join are the students who took both courses
     * 
     * If you don't use a full course name (i.e. "CS.117") then it will crash.
     * 
     * @param courseOne the first course
     * @param courseTwo the second course
     * @return the students who took both courses
     */
    public ArrayList<Pair> innerJoin(String courseOne, String courseTwo) {
        String[] split1 = courseOne.split("\\."), split2 = courseTwo
                .split("\\.");
        Entry a = new Entry(-1, 0, "-1", split1[0],
                Integer.parseInt(split1[1]), "-1", "-1", "-1");
        Entry b = new Entry(-1, 0, "-1", split2[0],
                Integer.parseInt(split2[1]), "-1", "-1", "-1");

        return join(a, b, false, false);
    }

    /**
     * Performs a full outer join between two different SIDs.  The results
     * of the join are all the courses taken by the two students.  If
     * they do not match up on a course, then the one who did not take it
     * receives a null entry.
     * 
     * @param studOne the first sid
     * @param studTwo the second sid
     * @return all courses taken by the two, joined
     */
    public ArrayList<Pair> fullOuterJoin(int studOne, int studTwo) {
        Entry a = new Entry(studOne, 0, "-1", "-1", 0, "-1", "-1", "-1");
        Entry b = new Entry(studTwo, 0, "-1", "-1", 0, "-1", "-1", "-1");

        return join(a, b, true, true);
    }

    /**
     * Performs a full outer join between two different courses.  The results
     * of the join are all the students who took the two courses.  If
     * they do not match up on a student, then the one who did not have 
     * the student receives a null entry.
     * 
     * If you don't use a full course name (i.e. "CS.117") then it will crash.
     * 
     * @param courseOne the first course
     * @param courseTwo the second course
     * @return all students who took both courses, joined
     */
    public ArrayList<Pair> fullOuterJoin(String courseOne, String courseTwo) {
        String[] split1 = courseOne.split("\\."), split2 = courseTwo
                .split("\\.");
        Entry a = new Entry(-1, 0, "-1", split1[0],
                Integer.parseInt(split1[1]), "-1", "-1", "-1");
        Entry b = new Entry(-1, 0, "-1", split2[0],
                Integer.parseInt(split2[1]), "-1", "-1", "-1");

        return join(a, b, false, true);
    }

    /**
     * The nuts and bolts of the inner/full outer join.
     * 
     * If you want to use an inner/full outer join I suggest you use
     * the PUBLIC methods, which are easier to understand.
     * 
     * Note to self on values of which:
     * true == SIDs are different, courses common
     * false == courses different, SIDs common.
     * 
     * @param a the first entry
     * @param b the second entry
     * @param which determines whether to join on sid or course
     * @param useFullOuterJoin if true, uses full outer join.
     *                         otherwise, uses inner join.
     * @return the join results (can be empty)
     */
    private ArrayList<Pair> join(Entry a, Entry b, boolean which,
            boolean useFullOuterJoin) {
        ObjectArrayList left, right;
        left = right = null;

        if (which) {
            left = (ObjectArrayList) getCoursesTakenByStud(a.getSid());
            right = (ObjectArrayList) getCoursesTakenByStud(b.getSid());
        }
        else {
            left = (ObjectArrayList) getStudsTakingCourse(a.getFullCourse());
            right = (ObjectArrayList) getStudsTakingCourse(b.getFullCourse());
        }

        //Check that there is any data to do a join on.
        if (left == null || right == null) {
            return new ArrayList<Pair>();
        }

        // Join the two using a sort-merge join
        // Assumes that they two lists are already sorted
        ArrayList<Pair> match = new ArrayList<Pair>();
        int leftIndex = 0, rightIndex = 0;
        String l, r;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (which) {
                l = ((Entry) left.get(leftIndex)).getFullCourse();
                r = ((Entry) right.get(rightIndex)).getFullCourse();
            }
            else {
                l = Integer.toString(((Entry) left.get(leftIndex)).getSid());
                r = Integer.toString(((Entry) right.get(rightIndex)).getSid());
            }

            if (l.equals(r)) {
                match.add(new Pair((Entry) left.getQuick(leftIndex++),
                        (Entry) right.getQuick(rightIndex++)));
            }
            else if (l.compareTo(r) < 0) {
                if (useFullOuterJoin)
                    match
                            .add(new Pair((Entry) left.getQuick(leftIndex++),
                                    null));
                else
                    leftIndex++;
            }
            else {
                if (useFullOuterJoin)
                    match.add(new Pair(null, (Entry) right
                            .getQuick(rightIndex++)));
                else
                    rightIndex++;
            }
        }

        return match;
    }
    
    
    /**
     * Sick and tired of Cern Colt's failure of a binary search, I took up
     * the axe myself and made one that doesn't get NullPointerExceptions
     * all the damn time.
     */
    private int binarySearch(ObjectArrayList list, String course) {
        int low = -1, high = list.size(), mid;
        
        while(high - low > 1) {
            mid = (low + high) >>> 1;
            if(((Entry)list.getQuick(mid)).getFullCourse().compareTo(course) > 0)
                high = mid;
            else
                low = mid;
        }
        
        if (low == -1 || ((Entry)list.getQuick(low)).getFullCourse().compareTo(course) != 0)
            return -1;
        else
            return low;
    }
}
