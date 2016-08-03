package netflix.algorithms.modelbased.reader;

import java.util.ArrayList;
import netflix.utilities.Pair;
import netflix.utilities.Triple;

/**
 * An interface to describe a data reader to read in movie data
 * @author Amrit Tuladhar
 *
 */
public interface DataReader {
    /**
     * Returns the number of movies
     * @return the number of movies
     */
    public int getNumberOfMovies();

    /**
     * Returns the number of users
     * @return the number of users
     */
    public int getNumberOfUsers();
    /**
     * Closes and does necessary clean-up 
     */
    public void close();
    /**
     * Get the rating for a user - movie pair
     * @param uid User id
     * @param mid Movie id
     * @return rating for uid, mid
     */
    public int getRating(int uid, int mid);
    /**
     * Gets the individual and average ratings for common users between two movies
     * @param mId1
     * @param mId2
     * @return
     */
    public ArrayList<Triple> getCommonUserRatAndAve(int mId1, int mId2);
    /**
     * Gets the ratings for common users between two movies
     * @param mId1
     * @param mId2
     * @return
     */
    public ArrayList<Pair> getCommonUserRatings(int mId1, int mId2);
    /**
     * Gets the average rating for a particular movie
     * @param mid
     * @return
     */
    public double getAverageMovieRating(int mid);
    /**
     * Gets the rating from a user-rating or movie-rating block (see movie MemHelper for more information)
     * @param composite
     * @return
     */
    public int getRatingFromComposite(int composite);
	/**
     * Gets the ratings for common movies for two users
	 * @param uid1
	 * @param uid2
	 * @return
	 */
	public ArrayList<Pair> getCommonMovieRatings(int uid1, int uid2);
	/**
     * Gets the average rating for a user
	 * @param uid1
	 * @return
	 */
	public double getAverageRatingForUser(int uid1);
	/**
     * Gets all the common movie ratings and average ratings for two users
	 * @param uid1
	 * @param uid2
	 * @return
	 */
	public ArrayList<Triple> getCommonMovieRatAndAve(int uid1, int uid2);
}
