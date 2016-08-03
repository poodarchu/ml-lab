package transcript.recommender;

import java.io.*;
import java.util.*;
import transcript.memreader.*;
import transcript.utilities.*;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;
import cern.colt.matrix.impl.*;
import cern.colt.function.*;

/**
 * Recommends movies using the SVD-based technique 
 * described by Sarwar et. al. in "Application of 
 * Dimensionality Reduction in Recommender Systems -
 * A Case Study"
 *
 * Note that this class does not implement the resort() 
 * method in AbstractRecommender. Since the SVD is 
 * precomputed, new users cannot be added to the system.
 *
 */
public class SVDRecommender extends AbstractRecommender {

    private SingularValueDecomposition svd;
    private DoubleMatrix2D P;
    private int k;
    Hashtable<String, Integer> nameToNum;
    
    /**
     * Constructor. 
     *
     * @param  memReaderFile  File containing serialized MemReader.
     * @param  svdFile  File containing serialized SVD.
     * @param  k  Number of singular values to use.
     */

    public SVDRecommender(String memReaderFile, String svdFile, int k) {
        this(new TranscriptMemHelper(memReaderFile), svdFile, k);
    }

    /**
     * Constructor. 
     *
     * @param  mh  MemHelper object for training set. 
     * @param  svdFile  File containing serialized SVD.
     * @param  k Number of singular values to use.
     */
    public SVDRecommender(TranscriptMemHelper tmh, String svdFile, int k) {

        try {
            this.k = k;
            this.tmh = tmh;

            FileInputStream fis = new FileInputStream(svdFile);
            ObjectInputStream in = new ObjectInputStream(fis);

            System.out.println("Loading SVD...");

            svd = (SingularValueDecomposition) in.readObject();

            System.out.println("Loading nameToNum...");

            fis = new FileInputStream("nameToNum.dat");
            in = new ObjectInputStream(fis);
            
            nameToNum = (Hashtable<String, Integer>) in.readObject();

            buildModel();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Computes the recommendation matrix from the 
     * SVD. See the paper "Application of Dimensionality
     * Reduction in Recommender Systems - A Case Study"
     * for more information. 
     */
    private void buildModel() {

        System.out.println("Building model...");

        Algebra alg = new Algebra();

        System.out.println("Finding square root of S...");

        DoubleMatrix2D rootSk = svd.getS().viewPart(0, 0, k, k).copy();
            
        for(int i = 0; i < k; i++) {
            rootSk.set(i,i,Math.sqrt(rootSk.get(i,i)));
        }

        System.out.println("Finding Uk...");
        DoubleMatrix2D U = svd.getU();
        DoubleMatrix2D Uk = U.viewPart(0, 0, U.rows(), k).copy();


        System.out.println("Finding Vk...");
        DoubleMatrix2D VPrime = alg.transpose(svd.getV());
        DoubleMatrix2D VPrimek = 
            VPrime.viewPart(0, 0, k, VPrime.columns()).copy();

//        svd = null;


        System.out.println("Finding P...");
        DoubleMatrix2D left = alg.mult(Uk, rootSk);
        DoubleMatrix2D right = alg.mult(rootSk, VPrimek);

        P = alg.mult(left, right);


        if(P == null) {
            System.out.println("P is null");
        }
        if(nameToNum == null) {
            System.out.println("nameToNum is null");
        }
        if(tmh == null) {
            System.out.println("tmh is null");
        }

    }

    /**
     * Predicts the rating that student sid will give course.
     *
     * @param  sid  The student.
     * @param  course  The course.
     * @return The grade we predict student sid will get in course. 
     */
    public double recommend(int sid, String course) {

        if(P == null) {
            System.out.println("P is null");
        }
        if(nameToNum == null) {
            System.out.println("nameToNum is null");
        }
        if(tmh == null) {
            System.out.println("tmh is null");
        }
        if(course == null) {
            System.out.println("tmh is null");
        }
        if(nameToNum.get(course) == null) {
            System.out.println("nameToNum.get(course) is null");
        }



        double entry = P.get(sid, nameToNum.get(course));
        return (entry + tmh.getAvgForStud(sid));
    }

    /**
     * Tests this method and computes rmse.
     */
    public static void main(String[] args) {

        String base = args[0]; //training.dat
        String test = args[1]; //test.dat
        String svdFile = args[2]; //training.svd

        System.out.println("Training set: " + base + ", test set: " + test);
        SVDRecommender svdRec = new SVDRecommender(base, svdFile, 15);
        TranscriptMemHelper mh = new TranscriptMemHelper(test);
        System.out.println("RMSE: " + svdRec.testWithMemHelper(mh));
    }
}