package netflix.algorithms.memorybased.rectree;

import java.util.*;
import netflix.memreader.*;
import netflix.utilities.*;
import cern.colt.list.*;
import cern.colt.map.*;


class Centroid {

    private OpenIntDoubleHashMap sum;
    private OpenIntIntHashMap count;
    private double average;


    public int startingUid;

    /**
     * Default constructor. Initializes
     * movies and average. 
     */
    public Centroid() {
        sum = new OpenIntDoubleHashMap();
        count = new OpenIntIntHashMap();
        average = 0.0;
        startingUid = -1;
    }

    public Centroid(Centroid other) {
        sum = other.cloneSum();
        count = other.cloneCount();
        average = other.getAverage();
        startingUid = -1;
    }


    /**
     * Creates this centroid as a copy of an 
     * actual user. This is used when choosing
     * the random k initial seeds. 
     *
     * @param  uid  The user to use for this centroid. 
     * @param  helper  The MemHelper object containing
     *                 this user. 
     */
    public Centroid(int uid, MemHelper helper) {

        sum = new OpenIntDoubleHashMap();
        count = new OpenIntIntHashMap();
        startingUid = uid;

        IntArrayList mids = helper.getMoviesSeenByUser(uid);
        int mid, rating;

        for(int j = 0; j < mids.size(); j++) {
            mid = MemHelper.parseUserOrMovie(mids.get(j));
            rating = MemHelper.parseRating(mids.get(j));
            sum.put(mid, rating);
            count.put(mid, 1);
        }
        
        average = helper.getAverageRatingForUser(uid);
    }

    public OpenIntDoubleHashMap cloneSum() {
        return (OpenIntDoubleHashMap) sum.clone();
    }

    public OpenIntIntHashMap cloneCount() {
        return (OpenIntIntHashMap) count.clone();
    }

    /**
     * Gets this centroid's rating for the specified
     * movie. 
     *
     * @param  mid The movie to get the rating for
     * @return The rating for movie mid
     */
    public double getRating(int mid) {

//        System.out.println("Entering getRating " + mid);

        if(!count.containsKey(mid) || !sum.containsKey(mid))
            return 0.0;

//        System.out.println("In getRating: " + sum.get(mid) + " " + count.get(mid));

        return (sum.get(mid) / count.get(mid));
    }


    public double getAverage() {
        return average;
    }

    /** TESTING METHOD */
    public void printRatings() {
        
        IntArrayList keys = sum.keys();
        int mid;
        keys.sort();

        for(int i = 0; i < keys.size(); i++) {
            mid = keys.get(i);

            System.out.println(mid + " " + sum.get(mid) + " " + count.get(mid));
        }

    }


    /**
     *
     * DOES NOT UPDATE AVERAGE!
     */
    public void addPoint(int uid, MemHelper helper) {

        IntArrayList movies = helper.getMoviesSeenByUser(uid);
        int mid, rating;

        for(int i = 0; i < movies.size(); i++) {
            
            mid = MemHelper.parseUserOrMovie(movies.get(i));
            rating = MemHelper.parseRating(movies.get(i));
            

            if(!count.containsKey(mid)) {
                count.put(mid, 1);
            }
            else {
                count.put(mid, 1 + count.get(mid));
            }

            if(!sum.containsKey(mid)) {
                sum.put(mid, rating);
            }
            else {
                sum.put(mid, rating + sum.get(mid));
            }
        }
    }

    /**
     * Note that this method does not make sure that 
     * the count and sum remain positive. Don't remove
     * a point that's not in the cluster.
     *
     * DOES NOT UPDATE AVERAGE!
     */
    public void removePoint(int uid, MemHelper helper) {

        IntArrayList movies = helper.getMoviesSeenByUser(uid);
        int mid, rating;

        for(int i = 0; i < movies.size(); i++) {

            mid = MemHelper.parseUserOrMovie(movies.get(i));
            rating = MemHelper.parseRating(movies.get(i));

            //This movie is no longer rated in the centroid, 
            //so remove it. It would probably be okay to leave
            //a 0 in the hash, but at this point I'm more concerned
            //with correctness than speed. 
            if(count.get(mid) - 1 <= 0) {
                count.removeKey(mid);
                sum.removeKey(mid);
            }
            else {
                count.put(mid, count.get(mid) - 1);
                sum.put(mid, sum.get(mid) - rating);
            }
        }
    }



    /**
     * Computes the average rating in this
     * centroid. 
     */
    public void findAverage() {
        IntArrayList keys = sum.keys();
        double avg = 0.0;

        if(keys.size() == 0) {
            average = 0.0;
        }
        else {

            for(int i = 0; i < keys.size(); i++) {
                if(count.get(keys.get(i)) != 0)
                    avg += (sum.get(keys.get(i)) / count.get(keys.get(i)));
            }
        
            average = avg / keys.size();
        }
    }
 
    /**
     * 
     *
     *
     * @param  uid  The user to find the distance from. 
     * @param  helper  The MemHelper object containing uid. 
     *
     */
//     public double distance(int uid, MemHelper helper) {

//         int currMovie;
//         double rating1, rating2, topSum, bottomSumUser; 
//         double bottomSumCentroid, weight;

//         topSum = bottomSumUser = bottomSumCentroid = weight = 0.0;
        
//         double userAverage = helper.getAverageRatingForUser(uid);
//         IntArrayList userMovies = helper.getMoviesSeenByUser(uid);
       

//         for(int i = 0; i < userMovies.size(); i++) {
//             currMovie = userMovies.get(i);

//             if(sum.containsKey(MemHelper.parseUserOrMovie(currMovie))) {

//                 System.out.println(MemHelper.parseUserOrMovie(currMovie));

//                 rating1 = MemHelper.parseRating(currMovie) - userAverage;
//                 rating2 = getRating(MemHelper.parseUserOrMovie(currMovie)) 
//                     - average;

//                 topSum += rating1 * rating2;
//                 bottomSumUser += rating1 * rating1;
//                 bottomSumCentroid += rating2 * rating2;
//             }
//         }


//         // This handles an emergency case of dividing by zero
//         if(bottomSumUser != 0 && bottomSumCentroid != 0)
//             weight = topSum / Math.sqrt(bottomSumUser * bottomSumCentroid);
		
//         return weight;
//     }

    

