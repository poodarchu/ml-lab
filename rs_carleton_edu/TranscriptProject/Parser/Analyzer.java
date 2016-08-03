import java.util.ArrayList;
import java.util.Arrays;

/**
 * One *really sloppy* program to analyze the data we have.
 * 
 * Run it with the first argument being the text file to analyze.
 * 
 * Here is the data it gathers:
 * 1. The number of students
 * 2. Whether a major changes between two entries for the same student
 * 3. The number of blank fields for each attribute
 * 4. A list of all majors, and how many took each
 * (May be skewed if #2 turns out to be true)
 * 5. A list of all courses, and how many took each
 * (would count doubles if student took same course twice at different times)
 * 6. A list of all professors, and how many took classes from them
 * (counts doubles if a student took two classes from same prof)
 * 7. A list of all terms students took classes for, and how many in each term
 * 8. A list of all grades that can be earned.
 * 9. A list of all malformed 'term' attributes
 * 10. A list of all malformed 'course' attributes
 * 11. The max/min number of classes taken by a single student
 * 12. The number of classes taken by students in general.
 * 
 * @author Daniel
 *
 */
public class Analyzer {
	public Entry[] entries;

	public Analyzer(String filename) {
		entries = Parser.parseData(filename);
	}

	//WARNING: USE THIS FOR TESTING ONLY!
	//Actual use on transcript data will cause it all to be shown!
	public void printAll() {
		for(Entry e : entries) {
			System.out.println(e);
		}
	}

	//For more data gathering, can be deleted later
	//transEntries must not be empty
	public void spitOutMoreData(TransformedEntry[] transEntries) {
		int min, max;
		int[] courseCount = new int[100]; //No one will have taken 100 classes, this is safe to say
		Arrays.fill(courseCount, 0);
		min = max = transEntries[0].getNumClasses();
		
		ArrayList<TransformedEntry> oneclass = new ArrayList<TransformedEntry>();
		
		for(TransformedEntry te : transEntries) {
			int numClasses = te.getNumClasses();
			
			if(numClasses == 1) 
				oneclass.add(te);
			
			if(numClasses < min)
				min = numClasses;
			else if(numClasses > max)
				max = numClasses;
			
			courseCount[numClasses]++;
		}
		
		System.out.println("Lowest number of classes taken by a student: " + min);
		System.out.println("\nMost number of classes taken by a student: " + max);
		System.out.println("\nLIST OF NUMBER OF CLASSES AND HOW MANY TOOK THAT MANY");
		for(int i=min; i <= max; i++) {
			System.out.println(i + "\t" + courseCount[i]);
		}
		System.out.println("\nLIST OF STUDENTS WHO TOOK ONLY ONE CLASS: ");
		for(TransformedEntry te : oneclass)
			System.out.println(te);
	}
	
	//	For more data gathering, can be deleted later

	public static void main(String[] args) {
		Analyzer a = new Analyzer(args[0]);
		//a.printAll();
		Transformer trans = new Transformer();
		TransformedEntry[] transes = trans.transformAndGatherData(a.entries);
		
		System.out.println();
		trans.spitOutData();
		System.out.println();
		a.spitOutMoreData(transes);
	}
}
