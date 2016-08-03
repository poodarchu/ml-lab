package transcript.algorithms.modelbased.svd;

import java.util.*;
import java.io.*;
import transcript.memreader.*;
import transcript.utilities.*;
import cern.colt.list.*;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;
import cern.colt.matrix.impl.*;

/**
 * This class uses the colt library's SingularValueDecomposition class
 * to create and serialize an SVD object for the transcript data. The
 * program takes 4 arguments: 
 *      1) The number of courses.  
 *      2) The number of students.  
 *      3) The file containing the TranscriptMemReader. 
 *      4) Fhe file to write the SVD to.  
 * The program requires that the number of movies and number of users 
 * be explicitely input to allow for both 0 and 1 indexed datasets.
 *
 * @author sowellb
 */
public class SVDBuilder {

    Hashtable<String, Integer> nameToNum;
    Hashtable<Integer, String> numToName;

    public SVDBuilder() {
        
        try {

            FileInputStream fis = new FileInputStream("nameToNum.dat");
            ObjectInputStream in = new ObjectInputStream(fis);

            nameToNum = (Hashtable<String,Integer>) in.readObject();


            fis = new FileInputStream("numToName.dat");
            in = new ObjectInputStream(fis);

            numToName = (Hashtable<Integer,String>) in.readObject();


        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Builds and serializes the SVD. 
     *
     * @param  numCourses  The number of courses in the dataset.
     * @param  newStudents  The number of students in the dataset.
     * @param  datFile  The file containing the serialized TMH.
     * @param  destFile  The file to write the SVD to. 
     */
    public void makeSVD(int numCourses, int numStudents, 
                        String datFile, String destFile) {
   
        try{
            TranscriptMemHelper helper = new TranscriptMemHelper(datFile);
            double[][] data = new double[numStudents][numCourses];
            int rating;

            IntArrayList students = helper.getListOfStuds();
    
            for(int i = 0; i < numStudents; i++) {
                for(int j = 0; j < numCourses; j++) {

                    if(numToName.get(j) == null) 
                        rating = -1;
                    else
                        rating = helper.getGrade(i, numToName.get(j));

                    if(rating < 0) {
                        data[i][j] = helper.getAvgForCourse(numToName.get(j)) -
                            helper.getAvgForStud(i);
                    }
                    else {
                        data[i][j] = rating - helper.getAvgForStud(i);
                    }

                }
            }

            DenseDoubleMatrix2D matrix =
                (DenseDoubleMatrix2D) DoubleFactory2D.dense.make(data);
    

            SingularValueDecomposition svd = 
                new SingularValueDecomposition(matrix);        

            FileOutputStream fos = new FileOutputStream(destFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(svd);
            os.close();

        }
        catch(IOException e) {
            e.printStackTrace();
        }        

    }

    public static void main(String[] args) {
        int numCourses = Integer.parseInt(args[0]);
        int numStudents = Integer.parseInt(args[1]);
        String datFile = args[2];
        String destFile = args[3];

        SVDBuilder test = new SVDBuilder();
        test.makeSVD(numCourses, numStudents, datFile, destFile);

    }
}
