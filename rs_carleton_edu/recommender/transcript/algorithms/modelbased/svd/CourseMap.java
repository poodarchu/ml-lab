package transcript.algorithms.modelbased.svd;

import java.io.*;
import java.util.*;
import transcript.memreader.*;


/**
 * Simple class to map course names to 
 * 0-indexed ids and vice versa. 
 *
 * @author sowellb
 */
public class CourseMap {

    Hashtable<String, Integer> nameToNum;
    Hashtable<Integer, String> numToName;

    /**
     * Constructor. Initializes hash tables. 
     */
    public CourseMap() {
        nameToNum = new Hashtable<String, Integer>();
        numToName = new Hashtable<Integer, String>();
    }


    /**
     * Creates and serializes two maps - one from course name
     * to a 0-indexed id, and one from said id to course name. 
     *
     * @param  mh  The TranscriptMemHelper to get course data from. 
     */ 
    public void mapCourses(TranscriptMemHelper tmh) {

        Iterator<String> it = tmh.getListOfCourses().iterator();

        int index = 0;
        String course;

        while(it.hasNext()) {
            course = it.next();
            nameToNum.put(course, index);
            numToName.put(index, course);
            index++;
        }

        try {
            FileOutputStream fos = new FileOutputStream("nameToNum.dat");
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(nameToNum);
            os.close();

            fos = new FileOutputStream("numToName.dat");
            os = new ObjectOutputStream(fos);
            os.writeObject(numToName);
            os.close();
        }
        catch(IOException e) {
            System.out.println("Error serializing courseMap");
            e.printStackTrace();
        }


    }


    public static void main(String[] args) {
        CourseMap map = new CourseMap();
        map.mapCourses(new TranscriptMemHelper("transcript.dat"));
    }


}