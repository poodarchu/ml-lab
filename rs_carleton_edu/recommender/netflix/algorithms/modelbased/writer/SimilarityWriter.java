package netflix.algorithms.modelbased.writer;

/**
 * An interface to describe a way of writing similarity tables
 * @author Amrit Tuladhar
 *
 */
public interface SimilarityWriter {
    /**
     * Writes a similarity value for two movies
     * @param movieId1
     * @param movieId2
     * @param similarity
     * @throws Exception
     */
    public void write(int movieId1, int movieId2, double similarity) throws Exception;
    /**
     * Closes the writer
     * @throws Exception
     */
    public void close() throws Exception;
}
