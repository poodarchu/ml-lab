package transcript.data;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * Stores student information, where each student is its own entry as such:
 * sid | major | [year | term | dept | cnum | prof | grade]... 
 * 
 * Can store as many courses on each student as desired.  This can be
 * extremely useful, which is why TransformedEntry is sometimes
 * the preferred method of working with data.
 * 
 * Note that if you want to convert Entry[] into TransformedEntry[], 
 * you need to use Transformer.java.  It can also convert the reverse
 * as well.
 * 
 * @author lewda
 */
public class TransformedEntry {

    //Student information
    private int sid;
    private String major;
    private TreeSet<Course> courses;

    //used for finding out most recently declared major
    private int lastYear;

    /**
     * Creates a new student in the form of a TransformedEntry.
     * Note that it only needs sid and major, since all course
     * information should be added afterwards.
     */
    public TransformedEntry(int sid, String major) {
        this.sid = sid;
        this.major = major;
        courses = new TreeSet<Course>(new CourseComparator());
        lastYear = -1;
    }

    /**
     * Adds another course entry to this student
     */
    public void addClass(int year, String term, String dept, int cnum,
            String prof, String grade) {
        addClass(new Course(year, term, dept, cnum, prof, grade));
    }

    /**
     * Adds another course entry to this student
     */
    public void addClass(Course c) {
        courses.add(c);

        if (lastYear == -1)
            lastYear = c.getYear();
    }

    /**
     * @return the number of classes this student has taken
     */
    public int getNumClasses() {
        return courses.size();
    }

    /**
     * Prints out this record.
     * 
     * I decided to have it print out in Entry style, 
     * since it is easiest to parse Entry data.  Therefore,
     * each course gets its own line - if a student took
     * five courses, the printout takes up five lines.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        DecimalFormat format = new DecimalFormat("000");
        DecimalFormat format2 = new DecimalFormat("00");

        for (Course course : courses) {
            sb.append(sid + "; " + major + "; ");
            sb.append(format2.format(course.getYear()) + "/" + course.getTerm()
                    + "; " + course.getDept() + '.'
                    + format.format(course.getCnum()) + "; " + course.getProf()
                    + "; " + course.getGrade());
            sb.append('\n');
        }

        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * @return the courses this student took
     */
    public TreeSet<Course> getCourses() {
        return courses;
    }

    /**
     * @param courses the courses this student will now have taken
     */
    public void setCourses(TreeSet<Course> courses) {
        this.courses = courses;
    }

    /**
     * @return the student's major
     */
    public String getMajor() {
        return major;
    }

    /**
     * Sets the student's major.
     * 
     * Since a studen'ts major can change, it also asks for
     * the year that the student was the new major.  If past
     * information shows that there is newer major information,
     * it ignores the setMajor command.
     */
    public void setMajor(String major, int year) {
        if (year > lastYear) {
            this.major = major;
            lastYear = year;
        }
    }

    /**
     * @return the sid
     */
    public int getSid() {
        return sid;
    }

    /**
     * @param sid the sid to set
     */
    public void setSid(int sid) {
        this.sid = sid;
    }

    /**
     * Comparator for the purposes of sorting classes
     * 
     * Here is how it sorts between two courses (Assume lexicographical
     * sorting): 
     * 1. Sorts on dept 
     * 2. Sorts on cnum 
     * 3. Sorts on year 
     * 4. Sorts on term 
     * 5. Sorts on prof 
     * 6. Sorts on grade (it should NEVER get here)
     */
    private class CourseComparator implements Comparator<Course> {
        public int compare(Course a, Course b) {
            if (a.getDept().compareTo(b.getDept()) != 0)
                return a.getDept().compareTo(b.getDept());
            else if (a.getCnum() != b.getCnum())
                return a.getCnum() - b.getCnum();
            else if (a.getYear() != b.getYear())
                return a.getYear() - b.getYear();
            else if (a.getTerm().compareTo(b.getTerm()) != 0)
                return a.getTerm().compareTo(b.getTerm());
            else if (a.getProf().compareTo(b.getProf()) != 0)
                return a.getProf().compareTo(b.getProf());
            else if (a.getGrade().compareTo(b.getGrade()) != 0)
                return a.getGrade().compareTo(b.getGrade());
            else
                return 0;
        }
    }
}
