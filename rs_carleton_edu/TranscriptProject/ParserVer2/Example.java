import java.io.FileWriter;
import java.io.IOException;

/**
 * A simple example class for showing off
 * how the parser (and the encrypter) works.  Good for reference!
 * 
 * @author Daniel
 *
 */
public class Example {

	public static void printAll(Entry[] entries) {
		for(Entry e : entries) {
			System.out.println(e);
		}
	}
	
	public static void printAll(TransformedEntry[] entries) {
		for(TransformedEntry e : entries) {
			System.out.println(e);
		}
	}
	
	public static void printAll(Entry[] entries, String fileName) {
		try {
			FileWriter fW = new FileWriter(fileName);
			for(Entry e : entries) {
				fW.write(e.toString() + System.getProperty("line.separator"));
			}
			fW.close();
		} catch (IOException iOE) {
			System.out.println("Sorry, could not write to " + fileName);
		}
		
	}
	
	public static void printAll(TransformedEntry[] entries, String fileName) {
		FileWriter fW = null;
		try {
			fW = new FileWriter(fileName);
			for (TransformedEntry e : entries) {
				fW.write(e.toString() + System.getProperty("line.separator"));
			}
			fW.close();
		} catch (IOException iOE) {
			System.out.println("Sorry, could not write to " + fileName);
			
		}
	}
	
	public static void main(String[] args) {
		Entry[] e = Parser.parseData("data.txt");
		printAll(e);
		System.out.println();
		
		Entry[] encryptedE = Encrypter.encryptCoursesAndProfs(e);
		printAll(encryptedE);
		printAll(encryptedE, "encryptedCourses.txt");
		System.out.println();
		
		Entry[] encryptedE2 = Encrypter.encryptCoursesAndProfs(e, "SHA-1");
		printAll(encryptedE2);
		printAll(encryptedE2, "encryptedCoursesMD2.txt");
		System.out.println();
		
		TransformedEntry[] te = Transformer.transformAndGatherData(e);
		printAll(te);
		System.out.println();
		te = Transformer.removeSingles(te);
		printAll(te);
		System.out.println();
		
		TransformedEntry[] te2 = Transformer.transformAndGatherData(encryptedE);
		printAll(te2);
	}

}
