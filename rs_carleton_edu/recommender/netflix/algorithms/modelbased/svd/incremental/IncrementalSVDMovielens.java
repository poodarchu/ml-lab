package netflix.algorithms.modelbased.svd.incremental;

import netflix.memreader.*;
import netflix.rmse.*;
import java.io.*;
import java.util.*;
import cern.colt.map.OpenIntIntHashMap;
import cern.colt.list.*;


//=============================================================================
// This code is a (nearly) direct port from a C program by Timely Development
// (www.timelydevelopment.com). The following notices and attributes were
// distributed with the original source, and should be retained after
// any modifications. 
//
// @author sowellb
//
// Copyright (C) 2007 Timely Development (www.timelydevelopment.com)
//
// Special thanks to Simon Funk and others from the Netflix Prize contest 
// for providing pseudo-code and tuning hints.
//
// Feel free to use this code as you wish as long as you include 
// these notices and attribution. 
//
// Also, if you have alternative types of algorithms for accomplishing 
// the same goal and would like to contribute, please share them as well :)
//
// STANDARD DISCLAIMER:
//
// - THIS CODE AND INFORMATION IS PROVIDED "AS IS" WITHOUT WARRANTY
// - OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT
// - LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR
// - FITNESS FOR A PARTICULAR PURPOSE.
//
//=============================================================================
public class IncrementalSVDMovielens implements Serializable {

    private static final long serialVersionUID = 1526472295622776147L;
    
    
    //Global counts. The NUM_RATING count is for ua/ubbase
    private final int NUM_RATINGS = 90570;
    private final int NUM_USERS = 944;
    private final int NUM_MOVIES = 1683;
    private final int NUM_FEATURES = 64;
    private final int MIN_EPOCHS = 120;
    private final int MAX_EPOCHS = 200;
    
    private final float INIT_VALUE = 0.1f;
    private final float MIN_IMPROVEMENT = 0.0001f;
    private final float LRATE = 0.001f;
    private final float K = 0.015f;

    private int[] uid;
    private short[] mid;
    private byte[] rating;
    private float[] cache;
        

    private float[][] movieFeatures;
    private float[][] userFeatures;
    
    
    /**
     * Default constructor. Initializes
     * data structures.  
     */
    public IncrementalSVDMovielens() {

        uid = new int[NUM_RATINGS];
        mid = new short[NUM_RATINGS];
        rating = new byte[NUM_RATINGS];
        cache = new float[NUM_RATINGS];

        movieFeatures = new float[NUM_FEATURES][NUM_MOVIES];
        userFeatures = new float[NUM_FEATURES][NUM_USERS];


        for(int i = 0; i < NUM_FEATURES; i++) {
            for(int j = 0; j < NUM_MOVIES; j++) {
                movieFeatures[i][j] = INIT_VALUE;
            }
            for(int j = 0; j < NUM_USERS; j++) {
                userFeatures[i][j] = INIT_VALUE;
            }
        }
    }

    
    /**
     * Train each feature on 
     * the entire data set. 
     */
    private void calcFeatures() {

//        Rating rating;
        double err, p, sq, rmse_last = 2.0, rmse = 2.0;
        short currMid;
        int currUid;
        float cf, mf;

        for(int i = 0; i < NUM_FEATURES; i++) {

            System.out.println("Calculating feature: " + i);

            for(int j = 0; (j < MIN_EPOCHS) || (rmse <= rmse_last - MIN_IMPROVEMENT); j++) {

                sq = 0;
                rmse_last = rmse;

                for(int k = 0; k < rating.length; k++) {

                    currMid = mid[k];
                    currUid = uid[k];

                    // Predict rating and calculate error
                    p = predictRating(currMid, currUid, i, cache[k], true);
                    err = (1.0 * rating[k] - p);
                    sq += err*err;

                    // Cache old features
                    cf = userFeatures[i][currUid];
                    mf = movieFeatures[i][currMid];

                    // Cross-train
                    userFeatures[i][currUid] += (float) (LRATE * (err * mf - K * cf));
                    movieFeatures[i][currMid] += (float) (LRATE * (err * cf - K * mf));
                }
                rmse = Math.sqrt(sq/rating.length);
            }

            //Cache old predictions
            for(int j=0; j<rating.length; j++) {
                cache[j] = (float)predictRating(mid[j], uid[j], i, cache[j], false);
            }
        }
    }

