package transcript.algorithms.association;

import java.util.Arrays;
import java.util.HashMap;

import transcript.data.Entry;
import transcript.memreader.TranscriptMemHelper;
import cern.colt.list.IntArrayList;
import cern.colt.list.ObjectArrayList;

public class Association {
    private double[][] courseRules;
    private HashMap<String, Integer> courseToNum;
    private HashMap<Integer, String> numToCourse;
    private TranscriptMemHelper tmh;

    /**
     * Creates an Association predictor with just
     * one level of association rules.
     * @param tmh the TranscriptMemHelper database
     */
    public Association(TranscriptMemHelper tmh) {
        this.tmh = tmh;
        courseToNum = new HashMap<String, Integer>();
        numToCourse = new HashMap<Integer, String>();
    }

    /**
     * Builds the association rules for the underlying database.
     * 
     * Should be run before making any predictions, and should
     * be run after adding anything to the underlying database.
     */
    public void buildRules() {
        //Figure out hashes, initialize arrays
        int n = 0;
        courseToNum.clear();
        numToCourse.clear();
        for (String s : tmh.getListOfCourses()) {
            numToCourse.put(n, s);
            courseToNum.put(s, n++);
        }

        courseRules = new double[n][n];
        for (double[] d : courseRules)
            Arrays.fill(d, 0.0);

        //Calculate the one/two time counts of everything
        HashMap<String, Double> courseSupps = new HashMap<String, Double>();
        IntArrayList studs = tmh.getListOfStuds();
        ObjectArrayList entries;
        double numStuds = tmh.getNumberOfStuds();
        int row, col;
        String course;

        for (int i = 0; i < numStuds; i++) {
            entries = tmh.getCoursesTakenByStud(studs.getQuick(i));

            for (int j = 0; j < entries.size(); j++) {
                course = ((Entry) entries.getQuick(j)).getFullCourse();

                //Add single itemset support
                if (courseSupps.containsKey(course))
                    courseSupps.put(course, courseSupps.get(course) + 1.0);
                else
                    courseSupps.put(course, 1.0);

                //Add dual itemset support
                for (int k = j + 1; k < entries.size(); k++) {
                    row = courseToNum.get(course);
                    col = courseToNum.get(((Entry) entries.getQuick(k))
                            .getFullCourse());
                    courseRules[row][col]++;
                    courseRules[col][row]++;
                }
            }
        }

        //Calculate support from the counts
        for (String s : courseSupps.keySet())
            courseSupps.put(s, courseSupps.get(s) / numStuds);

        //Figure out the rules now (calculates confidence for all rules)
        for (String i : courseSupps.keySet()) {
            row = courseToNum.get(i);
            for (String j : courseSupps.keySet())
                courseRules[row][courseToNum.get(j)] /= (courseSupps.get(i) * numStuds);
        }
    }

    /**
     * Ranks a particular student and course.
     * 
     * Returns -1 if there's no answer to give, and
     * -2 if the student has already taken the course.
     * 
     * @param sid the student id
     * @param course the course (full course name, please)
     * @return its rank, or -1 if there are no rules related, or -2
     *          if the student has already taken the course.
     */
    public double rank(int sid, String course) {
        //Check that we have course as an association rule
        if (!courseToNum.containsKey(course))
            return -1;

        //Check that the class hasn't been taken already
        if (tmh.getGrade(sid, course) > 0)
            return -2;

        ObjectArrayList courses = tmh.getCoursesTakenByStud(sid);
        double rank = 0;
        int n = 0;  
        int chash = courseToNum.get(course);

        for (int i = 0; i < courses.size(); i++) {
            if(courseRules[courseToNum.get(((Entry) courses.getQuick(i)).getFullCourse())][chash] != 0) {
                n++;
                rank += courseRules[courseToNum.get(((Entry) courses.getQuick(i)).getFullCourse())][chash] 
                                 * (double) ((Entry)courses.getQuick(i)).getGradeAsInt();
            }
        }

        if(n != 0)
            return rank / (double) n;
        else
            return -1;
    }

    /**
     * Calculates the recommendation vector for a student.
     * It is the same as calling rank(sid, course) for every
     * course.  However, this version is about 8x faster,
     * so for recommendation it should be used.
     * 
     * @param sid the student id
     * @return the user's recommendation vector
     */
    public double[] getRecVec(int sid) {
        ObjectArrayList courses = tmh.getCoursesTakenByStud(sid);
        double[] recVec = new double[courseRules.length];
        double[] avgVec = new double[courseRules.length];
        double grade;
        Arrays.fill(recVec, 0.0);
        Arrays.fill(avgVec, 0.0);
        int hash;
        
        for (int i = 0; i < courses.size(); i++) {
            hash = courseToNum.get(((Entry) courses.getQuick(i)).getFullCourse());
            grade = ((Entry) courses.getQuick(i)).getGradeAsInt();

            for (int j = 0; j < courseRules.length; j++) {
                if(courseRules[hash][j] != 0) {
                    recVec[j] += courseRules[hash][j] * grade;
                    avgVec[j]++;
                }
            }
        }
        
        for(int i = 0; i < recVec.length; i++)
            if(avgVec[i] != 0)
                recVec[i] /= avgVec[i];
        
        return recVec;
    }
    
    /**
     * Returns the index for a course.  Used to interpret
     * the recVec.
     * 
     * @param course the course (full course name)
     * @return its index in recVec, or -1 if it is not in recVec
     */
    public int hash(String course) {
        if(courseToNum.containsKey(course))
            return courseToNum.get(course);
        else
            return -1;
    }
}