package netflix.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import netflix.ui.Item;
import netflix.ui.ItemComparator;

public class RankingUtilities {
    
    /**
     * Reads in a movie name file.
     * 
     * The movie names files are in Subversion, under the names
     * movies_netflix.txt and movies_movielens.txt.  This parser
     * can read both of them just fine. 
     * 
     * @param fileName the movie file to read
     * @return the movie file as an Item list
     */
    public static ArrayList<Item> readMovieFile(String fileName) {
        ArrayList<Item> movies = new ArrayList<Item>();
        String[] split;

        try {
            Scanner sc = new Scanner(new File(fileName));
            
            while (sc.hasNextLine()) {
                split = sc.nextLine().split(",");
                movies.add(new Item(split[0], split[2], -1));
            }
        }
        catch (IOException e) {
            System.out.println("Error reading movie file:\n" + e);
            System.exit(1);
        }

        return movies;
    }
    
    /**
     * This reads a flattext file of courses and returns
     * a series of Item objects for each.
     * 
     * It is used primarily to rank recommendations - i.e., 
     * rate each class for a user, then rank them and recommend.
     * 
     * @param file the flattext file (from Ben)
     * @return all courses in an ArrayList<Item>
     */
    public static ArrayList<Item> readCourseFile(String file) {
        ArrayList<Item> courses = new ArrayList<Item>();
        Scanner sc = null;
        
        try{
            sc = new Scanner(new File(file));
        }
        catch(IOException e) {
            System.out.println("Problem reading file:\n" + e);
            System.exit(1);
        }
        
        String[] split;
        while(sc.hasNextLine()) {
            split = sc.nextLine().split(" ", 3);
            courses.add(new Item(split[0] + "." + split[1], split[2], -1));
        }
        
        return courses;
    }
    
    /**
     * Returns the top n Items of an Item list
     * @param items
     * @param n
     * @return ArrayList<Item> with the top n items
     */
    public static ArrayList<Item> getTopNItems(ArrayList<Item> items, int n) {
        Collections.sort(items, new ItemComparator());
        n = (n > items.size()) ? items.size() : n;
        return new ArrayList<Item>(items.subList(0, n));
    }
    
    /**
     * Prints the top n Items of an Item list.
     * 
     * @param items the items to print
     * @param n the number of them to print
     */
    public static void printTopNItems(ArrayList<Item> items, int n) {
        for (Item item : getTopNItems(items, n)) {
            System.out.println(item);
        }
    }
    
}
