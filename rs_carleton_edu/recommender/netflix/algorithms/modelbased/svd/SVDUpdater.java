package netflix.algorithms.modelbased.svd;

import java.text.*;

import netflix.memreader.*;

import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.*;

/**
 * This class implements the rank 1 updates to the singular
 * value decomposition described in the paper "Fast Online
 * SVD Revisions for Lightweight Recommender Systems" by 
 * Matthew Brand. Additional methods are provided to interface
 * with the MemReader/MemHelper framework. 
 *
 * @author Ben Sowell
 */
public class SVDUpdater {

    //The number of singular values to retain. 
    private int k;

    private DoubleMatrix2D S, U, Vt, recMatrix;

    //For Matrix Algebra. 
    private Algebra alg;

    /**
     * Constructor. Initializes objects and reduces the SVD to
     * its rank k version. 
     * 
     * @param  helper  The MemHelper object for the training set. 
     * @param  svd  The SingularValueDecomposition object. 
     * @param  k  The number of singular values to keep. 
     */
    public SVDUpdater(SingularValueDecomposition svd,
                       int k) {
        this.k = k;
        alg = new Algebra();


        S = svd.getS().viewPart(0, 0, k, k).copy();
        U = svd.getU().viewPart(0, 0, svd.getU().rows(), k).copy();
        Vt = alg.transpose(svd.getV()).copy();
        Vt = Vt.viewPart(0,0,k,Vt.columns()).copy();
    }

    /**
     * Performs a general rank 1 modification based on the paper "Fast
     * Online SVD Revisions for Lightweight Recommender Systems." 
     * Assuming we have already found the SVD of X = USV*, computes the
     * SVD of X + ab*
     *
     * The less general method addUser is faster and is preferable when
     * adding users. 
     *
     * @param  a  The a vector in the expression X + ab*
     * @param  b  the b vector in the expression X + ab*
     */
    private void rank1Modification(DoubleMatrix1D a, DoubleMatrix1D b) {
        //m = U'a
        DoubleMatrix1D m = alg.mult(alg.transpose(U), a);
        //pVec = a - Um
        DoubleMatrix1D pVec = MatrixHelper.subVectors(a, alg.mult(U, m));
        //p = sqrt(p'p)
        double p = Math.sqrt(alg.mult(pVec, pVec));
        //P = pVec/p
        DoubleMatrix1D P = MatrixHelper.divideVector(pVec, p);
        //n = V'b
        DoubleMatrix1D n = alg.mult(Vt, b);
        //pVec = b - Vn
        DoubleMatrix1D qVec 
            = MatrixHelper.subVectors(b, alg.mult(alg.transpose(Vt), n));
        //p = sqrt(p'p)
        double q = Math.sqrt(alg.mult(qVec, qVec));
        //Q = qVec/q
        DoubleMatrix1D Q = MatrixHelper.divideVector(qVec, q);
        DoubleMatrix2D rhs = alg.multOuter(MatrixHelper.appendToVector(m, p),         
                                           MatrixHelper.appendToVector(n, q),
                                           null);

        rhs = MatrixHelper.addMatrices(MatrixHelper.growMatrix(S), rhs);

        SingularValueDecomposition newSVD = new SingularValueDecomposition(rhs);


        DoubleMatrix2D newU = alg.mult(MatrixHelper.appendToMatrix(U,P), 
                                       newSVD.getU());

        DoubleMatrix2D newS = newSVD.getS();

        DoubleMatrix2D newVt = alg.mult(MatrixHelper.appendToMatrix(
                                            alg.transpose(Vt), Q), newSVD.getV());
        
        newVt = alg.transpose(newVt);

        S = newS;
        U = newU;
        Vt = newVt;
    }



    /**
     * Perfoms the rank 1 modification that is necessary to add
     * a user to the matrix. By removing some of steps that are
     * not necessary in this case, this method is faster than 
     * the general rank1modification method. 
     *
     * @param  user  The user to add, represented as a vector 
     *               of movie ratings. 
     */
    public void addUser(DoubleMatrix1D user) {


        //Append a row of zeros to V
        Vt = MatrixHelper.appendColToMatrix(Vt);

        DoubleMatrix1D b = new DenseDoubleMatrix1D(Vt.columns());

        b.set(b.size() - 1, 1);
        
        //m = U'a
        DoubleMatrix1D m = alg.mult(alg.transpose(U), user);
        //pVec = a - Um
        DoubleMatrix1D pVec = MatrixHelper.subVectors(user, alg.mult(U, m));
        //p = sqrt(p'p)
        double p = Math.sqrt(alg.mult(pVec, pVec));
        DoubleMatrix1D P = MatrixHelper.divideVector(pVec, p);

        DoubleMatrix2D matrix = 
            new DenseDoubleMatrix2D(S.rows() + 1, S.columns() + 1);

        for(int i = 0; i < S.rows(); i++) {
            matrix.set(i, i, S.get(i, i));
        }

        for(int i = 0; i < m.size(); i++) {
            matrix.set(i, S.rows(), m.get(i));
        }

        matrix.set(S.rows(), S.columns(), p);

        SingularValueDecomposition newSVD
            = new SingularValueDecomposition(matrix);


        DoubleMatrix2D newU = alg.mult(MatrixHelper.appendToMatrix(U, P),
                                       newSVD.getU());

        DoubleMatrix2D newS = newSVD.getS();

        DoubleMatrix2D newVt = alg.mult(MatrixHelper.appendToMatrix(
                                            alg.transpose(Vt), b), newSVD.getV());

        newVt = alg.transpose(newVt);

        S = newS;
        U = newU;
        Vt = newVt;
    }


    /**
     * Creates a recommendation matrix from the
     * singular value decomposition using the
     * technique described in the paper "Application
     * of Dimensionality Reduction in Recommender
     * Systems - A Case Study" by Sarwar et al. 
     */
    public void makeRecommendationMatrix() {

         Algebra alg = new Algebra();

         DoubleMatrix2D rootS = S.copy();

         for(int i = 0; i < k; i++) {
             rootS.set(i,i,Math.sqrt(rootS.get(i,i)));
         }
            
         DoubleMatrix2D left = alg.mult(U, rootS);
         DoubleMatrix2D right = alg.mult(rootS, Vt);
         
         recMatrix = alg.mult(left, right);
    }


    /**
     * Returns the rating predicted for movie mid by 
     * user uid. 
     *
     * @param  uid  The user
     * @param  mid  The movie
     * @return Predicted rating for movie mid by user uid.
     */
    public double recommend(int uid, int mid) {
        double entry = recMatrix.get(mid, uid);
        return entry;
    }

}
