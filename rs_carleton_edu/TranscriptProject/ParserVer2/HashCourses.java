import java.io.FileWriter;
import java.io.IOException;

/**
 * A driver file for encrypting courses
 * Somewhat based on Dan's Example.java
 * @author AST
 *
 */
public class HashCourses {

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
	
	public static void printAllAndWriteToFile(Entry[] entries, String fileName) {
		FileWriter fW = null;
		try {
			fW = new FileWriter(fileName);
			for (Entry e : entries) {
				System.out.println(e);
				fW.write(e.toString() + System.getProperty("line.separator"));
			}
			fW.close();
		} catch (IOException iOE) {
			System.out.println("Sorry, could not write to " + fileName);
			
		}
	}
	
	public static void printAllAndWriteToFile(TransformedEntry[] entries, String fileName) {
		FileWriter fW = null;
		try {
			fW = new FileWriter(fileName);
			for (TransformedEntry e : entries) {
				System.out.println(e);
				fW.write(e.toString() + System.getProperty("line.separator"));
			}
			fW.close();
		} catch (IOException iOE) {
			System.out.println("Sorry, could not write to " + fileName);
			
		}
	}
	public static void main(String[] args) {
		String dataFileName = (args.length > 0) ? args[0] : "data.txt";
		String coursesEncryptedFileName = (args.length > 1) ? args[1] : "encryptedCourses.txt";
		Entry[] e = Parser.parseData(dataFileName);
		Entry[] encryptedE = null;
		boolean optionsFound = false;
		if (args.length > 2) {
			String options = args[2];
			if (options.substring(0,2).equalsIgnoreCase("-e")) {
				optionsFound = true;
				String optionList = options.substring(2);
				boolean encryptTerm = (optionList.indexOf("t") >= 0);
				boolean encryptDept = (optionList.indexOf("d") >= 0);
				boolean encryptCNum = (optionList.indexOf("c") >= 0);
				boolean encryptSect = (optionList.indexOf("s") >= 0);
				boolean encryptProf = (optionList.indexOf("p") >= 0);
				boolean encryptGrade = (optionList.indexOf("g") >= 0);
				boolean encryptMajor = (optionList.indexOf("m") >= 0);
				encryptedE = Encrypter.encryptData(e, "MD5", false,
						encryptTerm, encryptDept, encryptCNum, encryptSect,
						encryptProf, encryptGrade, encryptMajor);
			} else {
				printUsage();
				System.exit(1);
			}
		}
		if (!optionsFound) encryptedE = Encrypter.encryptCoursesAndProfs(e);
		printAllAndWriteToFile(encryptedE, coursesEncryptedFileName);
		System.out.println();
	}
	
	private static void printUsage() {
		System.out.println("java HashCourses [datafilespec] [encryptedfilespec] [options]\n");
		System.out.println(" options: -e[tdcspgm]");
	}

}

