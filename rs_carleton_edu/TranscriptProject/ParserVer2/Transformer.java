import java.util.ArrayList;
import java.util.HashMap;

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
	public static TransformedEntry[] transformAndGatherData(Entry[] entries) {
		HashMap<String, TransformedEntry> transEntries = new HashMap<String, TransformedEntry>();

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
				TransformedEntry te = new TransformedEntry(e.getSid(), e.getMajor());
				te.addClass(e.getYear(), e.getTerm(), e.getDept(), e.getCnum(), 
						e.getSect(), e.getProf(), e.getGrade());
				transEntries.put(e.getSid(), te);
			}
		}

		return transEntries.values().toArray(new TransformedEntry[0]);
	}
	
	/**
	 * Removes all the students who have only taken one class
	 * @param entries
	 * @return
	 */
	public static TransformedEntry[] removeSingles(TransformedEntry[] entries) {
		ArrayList<TransformedEntry> temp = new ArrayList<TransformedEntry>();
		
		for(TransformedEntry te : entries)
			if(te.getNumClasses() > 1)
				temp.add(te);
		
		return temp.toArray(new TransformedEntry[0]);
	}
}
