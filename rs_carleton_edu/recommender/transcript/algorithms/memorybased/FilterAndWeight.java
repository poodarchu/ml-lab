package transcript.algorithms.memorybased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import transcript.data.Entry;
import transcript.memreader.TranscriptMemHelper;
import transcript.utilities.Pair;
import cern.colt.list.ObjectArrayList;
import cern.colt.map.OpenIntDoubleHashMap;

/**
 * A memory-based solution for recommendations for transcript
 * data.
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
 * recommend(int, String), where the integer is the student id,
 * and the String is a course (such as "CS.117").  It will
 * return its recommendation.
 * 
 * What can be confusing are some of the results.  If everything
 * goes well, it will return a grade (as an integer).  If there
 * is absolutely no data to use for recommending (ex, no one
 * has taken the target course) then it returns -1.  If the
 * user has already taken the course that you're trying to predict,
 * it will return -2.
 * 
 * Third, resetting.  If the underling database (the TranscriptMemReader)
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
    private TranscriptMemHelper tmh;
    private int options;
    
    //Constants for methods - feel free to change them!
    private final double amplifier = 2.5; //constant for amplifier - can be changed
    private final int d = 5; //constant for default voting
    private final int k = 10000; //constant for default voting
    private final int kd = k*d;
    private final int kdd = k*d*d;

    // Data that gets stored to speed up algorithms
    private HashMap<String, Double> savedWeights;
    private OpenIntDoubleHashMap vectorNorms;
    private HashMap<String, Double> frequencies;
    
    /**
     * Creates a new FilterAndWeight with a given 
     * TranscriptMemHelper, using correlation.
     * @param tmh the TranscriptMemHelper object
     */    
    public FilterAndWeight(TranscriptMemHelper tmh) {
        this.tmh = tmh;
        options = CORRELATION;
        setOptions(options);
    }
    
    /**
     * Creates a new FilterAndWeight with a given TranscriptMemHelper,
     * using whatever options you want.  The options can
     * be set using the public constants in the class. 
     * @param tmh the TranscriptMemHelper object
     * @param options the options to use
     */
    public FilterAndWeight(TranscriptMemHelper tmh, int options) {
        this.tmh = tmh;
        setOptions(options);
    }
    
    /**
     * Sets up the options for this FAW.
     * @param options the options to set
     */
    private void setOptions(int options) {
        this.options = options;

        if ((options & VECTOR_SIMILARITY) != 0
                || (options & VS_INVERSE_USER_FREQUENCY) != 0)
            vectorNorms = new OpenIntDoubleHashMap();

        if ((options * SAVE_WEIGHTS) != 0)
            savedWeights = new HashMap<String, Double>();

        if ((options & VS_INVERSE_USER_FREQUENCY) != 0) {
            frequencies = new HashMap<String, Double>();
            double numStuds = tmh.getNumberOfStuds();
            Set<String> movieKeys = tmh.getCourseToStud().keySet();

            for (String course : movieKeys) {
                frequencies.put(course, Math.log(numStuds / 
                        (double) tmh.getNumberOfStudsTakingCourse(course)));
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
     * Predicts a rating for a student and a course.
     * 
     * @param activeStud the active student id
     * @param course the course (full course name, please)
     * @return the recommended rating
     */
    public double recommend(int activeStud, String course) {
        //If the course was already taken by activeStud, return -2
        //If you want more accurate results, return the actual grade
        //(This is done just so that it can tell you what courses to
        //take, but avoid the ones you have already taken)
        if (tmh.getGrade(activeStud, course) > 0) {
            return -2;
        }

        double currWeight, weightSum = 0, voteSum = 0;
        int sid;
        ObjectArrayList studs = tmh.getStudsTakingCourse(course);

        //If the database has no info on class, just return the student's average
        if (studs == null) {
            return -1;
        }

        for (int i = 0; i < studs.size(); i++) {
            sid = ((Entry) studs.getQuick(i)).getSid();
            currWeight = weight(activeStud, sid);
            weightSum += Math.abs(currWeight);
            voteSum += currWeight * (tmh.getGrade(sid, course) - tmh.getAvgForStud(sid));
        }

        // Normalize the sum, such that the unity of the weights is one
        voteSum *= 1.0 / weightSum;

        // Add to the average vote for user (rounded) and return
        double answer = tmh.getAvgForStud(activeStud) + voteSum;

        //This implies that there was no one associated with the current user.
        if (answer == 0 || Double.isNaN(answer))
            return -1;
        else
            return answer;
    }

    /**
     * Weights two students between each other.  It determines
     * how to weight these two students based on the options
     * defined in the constructor.
     *  
     * @param activeStud the active student id
     * @param targetStud the target student id
     * @return their weight
     */
    private double weight(int activeStud, int targetStud) {
        double weight = -99;

        if ((options & SAVE_WEIGHTS) != 0) {
            weight = getWeight(activeStud, targetStud);
            
            if (weight != -99)
                return weight;
        }

        // Use an algorithm to weigh the two users
        if ((options & CORRELATION) != 0) 
            weight = correlation(activeStud, targetStud);
        else if ((options & VECTOR_SIMILARITY) != 0
                || (options & VS_INVERSE_USER_FREQUENCY) != 0) 
            weight = vectorSimilarity(activeStud, targetStud);
        else if ((options & CORRELATION_DEFAULT_VOTING) != 0) 
            weight = correlationWithDefaultVoting(activeStud, targetStud);

        // If using case amplification, amplify the results
        if ((options & CASE_AMPLIFICATION) != 0)
            weight = amplifyCase(weight);

        // If saving weights, save this weight now
        if ((options & SAVE_WEIGHTS) != 0)
            addWeight(activeStud, targetStud, weight);
        
        return weight;
    }

    /**
     * Correlation weighting between two users, as provided in "Empirical
     * Analysis of Predictive Algorithms for Collaborative Filtering."
     * @param tmh the database to use
     * @param activeStud the active student id
     * @param targetStud the target student id
     * @return their correlation
     */
    private double correlation(int activeStud, int targetStud) {
        double topSum, bottomSumActive, bottomSumTarget, rating1, rating2;
        topSum = bottomSumActive = bottomSumTarget = 0;
        double activeAvg = tmh.getAvgForStud(activeStud);
        double targetAvg = tmh.getAvgForStud(targetStud);
        ArrayList<Pair> ratings = tmh.innerJoin(activeStud, targetStud);

        // Do the summations
        for (Pair pair : ratings) {
            rating1 = (double) pair.a.getGradeAsInt() - activeAvg;
            rating2 = (double) pair.b.getGradeAsInt() - targetAvg;

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
     * @param activeStud the active student id
     * @param targetStud the target student id
     * @return their correlation
     */
    private double correlationWithDefaultVoting(int activeStud, int targetStud) {
        int parta, partb, partc, partd, parte, n, rating1, rating2;
        ArrayList<Pair> ratings = tmh.fullOuterJoin(activeStud, targetStud);
        parta = partb = partc = partd = parte = 0;
        n = ratings.size();
        
        //Do the summations
        for(Pair p : ratings) {
            if(p.a == null)
                rating1 = d;
            else
                rating1 = p.a.getGradeAsInt();
            
            if(p.b == null)
                rating2 = d;
            else
                rating2 = p.b.getGradeAsInt();

            parta += rating1 * rating2;
            partb += rating1;
            partc += rating2;
            partd += Math.pow(rating1, 2);
            parte += Math.pow(rating2, 2);
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
     * @param activeStud the active student id
     * @param targetStud the target student id
     * @return their similarity
     */
    private double vectorSimilarity(int activeStud, int targetStud) {
        double bottomActive, bottomTarget, weight;
        ObjectArrayList ratings;
        ArrayList<Pair> commonRatings = tmh.innerJoin(activeStud, targetStud);
        bottomActive = bottomTarget = weight = 0;

        // Find out the bottom portion for summation on active user
        if (vectorNorms.containsKey(activeStud)) {
            bottomActive = vectorNorms.get(activeStud);
        }
        else {
            ratings = tmh.getCoursesTakenByStud(activeStud);
            if ((options & VS_INVERSE_USER_FREQUENCY) == 0) {
                for (int i = 0; i < ratings.size(); i++) {
                    bottomActive += Math.pow(((Entry) ratings.getQuick(i))
                            .getGradeAsInt(), 2);
                }
            }
            else {
                for (int i = 0; i < ratings.size(); i++) {
                    bottomActive += Math.pow(frequencies.get(((Entry) ratings
                            .getQuick(i)).getFullCourse())
                            * (double) ((Entry) ratings.getQuick(i)).getGradeAsInt(), 2);
                }
            }
            bottomActive = Math.sqrt(bottomActive);
            vectorNorms.put(activeStud, bottomActive);
        }

        // Find out the bottom portion for summation on target user
        if (vectorNorms.containsKey(targetStud)) {
            bottomTarget = vectorNorms.get(targetStud);
        }
        else {
            ratings = tmh.getCoursesTakenByStud(targetStud);
            if ((options & VS_INVERSE_USER_FREQUENCY) == 0) {
                for (int i = 0; i < ratings.size(); i++) {
                    bottomTarget += Math.pow(((Entry) ratings.getQuick(i))
                            .getGradeAsInt(), 2);
                }
            }
            else {
                for (int i = 0; i < ratings.size(); i++) {
                    bottomTarget += Math.pow(frequencies.get(((Entry) ratings
                            .getQuick(i)).getFullCourse())
                            * (double) ((Entry) ratings.getQuick(i)).getGradeAsInt(), 2);
                }
            }
            bottomTarget = Math.sqrt(bottomTarget);
            vectorNorms.put(targetStud, bottomTarget);
        }

        // Do the full summation
        if ((options & VS_INVERSE_USER_FREQUENCY) == 0) {
            for (Pair pair : commonRatings) {
                weight += (double) pair.a.getGradeAsInt()
                        * (double) pair.b.getGradeAsInt();
            }
        }
        else {
            for (Pair pair : commonRatings) {
                weight += frequencies.get(pair.a.getFullCourse()) 
                          * (double) pair.a.getGradeAsInt()
                        * frequencies.get(pair.b.getFullCourse()) 
                          * (double) pair.b.getGradeAsInt();
            }
        }
        
        weight /= bottomActive * bottomTarget;

        return weight;
    }

    /**
     * Save a weight in memory.
     * @param sid1 the first student id
     * @param sid2 the second student id
     * @param weight their weight
     */
    private void addWeight(int sid1, int sid2, double weight) {
        savedWeights.put(sid1 + ";" + sid2, weight);
    }

    /**
     * Retrieves a weight between two students, if it was saved
     * previous.  Otherwise, it returns -99.
     * 
     * @param sid1 the first student id
     * @param sid2 the second student id
     * @return their weight if saved, otherwise -99
     */
    private double getWeight(int sid1, int sid2) {
        if (savedWeights.containsKey(sid1 + ";" + sid2))
            return savedWeights.get(sid1 + ";" + sid2);
        else if (savedWeights.containsKey(sid2 + ";" + sid1))
            return savedWeights.get(sid2 + ";" + sid1);

        return -99;
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
     * Prints out the options being used for easy viewing
     * @param options the options to print
     */
    public static void printOptions(int options) {
        if ((options & CORRELATION) != 0)
            System.out.print("CORRELATION");
        else if ((options & VECTOR_SIMILARITY) != 0)
            System.out.print("VECTOR_SIMILARITY");
        else if ((options & CORRELATION_DEFAULT_VOTING) != 0)
            System.out.print("CORRELATION_DEFAULT_VOTING");

        if((options & VS_INVERSE_USER_FREQUENCY) != 0)
            System.out.print(" with INVERSE_USER_FREQUENCY");
        
        if ((options & CASE_AMPLIFICATION) != 0)
            System.out.print(" with CASE_AMPLIFICATION");
        
        if ((options & SAVE_WEIGHTS) != 0)
            System.out.print(", SAVING WEIGHTS");

        System.out.println(".");
    }
}
