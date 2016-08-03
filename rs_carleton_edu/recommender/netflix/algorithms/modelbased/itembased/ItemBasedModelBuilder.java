package netflix.algorithms.modelbased.itembased;

import java.util.Comparator;
import java.util.TreeSet;

import netflix.algorithms.modelbased.itembased.method.SimilarityMethod;
import netflix.algorithms.modelbased.reader.DataReader;
import netflix.algorithms.modelbased.writer.SimilarityWriter;
import netflix.algorithms.modelbased.writer.UserSimKeeper;
import netflix.utilities.IntDoublePair;
import netflix.utilities.Timer227;

/**
 * General class for writing an item-based model builder.
 * @author Amrit Tuladhar
 *
 */
public class ItemBasedModelBuilder {
    DataReader dataReader;
    SimilarityWriter similarityWriter;
    SimilarityMethod similarityMethod;
    int numSimilarItems;
    String fileName;

    public ItemBasedModelBuilder(DataReader dataReader,
            SimilarityWriter similarityWriter,
            SimilarityMethod similarityMethod) {
        this.dataReader = dataReader;
        this.similarityWriter = similarityWriter;
        this.similarityMethod = similarityMethod;
        this.numSimilarItems = 50;
    }

    public ItemBasedModelBuilder(DataReader dataReader,
            SimilarityWriter similarityWriter,
            SimilarityMethod similarityMethod,
            int numSimilarItems) {
        this.dataReader = dataReader;
        this.similarityWriter = similarityWriter;
        this.similarityMethod = similarityMethod;
        this.numSimilarItems = numSimilarItems;
    }
    
    /**
     * @author steinbel
     * Sets the name of the file to which the UserSimKeeper should be serialized if
     * we're working in memory
     * @param name - the filename
     */
    public void setFileName(String name) {
    	this.fileName = name;
    }
    
    //overloaded method added for backwards compatibility - steinbel
    public boolean buildModel() {
    	return buildModel(false, false);
    }
    
    /**
     * @author tuladara
     * Modified by steinbel to work with users.
     * @param inMemory - if the results should be written to a serializable object.
     * @param users - if we're calculating on users instead of movies
     * @return - true on completion
     */
    public boolean buildModel(boolean inMemory, boolean users) {
        Timer227 tim = new Timer227();
        TreeSet<IntDoublePair> similarMovies = new TreeSet<IntDoublePair>(new RatingComparator());
        int numberOfMovies = dataReader.getNumberOfMovies();
        if (users)
        	numberOfMovies = dataReader.getNumberOfUsers();
        int firstMovieId = 1;
        int startMovieId = 1;
        try {
            for (int m=startMovieId; m<=numberOfMovies; m++) {
                similarMovies.clear();
                System.out.print("Building model for " + m + "...");
                tim.start();

                for (int n=firstMovieId; n<=numberOfMovies; n++) {
                    if (m!=n) {
                    	double sim = 0.0;
                    	if (users)
                    		sim = similarityMethod.findUserSimilarity(dataReader, m, n);
                    	else
                    		sim = similarityMethod.findSimilarity(dataReader, m, n);
                        similarMovies.add(new IntDoublePair(n, sim));
                    }
                }
                int count = 1;
                for (IntDoublePair p : similarMovies) {
                  //  if (count > numSimilarItems)
                  //      break;
                    similarityWriter.write(m, p.a, p.b);
                    count++;
                }
                tim.stop();
                System.out.println("done: " + tim.getMilliTime() + " ms");
                tim.resetTimer();
            }

            dataReader.close();
            if (inMemory) {
            	UserSimKeeper.serialize(fileName, (UserSimKeeper) similarityWriter);
            }
            similarityWriter.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    protected class RatingComparator implements Comparator<IntDoublePair> {
        public int compare(IntDoublePair p1, IntDoublePair p2) {
            // Reverse order stored
            if (p1.b <= p2.b) {
                return 1;
            }
            return -1;
        }        
    }
}