    public double distanceWithDefault(int uid, double cliqueAverage, 
                                      MemHelper helper) {

        int currMovie, userIndex, centroidIndex, userMid, centroidMid;
        double rating1, rating2, topSum, bottomSumUser; 
        double bottomSumCentroid, weight;
  

        topSum = bottomSumUser = bottomSumCentroid = weight = 0.0;
        userIndex = centroidIndex = userMid = centroidMid = 0;

        double userAverage = helper.getAverageRatingForUser(uid);

        //To do a sort merge join, both lists must be sorted. We know
        //userMovies is sorted (by MemReader), but we must sort clusterMovies.
        IntArrayList userMovies = helper.getMoviesSeenByUser(uid);
        IntArrayList centroidMovies = count.keys();
        centroidMovies.sort();


        while(userIndex < userMovies.size() && 
              centroidIndex < centroidMovies.size()) {
            
            //System.out.println("\tin loop");

            userMid = MemHelper.parseUserOrMovie(userMovies.getQuick(userIndex));
            centroidMid = centroidMovies.getQuick(centroidIndex);

            //Both the user and centroid rated the movie
            if(userMid == centroidMid) {
                
                //temp
//                int userRating = MemHelper.parseRating(userMovies.getQuick(userIndex));

//                System.out.println(userMid + " " + userRating + " | " 
//                + getRating(centroidMid));

                rating1 = MemHelper.parseRating(userMovies.getQuick(userIndex))
                    - userAverage;
                rating2 = getRating(centroidMid) - average;
                userIndex++; centroidIndex++;
            }
            //User rated movie, centroid didn't
            else if(userMid < centroidMid) {
                
//                              int userRating = MemHelper.parseRating(userMovies.getQuick(userIndex));

//                System.out.println(userMid + " " + userRating + " | --"); 

                rating1 = MemHelper.parseRating(userMovies.getQuick(userIndex))
                    - userAverage;
                rating2 = cliqueAverage - average;
                userIndex++;
            }
            //Centroid rated movie, user didn't
            else {
//                             System.out.println(centroidMid + " --" + " | " 
//                                 + getRating(centroidMid));

                rating1 = cliqueAverage - userAverage;
                rating2 = getRating(centroidMid) - average;
                centroidIndex++;
            }
          
//            System.out.println("rating1: " + rating1 + " rating2 " + rating2);

//            System.out.println("topSum " + (rating1 * rating2));

            topSum += rating1 * rating2;
            bottomSumUser += rating1 * rating1;
            bottomSumCentroid += rating2 * rating2;
        }
        
//        System.out.println("userIndex: " + userIndex + 
        //                         " centroidIndex: " + centroidIndex);

        
        int tempMid;
        double tempRating;
        

        //The sort-merge loop stops when one of the indices goes out 
        //of bounds. Here we take into account the movies left in the
        //other list. 
        if(userIndex < userMovies.size()) {

//          System.out.println("in if 1: " + bottomSumCentroid);


            for(int i = userIndex; i < userMovies.size(); i++) {
  

//                tempMid = MemHelper.parseUserOrMovie(userMovies.get(i));
//                tempRating = MemHelper.parseRating(userMovies.get(i));

//                      System.out.println(tempMid + " " + tempRating + " | --");


                rating1 = MemHelper.parseRating(userMovies.getQuick(i))
                    - userAverage;
                rating2 = cliqueAverage - average;
                //              userIndex++;

//                System.out.println("\trating1: " + rating1 + " rating2: " + rating2);
//                System.out.println("topSum " + (rating1 * rating2));
                topSum += rating1 * rating2;
                bottomSumUser += rating1 * rating1;
                bottomSumCentroid += rating2 * rating2;
            }
        }
        else if(centroidIndex < centroidMovies.size()) {

//            System.out.println("in if 2: " + bottomSumCentroid);

            for(int i = centroidIndex; i < centroidMovies.size(); i++) {
                
//                tempMid = centroidMovies.get(i);
//                tempRating = getRating(tempMid);
                
//                System.out.println(tempMid + " " +  "-- | " + tempRating);


                rating1 = cliqueAverage - userAverage;
                rating2 = getRating(centroidMovies.get(i)) - average;
//                centroidIndex++;

//                System.out.println("\trating1: " + rating1 + " rating2: " + rating2);

//                System.out.println("topSum " + (rating1 * rating2));

                topSum += rating1 * rating2;
                bottomSumUser += rating1 * rating1;
                bottomSumCentroid += rating2 * rating2;

            }
        
            //           System.out.println("Leaving if 2: " + bottomSumCentroid);

        }



        // This handles an emergency case of dividing by zero
        if(bottomSumUser != 0 && bottomSumCentroid != 0)
            weight = topSum / Math.sqrt(bottomSumUser * bottomSumCentroid);
    

//        System.out.println("topSum: " + topSum + "\nbottomSumUser " + bottomSumUser + "\nbottomSumCentroid " + bottomSumCentroid);

        return Math.abs(weight);
    }
}