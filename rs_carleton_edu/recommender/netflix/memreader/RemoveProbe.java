package netflix.memreader;

import java.util.*;
import java.io.*;
import cern.colt.list.IntArrayList;
import cern.colt.map.*;
import cern.colt.function.IntObjectProcedure;


/**
 * Simple class to remove the probe data from a 
 * MemReader object containing the netflix training
 * data. 
 *
 * @author Ben Sowell
 */
public class RemoveProbe {

    private OpenIntObjectHashMap custToMovie;
    private OpenIntObjectHashMap movieToCust;
    private OpenIntIntHashMap sumByCust;
    private OpenIntIntHashMap sumByMovie;

    private MemHelper helper;


    /**
     * Constructs a RemoveProbe object and gets the
     * hashes from the specified MemReader object. 
     *
     * @param  fileName  The serialized MemReader object.
     */
    public RemoveProbe(String fileName) {
        helper = new MemHelper(fileName);
        custToMovie = helper.getCustToMovie();
        movieToCust = helper.getMovieToCust();
        sumByCust = helper.getSumByCust();
        sumByMovie = helper.getSumByMovie();
    }


    /**
     * Removes each user movie pair in the 
     * probe file from the MemReader object.
     *
     * @param fileName The file containing the 
     *                 probe data. 
     */
    public void parseFile(String fileName) {

        try {
            Scanner in = new Scanner(new File(fileName));
            String input, tempString;
            int currMovie = 0, cust = 0, tempRating;
            IntArrayList tempList, remList, custList = null;

            while(in.hasNext()) {

                input = in.next();

                // If the line ends in a colon then it is a movie id.
                if(input.charAt(input.length()-1) ==':') {
                    custList = new IntArrayList();
                    tempString = input.substring(0, input.length()-1);
                    currMovie = Integer.parseInt(tempString);
                }
                // Otherwise it is a user id. 
                else {
                    
                    cust = Integer.parseInt(input);
                    custList.add(cust);
                    
                    //Remove from custToMovie
                    tempList = (IntArrayList) custToMovie.get(cust);
                    custToMovie.put(cust, removeFromList(tempList, currMovie));
                    tempRating = helper.getRating(cust, currMovie);
                    sumByCust.put(cust, sumByCust.get(cust) - tempRating);
                    
                    //Remove from movieToCust
                    tempList = (IntArrayList) movieToCust.get(currMovie);
                    movieToCust.put(currMovie, removeFromList(tempList, cust));
                    sumByMovie.put(currMovie, sumByMovie.get(currMovie) - tempRating);
                }
            }
        }
        catch(FileNotFoundException e) {
            System.out.println("Could not find file");
            e.printStackTrace();
        }
        catch(IOException e) {
            System.out.println("IO Error");
            e.printStackTrace();
        }
    }

    /**
     * Removes a specific user or movie id from an 
     * IntArrayList. We cannot use existing methods for
     * this because each id is combined with its rating
     * when it is stored. 
     *
     * @param  list The original list. 
     * @param  id  The id of the user or movie to remove. 
     * @return A list containing all the elements of the original without
     *         id. 
     */
    public IntArrayList removeFromList(IntArrayList list, int id) {

        int temp;
        IntArrayList remList = new IntArrayList();


        for(int i = 0; i < list.size(); i++) {
            
            temp = list.get(i);
            
            if(MemHelper.parseUserOrMovie(temp) == id) {
                remList.add(temp);
                list.removeAll(remList);
                return list;
            }
        }
        
        return list;
    }


    public static void main(String args[]) {

        try {
            String dataFile = args[0];
            String probeFile = args[1];
            String destFile = args[2];

            RemoveProbe remover = new RemoveProbe(dataFile);
            remover.parseFile(probeFile);
            MemReader.serialize(destFile, remover.helper.getMemReader());
        }
        catch(Exception e) {
            System.out.println("usage: java RemoveProbe dataFile probeFile destFile");
        }
    }
}
