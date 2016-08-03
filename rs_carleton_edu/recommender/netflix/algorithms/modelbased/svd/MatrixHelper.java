package netflix.algorithms.modelbased.svd;

import java.text.*;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;
import cern.colt.matrix.impl.*;

/**
 * Provides utility functions for use with the Colt
 * matrix package. 
 *
 * @author sowellb
 */
public class MatrixHelper {


    /**
     * Prints a two dimensional matrix. 
     *
     * @param  matrix  The DoubbleMatrix2D object to print
     */
    public static void printMatrix(DoubleMatrix2D matrix) {

        String format = "0.000";
        DecimalFormat df = new DecimalFormat(format);

        for(int i = 0; i < matrix.rows(); i++) {
            for(int j = 0; j < matrix.columns(); j++) {
                System.out.print(df.format(matrix.get(i,j)) + " ");  
            }
            System.out.println();
        }
    }

    /**
     * Prints a one dimensional matrix (i.e. a vector). 
     *
     * @param  vector  The DoubleMatrix1D to print
     */
    public static void printMatrix(DoubleMatrix1D vector) {

        String format = "0.000";
        DecimalFormat df = new DecimalFormat(format);

        for(int i = 0; i < vector.size(); i++){
            System.out.print(df.format(vector.get(i)) + " ");
        }
        System.out.println();
    }

    /**
     * Adds two vectors. 
     *
     * @param a First vector to add
     * @param b Second vector to add
     * @throws IllegalArgumentException if a.size() != b.size()
     * @return a + b
     */
    public static DoubleMatrix1D addVectors(DoubleMatrix1D a, DoubleMatrix1D b) 
        throws IllegalArgumentException {

        if(a.size() != b.size())
            throw new IllegalArgumentException();

        DoubleMatrix1D c = new DenseDoubleMatrix1D(a.size());
        
        for(int i = 0; i < a.size(); i++) {
            c.set(i, a.get(i) + b.get(i));
        }
        
        return c;
    }


    /**
     * Subtracts two vectors. 
     *
     * @param a
     * @param b
     * @throws IllegalArgumentException if a.size() != b.size()
     * @return a - b.
     */
    public static DoubleMatrix1D subVectors(DoubleMatrix1D a, DoubleMatrix1D b) 
        throws IllegalArgumentException {

        if(a.size() != b.size())
            throw new IllegalArgumentException();

        DoubleMatrix1D c = new DenseDoubleMatrix1D(a.size());
        
        for(int i = 0; i < a.size(); i++) {
            c.set(i, a.getQuick(i) - b.getQuick(i));
        }
        return c;
    }


    /**
     * Divides a vector by a scalar. 
     *
     * @param a Vector
     * @param b Scalar to divide a by. 
     * @return a / b
     */
    public static DoubleMatrix1D divideVector(DoubleMatrix1D a, double b) {

        DoubleMatrix1D c = new DenseDoubleMatrix1D(a.size());

        for(int i = 0; i < a.size(); i++) {
            c.set(i, a.getQuick(i) / b);
        }

        return c;
    }

    /**
     * Appends the scalar b to the end of vector a. 
     *
     * @param a The vector to append to. 
     * @param b The scalar to append. 
     * @return A vector of size a.size() + 1 containing b as its last entry.
     */
    public static DoubleMatrix1D appendToVector(DoubleMatrix1D a, double b) {
        DoubleMatrix1D c = new DenseDoubleMatrix1D(a.size() + 1);

        for(int i = 0; i < a.size(); i++) {
            c.set(i, a.getQuick(i));
        }
        
        c.set(a.size(), b);
        
        return c;
    }

    /**
     * Appends a new column to the matrix A. 
     *
     * @param A The matrix. 
     * @param B The vector to append to A. 
     * @throws IllegalArgumentException if A.rows() != B.size()
     * @return  A matrix
     */
    public static DoubleMatrix2D appendToMatrix(DoubleMatrix2D A, DoubleMatrix1D B) 
        throws IllegalArgumentException{

        if(A.rows() != B.size())
            throw new IllegalArgumentException(A.rows() + " " + B.size());

        DoubleMatrix2D C = new DenseDoubleMatrix2D(A.rows(), A.columns() + 1);

        for (int row=0; row < A.rows(); row++) {
            for (int column=0; column < A.columns(); column++) {
                C.setQuick(row,column, A.getQuick(row,column));
            }
            
            C.setQuick(row, A.columns(), B.get(row));
        }

        return C;

    }

    /**
     * Adds two matrices. 
     * 
     * @param A First matrix to add. 
     * @param B Second matrix to add. 
     * @throws IllegalArgumentException if A and B are not the same size. 
     * @return A + B
     */
    public static DoubleMatrix2D addMatrices(DoubleMatrix2D A, DoubleMatrix2D B) 
        throws IllegalArgumentException {
        
        if(A.rows() != B.rows() || A.columns() != B.columns())
            throw new IllegalArgumentException();
            
        DoubleMatrix2D C = new DenseDoubleMatrix2D(A.rows(), A.columns());
        
        for (int row=0; row < A.rows(); row++) {
            for (int column=0; column < A.columns(); column++) {
                C.setQuick(row,column, A.getQuick(row,column) + B.getQuick(row,column));
            }
        }
        
        return C;
    }
    

    /**
     * Appends an additional row and column of zeros to the specified 
     * matrix. 
     *
     * @param A The matrix to grow. 
     * @return A matrix with A.rows() + 1 rows and A.columns() + 1 columns. 
     */
    public static DoubleMatrix2D growMatrix(DoubleMatrix2D A) {

        DoubleMatrix2D B = new DenseDoubleMatrix2D(A.rows() + 1, A.columns() + 1);

        for (int row=0; row < A.rows(); row++) {
            for (int column=0; column < A.columns(); column++) {
                B.setQuick(row,column,A.getQuick(row,column));
            }
        }
            
        return B;
    }

    /**
     * Appends an additional column of zeros to the specified 
     * matrix. 
     *
     * @param A The matrix to grow. 
     * @return A matrix with A.rows() rows and A.columns() + 1 columns. 
     */
    public static DoubleMatrix2D appendColToMatrix(DoubleMatrix2D A) {

        DoubleMatrix2D B = new DenseDoubleMatrix2D(A.rows(), A.columns() + 1);

        for (int row=0; row < A.rows(); row++) {
            for (int column=0; column < A.columns(); column++) {
                B.setQuick(row,column,A.getQuick(row,column));
            }
        }

        return B;
    }

//     public static void main(String[] args) {

//         DoubleMatrix2D A = new DenseDoubleMatrix2D(4,4);
//         DoubleMatrix2D B = new DenseDoubleMatrix2D(4,4);

//         for(int i = 0; i < 4; i++) {
//             for(int j = 0; j < 4; j++) {
//                 A.set(i, j, i*j);
//                 B.set(i, j, 2*i*j);
//             }
//         }

//         System.out.println(A);
//         System.out.println(B);

//         System.out.println(addMatrices(A, B));
//         System.out.println(growMatrix(A));
//     }


}