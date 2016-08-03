import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Parses files for transcript data
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
				String[] data = in.split("\t{1}");
				
				String sid = "NONE";
				String term = "NONE/NONE";
				String course = "NONE.000.00";
				String prof = "NONE, ";
				String grade = "NONE";
				String major = "NONE";

				//Parses the data on a line.
				//Makes no assumptions.
				if(data.length > 0 && !data[0].equals(""))
					sid = data[0];
				
				if(data.length > 1 && !data[1].equals(""))
					term = data[1];
					
				if(data.length > 2 && !data[2].equals(""))
					course = data[2];
				
				if(data.length > 3 && !data[3].equals(""))
					grade = data[3];
							
				if(data.length > 4 && !data[4].equals(""))
					prof = data[4];
				
				if(data.length == 6 && !data[5].equals(""))
					major = data[5];

				/*
				 * If you want to further seperate data, this might be useful later on
				 * 
				int year = Integer.parseInt(data[1].substring(0, 2));
				String term = (data[1].substring(3, 5));
				String[] course = data[2].split("\\.");
				int cnum = Integer.parseInt(course[1]);
				int sect = Integer.parseInt(course[2]);
				 */
				
				entries.add(new Entry(sid, term, course, prof, grade, major));
			}
		}
		catch(IOException e) {
			System.out.println("Something sad happened when scanning.\n" + e);
			System.exit(1);
		}

		return entries.toArray(new Entry[0]);
	}
}
