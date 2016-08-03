package transcript.data;

import java.io.Serializable;
import java.text.DecimalFormat;

import transcript.utilities.Converter;

/**
 * Stores one row (or entry), formatted thus:
 * sid | term | course | prof | grade | major
 * 
 * Be warned that not all information will necessarily be
 * available.  Only sid, year, and term are guaranteed.
 * 
 * Can be converted to TransformedEntry by using Transformer.
 * The advantage of a TransformedEntry is that each student
 * is their own row, so you can examine all classes a student
 * has taken.  Also, you can convert a TransformedEntry back
 * into a bunch of Entrys by using Transformer.
 * 
 * @author lewda
 */
public class Entry implements Serializable {
    static final long serialVersionUID = 54321;

    //The entry information
    private int sid;
    private int year;
    private String term;
    private String dept;
    private int cnum;
    private String prof;
    private String grade;
    private String major;
    private String fullCourse;

    /**
     * Creates a fully-featured Entry object.
     */
    public Entry(int sid, int year, String term, String dept, int cnum,
            String prof, String grade, String major) {
        this.sid = sid;
        this.year = year;
        this.term = term;
        this.dept = dept;
        this.cnum = cnum;
        this.prof = prof;
        this.grade = grade;
        this.major = major;

        DecimalFormat format = new DecimalFormat("000");
        this.fullCourse = dept + "." + format.format(cnum);
    }

    /**
     * Prints out an entry.
     */
    public String toString() {
        return sid + "; " + major + "; " + year + "/" + term + "; "
                + fullCourse + "; " + prof + "; " + grade;
    }

    /**
     * @return the full course name, i.e. DEPT.###
     */
    public String getFullCourse() {
        return fullCourse;
    }

    /**
     * @return the grade as an int, as given by transcript.utilities.Converter
     */
    public int getGradeAsInt() {
        return Converter.gradeToNum(grade);
    }

    /**
     * @return the cnum
     */
    public int getCnum() {
        return cnum;
    }

    /**
     * @param cnum
     *            the cnum to set
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
     * @param dept
     *            the dept to set
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
     * @param grade
     *            the grade to set
     */
    public void setGrade(String grade) {
        this.grade = grade;
    }

    /**
     * @return the major
     */
    public String getMajor() {
        return major;
    }

    /**
     * @param major
     *            the major to set
     */
    public void setMajor(String major) {
        this.major = major;
    }

    /**
     * @return the prof
     */
    public String getProf() {
        return prof;
    }

    /**
     * @param prof
     *            the prof to set
     */
    public void setProf(String prof) {
        this.prof = prof;
    }

    /**
     * @return the sid
     */
    public int getSid() {
        return sid;
    }

    /**
     * @param sid
     *            the sid to set
     */
    public void setSid(int sid) {
        this.sid = sid;
    }

    /**
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * @param term
     *            the term to set
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
     * @param year
     *            the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }
}
