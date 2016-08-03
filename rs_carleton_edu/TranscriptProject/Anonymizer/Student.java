import java.util.*;


public class Student {

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
		

		public int courseSimilarity(Course otherCourse)
		{
			int simCount = 0;
			
			if(dept.equals(otherCourse.dept) && cnum.equals(otherCourse.cnum))
				simCount++;
			else 
				return 0;
			
			if(year == otherCourse.year)
				simCount++;
			if(term.equals(otherCourse.term))
				simCount++;
			if(prof.equals(otherCourse.prof))
				simCount++;
			if(grade.equals(otherCourse.grade))
				simCount++;
				
			return simCount;
		}
		
		public boolean equals(Object obj) {
			if (obj == null) return false;
	    	if (!this.getClass().equals(obj.getClass())) return false;
			if(courseSimilarity((Course) obj) != 5) return false;
			
			return true;
		}
		
	}
	
	
	private HashMap<String, Course> courses; 
	private String sid;
	private String major; //Listed as "NONE" if undeclared
	
	public Student(String sid, String major) {
		this.sid = sid;
		this.major = major;
		courses = new HashMap<String, Course>();
	}
	
	
	public void addClass(int year, String term, String dept,
						String cnum, String sect, String prof, 
						String grade) {
	
		String key = dept + cnum;
		Course newCourse = new Course(year, term, dept, cnum, sect, prof, grade);
		courses.put(key, newCourse);
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
	
	public Course getCourse(String cid) {
		return courses.get(cid);
	}
	
	public int computeSimilarity(Student otherStudent) {
		int similarity = 0;
		Course otherCourse;
		
		if(major.equals(otherStudent.major))
			similarity++;			
			
		for(String cid : courses.keySet()) {
			otherCourse = otherStudent.getCourse(cid);
			
			if(otherCourse != null) {
				similarity += courses.get(cid).courseSimilarity(otherCourse);
			}			
		}
		
		return similarity;
	}
	
	
	public boolean equals(Object obj) {
		
		if (obj == null) return false;
    	if (!this.getClass().equals(obj.getClass())) return false;
   	
		Student otherStudent = ((Student) obj);
		Course otherCourse;

		if(getNumClasses() != otherStudent.getNumClasses()) return false;

		for(String cid : courses.keySet()) {
			otherCourse = otherStudent.getCourse(cid);
			
			if(otherCourse == null) return false;
			if(!courses.get(cid).equals(otherCourse)) return false;			
			
		}
		
		return true;

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
		
		for(Course course : courses.values()) {
			sb.append(course.getYear() + "/" + course.getTerm() + '\t' 
						+ course.getDept() + '.' + course.getCnum() + '.' 
						+ course.getSect() + '\t' + course.getProf() + '\t'
						+ course.getGrade() + '\t');
		}
			
		
		return sb.toString();
	}
	
		
}
