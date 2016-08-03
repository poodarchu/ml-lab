package netflix.recommender;

import java.util.ArrayList;
import java.util.Collections;

import netflix.algorithms.association.Association;
import netflix.memreader.MemHelper;
import netflix.ui.Item;
import netflix.ui.ItemComparator;
import netflix.utilities.RankingUtilities;

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
        AssociationRecommender ar = new AssociationRecommender("uabase.dat");
        ArrayList<Item> movies = RankingUtilities.readMovieFile("movies_movielens.txt"); 
        ArrayList<Item> courses = ar.getUnratedMovies(944, movies);
        long start = System.currentTimeMillis();
        ar.rankMovies(944, courses);
        long end = System.currentTimeMillis();
        System.out.println((end - start));
        RankingUtilities.printTopNItems(courses, 5);
    }
    //DELETE ME AFTER DONE TESTING
    
    public AssociationRecommender(String file) {
        mh = new MemHelper(file);
        ass = new Association(mh);
        ass.buildRules();
    }
    
    public AssociationRecommender(MemHelper mh) {
        this.mh = mh;
        ass = new Association(this.mh);
        ass.buildRules();
    }
    
    /**
     * Custom version of rankCourses that will run
     * much faster than the normal rankCourses, for
     * association rule checking.
     */
    public void rankMovies(int uid, ArrayList<Item> courses) {
        double[] recVec = ass.getRecVec(uid);
        
        for(Item i : courses) {
            if(ass.hash(i.getIdAsInt()) != -1)
                i.setRating(recVec[ass.hash(i.getIdAsInt())]);
        }
        
        Collections.sort(courses, new ItemComparator());
    }
    
    public double recommend(int uid, int mid, String date) {
        return ass.rank(uid, mid);
    }

    public void resort() {
        super.resort();
        ass.buildRules();
    }
}
