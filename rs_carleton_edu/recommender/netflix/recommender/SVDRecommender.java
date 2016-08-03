package netflix.recommender;

import java.io.*;
import java.util.*;
import netflix.memreader.*;
import netflix.utilities.*;
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
 * @author sowellb
 */
public class SVDRecommender extends AbstractRecommender {

    private SingularValueDecomposition svd;
    private DoubleMatrix2D P;
    private int k;

    /**
     * Constructor. 
     *
     * @param  memReaderFile  File containing serialized MemReader.
     * @param  svdFile  File containing serialized SVD.
     * @param  k  Number of singular values to use.
     */

    public SVDRecommender(String memReaderFile, String svdFile, int k) {
        this(new MemHelper(memReaderFile), svdFile, k);
    }

    /**
     * Constructor. 
     *
     * @param  mh  MemHelper object for training set. 
     * @param  svdFile  File containing serialized SVD.
     * @param  k Number of singular values to use.
     */
    public SVDRecommender(MemHelper mh, String svdFile, int k) {

        try {
            this.k = k;
            this.mh = mh;

            FileInputStream fis = new FileInputStream(svdFile);
            ObjectInputStream in = new ObjectInputStream(fis);

            svd = (SingularValueDecomposition) in.readObject();

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

            Algebra alg = new Algebra();

            DoubleMatrix2D rootSk = svd.getS().viewPart(0, 0, k, k);
            
            for(int i = 0; i < k; i++) {
                rootSk.set(i,i,Math.sqrt(rootSk.get(i,i)));
            }

            DoubleMatrix2D U = svd.getU();
            DoubleMatrix2D Uk = U.viewPart(0, 0, U.rows(), k).copy();

            DoubleMatrix2D VPrime = alg.transpose(svd.getV());
            DoubleMatrix2D VPrimek = 
                VPrime.viewPart(0, 0, k, VPrime.columns()).copy();

            DoubleMatrix2D left = alg.mult(Uk, rootSk);
            DoubleMatrix2D right = alg.mult(rootSk, VPrimek);

            P = alg.mult(left, right);
    }

    /**
     * Predicts the rating that activeUser will give targetMovie.
     *
     * @param  activeUser  The user.
     * @param  targetMovie  The movie.
     * @param  date  The date the rating was given. 
     * @return The rating we predict activeUser will give to targetMovie. 
     */
    public double recommend(int activeUser, int targetMovie, String date) {
        double entry = P.get(targetMovie, activeUser);
        double prediction = entry + mh.getAverageRatingForUser(activeUser);
        
        if(prediction < 1)
            return 1;
        else if(prediction > 5)
            return 5;
        else
            return prediction;
    }

    /**
     * Tests this method and computes rmse.
     */
/*    public static void main(String[] args) {

        String base = "/Users/bsowell/recommender/movielens/0indexed/uabase.dat";
        String test = "/Users/bsowell/recommender/movielens/0indexed/uatest.dat";
        String svdFile = "/Users/bsowell/recommender/movielens/0indexed/uabase.svd";
        System.out.println("Training set: " + base + ", test set: " + test);
        SVDRecommender svdRec = new SVDRecommender(base, svdFile, 15);
        MemHelper mh = new MemHelper(test);
        System.out.println("RMSE: " + svdRec.testWithMemHelper(mh));
        }*/




}