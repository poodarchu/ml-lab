package netflix.algorithms.modelbased.svd;

import java.util.*;
import java.io.*;
import netflix.memreader.*;
import netflix.utilities.*;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;
import cern.colt.matrix.impl.*;

/**
 * This class uses the colt library's 
 * SingularValueDecomposition class to create
 * and serialize an SVD object for the movielens
 * data. The program takes 4 arguments:
 *     1) The number of movies.
 *     2) The number of users.
 *     3) The file containing the MemReader.
 *     4) the file to write the SVD to. 
 * The program requires that the number of 
 * movies and number of users be explicitely input
 * to allow for both 0 and 1 indexed datasets. 
 *
 * @author sowellb
 */
class SVDBuilder {

    public static void main(String args[]) {

        try {
            
            int numMovies = Integer.parseInt(args[0]);
            int numUsers = Integer.parseInt(args[1]);
            String datFile = args[2];
            String destfile = args[3];

            MemHelper helper = new MemHelper(datFile);
            double[][] data  = new double[numMovies][numUsers];
            int rating;


            for(int i = 0; i < numMovies; i++) {
                for(int j = 0; j < numUsers; j++) {

                    rating = helper.getRating(j, i);

                    //If the user did not rate the movie
                    if(rating == -99) {
                        data[i][j] = helper.getAverageRatingForMovie(i) -
                            helper.getAverageRatingForUser(j);
                    }
                    else {
                        data[i][j] = rating - helper.getAverageRatingForUser(j);
                    }

                }
            }


            DenseDoubleMatrix2D matrix =
                (DenseDoubleMatrix2D) DoubleFactory2D.dense.make(data);
    
            Timer227 timer = new Timer227();

            timer.start();
            SingularValueDecomposition svd = 
                new SingularValueDecomposition(matrix);        

            timer.stop();
            System.out.println("SVD Calculation took: " + timer.getTime());

            FileOutputStream fos = new FileOutputStream(destfile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(svd);
            os.close();

        }
        catch(Exception e) {
            System.out.println("usage: java SVDBuilder numMovies numUsers dataFile destFile");
            e.printStackTrace();
        }


    }
}