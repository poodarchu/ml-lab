package transcript.data;

import java.util.Random;
import java.util.TreeSet;

import transcript.utilities.Converter;

/**
 * Perturbs data fed into it.
 * 
 * Range typically means how much deviation can occur. Thus, a range of 1 can
 * add (or subtract) one point.
 * 
 * It will never give someone below an F or above an A.
 * 
 * @author lewda
 */
public class Perturber {
    
    public static void main(String[] args) {
        System.out.println("Perturbing...");
        Entry[] data = Parser.parseTransformedData(args[0]);
        perturb(data, Integer.parseInt(args[2]), true);
        Writer.write(data, args[1]);
        System.out.println("Done!");
    }

    /**
     * Perturbs Entry[] data.
     * 
     * @param data the data to perturb
     * @param range how much to perturb by.  (Each int is a + or -)
     * @param useGaussian if true, uses Gaussian perturbation;
     *                    otherwise, uses uniform perturbation;
     */
    public static void perturb(Entry[] data, int range, boolean useGaussian) {
        Random rand = new Random();
        int a, r, n;

        for (int i = 0; i < data.length; i++) {
            a = Converter.gradeToNum(data[i].getGrade());

            //Only perturb data that is a grade we're counting
            if (a >= 1) {
                if (useGaussian)
                    r = (int) Math.round(rand.nextGaussian() * range);
                else
                    r = (int) Math.round((rand.nextDouble() - .5) * 2 * range);

                n = a + r;
                if (n > 12)
                    n = 12;
                else if (n < 1)
                    n = 1;

                data[i].setGrade(Converter.numToGrade(n));
            }
        }
    }

    /**
     * Perturbs TransformedEntry[] data.
     * 
     * @param data the data to perturb
     * @param range how much to perturb by.  (Each int is a + or -)
     * @param useGaussian if true, uses Gaussian perturbation;
     *                    otherwise, uses uniform perturbation;
     */
    public static void perturb(TransformedEntry[] data, int range,
            boolean useGaussian) {
        Random rand = new Random();
        int a, r, n;
        TreeSet<Course> courses;

        for (int i = 0; i < data.length; i++) {
            courses = data[i].getCourses();

            for (Course c : courses) {
                a = Converter.gradeToNum(c.getGrade());

                //Only perturb data that is a grade we're counting
                if (a >= 1) {
                    if (useGaussian)
                        r = (int) Math.round(rand.nextGaussian() * range);
                    else
                        r = (int) Math.round((rand.nextDouble() - .5) * 2
                                * range);

                    n = a + r;
                    if (n > 12)
                        n = 12;
                    else if (n < 1)
                        n = 1;

                    c.setGrade(Converter.numToGrade(n));
                }
            }
        }
    }
}
