package netflix.recommender;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import netflix.memreader.MemHelper;
import netflix.rmse.RMSECalculator;
import netflix.ui.Item;
import netflix.ui.ItemComparator;
import cern.colt.list.IntArrayList;

/**
 * An abstract class that is the basis for each recommender's actual run.
 * 
 * It includes one abstract function to implement - recommend(int, int, String).
 * 
 * Also, it includes some methods for adding rows to the database.
 * Note that in order for this to work, you must set MemHelper mh in
 * the extended classes' constructor.  Also, make sure to call resort()
 * after adding entries, so that the underlying database is sorted.
 * 
 * There are also a few helpful methods that apply to all recommenders.
 * 
 * @author lewda
 */
public abstract class AbstractRecommender {

    //The underlying database
    protected MemHelper mh;
    
    /**
     * Recommends a rating based on a uid and mid.
     * 
     * @param uid the user id
     * @param mid the movie id
     * @param date the date
     * @return a rating
     */
    public abstract double recommend(int uid, int mid, String date);

    /**
     * Adds an entry to the database.
     * 
     * Be sure to call resort() after adding entries.
     * 
     * @param uid the user id
     * @param mid the movie id
     * @param rating the rating
     * @return true if successful, false if parameters were bad
     */
    public boolean add(int uid, int mid, int rating) {
        if(uid >= 0 && mid >= 0 && mid < Short.MAX_VALUE && rating >= 1 && rating <= 5) {
            mh.getMemReader().addToCust((short)mid, uid, (byte)rating);
            mh.getMemReader().addToMovies((short)mid, uid, (byte)rating);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Resorts the values in the underlying database.
     * It is important to call this after adding entries.
     */
    public void resort() {
        mh.getMemReader().sortHashes();
    }
    
    /**
     * Given a user id, it finds what movies the user 
     * has *not* seen from among all the movies.
     * 
     * @param sid the user id
     * @return the movies the user has not rated
     */
    public ArrayList<Item> getUnratedMovies(int uid) {
        ArrayList<Item> toTest = new ArrayList<Item>();
        IntArrayList movies = mh.getListOfMovies();
        for (int i = 0; i < movies.size(); i++) {
            toTest.add(new Item(Integer.toString(movies.getQuick(i)), "", 0));
        }
        
        return getUnratedMovies(uid, toTest);
    }
    
    /**
     * Given a user id and a list of movies, it takes
     * out those movies which have been rated by
     * the user.  Non-volatile to parameters.
     * @param uid the user id
     * @param movies a list of movies to test
     * @return all movies the user has *not* rated from the list
     */
    public ArrayList<Item> getUnratedMovies(int uid, ArrayList<Item> movies) {
        ArrayList<Item> unrated = new ArrayList<Item>();
        
        for(Item i : movies) {
            if(mh.getRating(uid, i.getIdAsInt()) < 0) {
                unrated.add(i);
            }
        }
        
        return unrated;
    }
    
    /**
     * Takes in a list of Items (as movies) and ranks them using
     * the recommender system.  Note that it ranks the movies in
     * the original ArrayList, so the old ratings are destroyed and
     * a new ordering is imposed on movies.
     * 
     * @param uid the user to rank the movies for
     * @param movies the movies to rank
     */
    public void rankMovies(int uid, ArrayList<Item> movies) {
        for (Item m : movies)
            m.setRating(recommend(uid, Integer.parseInt(m.getId()), ""));

        Collections.sort(movies, new ItemComparator());
    }
    
    /**
     * Given an input file of data, will output properly formatted results.
     * This should only be used for Netflixprize entries.
     * 
     * Input should be formatted thus:
     * mid:
     * uid,date
     * uid,date
     * ...
     * 
     * Output should be formatted thus:
     * mid:
     * rating
     * rating
     * ...
     * 
     * @param inFile the name of the input file
     */
    public void recommendFile(String inFile, String outFile) {
        File in = new File(inFile);
        Scanner sc = null;
        BufferedWriter out;
        String currLine;
        String[] split;
        int currMovie = 0;

        try {
            sc = new Scanner(in);
        }
        catch (FileNotFoundException e) {
            System.out.println("Infile error, file not found!  Java error: "
                    + e);
            return;
        }

        try {
            out = new BufferedWriter(new FileWriter(outFile));

            while (sc.hasNextLine()) {
                currLine = sc.nextLine().trim();
                split = currLine.split(",");

                if (split.length == 1) {
                    currMovie = Integer.parseInt(currLine.substring(0, currLine
                            .length() - 1));
                    out.write(currLine);
                }
                else {
                    out.write(Double.toString(recommend(Integer
                            .parseInt(split[0]), currMovie, split[1])));
                }

                out.newLine();
            }

            out.close();
        }
        catch (IOException e) {
            System.out.println("Write error!  Java error: " + e);
            System.exit(1);
        }
    }
    
    /**
     * Using RMSE as measurement, this will compare a test set
     * (in MemHelper form) to the results gotten from the recommender
     *  
     * @param testmh the memhelper with test data in it
     * @return the rmse in comparison to testmh 
     */
    public double testWithMemHelper(MemHelper testmh) {
        RMSECalculator rmse = new RMSECalculator();
        IntArrayList users, movies;
        String blank = "";
        int uid, mid;

        // For each user, make recommendations
        users = testmh.getListOfUsers();
        for (int i = 0; i < users.size(); i++) {
            uid = users.getQuick(i);
            movies = testmh.getMoviesSeenByUser(uid);

            for (int j = 0; j < movies.size(); j++) {
                mid = MemHelper.parseUserOrMovie(movies.getQuick(j));
                rmse.add(testmh.getRating(uid, mid), recommend(uid, mid, blank));
            }
        }

        return rmse.rmse();
    }
    
    /**
     * Stub so one can test without having to initialize
     * their own MemHelper object.
     * @param testFile the MemHelper file
     * @return its rmse in testing
     */
    public double testWithMemHelper(String testFile) {
        MemHelper testmh = new MemHelper(testFile);
        return testWithMemHelper(testmh);
    }
}
