package netflix.algorithms.memorybased.database;

/**
 * WARNING: THIS CLASS IS DEPRECATED DUE TO THE MUCH-INCREASED SPEED
 * OF MEMREADER ACCESS.  NOTHING HERE IS UP TO DATE AS OF 11/07/06.
 */

import java.util.ArrayList;
import java.util.HashMap;

import netflix.utilities.Pair;



public class Weight {
	public static final int CORRELATION = 1;
	public static final int VECTOR_SIMILARITY = 2;
	public static final int DEFAULT_VOTING = 4;
	public static final int INVERSE_USER_FREQUENCY = 8;
	public static final int CASE_AMPLIFICATION = 16;
	public static final int SAVE_WEIGHTS = 32;
	
	// Uses a constant for the amplifier - could be changed
	private final double amplifier = 2.5;
	private MyDatabase db;
	private int options;
	private HashMap<String, Double> weights;
	private HashMap<Integer, Double> vectorNorms;
	private HashMap<Integer, Double> frequencies;
	
	/**
	 * Creates a default Weight object that simply uses correlation.
	 *
	 */
	public Weight(MyDatabase db) {
		this.db = db;
		weights = new HashMap<String, Double>();
		vectorNorms = new HashMap<Integer, Double>();
		options = CORRELATION;
	}
	
	/**
	 * Creates a Weight object with options of which algorithms to use.
	 * 
	 * To pick, you add together the constants of Weight that correspond
	 * to what you want to use.
	 * 
	 * For example, if you want to use correlation with default voting:
	 * Weight w = new Weight(Weight.CORRELATION + Weight.DEFAULT_VOTING);
	 * 
	 * @param options
	 */
	public Weight(MyDatabase db, int options) {
		this.db = db;
		weights = new HashMap<String, Double>();
		vectorNorms = new HashMap<Integer, Double>();
		this.options = options;
		
		// If using inverse user frequency,
		// pre-calculate all of the data
		if((options & INVERSE_USER_FREQUENCY) != 0) {
			frequencies = new HashMap<Integer, Double>();
			int numUsers = db.getNumUsers();
			HashMap<Integer, Integer> movies = db.getMovieRatingNums();
			
			for(Integer i : movies.keySet()) {
				frequencies.put(i, (double)movies.get(i) / (double) numUsers);
			}
		}
	}
	
	/**
	 * Weights two users, based upon the constructor's options.
	 * @param db
	 * @param activeUser
	 * @param targetUser
	 * @return
	 */
	public double weight(int activeUser, int targetUser) {
		// Check to see if I already have this weight stored
		double weight = -99;
		
		if ((options & SAVE_WEIGHTS) != 0) {
			weight = alreadyHaveWeight(activeUser, targetUser);
			if(weight != -99)
				return weight;
		}
		
		if ((options & CORRELATION) != 0) {
			if ((options & INVERSE_USER_FREQUENCY) != 0)
				weight = correlationWithIUF(activeUser, targetUser);
			else
				weight = correlation(activeUser, targetUser);
		}
		else if ((options & VECTOR_SIMILARITY) != 0) {
			if ((options & INVERSE_USER_FREQUENCY) != 0)
				weight = vectorSimilarityWithIUF(activeUser, targetUser);
			else
				weight = vectorSimilarity(activeUser, targetUser);
		}
		
		if ((options & CASE_AMPLIFICATION) != 0)
			weight = amplifyCase(weight);
		
		return weight;
	}
	
	/**
	 * Correlation weighting between two users, as provided in "Empirical
	 * Analysis of Predictive Algorithms for Collaborative Filtering."
	 * @param db the database to use
	 * @param activeUser the active user
	 * @param targetUser the target user
	 * @return their correlation
	 */
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
	
	private double correlationWithIUF(int activeUser, int targetUser) {
		return 0;
	}
	
	private double vectorSimilarity(int activeUser, int targetUser) {
		double bottomActive, bottomTarget, weight;
		ArrayList<Integer> ratings;
		ArrayList<Pair> commonRatings = db.getCommonRatings(activeUser, targetUser);
		bottomActive = bottomTarget = weight = 0;
		
		// Find out the bottom portion for summation on active user
		if(vectorNorms.containsKey(activeUser)) {
			bottomActive = vectorNorms.get(activeUser);
		}
		else {
			ratings = db.getRatingsForMoviesSeenByUser(activeUser);
			for (Integer rating : ratings ) {
				bottomActive += Math.pow(rating, 2);
			}
			bottomActive = Math.sqrt(bottomActive);
			vectorNorms.put(activeUser,bottomActive);
		}

		// Find out the bottom portion for summation on target user
		if(vectorNorms.containsKey(targetUser)) {
			bottomTarget = vectorNorms.get(targetUser);
		}
		else {
			ratings = db.getRatingsForMoviesSeenByUser(targetUser);
			for (Integer rating : ratings ) { 
				bottomTarget += Math.pow(rating, 2);
			}
			bottomTarget = Math.sqrt(bottomTarget);
			vectorNorms.put(targetUser, bottomTarget);
		}
		
		// Do the full summation
		for (Pair pair : commonRatings )
			weight += (pair.a / bottomActive) * (pair.b / bottomTarget);
				
		return weight;
	}
	
	private double vectorSimilarityWithIUF(int activeUser, int targetUser) {
		return 0;
	}
	
	private double amplifyCase(double weight) {
		if(weight >= 0)
			return Math.pow(weight, amplifier);
		else
			return -Math.pow(-weight, amplifier);
	}
	
	/**
	 * Returns a weight if this object has calculated the weight
	 * between the two users before.
	 * 
	 * Returns -99 if there is no weight.
	 * @param activeUser
	 * @param targetUser
	 * @return
	 */
	private double alreadyHaveWeight(int activeUser, int targetUser) {
		Double one = weights.get(new String(activeUser + ";" + targetUser));
		if(one != null)
			return one;
		
		Double two = weights.get(new String(targetUser + ";" + activeUser));
		if(two != null)
			return two;
		
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
		
		if ((options & DEFAULT_VOTING) != 0)
			System.out.print(" with DEFAULT_VOTING");
		
		if ((options & INVERSE_USER_FREQUENCY) != 0)
			System.out.print(" with INVERSE_USER_FREQUENCY");

		if ((options & CASE_AMPLIFICATION) != 0)
			System.out.print(" with CASE_AMPLIFICATION");

		if ((options & SAVE_WEIGHTS) != 0)
			System.out.print(", SAVE_WEIGHTS active");
		
		System.out.println(".");
	}
}
