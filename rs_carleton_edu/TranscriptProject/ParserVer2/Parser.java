import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Parses files for transcript data
 * 
 * Some information on missing data:
 * Some of the transcript data is missing fields.  Based upon the parser,
 * this is how the data should come out if the data is missing:
 * 
 * dept = "NONE"
 * cnum = "NONE"
 * sect = "NONE"
 * prof = "NONE, "
 * grade = "NONE"
 * major = "NONE"
 * 
 * (Note 1: sid, year, and term are *always* available)
 * 
 * 
 * @author Daniel
 *
 */
public class Parser {
	
	/**
	 * Parses a text file, provided in the Entry format (see Entry.java)
	 * 
	 * @param filename the input file
	 * @return an array of entries
	 */
	public static Entry[] parseData(String filename) {
		File inFile = new File(filename);
		ArrayList<Entry> entries = new ArrayList<Entry>();

		try {
			Scanner scan = new Scanner(inFile);

			while(scan.hasNextLine()) {
				String in = scan.nextLine();
				String[] data = in.split("\t");
				
				String sid, term;
				String course = "NONE.000.00";
				String prof = "NONE, ";
				String grade = "NONE";
				String major = "NONE";

				//Parses the data on a line.
				//Assumes that there is always a term and sid (which is true)
				sid = data[0];
				term = data[1];
				
				if(data.length > 2 && !data[2].equals(""))
					course = data[2];
				
				if(data.length > 3 && !data[3].equals(""))
					grade = data[3];
							
				if(data.length > 4 && !data[4].equals(""))
					prof = data[4];
				
				if(data.length == 6 && !data[5].equals(""))
					major = data[5];
 
				int year = Integer.parseInt(term.substring(0, 2));
				term = (term.substring(3, 5));
				
				//Parses courses into three parts.
				//Handles missing information by inserting "NA"
				//in its place if non-existant
				String[] temp = course.split("\\.");
				String dept = "NONE";
				String cnum = "NONE";
				String sect = "NONE";
				if (temp.length == 1) {
					dept = temp[0];
				}
				else if (temp.length == 2) {
					dept = temp[0];
					cnum = temp[1];				
				}
				else if (temp.length == 3){
					dept = temp[0];
					cnum = temp[1];
					sect = temp[2];					
				}

				entries.add(new Entry(sid, year, term, dept, cnum, sect, prof, grade, major));
			}
		}
		catch(IOException e) {
			System.out.println("Something sad happened when scanning.\n" + e);
			System.exit(1);
		}

		return entries.toArray(new Entry[0]);
	}
}
