package netflix.memreader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import netflix.utilities.Pair;
import cern.colt.function.IntObjectProcedure;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;
import cern.colt.map.OpenIntObjectHashMap;

/**
 * MemHelper provides all the methods for retrieving, parsing,
 * and joining data together from a MemReader object.
 *
 * @author Dan Lew
 * @author Amrit Tuladhar
 * @author Ben Sowell
 */
public class MemHelper {
    // The "database" for this MemHelper
    private MemReader mr;
    private OpenIntObjectHashMap midToName;

    /**
     * Constructs a new MemHelper that uses the specified MemReader for data
     * @param mr the MemReader that holds all the data
     */
    public MemHelper(MemReader mr) {
        this.mr = mr;
        midToName = null;
    }

    /**
     * Constructs a new MemHelper by loading the serialized
     * MemReader from the specified file. 
     *
     * @param  fileName  The file containing serialized MemReader object
     */
    public MemHelper(String fileName) {
        mr = MemReader.deserialize(fileName);
        midToName = null;
    }

    /**
     * Returns the MemReader object maintained 
     * by this MemHelper. 
     *
     * @return  The MemReader object managed by
     *          this MemHelper.
     */
    public MemReader getMemReader() {
        return mr;
    }

    /**
     * Returns the customer to movie hash table. 
     *
     * @return  The custToMovie hash table. 
     */
    public OpenIntObjectHashMap getCustToMovie() {
        return mr.custToMovie;
    }

    /**
     * Returns the movie to customer hash table. 
     *
     * @return  The custToMovie hash table. 
     */
    public OpenIntObjectHashMap getMovieToCust() {
        return mr.movieToCust;
    }

    /**
     * Returns the sumByCust hash table. 
     *
     * @return  The custToMovie hash table. 
     */
    public OpenIntIntHashMap getSumByCust() {
        return mr.sumByCust;
    }

    /**
     * Returns the sumByMovie hash table. 
     *
     * @return  The custToMovie hash table. 
     */
    public OpenIntIntHashMap getSumByMovie() {
        return mr.sumByMovie;
    }

    /**
     * Applies the specified IntObjectProcedure to each key, value pair
     * in the custToMovie hash table. 
     *
     * @param  procedure  The IntObjectProcedure to appy to
     *                    the custToMovie hash table. 
     * @return  True if the apply function returned true. 
     */
    public boolean applyToUserPairs(IntObjectProcedure procedure) {
        return mr.custToMovie.forEachPair(procedure);
    }

    /**
     * Applies the specified IntObjectProcedure to each key, value pair
     * in the movieToCust hash table. 
     *
     * @param  procedure  The IntObjectProcedure to appy to
     *                    the movieToCust hash table. 
     * @return  True if the apply function returned true. 
     */
    public boolean applyToMoviePairs(IntObjectProcedure procedure) {
        return mr.movieToCust.forEachPair(procedure);
    }

    /**
     * Returns the rating portion of a uid/rating or mid/rating block
     *
     * @param block a uid/rating or mid/rating block
     * @return the rating
     */
    public static int parseRating(int block) {
        int mask = 0x000000FF;
        return (block & mask);
    }

    /**
     * Returns the uid or mid portion of a uid/rating or mid/rating block
     *
     * @param block a uid/rating or mid/rating block
     * @return the uid or mid
     */
    public static int parseUserOrMovie(int block) {
        int mask = 0xFFFFFF00;
        return (block & mask)>>8;
    }

    /**
     * Returns the rating that the user gave the movie
     * 
     * @param uid the user id
     * @param mid the movie id
     * @return the rating
     */
    public int getRating(int uid, int mid) {
        if (mr.movieToCust.containsKey(mid) && mr.custToMovie.containsKey(uid)) {
            IntArrayList custList = (IntArrayList) mr.movieToCust.get(mid);
            IntArrayList movieList = (IntArrayList) mr.custToMovie.get(uid);
			
            // Binary search through a list, assuming:
            // 1. The list is already sorted
            // 2. Ratings are limited to 1-5
            if (custList.size() > movieList.size()) {
                int tempmid = mid << 8;
                for (int i=1; i<=5; i++)
                    if (movieList.binarySearch(++tempmid) >= 0)
                        return i;
            } else {
                int tempuid = uid << 8;
                for (int i=1; i<=5; i++)
                    if (custList.binarySearch(++tempuid) >= 0)
                        return i;
            }
        }

        // Not found, return default value
        return -99;
    }

