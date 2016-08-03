package netflix.algorithms.memorybased.memreader;

import java.util.ArrayList;
import java.util.HashMap;

import netflix.memreader.MemHelper;
import netflix.utilities.Pair;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntDoubleHashMap;
import cern.colt.map.OpenIntObjectHashMap;

/**
 * A memory-based solution for recommendations for movie data.
 * 
 * For someone using the class, there's only three things you need
 * to know: how to use options, how to recommend, and when to reset.
 * 
 * First, options.  This class actually contains a few memory-based
 * algorithms in one, due to their similarities.  As such, you 
 * need to define which algorithm to use.  This is made easier
 * via the options parameter in the constructor - simply input
 * the constants to define which memory-based algorithm to use.
 * 
 * Note that correlation, vector similarity, correlation with
 * default voting, and vector similarity with inverse user
 * frequency are mutually exclusive.  Case amplification and
 * saving weights can be used with any of these.
 * 
 * Though it seems like a good idea, I wouldn't use SAVE_WEIGHTS
 * unless you're trying to rank courses for a particular user.
 * This is because SAVE_WEIGHTS will actually slow the program down
 * if there are too many misses - that is, weights that need to be
 * retrieved.  However, if you're constantly ranking one user
 * in comparison to all others, it should definitely be used as it
 * will be a real time saver.
 * 
 * Second, recommendations.  Once you've setup the options the 
 * actual recommendation process is a snap.  Just call 
 * recommend(int, int), where the first int is the user id
 * and the second int is the movie id.  It will return its 
 * recommendation.
 * 
 * What can be confusing are some of the results.  If everything
 * goes well, it will return a rating.  If there is absolutely 
 * no data to use for recommending (ex, no one has rated the target
 * movie) then it returns -1.  If the user has already rated the 
 * movie that you're trying to predict, it will return -2.
 * 
 * Third, resetting.  If the underling database (the MemReader)
 * should ever change, you should call reset().  Some of the time
 * saving features stores data, and will not know that the database
 * has changed otherwise. 
 * 
 * @author lewda
 */
public class FilterAndWeight {
    //Codes for options variable
    public static final int CORRELATION = 1;
    public static final int CORRELATION_DEFAULT_VOTING = 2;
    public static final int VECTOR_SIMILARITY = 4;
    public static final int VS_INVERSE_USER_FREQUENCY = 8;
    public static final int CASE_AMPLIFICATION = 16;
    public static final int SAVE_WEIGHTS = 32;

    // Important variables for all processes
    private MemHelper mh;
    private int options;
    
    // Constants for methods - feel free to change them!
    private final double amplifier = 2.5; //constant for amplifier - can be changed
    private final int d = 2; //constant for default voting
    private final int k = 10000; //constant for default voting
    private final int kd = k*d;
    private final int kdd = k*d*d;

    // Data that gets stored to speed up algorithms
    private HashMap<String, Double> savedWeights;
    private OpenIntDoubleHashMap vectorNorms;
    private OpenIntDoubleHashMap frequencies;
    private OpenIntDoubleHashMap stdevs;
    
    /**
     * Creates a new FilterAndWeight with a given 
     * MemHelper, using correlation.
     * @param tmh the MemHelper object
     */    
    public FilterAndWeight(MemHelper mh) {
        this.mh = mh;
        options = CORRELATION;
        setOptions(options);
    }
    
    /**
     * Creates a new FilterAndWeight with a given MemHelper,
     * using whatever options you want.  The options can
     * be set using the public constants in the class. 
     * @param tmh the MemHelper object
     * @param options the options to use
     */
    public FilterAndWeight(MemHelper mh, int options) {
        this.mh = mh;
        setOptions(options);
    }

