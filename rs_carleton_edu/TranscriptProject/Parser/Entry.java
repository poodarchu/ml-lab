
/**
 * Stores one row (or entry), formatted thus:
 * student id | term | course | prof | grade | major
 * 
 * @author Daniel
 *
 */
public class Entry {
	private String sid;    //Should *always* be shown (check?)
	private String term;   //Should *always* be shown (check?  if it is not there, make it "NONE/NONE")
	private String course; //If not shown, should be "NONE.000.00
	private String prof;  //If not shown, should be "NONE, "
	private String grade; //If not shown, should be "NONE"
	private String major; //If undeclared, value should be "NONE"

	public Entry(String sid, String term, String course, 
			String prof, String grade, String major) {
		this.sid = sid;
		this.term = term;
		this.course = course;
		this.prof = prof;
		this.grade = grade;
		this.major = major;
	}

	public String toString() {
		return sid + "\t" + term + "\t" + course 
			+ "\t" + grade + "\t" + prof + "\t" + major;
	}

	public String getCourse() {
		return course;
	}

	public String getGrade() {
		return grade;
	}

	public String getMajor() {
		return major;
	}

	public String getProf() {
		return prof;
	}

	public String getSid() {
		return sid;
	}

	public String getTerm() {
		return term;
	}
}
