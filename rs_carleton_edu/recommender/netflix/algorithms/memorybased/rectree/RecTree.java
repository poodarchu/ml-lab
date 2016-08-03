package netflix.algorithms.memorybased.rectree;

import java.io.*;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntObjectHashMap;
import cern.colt.function.IntObjectProcedure;

/**
 * WORK IN PROGRESS. This won't compile yet. 
 *
 *
 *
 */

/* commented out by steinbel on 1.3.07 to allow mass compilation
public class RecTree {

    MemHelper helper;

    public RecTree(String sourceFile) {
        helper = new MemHelper(sourceFile);

    }

    public void constructRecTree(parent, dataSet, partitionMaxSize, 
                                 curDepth, maxDepth) {



    }


    public void kMeans(IntArrayList dataset, int k) {
        

        
        
    }

    private double correlation(int activeUser, IntArrayList mean) {

    }





    //From Dan's code
    private double correlation(int activeUser, int targetUser) {
        double topSum, bottomSumActive, bottomSumTarget, 
            weight, rating1, rating2;
        double activeAvg = db.getAverageRatingForUser(activeUser);
        double targetAvg = db.getAverageRatingForUser(targetUser);
        ArrayList<Pair> ratings = db.getCommonRatings(activeUser, targetUser);
        topSum = bottomSumActive = bottomSumTarget = weight = 0;
		
        // Do the summations	
        for(Pair pair : ratings ) {
            rating1 = (double)pair.a - activeAvg;
            rating2 = (double)pair.b - targetAvg;
			
            topSum += rating1 * rating2;
            bottomSumActive += Math.pow(rating1, 2);
            bottomSumTarget += Math.pow(rating2, 2);
        }
		
        // This handles an emergency case of dividing by zero
        if(bottomSumActive != 0 && bottomSumTarget != 0)
            weight = topSum / Math.sqrt(bottomSumActive * bottomSumTarget);

        // Add this new weight to memory and return
        weights.put(new String(activeUser + ";" + targetUser), weight);
        return weight;
    }


}*/
