package netflix.algorithms.memorybased.memreader;

import java.util.ArrayList;

import netflix.memreader.MemHelper;
import netflix.utilities.Pair;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntDoubleHashMap;
import cern.colt.map.OpenIntObjectHashMap;

/**
 * WARNING: THIS CODE NOT MEANT TO BE UNDERSTOOD BY THE LIKES OF MAN
 * 
 * Okay in all seriousness this code is just a streamlined
 * version of FilterAndWeight.  It was made because Netflix
 * dataset = huge (as in XBOX huge).  It may or may not work
 * as it was just made for one long run.
 * 
 * Also, the FilterAndWeight code is more updated.
 * 
 * @author lewda
 */
public class OptimizedFilterAndWeight {

	// Important variables for all processes
	private MemHelper mh;

	// Data that gets stored to speed up algorithms
	private OpenIntDoubleHashMap vectorNorms;
	private OpenIntDoubleHashMap frequencies;

	/**
	 * Default constructor, opens a default database connection
	 * Sets the method to correlation.
	 *
	 */
	public OptimizedFilterAndWeight(String memFile) {
		mh = new MemHelper(memFile);
		
		vectorNorms = new OpenIntDoubleHashMap();

		frequencies = new OpenIntDoubleHashMap();
		double numUsers = mh.getNumberOfUsers();
		OpenIntObjectHashMap movies = mh.getMovieToCust();
		IntArrayList movieKeys = movies.keys();

		for(int i = 0; i < movieKeys.size(); i++) {
			frequencies.put(movieKeys.getQuick(i), (double)((IntArrayList)movies.get(movieKeys.getQuick(i))).size() / numUsers);
		}

		OpenIntObjectHashMap users = mh.getCustToMovie();
		IntArrayList userKeys = users.keys();
		IntArrayList ratings;
		int user;
		double norm;

		for(int j = 0; j < userKeys.size(); j++) {
			user = userKeys.getQuick(j);
			ratings = mh.getMoviesSeenByUser(user);
			norm = 0;

			for(int k = 0; k < ratings.size(); k++) {
				norm += Math.pow(frequencies.get(MemHelper.parseUserOrMovie(ratings.getQuick(k))) 
								 * MemHelper.parseRating(ratings.getQuick(k)), 2);
			}

			norm = Math.sqrt(norm);
			vectorNorms.put(user, norm);
		}
	}
	
	/**
	 * Basic recommendation method for memory-based algorithms.
	 * 
	 * @param user
	 * @param movie
	 * @return the predicted rating, or -99 if it fails (mh error)
	 */
	public double recommend(int activeUser, int targetMovie) {
		double currWeight, weightSum = 0, voteSum = 0;
		int uid;

		IntArrayList users = mh.getUsersWhoSawMovie(targetMovie);

		for(int i = 0; i < users.size(); i++) {
			uid = MemHelper.parseUserOrMovie(users.getQuick(i));
			currWeight = vectorSimilarity(activeUser, uid);
			weightSum += Math.abs(currWeight);
			voteSum += currWeight * (mh.getRating(uid, targetMovie) - mh.getAverageRatingForUser(uid));			
		}

		// Normalize the sum, such that the unity of the weights is one
		voteSum *= 1.0 / weightSum;

		// Add to the average vote for user (rounded) and return
		return Math.round((mh.getAverageRatingForUser(activeUser) + voteSum)*10) / 10.0;
	}

	private double vectorSimilarity(int activeUser, int targetUser) {
		double weight = 0;
		ArrayList<Pair> commonRatings = mh.innerJoinOnMoviesOrRating(activeUser, targetUser, true);

		// Do the full summation
		for(Pair pair : commonRatings) {
			weight += ((frequencies.get(MemHelper.parseUserOrMovie(pair.a)) * MemHelper.parseRating(pair.a)) / vectorNorms.get(activeUser)) 
			* ((frequencies.get(MemHelper.parseUserOrMovie(pair.b)) * MemHelper.parseRating(pair.b)) / vectorNorms.get(targetUser));
		}

		return weight;
	}
}