    /**
     * For use during training. 
     *
     * @param  mid  Movie id. 
     * @param  uid   User id.
     * @param  feature  The feature we are training
     * @param  cache  Cache value, if applicable
     * @param  bTrailing
     * @return The predicted rating for use during training. 
     */
    private double predictRating(short mid, int uid, int feature, 
                                float cache, boolean bTrailing) {
        double sum;

        if(cache > 0) 
            sum = cache;
        else 
            sum = 1;

        sum += movieFeatures[feature][mid] * userFeatures[feature][uid];

        if(sum > 5)
            sum = 5;
        else if(sum < 1)
            sum = 1;
        
        if(bTrailing) {
            sum += (NUM_FEATURES - feature - 1) * (INIT_VALUE*INIT_VALUE);

            if(sum > 5) 
                sum = 5;
            else if(sum < 1) 
                sum = 1;   
        }
        return sum;
    }

    /**
     * Predicts the rating for a user/movie pair using
     * all features that have been trained. 
     *
     * @param  mid  The movie to predict the rating for. 
     * @param  uid  The user to predict the rating for.
     * @return The predicted rating for (uid, mid).
     */
    public  double predictRating(short mid, int uid) {

        double sum = 1;

        for(int i = 0; i < NUM_FEATURES; i++) {
            sum += movieFeatures[i][mid] * userFeatures[i][uid];
            if(sum > 5)
                sum = 5;
            else if(sum < 1)
                sum = 1;
        }

        return sum;
    }


    /**
     * Loads file containg all of the known 
     * ratings. 
     *
     * @param  fileName  The data file. 
     */
    public void loadData(String fileName) throws FileNotFoundException, IOException {

        Scanner in = new Scanner(new File(fileName));

        String[] line;
        short currMid;
        int currUid, newUid;
        byte currRating;
        String date;
        int idCounter = 0, ratingCounter = 0;
        while(in.hasNextLine()) {

            line = in.nextLine().split(",");
            currMid = Short.parseShort(line[0]);
            currUid = Integer.parseInt(line[1]);
            currRating = Byte.parseByte(line[2]);
         
            mid[ratingCounter] = currMid;
            uid[ratingCounter] = currUid;
            rating[ratingCounter] = currRating;
            ratingCounter++;
        }
    }

    /**
     * Serializes an IncrementalSVDMovielns object
     *
     * @param  fileName  The destination file.
     * @param  obj  The IncrementalSVDMovielens Object to serialize. 
     */
    public static void serialize(String fileName,
                                 IncrementalSVDMovielens obj) {

        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(obj);
            os.close();
        }
        catch(FileNotFoundException e) {
            System.out.println("Can't find file " + fileName);
            e.printStackTrace();
        }
        catch(IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }

    }

    /**
     * Deserializes an IncrementalSVDMovielens object. 
     *
     * @param  fileName  The name of the serialized file. 
     */
    public static IncrementalSVDMovielens deserialize(String fileName)
    {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fis);

            return (IncrementalSVDMovielens) in.readObject();
        }
        catch(ClassNotFoundException e) {
            System.out.println("Can't find class");
            e.printStackTrace();
        }
        catch(IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }

        //We should never get here
        return null;
    }

    
    /**
     * Using RMSE as measurement, this will compare a test set
     * (in MemHelper form) to the results gotten from the recommender
     *  
     * @param testmh the memhelper with test data in it
     * @return the rmse in comparison to testmh 
     */
    public double testWithMemHelper(MemHelper testmh) {
        RMSECalculator rmse = new RMSECalculator();
        IntArrayList users, movies;
        int uid, mid;

        // For each user, make recommendations
        users = testmh.getListOfUsers();
        for (int i = 0; i < users.size(); i++) {
            uid = users.getQuick(i);
            movies = testmh.getMoviesSeenByUser(uid);

            for (int j = 0; j < movies.size(); j++) {
                mid = MemHelper.parseUserOrMovie(movies.getQuick(j));
                rmse.add(testmh.getRating(uid, mid), 
                         predictRating((short) mid, uid));
            }
        }
        return rmse.rmse();
    }


    public static void main(String[] args) {

        try {
            //Change to appropriate location.
            String dataFile = "/Users/bsowell/recommender/movielens/textFiles/1indexed/ua.base";
            String testFile = "/Users/bsowell/recommender/movielens/1indexed/uatest.dat";


            if(args.length != 1) {
                System.out.println("usage: java IncrementalSVD serialFile");
            }
            else {
                IncrementalSVDMovielens incSVD = new IncrementalSVDMovielens();
                incSVD.loadData(dataFile);
                incSVD.calcFeatures();
                IncrementalSVDMovielens.serialize(args[0], incSVD);
                MemHelper mh = new MemHelper(testFile);

                double rmse = incSVD.testWithMemHelper(mh);
                System.out.println("RMSE = " + rmse);
            }
        }
        catch(FileNotFoundException e) {
            System.out.println("Could not find file.");
            System.out.println("usage: java IncrementalSVD serialFile");
            e.printStackTrace();
        }
        catch(IOException e) {
            System.out.println("Unknown IO error.");
            e.printStackTrace();
        }
    }
}
