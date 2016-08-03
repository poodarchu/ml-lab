package transcript.memreader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;

import transcript.data.Entry;
import transcript.data.Parser;
import cern.colt.function.IntObjectProcedure;
import cern.colt.list.ObjectArrayList;
import cern.colt.map.OpenIntIntHashMap;
import cern.colt.map.OpenIntObjectHashMap;

/**
 * The "database" for transcript data.
 * 
 * Overall it has limited capabilities in comparison to a real
 * database, but it is much faster since it all fits in memory
 * (or else it won't even start).  However, it maintains all
 * functionality needed for collaborative filtering.
 * 
 * @author lewda
 */
public class TranscriptMemReader implements Serializable {

    static final long serialVersionUID = 12345;
    //Thats the same as the combination on my briefcase!

    public HashMap<String, ObjectArrayList> courseToStud;
    public OpenIntObjectHashMap studToCourse;

    public OpenIntIntHashMap sumByStud;
    public HashMap<String, Integer> sumByCourse;

    /**
     * Creates an empty TranscriptMemReader
     */
    public TranscriptMemReader() {
        courseToStud = new HashMap<String, ObjectArrayList>();
        studToCourse = new OpenIntObjectHashMap();

        sumByStud = new OpenIntIntHashMap();
        sumByCourse = new HashMap<String, Integer>();
    }

    /////////////////////////////////////////////
    //                DATA INPUT               //
    /////////////////////////////////////////////

    /**
     * Reads in a TransformedEntry flattext file, and fills
     * the TranscriptMemReader with its data-y goodness.
     * 
     * @param fileName the input TransformedEntry flattext file
     */
    public void readData(String fileName) {
        Entry[] data = Parser.parseTransformedData(fileName);

        for (Entry e : data) {
            //Add the entry if there is a course (sid is always there)
            if (!(e.getDept().equals("NONE") && e.getCnum() == 0)) {
                addEntry(e);
            }
        }
    }

    /**
     * Adds an entry to the database.
     * 
     * While it is used for adding entries upon reading data,
     * it can also be used to add data later on.  If that's the case,
     * make sure to re-run sortHashes() and computeAverages() once
     * you are done adding new entries to re-sort the lists and
     * re-compute the averages.
     * 
     * @param e the Entry to add
     * @param recalculate if set to true, resorts and recalculates averages
     */
    public void addEntry(Entry e) {
        ObjectArrayList list;
        String hash = e.getFullCourse();

        //Adds to courseToStud
        if (courseToStud.containsKey(hash)) {
            list = (ObjectArrayList) courseToStud.get(hash);
        }
        else {
            list = new ObjectArrayList();
            courseToStud.put(hash, list);
        }

        list.add(e);

        // Adds to studToCourse
        int hash2 = e.getSid();

        if (studToCourse.containsKey(hash2)) {
            list = (ObjectArrayList) studToCourse.get(hash2);
        }
        else {
            list = new ObjectArrayList();
            studToCourse.put(hash2, list);
        }

        list.add(e);

        // Adds to sumByStud and sumByCourse
        if (sumByStud.containsKey(e.getSid()))
            sumByStud.put(e.getSid(), e.getGradeAsInt()
                    + sumByStud.get(e.getSid()));
        else
            sumByStud.put(e.getSid(), e.getGradeAsInt());

        if (sumByCourse.containsKey(e.getFullCourse()))
            sumByCourse.put(e.getFullCourse(), e.getGradeAsInt()
                    + sumByCourse.get(e.getFullCourse()));
        else
            sumByCourse.put(e.getFullCourse(), e.getGradeAsInt());
    }

    /////////////////////////////////////////////
    //                 SORTING                 //
    /////////////////////////////////////////////

    /**
     * Sorts the lists inside each hash.  That is, each list
     * is of the form int -> ObjectArrayList or 
     * String -> ObjectArrayList.  For both, it sorts the
     * ObjectArrayList portion.
     */
    public void sortHashes() {
        studToCourse.forEachPair(new studToCourseSorter());

        ObjectArrayList list;
        for (String s : courseToStud.keySet()) {
            list = (ObjectArrayList) courseToStud.get(s);
            list.quickSortFromTo(0, list.size() - 1, new sortOnStud());
        }
    }

    /**
     * Sorts studToCourse.
     */
    private class studToCourseSorter implements IntObjectProcedure {
        public boolean apply(int first, Object second) {
            ObjectArrayList list = (ObjectArrayList) second;
            list.quickSortFromTo(0, list.size() - 1, new sortOnCourse());
            return true;
        }
    }

    /**
     * Sorts an ObjectArrayList by course
     */
    private class sortOnCourse implements Comparator<Entry> {
        public int compare(Entry a, Entry b) {
            return a.getFullCourse().compareTo(b.getFullCourse());
        }
    }

    /**
     * Sorts an ObjectArrayList based on sid value
     */
    private class sortOnStud implements Comparator<Entry> {
        public int compare(Entry a, Entry b) {
            return a.getSid() - b.getSid();
        }
    }

    /////////////////////////////////////////////
    //               SERIALIZING               //
    /////////////////////////////////////////////

    /**
     * Turns a TranscriptMemReader object into a serialized file.
     * @param fileName the serialized output filename
     * @param obj the TranscriptMemReader to serialize
     */
    public static void serialize(String fileName, TranscriptMemReader obj) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(obj);
            os.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Can't find file " + fileName);
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }
    }

    /**
     * Deserializes a file into a TranscriptMemReader object.
     * 
     * @param fileName the serialized TranscriptMemReader file
     * @return the deserialized TranscriptMemReader object
     */
    public static TranscriptMemReader deserialize(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fis);

            return (TranscriptMemReader) in.readObject();
        }
        catch (ClassNotFoundException e) {
            System.out.println("Can't find class");
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }

        // We should never get here
        return null;
    }

    /**
     * A quick and dirty way to create a serialized 
     * TranscriptMemReader file.
     * 
     * Note that the input transcript file is 
     * of type TransformedEntry flattext file.
     * 
     * Usage: java TranscriptMemReader <transcript file> <serial file>
     */
    public static void main(String args[]) {
        if (args.length != 2) {
            System.out
                    .println("Needs two arguments: transcript file, serial file");
            System.exit(1);
        }

        TranscriptMemReader reader = new TranscriptMemReader();

        System.out.println("Reading data...");
        reader.readData(args[0]);

        System.out.println("Sorting data...");
        reader.sortHashes();

        System.out.println("Serializing...");
        serialize(args[1], reader);

        // reclaim space in memory so we can deserialize
        reader = null;
        System.gc();

        System.out.println("Deserializing...");
        reader = deserialize(args[1]);
    }
}
