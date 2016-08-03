package transcript.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Parses files for transcript data.
 * 
 * Some of the transcript data is missing fields. Below are the values
 * that are put in each section if it is null:
 * 
 * dept = "NONE"
 * cnum = "0"
 * prof = "NONE," 
 * grade = "NONE"
 * major = "NONE"
 * 
 * (Note: sid, year, and term are always available)
 * 
 * @author lewda
 */
public class Parser {

    /**
     * Parses a text file from the original compsdata.txt (Dave's file)
     * It is formatted like this (delimited by tabs):
     * sid  year/term   dept.cnum.sect  grade   prof    major
     * 
     * @param filename the input file
     * @return an array of entries
     */
    public static Entry[] parseData(String filename) {
        File inFile = new File(filename);
        ArrayList<Entry> entries = new ArrayList<Entry>();
        HashMap<String, Integer> sids = new HashMap<String, Integer>();
        int currSid = 1;

        try {
            Scanner scan = new Scanner(inFile);

            while (scan.hasNextLine()) {
                String in = scan.nextLine();
                String[] data = in.split("\t");

                int sidInt;
                String sid, term;
                String course = "NONE.000.00";
                String prof = "NONE,";
                String grade = "NONE";
                String major = "NONE";

                // Parses the data on a line.
                // Assumes that there is always a term and sid (which is true)
                sid = data[0];
                term = data[1];

                // Creates a non-String hash version of sid
                if (sids.containsKey(sid)) {
                    sidInt = sids.get(sid);
                }
                else {
                    sidInt = currSid;
                    sids.put(sid, sidInt);
                    currSid++;
                }

                if (data.length > 2 && !data[2].equals(""))
                    course = data[2];

                if (data.length > 3 && !data[3].equals(""))
                    grade = data[3];

                if (data.length > 4 && !data[4].equals(""))
                    prof = data[4];

                if (data.length == 6 && !data[5].equals(""))
                    major = data[5];

                int year = Integer.parseInt(term.substring(0, 2));
                term = (term.substring(3, 5));

                // Parses courses into three parts.
                // Handles missing information by inserting "NA"
                // in its place if non-existant
                String[] temp = course.split("\\.");
                String dept = "NONE";
                int cnum = 000;
                if (temp.length == 1) {
                    dept = temp[0];
                }
                else if (temp.length > 1) {
                    dept = temp[0];
                    temp[1] = temp[1].trim();
                    if (temp[1].equals(""))
                        temp[1] = "000";
                    if (temp[1].length() > 3)
                        temp[1] = temp[1].substring(0, 3);
                    cnum = Integer.parseInt(temp[1]);
                }

                prof = prof.trim();

                entries.add(new Entry(sidInt, year, term, dept, cnum, prof,
                        grade, major));
            }
        }
        catch (IOException e) {
            System.out.println("Something sad happened when scanning.\n" + e);
            System.exit(1);
        }

        return entries.toArray(new Entry[0]);
    }

    /**
     * This parses data that has been output from a series 
     * of TransformedEntry objects.  Their output is easier
     * to decode than Dave's CD data.
     * 
     * @param filename the input file
     * @return an array of entries
     */ 
    public static Entry[] parseTransformedData(String filename) {
        Scanner sc = null;

        try {
            sc = new Scanner(new File(filename));
        }
        catch (IOException e) {
            System.out.println(e);
        }

        String[] line;
        ArrayList<Entry> te = new ArrayList<Entry>();

        while (sc.hasNextLine()) {
            line = sc.nextLine().split("; ");
            String cnums = line[3].split("\\.")[1];

            if (cnums.length() > 3) {
                cnums = cnums.substring(0, 3);
            }

            int sid = Integer.parseInt(line[0]);
            int year = Integer.parseInt(line[2].substring(0, 2));
            String term = (line[2].substring(3, 5));
            String dept = line[3].split("\\.")[0];
            int cnum = Integer.parseInt(cnums);
            String prof = line[4];
            String grade = line[5];
            String major = line[1];

            te.add(new Entry(sid, year, term, dept, cnum, prof, grade, major));
        }

        return te.toArray(new Entry[0]);
    }
}
