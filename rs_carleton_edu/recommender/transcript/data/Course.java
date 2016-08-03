package transcript.data;

/**
 * Stores the details of one course.
 * 
 * It is a key component of TransformedEntry, allowing
 * one student to take on many courses.  For more details
 * on its role, check the TransformedEntry javadocs.
 * 
 * @author lewda
 */
class Course {
    private int year;
    private String term;
    private String dept;
    private int cnum;
    private String prof;
    private String grade;

    public Course(int year, String term, String dept, int cnum, String prof,
            String grade) {
        this.year = year;
        this.term = term;
        this.dept = dept;
        this.cnum = cnum;
        this.prof = prof;
        this.grade = grade;
    }
    
    public boolean equals(Course course) {
        if(this.dept.equals(course.getDept()) && this.cnum == course.getCnum())
            return true;
        else
            return false;
    }
    
    public String toString() {
        return year + "/" + term + "; " + dept + "." + cnum + "; " + prof + "; " + grade;
    }

    /**
     * @return the cnum
     */
    public int getCnum() {
        return cnum;
    }

    /**
     * @param cnum the cnum to set
     */
    public void setCnum(int cnum) {
        this.cnum = cnum;
    }

    /**
     * @return the dept
     */
    public String getDept() {
        return dept;
    }

    /**
     * @param dept the dept to set
     */
    public void setDept(String dept) {
        this.dept = dept;
    }

    /**
     * @return the grade
     */
    public String getGrade() {
        return grade;
    }

    /**
     * @param grade the grade to set
     */
    public void setGrade(String grade) {
        this.grade = grade;
    }

    /**
     * @return the prof
     */
    public String getProf() {
        return prof;
    }

    /**
     * @param prof the prof to set
     */
    public void setProf(String prof) {
        this.prof = prof;
    }

    /**
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * @param term the term to set
     */
    public void setTerm(String term) {
        this.term = term;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

}