    private void setOptions(int options) {
        this.options = options;
        
        stdevs = new OpenIntDoubleHashMap();
        IntArrayList users = mh.getListOfUsers();
        for(int i = 0; i < users.size(); i++) {
            if((options & CORRELATION) != 0 
                    || (options & CORRELATION_DEFAULT_VOTING) != 0)
                stdevs.put(users.getQuick(i), mh.getStandardDeviationForUser(users.getQuick(i)));
            else
                stdevs.put(users.getQuick(i), 1.0);
        }

        if ((options & SAVE_WEIGHTS) != 0)
            savedWeights = new HashMap<String, Double>();

        if ((options & VECTOR_SIMILARITY) != 0
                || (options & VS_INVERSE_USER_FREQUENCY) != 0)
            vectorNorms = new OpenIntDoubleHashMap();

        // If using inverse user frequency,
        // pre-calculate all of the data
        if ((options & VS_INVERSE_USER_FREQUENCY) != 0) {
            frequencies = new OpenIntDoubleHashMap();
            double numUsers = mh.getNumberOfUsers();
            OpenIntObjectHashMap movies = mh.getMovieToCust();
            IntArrayList movieKeys = movies.keys();

            for (int i = 0; i < movieKeys.size(); i++) {
                frequencies.put(movieKeys.getQuick(i), Math.log(numUsers /
                        (double) ((IntArrayList) movies.get(movieKeys
                                .getQuick(i))).size()));
            }
        }
    }
    
    /**
     * This should be run if you change the underlying database.
     */
    public void reset() {
        setOptions(options);
    }
    
    /**
     * Basic recommendation method for memory-based algorithms.
     * 
     * @param user the user id
     * @param movie the movie id
     * @return the predicted rating, -1 if nothing could be predicted, 
     *          -2 if already rated, or -99 if it fails (mh error)
     */
    public double recommend(int activeUser, int targetMovie) {
        //If the movie was already rated by the activeUser, return 02
        //If you want more accurate results, return the actual rating
        //(This is done just so that it can tell you what movies to
        //watch, but avoid the ones you have already watched)
        if (mh.getRating(activeUser, targetMovie) > 0) {
            return -2;
        }
        
        double currWeight, weightSum = 0, voteSum = 0;
        int uid;

        IntArrayList users = mh.getUsersWhoSawMovie(targetMovie);
        
        for (int i = 0; i < users.size(); i++) {
            uid = MemHelper.parseUserOrMovie(users.getQuick(i));
            currWeight = weight(activeUser, uid);
            weightSum += Math.abs(currWeight);
            voteSum += stdevs.get(activeUser) * ((currWeight * (mh.getRating(uid, targetMovie) 
                        - mh.getAverageRatingForUser(uid))) / stdevs.get(uid)) ;
        }

        // Normalize the sum, such that the unity of the weights is one
        voteSum *= 1.0 / weightSum;
        
        // Add to the average vote for user (rounded) and return
        double answer = mh.getAverageRatingForUser(activeUser) + voteSum;
        
        //This implies that there was no one associated with the current user.
        if (answer == 0 || Double.isNaN(answer))
            return -1;
        else
            return answer;
    }

    /**
     * Weights two users, based upon the constructor's options.
     * 
     * @param activeUser
     * @param targetUser
     * @return
     */
    private double weight(int activeUser, int targetUser) {
        double weight = -99;

        // If active, sees if this weight is already stored
        if ((options & SAVE_WEIGHTS) != 0) {
            weight = getWeight(activeUser, targetUser);
            if (weight != -99)
                return weight;
        }

        // Use an algorithm to weigh the two users
        if ((options & CORRELATION) != 0)
            weight = correlation(activeUser, targetUser);
        else if ((options & CORRELATION_DEFAULT_VOTING) != 0)
            weight = correlationWithDefaultVoting(activeUser, targetUser);
        else if ((options & VECTOR_SIMILARITY) != 0 
                || (options & VS_INVERSE_USER_FREQUENCY) != 0 )
            weight = vectorSimilarity(activeUser, targetUser);

        // If using case amplification, amplify the results
        if ((options & CASE_AMPLIFICATION) != 0)
            weight = amplifyCase(weight);

        // If saving weights, add this new weight to memory
        if ((options & SAVE_WEIGHTS) != 0)
            addWeight(activeUser, targetUser, weight);

        return weight;
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
        double activeAvg = mh.getAverageRatingForUser(activeUser);
        double targetAvg = mh.getAverageRatingForUser(targetUser);
        ArrayList<Pair> ratings = mh.innerJoinOnMoviesOrRating(activeUser,
                targetUser, true);
        
        // Do the summations
        for (Pair pair : ratings) {
            rating1 = (double) MemHelper.parseRating(pair.a) - activeAvg;
            rating2 = (double) MemHelper.parseRating(pair.b) - targetAvg;
            
            topSum += rating1 * rating2;
            bottomSumActive += Math.pow(rating1, 2);
            bottomSumTarget += Math.pow(rating2, 2);
        }
        
        double n = ratings.size() - 1;
        
        //So we get results even if they match on only one item
        //(Better than nothing, right?)
        if(n == 0)
            n++;

        // This handles an emergency case of dividing by zero
        if (bottomSumActive != 0 && bottomSumTarget != 0)
            return (n * topSum) / Math.sqrt(bottomSumActive * bottomSumTarget);
        else
            return 1;
    }
    
