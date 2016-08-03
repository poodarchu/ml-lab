package netflix.recommender;

/**
 * This class uses the item-item similarity table to predict ratings by a 
 * user on an unrated movie.
 */

import java.util.ArrayList;

import netflix.algorithms.modelbased.itembased.DatabaseImpl;
import netflix.memreader.MemHelper;
import netflix.utilities.IntDoublePair;
import netflix.utilities.Pair;
import netflix.utilities.Timer227;

public class ItemItemRecommender extends AbstractRecommender{

    private DatabaseImpl db;
    private boolean method; //true for weighted sums, false for linear regression

    //constructor sets up the database-access layer and determines which method will
    //be used to make recommendations (weighted sums or linear regression)
    public ItemItemRecommender(String dbName, String rateName, String movieName, String userName,
    		String simName, boolean weighted){
        db = new DatabaseImpl(dbName, rateName, movieName, userName, simName);
        this.method = weighted;
    }
    
    /**
     * @author steinbel
     * Implements the abstract method AbstractRecommender using whichever method was
     * set by constructor.  (Discards the date information.)
     * @param uid - the user to predict the rating for
     * @param mid - the movie the predict against
     * @param date - the date the rating would be made (irrelevant)
     * @return	the predicted rating for this user on this movie.
     */
    public double recommend(int uid, int mid, String date) {
    	if (method)
    		return weightedSum(mid, uid);
    	else
    		return regression(mid, uid);
    		
    }
    
    /**
     * @author steinbel
     * Uses a weighted sum method to find a predicted integer rating
     * from 1-5 for a user on an unrated movie.
     * @param movieID   The movie for which a rating should be predicted.
     * @param userID    The active user for whom we make the prediction.
     * @return the predicted rating for this user on this movie.
     */
    private double weightedSum(int movieID, int userID){
    	
        double sumTop=0;
        double sumBottom=0;
        
        //grab all similar movies and their similarities
        ArrayList<IntDoublePair> idSimList = db.getSimilarMovies(movieID, true);
        
        //grab the ratings for all the similar movies the user has seen
        int temp;
        for (IntDoublePair pair : idSimList){
            temp = db.getRatingForUserAndMovie(userID, pair.a);
            //if the user hasn't rated this one, skip it
            if (temp!=-99){
            	//calculate the weighted sums
            	sumTop += (temp * pair.b);
            	sumBottom += Math.abs(pair.b);
            }
        }
        //if user didn't see any similar movies give avg rating for user
        if (sumBottom == 0)
        	return db.getAverageRatingForUser(userID);
        
        return sumTop/sumBottom;
    }
    
    /**
     * @author steinbel
     * Uses a regression method to find a predicted integer rating
     * from 1-5 for a user on an unrated movie.
     * @param movieID   The movie for which a rating should be predicted.
     * @param userID    The active user for whom we make the prediction.
     * @return  the predicted rating for this user on this movie.
     */
    private double regression(int movieID, int userID){
    	double predicted = -99;
        int approxRating;
        double sumTop=0;
        double sumBottom=0;

    	//grab all similar movies and their similarities
        ArrayList<IntDoublePair> sims = db.getSimilarMovies(movieID, true);
    
	    //for each similar movie the user has seen
        for (IntDoublePair i : sims){
		    //use lin reg model to generate this user's rating for the sim movie
		    approxRating = predictKnownRating(movieID, i.a, userID);
            //use above result as rating, calculate the weighted sums
            sumTop += (i.b * approxRating);
            sumBottom += Math.abs(i.b);

        }

        predicted = sumTop/sumBottom;
        return predicted;
    }

    /**
     * @author steinbel
     * Builds a linear regression model combining the rating vectors for both movies
     * and using the userIDs as the independent varaiable.
     * @param movie1	The movie (id #) to use as an independent variable.
     * @param movie2	The movie (id #) to use for a dependent variable
     * @param userID	The user for whom the prediction on movie2 should be made.
     * @return 	The predicted rating of the user on movie2.
     */
    private int predictKnownRating(int movie1, int movie2, int userID){
    	int predicted = -99;

	    //build the model:
	    //grab the rating vector for movie1 in <user, rating> pairs
        ArrayList<Pair> targetV = db.getRatingVector(movie1);
	    //grab the rating vector for movie2
        ArrayList<Pair> simV = db.getRatingVector(movie2);
        //create one list of rating instances
        for (Pair p : simV)
        	targetV.add(p);

       /* grab mean (avg) ratings for movies 1 and 2 and for the userID
	    * calculate standard deviation for ratings at the same time for efficiency
	    */
        double meanRate = (db.getAverageRatingForMovie(movie1)
                            + db.getAverageRatingForMovie(movie2))/2;
        double meanUser = 0;
        double sdRate = 0;
        for (Pair p : targetV) {
        	meanUser += p.a;
            sdRate += ( ((double)p.b - meanRate)*((double)p.b - meanRate) );
        }
        meanUser /= (targetV.size()-1);
        sdRate = Math.sqrt( sdRate/(targetV.size() - 1) );
	    
        //now find standard deviation for userIDs
        double sdUser = 0;
        for (Pair p: targetV)
        	sdUser += ( ((double)p.a - meanUser)*((double)p.a - meanUser) );
        sdUser = Math.sqrt( sdUser/(targetV.size() - 1) );
         
	    //find correlation between user and rating
        double r = 0;
        for (Pair p : targetV) 
        	r += ( ((p.a - meanUser)/sdUser) * ((p.b - meanRate)/sdRate) );
        r /= (targetV.size() - 1);

        //calculate the coefficients
        double c2 = r * (sdUser/sdRate);
        double c1 = meanUser - c2*meanRate;

        //TODO: calculate error for epsilon
        
	    //assemble formula and use model to predict rating for user on movie2
        predicted = (int) Math.round(c1 + c2*movie2);
        
	    return predicted;
    }	   
    
    public void open(){
    	db.openConnection();
    }
    public void close(){
    	db.closeConnection();
    }

 
    public static void main (String[] args){
       ItemItemRecommender rec;
       MemHelper h;
       Timer227 time = new Timer227();
       double rmse;
       for (int i=1; i<6; i++){
           rec = new ItemItemRecommender("movielens", "ratings", "movies", "u" + i + "users", 
        		   "itemsim_adjcos_" + i, true);
           rec.open();
           h = new MemHelper("/home/steinbel/u" + i + "test.dat");
           System.out.println("Ready to start recommendations.");
           time.resetTimer();
           time.start();
           rmse = rec.testWithMemHelper(h);
           time.stop();
           System.out.println("test " + i + " took " + time.getTime() + " s with rmse " + rmse);
           
           rec.close();
       }

    }

}
		
