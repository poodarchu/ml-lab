package transcript.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

/**
 * Adds functionality for creating test sets from transcript data.
 * 
 * @author lewda
 */
public class CreateTestSet {

    public static void main(String[] args) {
        System.out.println("Creating training/test sets...");
        createTestSet(args[0], args[1], args[2], Integer.parseInt(args[3]), 123456);
        System.out.println("Done.");
    }
    
    /**
     * Creates a test set from the given TransformedEntry flattext file
     * (i.e., created by the output from TransformedEntry objects)
     * and removes numOff entries from a student (as long as it does
     * not reduce them to less than one entry) to make a test set.
     * 
     * @param fileName the TransformedEntry flattext file
     * @param trainingOutFile the output file name for the training entries
     * @param testOutFile the output file name for the test entries
     * @param numOff the number of entries to remove from each student for
     *               the test set.  Will leave at least one remaining entry.
     * @param seed the seed to use for the random number generator
     */
    public static void createTestSet(String fileName,
            String trainingOutFile, String testOutFile, int numOff, long seed) {
        TransformedEntry[] data = Transformer.transform(Parser
                .parseTransformedData(fileName));

        ArrayList<TransformedEntry> trainingData = new ArrayList<TransformedEntry>();
        ArrayList<TransformedEntry> testData = new ArrayList<TransformedEntry>();
        Random rand = new Random(seed);
        TransformedEntry testTE;

        //For each student, remove numOff courses
        for (TransformedEntry te : data) {
            testTE = new TransformedEntry(te.getSid(), te.getMajor());
            TreeSet<Course> tsc = te.getCourses();
            
            //Remove numOff courses randomly
            for (int i = 0; i < numOff && tsc.size() > 1; i++) {
                int r = rand.nextInt(tsc.size());
                Iterator<Course> it = tsc.iterator();

                for (int j = 0; j < r; j++) {
                    it.next();
                }

                Course c = it.next();
                testTE.addClass(c);
                tsc.remove(c);
            }

            //Add remaining data to training/test sets
            trainingData.add(te);
            testData.add(testTE);
        }

        //Sort the data so that the sids are in order
        Collections.sort(trainingData, new sorter());
        Collections.sort(testData, new sorter());

        //Output to training file
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(
                    trainingOutFile));
            for (TransformedEntry te : trainingData) {
                bw.write(te.toString() + "\n");
            }
            bw.close();
        }
        catch (IOException e) {
            System.out.println("Error writing training file:\n" + e);
        }

        //Output to test file
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(testOutFile));
            for (TransformedEntry te : testData) {
                if(te.getNumClasses() > 0)
                    bw.write(te.toString() + "\n");
            }
            bw.close();
        }
        catch (IOException e) {
            System.out.println("Error writing test file:\n" + e);
        }
    }

    /**
     * Same as other createTestSet, but for
     * those too lazy to write down their own seed.
     */
    public static void createTestSet(String fileName,
            String trainingOutFile, String testOutFile, int numOff) {
        Random rand = new Random();
        createTestSet(fileName, trainingOutFile, testOutFile,
                numOff, rand.nextLong());
    }

    /**
     * Sorts users so that the output entries are in sorted order.
     */
    private static class sorter implements Comparator<TransformedEntry> {
        public int compare(TransformedEntry a, TransformedEntry b) {
            return a.getSid() - b.getSid();
        }
    }
}
