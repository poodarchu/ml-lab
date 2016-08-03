import java.util.HashSet;

/**
 * Stores student information, where each student is its own entry as such:
 * sid | major | [term | course | prof | grade] [term | course | prof | grade]...
 * Can store as many classes as you want
 * 
 * @author Daniel
 *
 */
public class TransformedEntry {
	
	private class OtherStuff {
		private String term;
		private String course;
		private String prof;
		private String grade;
		
		public OtherStuff(String term, String course, String prof, String grade) {
			this.term = term;
			this.course = course;
			this.prof = prof;
			this.grade = grade;
		}

		public String getCourse() {
			return course;
		}

		public String getGrade() {
			return grade;
		}

		public String getProf() {
			return prof;
		}

		public String getTerm() {
			return term;
		}
	}
	
	private String sid;
	private String major; //Listed as "NONE" if undeclared
	private HashSet<OtherStuff> otherStuff;
	
	public TransformedEntry(String sid, String major) {
		this.sid = sid;
		this.major = major;
		otherStuff = new HashSet<OtherStuff>();
	}
	
	/**
	 * Adds another class to this entry.
	 * 
	 * @param term
	 * @param course
	 * @param prof
	 * @param grade
	 */
	public void addClass(String term, String course, String prof, String grade) {
		otherStuff.add(new OtherStuff(term, course, prof, grade));
	}
	
	/**
	 * @return the number of classes this student has taken
	 */
	public int getNumClasses() {
		return otherStuff.size();
	}

	public String getMajor() {
		return major;
	}

	public String getSid() {
		return sid;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(sid + "\t" + major + "\t");
		
		for(OtherStuff os : otherStuff)
			sb.append(os.term + "\t" + os.course + "\t" + os.prof + "\t" + os.grade);
		
		return sb.toString();
	}
}
