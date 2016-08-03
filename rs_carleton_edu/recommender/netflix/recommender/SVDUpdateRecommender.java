package netflix.recommender;

import java.util.*;
import java.io.*;
import java.text.*;

import netflix.memreader.*;
import netflix.algorithms.modelbased.svd.*;
import cern.colt.list.*;
import cern.colt.map.*;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.*;


/**
 * Frontend for the SVDUpdater class based on the paper "Fast Online
 * SVD Revisions for Lightweight Recommender Systems" by Matthew
 * Brand. Note that while this class works and implements all of the
 * features of AbstractRecommender (including add and resort), it
 * would be wise to test the rank 1 updates more thoroughly before
 * doing anything important with this class. I have verified that the
 * rank 1 updates work for SVDs of full rank, but there's still
 * something fishy happening when we keep only the k largest singular
 * values.
 *
 * @author sowellb
 */
public class SVDUpdateRecommender extends AbstractRecommender {

    private int k = 14;
    private int nextUID;
    SVDUpdater svu;
    OpenIntObjectHashMap moviesToAdd;
    
    /**
     * Create a recommender using the file
     * as the MemReader object.
     * @param file the MemReader serialized object
     */
    public SVDUpdateRecommender(String memReaderFile, String svdFile) {
        this(new MemHelper(memReaderFile), svdFile);
    }
    
    /**
     * Create a recommender using a MemReader object.
     * @param file the MemReader serialized objec
     */
    public SVDUpdateRecommender(MemHelper mh, String svdFile) {
        try {

            this.mh = mh;

            FileInputStream fis = new FileInputStream(svdFile);
            ObjectInputStream in = new ObjectInputStream(fis);

            SingularValueDecomposition svd = 
                (SingularValueDecomposition) in.readObject();

            svu = new SVDUpdater(svd,k);
            moviesToAdd = new OpenIntObjectHashMap();

            //The next sequential uid if we want to add a user
            nextUID = mh.getNumberOfUsers();
            svu.makeRecommendationMatrix();
        }
        catch(ClassNotFoundException e) {
            System.out.println("Can't find class");
            e.printStackTrace();
        }
        catch(IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }
    }


    /**
     * Adds a rating to the database. Note that
     * this method caches the ratings to add until
     * the resort method is called. Currently this 
     * method only supports adding ratings for new
     * users who are not already in the database. 
     *
     * @param  uid  The uid of the rating to add.
     * @param  mid  The mid of the rating to add.
     * @param  rating  The rating to add.
     */
    public boolean add(int uid, int mid, int rating) {
        
        DoubleMatrix1D userVector;

        if(uid >= 0 && mid >= 0 
           && mid < Short.MAX_VALUE 
           && rating >= 1 && rating <= 5) {
            //If this is a new user, adjust it's uid so that it is the 
            //sequentially next in the list of users (i.e. so it fits
            //into a matrix nicely).
            if(uid > nextUID) {
                uid = nextUID;
            }
            mh.getMemReader().addToCust((short)mid, uid, (byte)rating);
            mh.getMemReader().addToMovies((short)mid, uid, (byte)rating);
        }
        if(moviesToAdd.containsKey(uid)) {
            userVector = (DoubleMatrix1D) moviesToAdd.get(uid);
        }
        else {
            userVector = new DenseDoubleMatrix1D(1682);
            moviesToAdd.put(uid, userVector);
        }

        userVector.set(mid, rating);
        moviesToAdd.put(uid, userVector);
        return true;
    }
    
    /**
     * This method is called after recommendations
     * are added via the add method. This is where
     * the new users are added to the SVD and the 
     * recommendation matrix is actually recomputed. 
     */
    public void resort() {

        IntArrayList users = moviesToAdd.keys();
        DoubleMatrix1D user;
        double rating;

        for(int i = 0; i < users.size(); i++) {

            user = (DoubleMatrix1D) moviesToAdd.get(users.get(i));
            
            for(int j = 0; j < user.size(); j++) {
                rating = user.get(j);
                if(rating == 0) {
                    rating = mh.getAverageRatingForMovie(j);
                }
                rating -= mh.getAverageRatingForUser(i);
            }
            svu.addUser(user);
        }

        //Update the recommendation matrix with the new users
        svu.makeRecommendationMatrix();
    }

    /**
     * Returns the rating that we predict user uid will give
     * moive mid. 
     *
     * @param  uid  The user for which to predict rating. 
     * @param  mid  The movie for which to predict rating. 
     * @param  date  The date of the recommendation. Not used
     * @return The predicted rating that user uid will give movie mid.
     */
    public double recommend(int uid, int mid, String date) {
        return svu.recommend(uid,mid) + mh.getAverageRatingForUser(uid);
    }


//     TESTING METHOD
//     public void test() {
//         for(int i = 843; i < 943; i++) {

//             IntArrayList movies = mh.getMoviesSeenByUser(i);
//             int size = movies.size();

//             for(int j = 0; j < size; j++) {

//                 add(i, MemHelper.parseUserOrMovie(movies.get(j)), MemHelper.parseRating(movies.get(j)));

//             }
//         }
//        resort();
//     }


    public static void main(String[] args) {
        String base = "/Users/bsowell/recommender/movielens/0indexed/uabase.dat";
        String test = "/Users/bsowell/recommender/movielens/0indexed/uatest.dat";
        String svdFile = "/Users/bsowell/recommender/movielens/0indexed/uabase.svd";
        System.out.println("Training set: " + base + ", test set: " + test);


        SVDUpdateRecommender svdRec = new SVDUpdateRecommender(base, svdFile);

        //svdRec.buildRecommendations();
         MemHelper mh = new MemHelper(test);
         System.out.println(svdRec.testWithMemHelper(mh));

    }


}
