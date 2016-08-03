package transcript.recommender;

import java.util.ArrayList;
import java.util.Collections;

import netflix.rmse.RMSECalculator;
import netflix.ui.Item;
import netflix.ui.ItemComparator;
import transcript.data.Entry;
import transcript.memreader.TranscriptMemHelper;
import cern.colt.list.IntArrayList;
import cern.colt.list.ObjectArrayList;

/**
 * An abstract class that is the basis for each recommender's actual run.
 * 
 * It includes one abstract function to implement - recommend(int, String).
 * 
 * Also, it includes some methods for adding rows to the database.
 * Note that in order for this to work, you must set TranscriptMemHelper tmh in
 * the extended classes' constructor.  Also, make sure to call resort()
 * after adding entries, so that the underlying database is sorted.
 * 
 * There are also a few helpful methods that apply to all recommenders.
 * 
 * @author lewda
 */
public abstract class AbstractRecommender {
    
    //The underlying database
    protected TranscriptMemHelper tmh;
    
    /**
     * The backbone of the recommender.  It recommends.
     * 
     * Needs to be implemented in whatever way necessary for
     * an algorithm.
     * 
     * @param sid an sid
     * @param course a course (full course name)
     * @return its recommended rating
     */
    public abstract double recommend(int sid, String course);
    
    /**
     * Adds an entry to the database.
     * 
     * Be sure to call resort() after adding entries.
     * 
     * @param e the Entry to add
     * @return true if successful, false if parameters were bad
     */
    public boolean add(Entry e) {
        if (!(e.getDept().equals("NONE") && e.getCnum() == 0) 
                && e.getSid() > 0 && e.getGradeAsInt() > 0) {
            tmh.getTranscriptMemReader().addEntry(e);
            return true;
        }
        
        return false;
    }
    
    /**
     * Resorts the values in the underlying database.
     * It is important to call this after adding entries.
     */
    public void resort() {
        tmh.getTranscriptMemReader().sortHashes();
    }
    
    /**
     * Given a student id, it finds what courses the student 
     * has *not* taken from among all the courses.
     * 
     * @param sid the student id
     * @return the courses the student has not taken
     */
    public ArrayList<Item> getUnratedCourses(int sid) {
        ArrayList<Item> coursesToRank = new ArrayList<Item>();
        
        for (String courseName :  tmh.getListOfCourses()) {
            if(tmh.getGrade(sid, courseName) < 0) {
                coursesToRank.add (new Item(courseName, "", 0));
            }
        }
        
        return coursesToRank;
    }
    
    /**
     * Given a student id and a list of courses, 
     * it finds what courses the student has *not*
     * taken from among the list of courses.  It is
     * non-volatile to parameters.
     * 
     * @param sid the student id
     * @param courses the list of courses to inspect
     * @return the courses the student has not taken from the list
     */
    public ArrayList<Item> getUnratedCourses(int sid, ArrayList<Item> courses) {
        ArrayList<Item> coursesToRank = new ArrayList<Item>();
        
        for(Item i : courses) {
            if(tmh.getGrade(sid, i.getId()) < 0) {
                coursesToRank.add(i);
            }
        }
        
        return coursesToRank;
    }
    
    /**
     * Removes levels of courses - i.e., 100, 200, 300, etc.
     * 
     * The level variable should represent the level - so 1 would
     * mean level 100, 2 means level 200, etc.
     * 
     * @param level the level to remove
     * @param courses the courses to remove it from
     * @return the courses minus that level
     */
    public ArrayList<Item> removeCourseLevel(int level, ArrayList<Item> courses) {
        ArrayList<Item> coursesToRank = new ArrayList<Item>();
        int cnum;
        
        for(Item i : courses) {
            cnum = Integer.parseInt((i.getId().split("\\."))[1]);
            
            if(cnum / 100 != level)
                coursesToRank.add(i);
        }
        
        return coursesToRank;
    }
    
    /**
     * Taking a course list, it ranks them based upon recommendation
     * values.  Note that it modifies the Item objects in the
     * ArrayList, so nothing is returned.
     * 
     * @param sid who to rate the courses for
     * @param courses the courses to rank
     */
    public void rankCourses(int sid, ArrayList<Item> courses) {
        for(Item c : courses)
            c.setRating(recommend(sid, c.getId()));
        
        Collections.sort(courses, new ItemComparator());
    }

    /**
     * Using RMSE as measurement, this will compare a test set
     * (in TranscriptMemHelper form) to the results gotten from the recommender
     *  
     * @param testtmh the transcriptmemhelper with test data in it
     * @return the rmse in comparison to testtmh 
     */
    public double testWithMemHelper(TranscriptMemHelper testtmh) {
        RMSECalculator rmse = new RMSECalculator();
        IntArrayList studs;
        ObjectArrayList courses;
        int sid;
        String course;
        double rec;

        // For each user, make recommendations
        studs = testtmh.getListOfStuds();
        for (int i = 0; i < studs.size(); i++) {
            sid = studs.getQuick(i);
            courses = testtmh.getCoursesTakenByStud(sid);

            for (int j = 0; j < courses.size(); j++) {
                course = ((Entry)courses.getQuick(j)).getFullCourse();
                rec = recommend(sid, course);
                
                if(rec < 0)
                    rec = tmh.getAvgForStud(sid);
                
                rmse.add(testtmh.getGrade(sid, course), rec);
            }
        }

        return rmse.rmse();
    }
    
    /**
     * Stub for using testWithMemHelper without having
     * to initialize your own TranscriptMemHelper
     * @param testFile the file for the TMH
     * @return the rmse of the test
     */
    public double testWithMemHelper(String testFile) {
        TranscriptMemHelper testtmh = new TranscriptMemHelper(testFile);
        return testWithMemHelper(testtmh);
    }
}
