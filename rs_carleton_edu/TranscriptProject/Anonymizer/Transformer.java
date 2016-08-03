import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Transforms data from one format to another.
 * 
 * Warning: it is more than meets the eye.
 * 
 * @author Daniel
 *
 */
public class Transformer {

	/**
	 * Takes an array of Entry objects and transforms it
	 * so that each student takes up one line.
	 * 
	 * @param entries
	 * @return
	 */
	public static HashSet<Student> transformAndGatherData(Entry[] entries) {
		HashMap<String, Student> transEntries = new HashMap<String, Student>();

		for(Entry e : entries) {
			//First, add the entry or class
			if(transEntries.containsKey(e.getSid())) {
				//Add to the existing entry
				transEntries.get(e.getSid()).addClass(e.getYear(), e.getTerm(),
						e.getDept(), e.getCnum(), e.getSect(),
						e.getProf(), e.getGrade());
			}
			else {
				//Make a new entry
				Student te = new Student(e.getSid(), e.getMajor());
				te.addClass(e.getYear(), e.getTerm(), e.getDept(), e.getCnum(), 
						e.getSect(), e.getProf(), e.getGrade());
				transEntries.put(e.getSid(), te);
			}
		}

		return new HashSet<Student>(transEntries.values());
	}
	
	/**
	 * Removes all the students who have only taken one class
	 * @param entries
	 * @return
	 */
	public static HashSet<Student> removeSingles(HashSet<Student> entries) {
		HashSet<Student> temp = new HashSet<Student>();
		
		for(Student te : entries)
			if(te.getNumClasses() > 1)
				temp.add(te);
		
		return temp;
	}
}
