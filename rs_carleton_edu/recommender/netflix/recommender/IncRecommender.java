package netflix.recommender;

import java.io.*;
import java.util.*;
import netflix.memreader.*;
import netflix.utilities.*;
import netflix.algorithms.modelbased.svd.incremental.*;

/**
 * Uses a serialized IncrementalSVDMovielens object to make
 * predictions using the AbstractRecommender framework. This
 * recommender is quite fast and accurate, but it does not support
 * adding users or movies.
 *
 * @author sowellb
 */
public class IncRecommender extends AbstractRecommender {

    //Default serialized IncrementalSVDMovielens file.
    public String FEATURE_FILE = "uabaseinc.dat";
    IncrementalSVDMovielens inc;

    /**
     * Constructor. The MemHelper is not used, but is necessary
     * for compatibility with AbstractRecommender.
     *
     * @param  mhFile  The file containing MemHelper object.
     */
    public IncRecommender(String mhFile) {
        this(new MemHelper(mhFile));
    }


    /**
     * Constructor. The MemHelper is not used, but is necessary
     * for compatibility with AbstractRecommender.
     *
     * @param  mh The MemHelper object.
     */
    public IncRecommender(MemHelper mh) {
        this.mh = mh;
        inc = IncrementalSVDMovielens.deserialize(FEATURE_FILE);
    }

    /**
     * Constructor. The MemHelper is not used but is necessary
     * for compatiblity with AbstractRecommender.
     *
     * @param  mh  The MemHelper object.
     * @param  featureFile  Serialzed IncrementalSVDMovielens object. 
     */
    public IncRecommender(MemHelper mh, String featureFile) {
        this.mh = mh;
        inc = IncrementalSVDMovielens.deserialize(featureFile);
    }

    /**
     * Predicts the rating for movie targetMovie by user
     * activeUser.
     *
     * @param  activeUser  User to predict rating for. 
     * @param  targetMovie  Movie to predict rating for. 
     * @param  date  The date the movie was rated (not used)
     * @return The predicted rating. 
     */
    public double recommend(int activeUser, int targetMovie, String date) {
        return inc.predictRating((short) targetMovie, activeUser);
    }


/*   public static void main(String[] args) {

        String base = "/Users/bsowell/recommender/movielens/0indexed/uabase.dat";
        String test = "/Users/bsowell/recommender/movielens/0indexed/uatest.dat";
        System.out.println("Training set: " + base + ", test set: " + test);
        IncRecommender svdRec = new IncRecommender(base);
        MemHelper mh = new MemHelper(test);
        System.out.println("RMSE: " + svdRec.testWithMemHelper(mh));
        }*/


}