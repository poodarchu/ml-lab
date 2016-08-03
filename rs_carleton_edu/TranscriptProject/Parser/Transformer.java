import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Transforms data from one format to another.
 * At this point, it also collects some data,
 * as we're not getting to hold onto transformed data...
 * 
 * Warning: it is more than meets the eye.
 * 
 * @author Daniel
 *
 */
public class Transformer {
	
	//THIS IS ALL FOR GATHERING DATA, WILL BE REMOVED LATER
	public int numStudents;
	public boolean majorsDoNotChange;	//majors do not change between entries for same student
	public int numMajorChanges; //NOTE - this is a REALLY BAD ESTIMATE
	public int numBlanksSID;
	public int numBlanksTerm;
	public int numBlanksCourse;
	public int numBlanksGrade;
	public int numBlanksProf;
	public int numBlanksMajor;
	public TreeMap<String, Integer> majors;
	public TreeMap<String, Integer> courses;
	public TreeMap<String, Integer> profs;
	public TreeMap<String, Integer> terms;
	public TreeSet<String> grades;
	public TreeSet<String> malformedTerms;
	public TreeSet<String> malformedCourses;
	
	public Transformer() {
		numStudents = 0;
		majorsDoNotChange = true;
		numMajorChanges = 0;
		numBlanksSID = numBlanksTerm = numBlanksCourse = numBlanksGrade = numBlanksProf = numBlanksMajor = 0;
		majors = new TreeMap<String, Integer>();
		courses = new TreeMap<String, Integer>();
		profs = new TreeMap<String, Integer>();
		terms = new TreeMap<String, Integer>();
		grades = new TreeSet<String>();
		malformedTerms = new TreeSet<String>();
		malformedCourses = new TreeSet<String>();
	}
	
	//Spits out a bunch of data
	public void spitOutData() {
		System.out.println("Number of students: " + numStudents);
		System.out.println("\nMajors do not change: " + majorsDoNotChange);
		System.out.println("\nNumber of major changes detected (VERY ROUGH ESTIMATE): " + numMajorChanges);
		System.out.println("\nNumber of blanks for field 'sid': " + numBlanksSID);
		System.out.println("Number of blanks for field 'term': " + numBlanksTerm);
		System.out.println("Number of blanks for field 'course': " + numBlanksCourse);
		System.out.println("Number of blanks for field 'grade': " + numBlanksGrade);
		System.out.println("Number of blanks for field 'prof': " + numBlanksProf);
		System.out.println("Number of blanks for field 'major': " + numBlanksMajor);
		
		System.out.println("\nLIST OF MAJORS AND NUMBER OF STUDENTS IN EACH");
		for(String s : majors.keySet()) {
			System.out.println(s + "\t" + majors.get(s));
		}
		System.out.println("\nLIST OF ALL COURSES AND NUMBER OF STUDENTS TAKING EACH");
		for(String s : courses.keySet()) {
			System.out.println(s + "\t" + courses.get(s));
		}
		System.out.println("\nLIST OF ALL PROFESSORS AND NUMBER OF STUDENTS TAUGHT");
		for(String s : profs.keySet()) {
			System.out.println(s + "\t" + profs.get(s));
		}
		System.out.println("\nLIST OF ALL TERMS AND OVERALL NUMBER OF CLASSES TAKEN");
		for(String s : terms.keySet()) {
			System.out.println(s + "\t" + terms.get(s));
		}
		System.out.println("\nLIST OF ALL GRADES EARNED");
		for(String g : grades) {
			System.out.println(g);
		}
		System.out.println("\nLIST OF ALL MALFORMED TERM ATTRIBUTES");
		for(String mt : malformedTerms) {
			System.out.println(mt);
		}
		System.out.println("\nLIST OF ALL MALFORMED COURSE ATTRIBUTES");
		for(String mc : malformedCourses) {
			System.out.println(mc);
		}
	}
	//THIS IS ALL FOR GATHERING DATA, WILL BE REMOVED LATER
	
	public TransformedEntry[] transformAndGatherData(Entry[] entries) {
		HashMap<String, TransformedEntry> transEntries = new HashMap<String, TransformedEntry>();
		
		for(Entry e : entries) {
			//First, add the entry or class
			if(transEntries.containsKey(e.getSid())) {
				//Add to the existing entry
				transEntries.get(e.getSid()).addClass(e.getTerm(), e.getCourse(),
							e.getProf(), e.getGrade());
				
				//Check that the major did not change
				if(!transEntries.get(e.getSid()).equals(e.getMajor())) {
					majorsDoNotChange = false;
					numMajorChanges++;
				}
			}
			else {
				//Make a new entry
				TransformedEntry te = new TransformedEntry(e.getSid(), e.getMajor());
				te.addClass(e.getTerm(), e.getCourse(), e.getProf(), e.getGrade());
				transEntries.put(e.getSid(), te);
				numStudents++;
				
				if(majors.containsKey(e.getMajor())) {
					Integer n = majors.get(e.getMajor());
					majors.put(e.getMajor(), n + 1);
				}
				else {
					majors.put(e.getMajor(), 1);
				}
			}
			
			//All of this below is data gathering, can be removed for
			//the purposes of simply transforming data.
			if(e.getSid().equals("NONE"))
				numBlanksSID++;
			if(e.getTerm().equals("NONE/NONE"))
				numBlanksTerm++;
			if(e.getCourse().equals("NONE.000.00"))
				numBlanksCourse++;
			if(e.getGrade().equals("NONE"))
				numBlanksGrade++;
			if(e.getProf().equals("NONE, "))
				numBlanksProf++;
			if(e.getMajor().equals("NONE"))
				numBlanksMajor++;
			
			if(courses.containsKey(e.getCourse())) {
				Integer n = courses.get(e.getCourse());
				courses.put(e.getCourse(), n + 1);
			}
			else {
				courses.put(e.getCourse(), 1);
			}
			
			if(profs.containsKey(e.getProf())) {
				Integer n = profs.get(e.getProf());
				profs.put(e.getProf(), n + 1);
			}
			else {
				profs.put(e.getProf(), 1);
			}
			
			if(terms.containsKey(e.getTerm())) {
				Integer n = terms.get(e.getTerm());
				terms.put(e.getTerm(), n + 1);
			}
			else {
				terms.put(e.getTerm(), 1);
			}
			
			grades.add(e.getGrade());
			
			//Check that term and courses is not missing something
			//ex: term = "97/" or "/WI"
			//ex: course = "ENTS.123." or "123." etc.
			if(e.getTerm().split("/").length != 2)
				malformedTerms.add(e.getTerm());
			if(e.getCourse().split("\\.").length != 3)
				malformedCourses.add(e.getCourse());
		}
		
		return transEntries.values().toArray(new TransformedEntry[0]);
	}
}
