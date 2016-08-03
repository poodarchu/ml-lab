
/**
 * Stores one row (or entry), formatted thus:
 * student id | term | course | prof | grade | major
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
public class Entry {
	private String sid; 
	private int year;
	private String term;
	private String dept; 
	private String cnum; //Should be "int", is kept as "String" for hashing
	private String sect; //Should be "int", is kept as "String" for hashing
	private String prof;
	private String grade;
	private String major;

	public Entry(String sid, int year, String term, String dept, String cnum, 
			String sect, String prof, String grade, String major) {
		this.sid = sid;
		this.year = year;
		this.term = term;
		this.dept = dept;
		this.cnum = cnum;
		this.sect = sect;
		this.prof = prof;
		this.grade = grade;
		this.major = major;
	}

	public String toString() {
		return sid + '\t' + year + '/' + term + '\t' 
			+ dept + '.' + cnum + '.' + sect + '\t'
			+ grade + "\t" + prof + "\t" + major;
	}

	public String getCnum() {
		return cnum;
	}

	public String getDept() {
		return dept;
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

	public String getSect() {
		return sect;
	}

	public String getSid() {
		return sid;
	}

	public String getTerm() {
		return term;
	}

	public int getYear() {
		return year;
	}
}
