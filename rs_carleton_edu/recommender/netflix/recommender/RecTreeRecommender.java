package netflix.recommender;
import java.util.*;
import netflix.algorithms.memorybased.rectree.*;
import netflix.memreader.*;
import netflix.utilities.*;
import cern.colt.list.*;
import cern.colt.map.*;

public class RecTreeRecommender extends AbstractRecommender {

    private RecTree2 tree;
    MemHelper helper;
    Timer227 timer;

    public RecTreeRecommender(String memReaderFile) {
        timer = new Timer227();
        helper = new MemHelper(memReaderFile);

        timer.start();
        tree = new RecTree2(helper);
        tree.cluster();
        timer.stop();
        System.out.println("Tree took " + timer.getTime() + " s to build");
    }

    /**
     * Correlation weighting between two users, as provided in "Empirical
     * Analysis of Predictive Algorithms for Collaborative Filtering."
     * @param mh the database to use
     * @param activeUser the active user
     * @param targetUser the target user
     * @return their correlation
     */
    private double correlation(int activeUser, int targetUser) {
        double topSum, bottomSumActive, bottomSumTarget, rating1, rating2;
        topSum = bottomSumActive = bottomSumTarget = 0;
        double activeAvg = helper.getAverageRatingForUser(activeUser);
        double targetAvg = helper.getAverageRatingForUser(targetUser);
        ArrayList<Pair> ratings = helper.innerJoinOnMoviesOrRating(activeUser, targetUser, true);
		
        // Do the summations
        for(Pair pair : ratings) {
            rating1 = (double)MemHelper.parseRating(pair.a) - activeAvg;
            rating2 = (double)MemHelper.parseRating(pair.b) - targetAvg;
			
            topSum += rating1 * rating2;
            bottomSumActive += Math.pow(rating1, 2);
            bottomSumTarget += Math.pow(rating2, 2);
        }
		
        // This handles an emergency case of dividing by zero
        if(bottomSumActive != 0 && bottomSumTarget != 0)
            return topSum / Math.sqrt(bottomSumActive * bottomSumTarget);
        else
            return 0;
    }

    /**
     * Basic recommendation method for memory-based algorithms.
     * 
     * @param user
     * @param movie
     * @return the predicted rating, or -99 if it fails (mh error)
     */
    public double recommend(int activeUser, int targetMovie, String date) {
        double currWeight, weightSum = 0, voteSum = 0;
        int uid, rating;

        IntArrayList users = tree.getClusterByUID(activeUser);

        for(int i = 0; i < users.size(); i++) {

            uid = users.getQuick(i);
            rating = helper.getRating(uid, targetMovie);

            //If the user rated the target movie and the target
            //user is not the same as the active user. 
            if(rating != -99 && uid != activeUser) {
                
                currWeight = correlation(activeUser, uid);
                weightSum += Math.abs(currWeight);
                voteSum += currWeight * (rating - helper.getAverageRatingForUser(uid));			
            }
        }

        // Normalize the sum, such that the unity of the weights is one
        voteSum *= 1.0 / weightSum;



        // Add to the average vote for user (rounded) and return
        double recommendation =  Math.round((helper.getAverageRatingForUser(activeUser) + voteSum)*10) / 10.0;

        System.out.println("uid: " + activeUser + " mid: " + targetMovie + " rating: " + recommendation);

        return recommendation;
    }

    public static void main(String[] args) {
        String base = "uabase.dat", test = "uatest.dat";
		
        System.out.println("Training set: " + base + ", test set: " + test);
        RecTreeRecommender rec = new RecTreeRecommender(base);
        MemHelper mh = new MemHelper(test);
        System.out.println("RMSE: " + rec.testWithMemHelper(mh));
    }


}