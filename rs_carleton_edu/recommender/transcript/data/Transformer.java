package transcript.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import transcript.utilities.Converter;

/**
 * Transforms data from one format to another.
 * 
 * Warning: it is more than meets the eye.
 * 
 * @author lewda 
 */
public class Transformer {

    /**
     * Takes an array of Entry objects and transforms it so that each student
     * takes up one line.
     * 
     * @param entries
     * @return
     */
    public static TransformedEntry[] transform(Entry[] entries) {
        HashMap<Integer, TransformedEntry> transEntries = new HashMap<Integer, TransformedEntry>();
        TransformedEntry te;

        for (Entry e : entries) {
            // add to an existing entry
            if (transEntries.containsKey(e.getSid())) {
                te = transEntries.get(e.getSid());
                te.addClass(e.getYear(), e.getTerm(), e.getDept(), e.getCnum(),
                        e.getProf(), e.getGrade());
                te.setMajor(e.getMajor(), e.getYear());
            }
            // make a new entry
            else {
                te = new TransformedEntry(e.getSid(), e.getMajor());
                te.addClass(e.getYear(), e.getTerm(), e.getDept(), e.getCnum(),
                        e.getProf(), e.getGrade());
                transEntries.put(e.getSid(), te);
            }
        }

        return transEntries.values().toArray(new TransformedEntry[0]);
    }

    /**
     * Converts an array of TransformedEntry objects and turns them
     * into normal Entry objects.
     * 
     * @param data the TransformedEntry array
     * @return an equivalent Entry array
     */
    public static Entry[] transform(TransformedEntry[] data) {
        ArrayList<Entry> ret = new ArrayList<Entry>();

        for (TransformedEntry te : data) {
            for (Course c : te.getCourses()) {
                ret.add(new Entry(te.getSid(), c.getYear(), c.getTerm(), c
                        .getDept(), c.getCnum(), c.getProf(), c.getGrade(), te
                        .getMajor()));
            }
        }

        Collections.sort(ret, new sorter2());

        return ret.toArray(new Entry[0]);
    }

    /**
     * This tidies up the data by doing a few things:
     * 1. Removes all single entries (useless) 
     * 2. Removes all entries with no rating 
     * 3. Makes sure the SIDs are all sequential with no gaps.
     * 4. Removes all entries that are missing course info.
     * 5. Combines courses when a person took a course multiple times
     * 
     * @param data the data to tidy
     * @return the tidied data
     */
    public static TransformedEntry[] tidyData(TransformedEntry[] data) {
        ArrayList<TransformedEntry> temp = new ArrayList<TransformedEntry>();
        ArrayList<Course> coursesToRemove = new ArrayList<Course>();
        ArrayList<Course> coursesToAdd = new ArrayList<Course>();
        int sid = 1, avg;
        double sum, n;        

        for (TransformedEntry te : data) {
            if (te.getNumClasses() > 1) {
                //Get rid of classes that don't have a grade or course name
                coursesToRemove.clear();
                for (Course c : te.getCourses()) {
                    if (Converter.gradeToNum(c.getGrade()) <= 0
                            || (c.getDept().equals("NONE") && c.getCnum() == 0)) {
                        coursesToRemove.add(c);
                    }
                }

                for (int i = 0; i < coursesToRemove.size(); i++) {
                    te.getCourses().remove(coursesToRemove.get(i));
                }

                //check for duplicate courses
                coursesToRemove.clear();
                coursesToAdd.clear();
                Course[] courses = te.getCourses().toArray(new Course[0]);
                boolean[] check = new boolean[courses.length];
                Arrays.fill(check, true);

                //Essentially, it checks each course for duplicates.
                //Each outer loop can be considered the course being
                //checked for duplicates.  Each inner loop is the checker.
                //All are added at the end.
                for (int i = 0; i < courses.length; i++) {
                    //Ensure that this particular course hasn't been counted yet
                    if (check[i] == true) {
                        n = 1;
                        sum = Converter.gradeToNum(courses[i].getGrade());

                        //Check for duplicates
                        for (int j = i + 1; j < courses.length; j++) {
                            if (courses[i].equals(courses[j])) {
                                sum += Converter.gradeToNum(courses[j]
                                        .getGrade());
                                n++;
                                check[j] = false;
                                coursesToRemove.add(courses[j]);
                            }
                        }

                        //Add courses
                        if (n > 1) {
                            coursesToRemove.add(courses[i]);
                            avg = (int) Math.round(sum / n);
                            coursesToAdd.add(new Course(courses[i].getYear(),
                                    courses[i].getTerm(), courses[i].getDept(),
                                    courses[i].getCnum(), courses[i].getProf(),
                                    Converter.numToGrade(avg)));
                        }
                    }
                }
                
                //Remove the old duplicate entries
                for (int i = 0; i < coursesToRemove.size(); i++) {
                    te.getCourses().remove(coursesToRemove.get(i));
                }
                
                //Add the new combined entries
                for (int i = 0; i < coursesToAdd.size(); i++) {
                    te.getCourses().add(coursesToAdd.get(i));
                }

                //Only add people with more than one class remaining
                if (te.getCourses().size() > 1) {
                    te.setSid(sid);
                    temp.add(te);
                    sid++;
                }
            }
        }

        Collections.sort(temp, new sorter());

        return temp.toArray(new TransformedEntry[0]);
    }
    
    /**
     * Sorts TransformedEntry students based on sid.
     */
    private static class sorter implements Comparator<TransformedEntry> {
        public int compare(TransformedEntry a, TransformedEntry b) {
            return a.getSid() - b.getSid();
        }
    }

    /**
     * Sorts Entry students based on sid.
     */
    private static class sorter2 implements Comparator<Entry> {
        public int compare(Entry a, Entry b) {
            return a.getSid() - b.getSid();
        }
    }
}