    /**
     * Correlation weighting between two users, as provided in "Empirical
     * Analysis of Predictive Algorithms for Collaborative Filtering."
     * 
     * Also uses default voting, which uses a full outer join and adds
     * mythical votes to each user.  (It does work better, trust me.)
     * 
     * @param activeUser the active user id
     * @param targetUser the target user id
     * @return their correlation
     */
    private double correlationWithDefaultVoting(int activeUser, int targetUser) {
        int parta, partb, partc, partd, parte, rating1, rating2, n;
        ArrayList<Pair> ratings = mh.fullOuterJoinOnMoviesOrRating(activeUser,
                targetUser, true);
        parta = partb = partc = partd = parte = 0;
        n = ratings.size();

        // Do the summations
        for (Pair pair : ratings) {
            if(pair.a == 0)
                rating1 = d;
            else
                rating1 = MemHelper.parseRating(pair.a);
            
            if(pair.b == 0)
                rating2 = d;
            else
                rating2 = MemHelper.parseRating(pair.b);

            parta += rating1 * rating2;
            partb += rating1;
            partc += rating2;
            partd += Math.pow(rating1, 2);
            parte += Math.pow(rating2, 2);;
        }
        
        //Do some crazy calculations to come up with the correlation
        double answer = ((n+k)*(double)(parta+kdd) - (partb+kd)*(double)(partc+kd)) / 
                Math.sqrt(((n+k)*(double)(partd+kdd) - Math.pow(partb+kd, 2))
                     *((n+k)*(double)(parte+kdd) - Math.pow(partc+kd, 2)));
        
        //In case one student got the same grade all the time, etc.
        if(Double.isNaN(answer))
            return 1;
        else
            return answer;
    }
    
