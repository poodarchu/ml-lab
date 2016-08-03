import java.util.Comparator;
import java.util.TreeSet;

/**
 * Stores student information, where each student is its own entry as such:
 * sid | major | [year | term | dept | cnum | sect | prof | grade]...
 * Can store as many classes as you want
 * 
 * NOTE - Does NOT sort Courses yet!
 * 
 * @author Daniel
 *
 */
public class TransformedEntry {
	
	private class Course {
		private int year;
		private String term;
		private String dept;
		private String cnum;
		private String sect;
		private String prof;
		private String grade;
		
		public Course(int year, String term, String dept, String cnum,
					String sect, String prof, String grade) {
			this.year = year;
			this.term = term;
			this.dept = dept;
			this.cnum = cnum;
			this.sect = sect;
			this.prof = prof;
			this.grade = grade;
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

		public String getProf() {
			return prof;
		}

		public String getSect() {
			return sect;
		}

		public String getTerm() {
			return term;
		}

		public int getYear() {
			return year;
		}
	}
	
	/**
	 * Comparator for the purposes of sorting classes
	 * @author Daniel
	 *
	 */
	private class CourseComparator implements Comparator<Course> {
		/**
		 * Here is how it sorts between two courses (Assume lexicographical sorting):
		 * 1. Sorts on dept
		 * 2. Sorts on cnum
		 * 3. Sorts on sect
		 * 4. Sorts on year
		 * 5. Sorts on term
		 * 6. Sorts on prof
		 * 7. Sorts on grade (it should NEVER get here)
		 */
		public int compare(Course a, Course b) {
			//This try-catch clause is here just to protect against
			//unexpected errors with this sorting method.  Better to
			//catch them now rather than later.
			try {
				if(a.getDept().compareTo(b.getDept()) != 0)
					return a.getDept().compareTo(b.getDept());
				if(a.getCnum().compareTo(b.getCnum()) != 0)
					return a.getCnum().compareTo(b.getCnum());
				if(a.getSect().compareTo(b.getSect()) != 0)
					return a.getSect().compareTo(b.getSect());
				if(a.getYear() != b.getYear())
					return a.getYear() - b.getYear();
				if(a.getTerm().compareTo(b.getTerm()) != 0)
					return a.getTerm().compareTo(b.getTerm());
				if(a.getProf().compareTo(b.getProf()) != 0)
					return a.getProf().compareTo(b.getProf());
				if(a.getGrade().compareTo(b.getGrade()) != 0)
					return a.getGrade().compareTo(b.getGrade());

				return 0;
			}
			catch(Exception e) {
				System.out.println("Something sad happened when comparing two Course objects:\n" + e);
				System.out.println("Here are the two courses: ");
				System.out.println("Course a: " + a.getYear() + "/" + a.getTerm() + '\t' 
						+ a.getDept() + '.' + a.getCnum() + '.'+ a.getSect() + '\t' 
						+ a.getProf() + '\t' + a.getGrade());
				System.out.println("Course b: " + b.getYear() + "/" + b.getTerm() + '\t' 
						+ b.getDept() + '.' + b.getCnum() + '.'+ b.getSect() + '\t' 
						+ b.getProf() + '\t' + b.getGrade());
				System.out.println("Continuing program...");
				return 0;
			}
		}
	}
	
	private String sid;
	private String major; //Listed as "NONE" if undeclared
	private TreeSet<Course> courses;
	
	public TransformedEntry(String sid, String major) {
		this.sid = sid;
		this.major = major;
		courses = new TreeSet<Course>(new CourseComparator());
	}
	
	/**
	 * Adds another course entry to this person
	 * 
	 * @param year
	 * @param term
	 * @param dept
	 * @param cnum
	 * @param sect
	 * @param prof
	 * @param grade
	 */
	public void addClass(int year, String term, String dept,
						String cnum, String sect, String prof, String grade) {
		courses.add(new Course(year, term, dept, cnum, sect, prof, grade));
	}
	
	/**
	 * @return the number of classes this student has taken
	 */
	public int getNumClasses() {
		return courses.size();
	}

	public String getMajor() {
		return major;
	}

	public String getSid() {
		return sid;
	}
	
	/**
	 * Prints out this record.
	 * 
	 * At the moment, it puts everything on one BIG line.  Perhaps
	 * it would be better as multiple lines?  I leave that up to you.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(sid + '\t' + major + '\t');
		
		for(Course course : courses) {
			sb.append(course.getYear() + "/" + course.getTerm() + '\t' 
						+ course.getDept() + '.' + course.getCnum() + '.' 
						+ course.getSect() + '\t' + course.getProf() + '\t'
						+ course.getGrade() + '\t');
		}
			
		
		return sb.toString();
	}
}
