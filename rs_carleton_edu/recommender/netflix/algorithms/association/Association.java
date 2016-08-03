package netflix.algorithms.association;

import java.util.Arrays;

import netflix.memreader.MemHelper;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntDoubleHashMap;
import cern.colt.map.OpenIntIntHashMap;

public class Association {
    private double[][] movieRules;
    private OpenIntIntHashMap movieToNum;
    private OpenIntIntHashMap numToMovie;
    private MemHelper mh;

    /**
     * Creates an Association predictor with just
     * one level of association rules.
     * @param mh the MemHelper database
     */
    public Association(MemHelper mh) {
        this.mh = mh;
        movieToNum = new OpenIntIntHashMap();
        numToMovie = new OpenIntIntHashMap();
    }

    /**
     * Builds the association rules for the underlying database.
     * 
     * Should be run before making any predictions, and should
     * be run after adding anything to the underlying database.
     */
    public void buildRules() {
        //Figure out hashes, initialize arrays
        movieToNum.clear();
        numToMovie.clear();
        IntArrayList movies = mh.getListOfMovies();
        for (int i = 0; i < movies.size(); i++) {
            numToMovie.put(i, movies.getQuick(i));
            movieToNum.put(movies.getQuick(i), i);            
        }

        movieRules = new double[movies.size()][movies.size()];
        for (double[] d : movieRules)
            Arrays.fill(d, 0.0);

        //Calculate the one/two time counts of everything
        OpenIntDoubleHashMap movieSupps = new OpenIntDoubleHashMap();
        IntArrayList users = mh.getListOfUsers();
        double numUsers = mh.getNumberOfUsers();
        int row, col;
        int mid;

        for (int i = 0; i < numUsers; i++) {
            movies = mh.getMoviesSeenByUser(users.getQuick(i));

            for (int j = 0; j < movies.size(); j++) {
                mid = MemHelper.parseUserOrMovie(movies.getQuick(j));

                //Add single itemset support
                if (movieSupps.containsKey(mid))
                    movieSupps.put(mid, movieSupps.get(mid) + 1.0);
                else
                    movieSupps.put(mid, 1.0);

                //Add dual itemset support
                for (int k = j + 1; k < movies.size(); k++) {
                    row = movieToNum.get(mid);
                    col = movieToNum.get(MemHelper.parseUserOrMovie(movies.getQuick(k)));
                    movieRules[row][col]++;
                    movieRules[col][row]++;
                }
            }
        }

        //Calculate support from the counts
        movies = movieSupps.keys();
        for (int i = 0; i < movies.size(); i++)
            movieSupps.put(i, movieSupps.get(i) / numUsers);

        //Figure out the rules now (calculates confidence for all rules)
        movies = movieSupps.keys();
        for (int i = 0; i < movies.size(); i++) {
            row = movieToNum.get(i);
            for(int j = 0; j < movies.size(); j++) {
                movieRules[row][movieToNum.get(j)] /= (movieSupps.get(i) * numUsers);
            }
        }
    }

    /**
     * Ranks a particular user and movie.
     * 
     * Returns -1 if there's no answer to give, and
     * -2 if the user has already seen the movie.
     * 
     * @param sid the user id
     * @param mid the movie id
     * @return its rank, or -1 if there are no rules related, or -2
     *          if the user has already seen the movie.
     */
    public double rank(int uid, int mid) {
        //Check that we have movie as an association rule
        if (!movieToNum.containsKey(mid))
            return -1;

        //Check that the class hasn't been taken already
        if (mh.getRating(uid, mid) > 0)
            return -2;

        IntArrayList movies = mh.getMoviesSeenByUser(uid);
        double rank = 0, temp;
        int chash = movieToNum.get(mid);
        int n = 0;
        
        for (int i = 0; i < movies.size(); i++) {
            temp = movieRules[movieToNum.get(MemHelper.parseUserOrMovie(movies.getQuick(i)))][chash];
            
            if(temp != 0) {
                n++;
                rank += temp * MemHelper.parseRating(movies.getQuick(i));
            }
        }

        if(n != 0)
            return rank / (double) n;
        else
            return -1;
    }

    /**
     * Calculates the recommendation vector for a user.
     * It is the same as calling rank(sid, mid) for every
     * movie.  However, this version is about 8x faster,
     * so for recommendation it should be used.
     * 
     * @param uid the user id
     * @return the user's recommendation vector
     */
    public double[] getRecVec(int uid) {
        IntArrayList movies = mh.getMoviesSeenByUser(uid);
        double[] recVec = new double[movieRules.length];
        double[] avgVec = new double[movieRules.length];
        int rating;
        Arrays.fill(recVec, 0.0);
        Arrays.fill(avgVec, 0.0);
        int hash;

        for (int i = 0; i < movies.size(); i++) {
            hash = movieToNum.get(MemHelper.parseUserOrMovie(movies.getQuick(i)));
            rating = MemHelper.parseRating(movies.getQuick(i));
            
            for (int j = 0; j < movieRules.length; j++) {
                if(movieRules[hash][j] != 0) {
                    recVec[j] += movieRules[hash][j] * (double) rating;
                    avgVec[j]++;
                }
            }
        }
        
        for(int i = 0; i < recVec.length; i++)
            if(avgVec[i] != 0)
                recVec[i] /= avgVec[i];
        
        return recVec;
    }
    
    /**
     * Returns the index for a movie.  Used to interpret
     * the recVec.
     * 
     * @param movie the movie (full movie name)
     * @return its index in recVec, or -1 if it is not in recVec
     */
    public int hash(int mid) {
        if(movieToNum.containsKey(mid))
            return movieToNum.get(mid);
        else
            return -1;
    }
}