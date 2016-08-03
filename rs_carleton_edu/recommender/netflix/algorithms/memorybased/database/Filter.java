package netflix.algorithms.memorybased.database;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Filter crazy!
 * 
 * WARNING: THIS CLASS IS DEPRECATED DUE TO THE MUCH-INCREASED SPEED
 * OF MEMREADER ACCESS.  NOTHING HERE IS UP TO DATE AS OF 11/07/06.
 * 
 * @author Daniel
 *
 */

public class Filter {
	private MyDatabase db;
	private Weight myWeights;
	
	/**
	 * Default constructor, opens a default database connection
	 * with a default Weight function.
	 *
	 */
	public Filter() {
		db = new MyDatabase();
		db.openConnection();
		myWeights = new Weight(db);
	}
	
	/**
	 * Opens a default database connection and a Weight function (with options).
	 * 
	 * (See Weight constructor for more about options)
	 * @param options the options as defined in Weight.java
	 */
	public Filter(int options) {
		db = new MyDatabase();
		db.openConnection();
		myWeights = new Weight(db, options);
	}
	
	/**
	 * Opens a database with specified parameters and a default Weight function.
	 * @param dbName the name of the database
	 * @param ratingsName the name of the ratings table
	 * @param moviesName the name of the movies table
	 * @param usersName the name of the users table
	 */
	public Filter(String dbName, String ratingsName,
			String moviesName, String usersName) {
		db = new MyDatabase(dbName, ratingsName, moviesName, usersName);
		db.openConnection();
		myWeights = new Weight(db);
	}
	
	/**
	 * Opens a database with specified parameters and a Weight function (with options).
	 * 
	 * (See Weight constructor for more about options)
	 * @param dbName the name of the database
	 * @param ratingsName the name of the ratings table
	 * @param moviesName the name of the movies table
	 * @param usersName the name of the users table
	 * @param options the options as defined in Weight.java
	 */
	public Filter(String dbName, String ratingsName,
			String moviesName, String usersName, int options) {
		db = new MyDatabase(dbName, ratingsName, moviesName, usersName);
		db.openConnection();
		myWeights = new Weight(db, options);
	}
	
	/**
	 * UGLY HACK for testing purposes
	 * @return
	 */
	public MyDatabase getDB() {
		return db;
	}
	
	/**
	 * Basic recommendation method for memory-based algorithms.
	 * 
	 * @param user
	 * @param movie
	 * @return the predicted rating, or -99 if it fails (db error)
	 */
	public int recommend(int activeUser, int targetMovie) {
		// Create a list to store uids/weights in relation to current user
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		
		ArrayList<Integer> users = db.getUsersWhoSawMovie(targetMovie);
		//System.out.println("Found " + users.size() + " relevant users.");
		
		for(Integer uid : users) {
			if(!weights.containsKey(uid) && uid != activeUser) {
				double weight = myWeights.weight(activeUser, uid);
				weights.put(uid, weight);
				//System.out.println("Adding user " + uid + " to list with weight " 
				//				+ weight);
			}
		}
		
		//System.out.println("Done finding relevant users.  Number of relevant users = " + weights.size());
		//System.out.println("Now figuring out weighted sum of votes of other users...");

		// Figure out the weighted sum of all other users
		double currWeight, rating, avg, weightSum = 0, voteSum = 0;
		for(Integer uid : weights.keySet().toArray(new Integer[0])) {
			currWeight = weights.get(uid);
			weightSum += Math.abs(currWeight);
			rating = db.getRatingForUserAndMovie(uid, targetMovie);
			avg = db.getAverageRatingForUser(uid);
			//System.out.println("Adding user " + uid + ", weight = " + currWeight
			//					+ ", rating = " + rating + ", avg = " + avg);
			voteSum += (currWeight * (rating - avg));
		}
		
		//System.out.println("voteSum before normalization: " + voteSum);
		// Normalize the sum, such that the unity of the weights is one
		voteSum = voteSum * (1.0 / weightSum);
		
		//DELETE ME LATER!
		/*
		System.out.println("Weightsum: " + weightSum);
		System.out.println("Normalizing value: " + (1.0 / weightSum));
		System.out.println("Global average vote for this movie: " + db.getAverageRatingForMovie(targetMovie));
		System.out.println("Recommendation for this movie: " + (db.getAverageRatingForUser(activeUser) + voteSum));
		*/
		//DELETE ME LATER!
		
		// Add to the average vote for user (rounded) and return
		return (int)Math.round(db.getAverageRatingForUser(activeUser) + voteSum);
	}
}
