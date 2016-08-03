package transcript.recommender;

import java.util.ArrayList;
import java.util.Collections;

import netflix.ui.Item;
import netflix.ui.ItemComparator;
import netflix.utilities.RankingUtilities;
import transcript.algorithms.association.Association;
import transcript.memreader.TranscriptMemHelper;

/**
 * Uses association rules to recommend courses to take.
 * 
 * WARNING: Association rules cannot be used to predict course
 * grades!  The values that are used to determine which
 * course to recommend have nothing to do with the grade you
 * would recieve in a course.  Use a different recommender if
 * you want to know the actual grade received.   
 * 
 * @author lewda
 */
public class AssociationRecommender extends AbstractRecommender {
    private Association ass;
    
    //DELETE ME AFTER DONE TESTING
    public static void main(String[] args) {
        TranscriptMemHelper tmh = new TranscriptMemHelper("transcript.dat");
        AssociationRecommender ar = new AssociationRecommender(tmh);
        ArrayList<Item> courses = ar.getUnratedCourses(5165);
        courses = ar.removeCourseLevel(1, courses);
        courses = ar.removeCourseLevel(3, courses);
        courses = ar.removeCourseLevel(4, courses);
        ar.rankCourses(5165, courses);
        RankingUtilities.printTopNItems(courses, 30);
    }
    //DELETE ME AFTER DONE TESTING
    
    public AssociationRecommender(String file) {
        tmh = new TranscriptMemHelper(file);
        ass = new Association(tmh);
        ass.buildRules();
    }
    
    public AssociationRecommender(TranscriptMemHelper tmh) {
        this.tmh = tmh;
        ass = new Association(tmh);
        ass.buildRules();
    }
    
    /**
     * Custom version of rankCourses that will run
     * much faster than the normal rankCourses, for
     * association rule checking.
     */
    public void rankCourses(int sid, ArrayList<Item> courses) {
        double[] recVec = ass.getRecVec(sid);
        
        for(Item i : courses) {
            if(ass.hash(i.getId()) >= 0)
                i.setRating(recVec[ass.hash(i.getId())]);
        }
        
        Collections.sort(courses, new ItemComparator());
    }
    
    public double recommend(int sid, String course) {
        return ass.rank(sid, course);
    }

    public void resort() {
        super.resort();
        ass.buildRules();
    }
}