    /**
     * Treats two users as vectors and find out their cosine similarity.
     * 
     * It can also use inverse user frequency, if VS_INVERSE_USER_FREQUENCY
     * is active.
     * 
     * As described in "Empirical Analysis of Predictive Algorithms 
     * for Collaborative Filtering."
     * 
     * @param activeUser the active user id
     * @param targetUser the target user id
     * @return their similarity
     */
    private double vectorSimilarity(int activeUser, int targetUser) {
        double bottomActive, bottomTarget, weight;
        IntArrayList ratings;
        ArrayList<Pair> commonRatings = mh.innerJoinOnMoviesOrRating(
                activeUser, targetUser, true);
        bottomActive = bottomTarget = weight = 0;

        // Find out the bottom portion for summation on active user
        if (vectorNorms.containsKey(activeUser)) {
            bottomActive = vectorNorms.get(activeUser);
        }
        else {
            ratings = mh.getMoviesSeenByUser(activeUser);
            if ((options & VS_INVERSE_USER_FREQUENCY) == 0) {
                for (int i = 0; i < ratings.size(); i++) {
                    bottomActive += Math.pow(MemHelper.parseRating(ratings
                            .getQuick(i)), 2);
                }
            }
            else {
                for (int i = 0; i < ratings.size(); i++) {
                    bottomActive += Math.pow(frequencies.get(MemHelper
                            .parseUserOrMovie(ratings.getQuick(i)))
                            * MemHelper.parseRating(ratings.getQuick(i)), 2);
                }
            }
            bottomActive = Math.sqrt(bottomActive);
            vectorNorms.put(activeUser, bottomActive);
        }

        // Find out the bottom portion for summation on target user
        if (vectorNorms.containsKey(targetUser)) {
            bottomTarget = vectorNorms.get(targetUser);
        }
        else {
            ratings = mh.getMoviesSeenByUser(targetUser);
            if ((options & VS_INVERSE_USER_FREQUENCY) == 0) {
                for (int i = 0; i < ratings.size(); i++) {
                    bottomTarget += Math.pow(MemHelper.parseRating(ratings
                            .getQuick(i)), 2);
                }
            }
            else {
                for (int i = 0; i < ratings.size(); i++) {
                    bottomTarget += Math.pow(frequencies.get(MemHelper
                            .parseUserOrMovie(ratings.getQuick(i)))
                            * MemHelper.parseRating(ratings.getQuick(i)), 2);
                }
            }
            bottomTarget = Math.sqrt(bottomTarget);
            vectorNorms.put(targetUser, bottomTarget);
        }

        // Do the full summation
        if ((options & VS_INVERSE_USER_FREQUENCY) == 0) {
            for (Pair pair : commonRatings) {
                weight += MemHelper.parseRating(pair.a) * MemHelper.parseRating(pair.b);
            }
        }
        else { 
            for (Pair pair : commonRatings) {
                weight += (frequencies.get(MemHelper.parseUserOrMovie(pair.a)) * MemHelper
                        .parseRating(pair.a))
                        * (frequencies.get(MemHelper.parseUserOrMovie(pair.b)) * MemHelper
                        .parseRating(pair.b));
            }
        }
        
        weight /= bottomActive * bottomTarget;
        
        return weight;
    }

    /**
     * "Amplifies" any weight, by a constant (defined at top).
     * 
     * @param weight the weight
     * @return the amplified weight
     */
    private double amplifyCase(double weight) {
        if (weight >= 0)
            return Math.pow(weight, amplifier);
        else
            return -Math.pow(-weight, amplifier);
    }

    /**
     * Saves the weight between two users.
     *  
     * @param user1 
     * @param user2 
     * @param weight 
     */
    private void addWeight(int user1, int user2, double weight) {
        savedWeights.put(user1 + ";" + user2, new Double(weight));
    }

    /**
     * Returns a weight if this object has calculated the weight
     * between the two users before.
     * 
     * Returns -99 if there is no weight.
     * @param user1
     * @param user2
     * @return the weight if found, otherwise -99
     */
    private double getWeight(int user1, int user2) {
        if(savedWeights.containsKey(user1 + ";" + user2))
            return savedWeights.get(user1 + ";" + user2);
        else if(savedWeights.containsKey(user2 + ";" + user1))
            return savedWeights.get(user2 + ";" + user1);

        return -99;
    }

    /**
     * Prints out the options being used for easy viewing
     * @param options
     */
    public static void printOptions(int options) {
        if ((options & CORRELATION) != 0)
            System.out.print("CORRELATION");
        else if ((options & VECTOR_SIMILARITY) != 0)
            System.out.print("VECTOR_SIMILARITY");
        else if ((options & CORRELATION_DEFAULT_VOTING) != 0)
            System.out.print("CORRELATION_DEFAULT_VOTING");
        else if ((options & VS_INVERSE_USER_FREQUENCY) != 0)
            System.out.print("VS_INVERSE_USER_FREQUENCY");

        if ((options & CASE_AMPLIFICATION) != 0)
            System.out.print(" with CASE_AMPLIFICATION");

        if ((options & SAVE_WEIGHTS) != 0)
            System.out.print(", SAVE_WEIGHTS active");

        System.out.println(".");
    }
}
