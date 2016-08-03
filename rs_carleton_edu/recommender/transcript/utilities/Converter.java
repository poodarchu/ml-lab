package transcript.utilities;

/**
 * Converts various String portions of transcript data into numbers which are
 * easier to deal with, and back again.
 * 
 * WARNING: THESE CONVERSIONS ARE LOSSY! READ MORE: In an effort to keep this
 * project manageable, some data is purposely tossed aside. In the case of
 * grades, the functions throw out quite a few grades (such as the ones that are
 * placeholders for multi-term courses), and convert SCRNC to letter grades.
 * 
 * LIST OF ALL GRADES EARNED (and what score they get) : 
 * 12: A+ 
 * 12: A 
 * 11: A- 
 * 10: B+ 
 * 9: B 
 * 8: B- 
 * 7: C+ 
 * 6: C 
 * 5: C- 
 * 4: D+ 
 * 3: D 
 * 2: D- 
 * 1: F 
 * 1: DRP (drop)
 * 
 * 9: S (range A through C-, count as B) 
 * 9: S* 
 * 3: CR (range D+ through D -, count as D) 
 * 3: CR* 
 * 1: NC (fail) 
 * 1: NC*
 * 
 * Grades that should be ignored: (Given a -1 for the purposes of ranking) 
 * ATT (summer graduate courses) 
 * CNT (course continued over 2 terms) 
 * CI (integrative exercise until completed) 
 * L (lab) 
 * NONE (no grade given)
 * 
 * @author lewda
 */
public class Converter {
    
    public static final int MAX_GRADE = 12;
    public static final int MIN_GRADE = 1;

    /**
     * Takes a number representation of a grade and changes it
     * to its letter equivalent.
     * 
     * @param newGrade the number representation of a grade
     * @return its letter equivalent
     */
    public static String numToGrade(int newGrade) {
        switch (newGrade) {
            case 12:
                return "A";
            case 11:
                return "A-";
            case 10:
                return "B+";
            case 9:
                return "B";
            case 8:
                return "B-";
            case 7:
                return "C+";
            case 6:
                return "C";
            case 5:
                return "C-";
            case 4:
                return "D+";
            case 3:
                return "D";
            case 2:
                return "D-";
            case 1:
                return "F";
            default:
                return "NONE";
        }
    }

    /**
     * Takes a letter grade and converts it to its number representation.
     * @param grade a letter grade
     * @return its number representation
     */
    public static int gradeToNum(String grade) {
        if (grade.equals("A+"))
            return 12;
        if (grade.equals("A"))
            return 12;
        if (grade.equals("A-"))
            return 11;
        if (grade.equals("B+"))
            return 10;
        if (grade.equals("B"))
            return 9;
        if (grade.equals("B-"))
            return 8;
        if (grade.equals("C+"))
            return 7;
        if (grade.equals("C"))
            return 6;
        if (grade.equals("C-"))
            return 5;
        if (grade.equals("D+"))
            return 4;
        if (grade.equals("D"))
            return 3;
        if (grade.equals("D-"))
            return 2;
        if (grade.equals("F"))
            return 1;
        if (grade.equals("DRP"))
            return 1;
        if (grade.equals("S"))
            return 8;
        if (grade.equals("S*"))
            return 9;
        if (grade.equals("CR"))
            return 3;
        if (grade.equals("CR*"))
            return 3;
        if (grade.equals("NC"))
            return 1;
        if (grade.equals("NC*"))
            return 1;

        // Otherwise, ignore the grade, return -1
        return -1;
    }
}