    /**
     * Returns the average rating for a particular movie
     *
     * @param mid the movie id
     * @return the average rating for the movie
     */
    public double getAverageRatingForMovie(int mid) {
        double avg = (double) mr.sumByMovie.get(mid) 
            / (double) getNumberOfUsersWhoSawMovie(mid);

        if(Double.isNaN(avg))
            return 0.0;
        else 
            return avg;
    }

    /**
     * Returns the average rating for a particular user
     *
     * @param uid the user id
     * @return the average rating for the user
     */
    public double getAverageRatingForUser(int uid) {

        double avg = (double) mr.sumByCust.get(uid) 
            / (double)getNumberOfMoviesSeen(uid);

        if(Double.isNaN(avg))
            return 0.0;
        else 
            return avg;


    }

    /**
     * Calculates the standard deviation for a particular user
     * @param uid the user id
     * @return the user's standard deviation
     */
    public double getStandardDeviationForUser(int uid) {
        double avg = getAverageRatingForUser(uid), sd = 0;
        IntArrayList movies = getMoviesSeenByUser(uid);
        
        for(int i = 0; i < movies.size(); i++)
            sd += Math.pow((double)parseRating(movies.getQuick(i)) - avg, 2);
            
        if(movies.size() == 1)
           return Math.sqrt(sd);
        else
           return Math.sqrt(sd / (movies.size() - 1.0));
    }
    
    /**
     * Calculates the standard deviation for a particular movie
     * @param mid the movie id
     * @return the movie's standard deviation
     */
    public double getStandardDeviationForMovie(int mid) {
        double avg = getAverageRatingForMovie(mid), sd = 0;
        IntArrayList users = getUsersWhoSawMovie(mid);
        
        for(int i = 0; i < users.size(); i++)
            sd += Math.pow((double)parseRating(users.getQuick(i)) - avg, 2);
            
        if(users.size() == 1)
            return Math.sqrt(sd);
        else
            return Math.sqrt(sd / (users.size() - 1.0));        
    }
    
    
    /**
     * Returns the sum of the ratings for a 
     * particular user. This is useful for computing
     * the average rating of an arbitrary subset of 
     * users.
     *
     * @param  uid  The user id
     * @return The averagae rating for the user. 
     */
    public double getRatingSumForUser(int uid) {
        return mr.sumByCust.get(uid);
     }

    /**
     * Returns the average rating in the dataset. 
     *
     * @return The average rating in the dataset. 
     */
    public double getGlobalAverage() {
        IntArrayList users = getListOfUsers();
        double sum = 0.0;
        int count = 0;

        for(int i = 0; i < users.size(); i++) {
            sum += getRatingSumForUser(users.get(i));
            count += getNumberOfMoviesSeen(users.get(i));
        }
        
        return sum/count;
    }


    /**
     * Returns the list of all mid/rating blocks for movies
     *
     * @return the list of all mid/rating blocks for movies
     */
    public IntArrayList getListOfMovies() {
        return mr.movieToCust.keys();
    }

    /**
     * Returns the list of all uid/rating blocks for users
     *
     * @return the list of all uid/rating blocks for users
     */
    public IntArrayList getListOfUsers() {
        return mr.custToMovie.keys();
    }

    /**
     * Returns the number of movies
     *
     * @return the number of movies
     */
    public int getNumberOfMovies() {
        return mr.movieToCust.size();
    }

    /**
     * Returns the number of users
     *
     * @return the number of users
     */
    public int getNumberOfUsers() {
        return mr.custToMovie.size();
    }

    /**
     * Returns the number of users who saw a particular movie
     *
     * @param mid the movie id
     * @return the number of users who saw the movie
     */
    public int getNumberOfUsersWhoSawMovie(int mid) {
        if (mr.movieToCust.containsKey(mid)) {
            return ((IntArrayList)mr.movieToCust.get(mid)).size();
        }
        return 0;
    }

    /**
     * Returns the number of movies seen by a particular user
     *
     * @param uid the user id
     * @return the number of movies seen by the user
     */
    public int getNumberOfMoviesSeen(int uid) {
        if (mr.custToMovie.containsKey(uid)) {
            return ((IntArrayList)mr.custToMovie.get(uid)).size();
        }
        return 0;
    }

    /**
     * Returns all users/ratings who have seen a particular movie.
     * 
     * It is returned as an array of uid/rating blocks.
     * @param mid the movie id
     * @return an array of uid/rating blocks
     */
    public IntArrayList getUsersWhoSawMovie(int mid) {
        if (mr.movieToCust.containsKey(mid)) {
            return (IntArrayList) mr.movieToCust.get(mid);
        }

        return new IntArrayList();
    }

