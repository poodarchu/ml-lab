package netflix.memreader;

import java.util.*;
import java.io.*;

import netflix.utilities.*;

import cern.colt.list.*;
import cern.colt.map.*;
import cern.colt.function.*;

/**
 * Stores a collection of movie ratings efficiently in  
 * memory. Ratings are hashed by both user and movie
 * ids, and additional hashes contain the sum of the 
 * ratings for each user and movie, which allows 
 * for fast calculation of user and movie averages. 
 *
 * This code is modified from an example provided by user
 * "voidanswer" on the Netflixprize forums. 
 * http://www.netflixprize.com/community/viewtopic.php?id=323
 *
 * @author Ben Sowell
 * @author Dan Lew
 */
public class MemReader implements Serializable{

    private static final long serialVersionUID = 7526472295622776147L;

    public OpenIntObjectHashMap movieToCust;
    public OpenIntObjectHashMap custToMovie;
    public OpenIntIntHashMap sumByCust;
    public OpenIntIntHashMap sumByMovie;


    /**
     * Default constructor. Initializes hashtables. 
     */
    public MemReader() {
        movieToCust = new OpenIntObjectHashMap();
        custToMovie = new OpenIntObjectHashMap();    
        sumByCust = new OpenIntIntHashMap();
        sumByMovie = new OpenIntIntHashMap();
    }


    /**
     * Reads a text file in the form 
     *
     * mid,uid,rating
     *
     * and stores this data in the custToMovie and 
     * movieToCust hashtables. 
     *
     * @param  fileName  The file containing the movie
     *                   data in the specified format.
     */
    public void readData(String fileName) {

        try {

            Scanner in = new Scanner(new File(fileName));

            String[] line;
            short mid;
            int uid;
            byte rating;
            String date;


            while(in.hasNextLine()) {

                line = in.nextLine().split(",");
                mid = Short.parseShort(line[0]);
                uid = Integer.parseInt(line[1]);
                rating = Byte.parseByte(line[2]);
				
                addToMovies(mid, uid, rating);
                addToCust(mid, uid, rating);

            }
        }
        catch(FileNotFoundException e) {
            System.out.println("Can't find file " + fileName);
            e.printStackTrace();

        }
        catch(IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }
    }


    /**
     * Serializes a MemReader object so that it can be
     * read back later. 
     *
     * @param  fileName  The file to serialize to. 
     * @param  obj  The name of the MemReader object to serialize.
     */
    public static void serialize(String fileName, MemReader obj) {

        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(obj);
            os.close();
        }
        catch(FileNotFoundException e) {
            System.out.println("Can't find file " + fileName);
            e.printStackTrace();
        }
        catch(IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }
    }


    /**
     * Deserializes a previously serialized MemReader object. 
     *
     * @param  fileName  The file containing the serialized object. 
     * @return The deserialized MemReader object. 
     */
    public static MemReader deserialize(String fileName)
    {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fis);

            return (MemReader) in.readObject();
        }
        catch(ClassNotFoundException e) {
            System.out.println("Can't find class");
            e.printStackTrace();
        }
        catch(IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }

        //We should never get here
        return null;
    }




    /**
     * Adds an entry to the movieToCust hashtable. The
     * uid and rating are packed into one int to 
     * conserve memory. 
     *
     * @param  mid  The movie id. 
     * @param  uid  The user id. 
     * @param  rating  User uid's rating for movie mid.
     */
    public void addToMovies(short mid, int uid, byte rating) {

        IntArrayList list;

        if(mid == 0 && uid == 0)
            return;

        if(movieToCust.containsKey(mid)) {
            list = (IntArrayList) movieToCust.get(mid);
        }
        else {
            list = new IntArrayList();
        }

        list.add(uid<<8 | rating);
        movieToCust.put(mid, list);

        int sum = sumByMovie.get(mid);
        sumByMovie.put(mid, sum + rating);

    }

    
    /**
     * Adds an entry to the custToMovie hashtable. The
     * mid and rating are packed into one int to 
     * conserve memory. 
     *
     * @param  mid  The movie id. 
     * @param  uid  The user id. 
     * @param  rating  User uid's rating for movie mid.
     */
    public void addToCust(short mid, int uid, byte rating) {

        IntArrayList list;

        if(mid == 0 && uid == 0)
            return;

        if(custToMovie.containsKey(uid))
            list = (IntArrayList) custToMovie.get(uid);
        else
            list = new IntArrayList();

        list.add(mid<<8 | rating);
        custToMovie.put(uid, list);

        int sum = sumByCust.get(uid);
        sumByCust.put(uid, sum + rating);
    }


    /**
     * Sorts each entry in the movieToCust and 
     * custToMovie hashes to allow for efficient
     * searching. 
     */
    public void sortHashes() {
        Sorter sorter = new Sorter();
        movieToCust.forEachPair(sorter);
        custToMovie.forEachPair(sorter);
    }


    /**
     * This class is used with the forEachPair method
     * of an OpenIntObjectHashMap when the Object is 
     * an IntArrayList. The apply method sorts the 
     * IntArrayList in ascending order. 
     */
    private class Sorter implements IntObjectProcedure{

        /**
         * Sorts the IntArrayList in ascending order. 
         *
         * @param  first  uid or mid
         * @param  second IntArrayList of ratings. 
         * @return true
         */
        public boolean apply(int first, Object second) {
            IntArrayList list = (IntArrayList) second;
            list.trimToSize();
            list.sortFromTo(0, list.size() -1);
            return true;
        }
    }


    public static void main(String args[]){

        MemReader reader = new MemReader();

        String sourceFile = null;
        String destFile = null;

        try {
            sourceFile = args[0];
            destFile = args[1];

            reader.readData(sourceFile);
            reader.sortHashes();


            IntArrayList users = reader.custToMovie.keys();

            for(int i = 0; i < users.size(); i++) {
                System.out.println(users.get(i));
            }


            serialize(destFile, reader);

        }
        catch(Exception e) {
            System.out.println("usage: java MemReader sourceFile destFile");
            e.printStackTrace();
        }
    }


}
