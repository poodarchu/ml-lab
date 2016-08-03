package netflix.algorithms.modelbased.itembased.method;

import netflix.algorithms.modelbased.reader.DataReader;

/**
 * Describes the similarity measure to be used by a ModelBuilder to build an item-based model
 * @author tuladhaa
 *
 */
public interface SimilarityMethod {
    /**
     * Finds the similarity between two movies.
     * @param dataReader DataReader object to use to read the data
     * @param mid1 first movie Id
     * @param mid2 second movie Id
     * @return a double similarity value
     */
    public double findSimilarity(DataReader dataReader, int mid1, int mid2);
    /**
     * Sets the least number of users needed to call two items similar
     * @param numMinUsers
     */
    public void setNumMinUsers(int numMinUsers);
    public double findUserSimilarity(DataReader dataReader, int uid1, int uid2);
    public void setNumMinMovies(int numMinMovies);
}