    /**
     * Returns all movies/ratings that a particular user has rated.
     * 
     * It is returned as an array of mid/rating blocks.
     * @param uid the user id
     * @return an array of mid/rating blocks
     */
    public IntArrayList getMoviesSeenByUser(int uid) {
        if (mr.custToMovie.containsKey(uid)) {
            return (IntArrayList) mr.custToMovie.get(uid);
        }
        return new IntArrayList();
    }

    /**
     * Inner joins together the data from two uids or two mids, depending.
     * 
     * To explain: if you want to find all the movies in common
     * between two users, you'd put true into onMovies, then enter
     * in two different user ids into the other parameters.  Bam, 
     * you now have 
     * 
     * @param a movie id or user id one
     * @param b movie id or user id two
     * @param which true if the parameters are user ids, false if the
     * parameters are movie ids
     * @return a join between the two users/movies, as uid/rating or mid/rating
     * blocks (depending)
     */
    public ArrayList<Pair> innerJoinOnMoviesOrRating(int a, int b, boolean which) {
        // Get the movies/users for each parameter
        IntArrayList left, right;
        if(which) {
            left = getMoviesSeenByUser(a);
            right = getMoviesSeenByUser(b);
        }
        else {
            left = getUsersWhoSawMovie(a);
            right = getUsersWhoSawMovie(b);
        }

        // Join the two using a sort-merge join
        // Assumes that they two lists are already sorted
        ArrayList<Pair> match = new ArrayList<Pair>();
        int leftIndex = 0, rightIndex = 0;
        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (parseUserOrMovie(left.getQuick(leftIndex)) 
                == parseUserOrMovie(right.getQuick(rightIndex))) {
                match.add(new Pair(left.getQuick(leftIndex++),
                                   right.getQuick(rightIndex++)));
            }
            else if(parseUserOrMovie(left.getQuick(leftIndex)) 
                    < parseUserOrMovie(right.getQuick(rightIndex))) {
                leftIndex++;
            }
            else {
                rightIndex++;
            }
        }

        return match;
    }

    /**
     * Full outer joins together the data from two uids or two mids, depending.
     * 
     * To explain: if you want to find all the movies in common
     * between two users, you'd put true into onMovies, then enter
     * in two different user ids into the other parameters.  Bam, 
     * you now have 
     * 
     * @param a movie id or user id one
     * @param b movie id or user id two
     * @param which true if the parameters are user ids, false if the
     * parameters are movie ids
     * @return a join between the two users/movies, as uid/rating or mid/rating
     * blocks (depending)
     */
    public ArrayList<Pair> fullOuterJoinOnMoviesOrRating(int a, int b, boolean which) {
        // Get the movies/users for each parameter
        IntArrayList left, right;
        if(which) {
            left = getMoviesSeenByUser(a);
            right = getMoviesSeenByUser(b);
        }
        else {
            left = getUsersWhoSawMovie(a);
            right = getUsersWhoSawMovie(b);
        }

        // Join the two using a sort-merge join
        // Assumes that they two lists are already sorted
        ArrayList<Pair> match = new ArrayList<Pair>();
        int leftIndex = 0, rightIndex = 0;
        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (parseUserOrMovie(left.getQuick(leftIndex)) 
                == parseUserOrMovie(right.getQuick(rightIndex))) {
                match.add(new Pair(left.getQuick(leftIndex++),
                                   right.getQuick(rightIndex++)));
            }
            else if(parseUserOrMovie(left.getQuick(leftIndex))
                    < parseUserOrMovie(right.getQuick(rightIndex))) {
                match.add(new Pair(left.getQuick(leftIndex++), 0));
            }
            else {
                match.add(new Pair(0, right.getQuick(rightIndex++)));
            }
        }

        return match;
    }


    /**
     * Reads a serialzed OpenIntObjectHashMap containing
     * a mapping from mid to movie name. 
     *
     * @param  filename  The serialized OpenIntObjectHashMap
     */
    public void readNames(String filename) {

        try {

            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis);
            midToName = (OpenIntObjectHashMap) in.readObject();
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
     * Returns the movie name for a given mid. 
     *
     * @param  mid  The movie id to look up. 
     */
    public String getMovieName(int mid) {

        if(midToName == null) {
            throw new RuntimeException("movie names not loaded");
        }
        
        if(!midToName.containsKey(mid)) {
            return "Error, Movie not in DB.";
        }
        else {
            return (String) midToName.get(mid);
        }
    }
}
