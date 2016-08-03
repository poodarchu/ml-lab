package netflix.recommender;

import java.util.ArrayList;

import cern.colt.list.ObjectArrayList;
import netflix.algorithms.modelbased.writer.UserSimKeeper;
import netflix.memreader.MemHelper;
import netflix.memreader.MemReader;
import netflix.utilities.IntDoublePair;
import netflix.utilities.Pair;
import netflix.utilities.Timer227;

/**
 * @author leahsteinberg
 * Extends AbstractRecommender for the Personality Diagnosis algorithm, allowing 
 * slight variation to the algorithm as it appears in the paper by allowing the
 * predicted rating to be generated as the max probility (as in paper) or by using
 * the probilities for a weighted sum to predict the outcome.
 */
public class PDRecommender extends AbstractRecommender{

	private UserSimKeeper simKeeper;
	private MemHelper memHelper;
	private boolean sumTogether; //true to sum together probabilities, false for max
	private boolean useSimilarities; //true to factor in similarities, false to ignore
	private int lowerRating = 1;
	private int upperRating = 5;
	private double sigma = 2; //paper gives 2.5 for rating scale 0-5, we have 1-5
	
	/**
	 * Sets up access to the similarity tables and the (memory-based) database.
	 * Decides which variant of the algorithm will run.
	 * @param simName - the name of the serialized UserSimKeeper file
	 * @param memName - the name of the serialized MemReader file
	 * @param sum - true to use a weighted sum of the probabilities of the different
	 * rating possibilities, false to use the maximum
	 * @param sim - true to use the similarities between the users in predictions
	 */
	public PDRecommender(String simName, String memName, boolean sum, boolean sim) {
		simKeeper = UserSimKeeper.deserialize(simName);
		memHelper = new MemHelper(MemReader.deserialize(memName));
		sumTogether = sum;
		useSimilarities = sim;
	}
	
	/**
	 * Allows user to reset the lower bound on ratings.
	 * @param newLower - the new lower bound
	 */
	public void resetLowerRating(int newLower) {
		lowerRating = newLower;
	}
	
	/**
	 * Allows the user to reset the upper bound on ratings.
	 * @param newUpper - the new upper bound
	 */
	public void resetUpperRating(int newUpper) {
		upperRating = newUpper;
	}
	
	/**
	 * Allows the user to reset the parameter little sigma.
	 * @param newSig - the new value for little sigma
	 */
	public void resetSigma(double newSig) {
		sigma = newSig;
	}
	
	@Override
	public double recommend(int uid, int mid, String date) {
		double predicted = 0.0, current = 0.0, max = 0.0;
		
		ObjectArrayList similarities = simKeeper.getSimilarities(uid);
		//find the probability for each possible rating
		for (int i=lowerRating; i<=upperRating; i++) {
			current = calculateProb(i, uid, mid, similarities);
			//if we're summing together, use a weighted sum here to make prediction
			if (sumTogether) {
				predicted += (current * i);
			} else { //if we're following the paper, keep track of the max probability
				if (current >= max) {
					max = current;
					//make prediction according to max
					predicted = i;
				}
			}
		}
		return predicted;
	}
	
	/**
	 * Calculates the probability that the given user will rate the given movie the
	 * given rating.
	 * @param possRating - the rating for which we are predicting
	 * @param uid - the userID
	 * @param mid - the movieID
	 * @param sims - the list of similar users and their similarities in <uid, sim>
	 * pairs
	 * @return - the probability that uid will rate mid possRating
	 */
	private double calculateProb(int possRating, int uid, int mid, ObjectArrayList sims) {
		double predictedRating = 0.0;
		if (sims == null)
			return 1/(upperRating-lowerRating);
		ArrayList<IntDoublePair> simList = sims.toList();
		ArrayList<Pair> commonMovies;
		int activeRating, simRating;
		double mTerm, uTerm, product;
		//iterate through the list of users
		for (IntDoublePair idPair : simList) {
			product = 1;
			//iterate through the list of movies common to target user and this user
			commonMovies = memHelper.innerJoinOnMoviesOrRating(uid, idPair.a, true);
			for (Pair p : commonMovies) {
				activeRating = MemHelper.parseRating(p.a);
				simRating = MemHelper.parseRating(p.b);
				/* Calculate the probability that these users are the same, given
				 * their ratings on common movies
				 */
				mTerm = Math.exp((-(activeRating - simRating)*(activeRating - simRating))
						/(2*sigma*sigma));

				product *= mTerm;
			}
			/* If we're taking the precalculated similarities into account, 
			 * multiply the probility by the similarity. (Note: not in paper.)
			 */
			if (useSimilarities) 
				//product *= idPair.b;
				product += idPair.b;
			
			simRating = memHelper.getRating(idPair.a, mid);
			/* Calculate the probability that the active user will rate the movie
			 * the possible rating, given what the similar user rated the movie
			 */
			uTerm = Math.exp((-(possRating - simRating)*(possRating - simRating))/
					(2*sigma*sigma));
			predictedRating += uTerm * product;

		}
			//give equal probability to each user in sim list
			predictedRating /= simList.size();
		return predictedRating;
	}
	
    public static void main (String[] args){
        PDRecommender rec;
        MemHelper h;
        Timer227 time = new Timer227();
        double rmse;
        //CHANGE BELOW ACCORDING TO VARIANT!
        System.out.println("sim (add) all");
        for (int i=1; i<6; i++){
            rec = new PDRecommender("movielens_" + i + ".dat", 
            		"u" + i + "base.dat", false, true); //ALSO CHANGE 
            h = new MemHelper("u" + i + "test.dat");
            System.out.println("Ready to start recommendations.");
            time.resetTimer();
            time.start();
            rmse = rec.testWithMemHelper(h);
            time.stop();
            System.out.println("test " + i + " took " + time.getTime() + " s with rmse " + rmse);
        }

     }

}
