package netflix.algorithms.modelbased.writer;

import netflix.algorithms.modelbased.itembased.DatabaseImpl;

/**
 * A SimilarityWriter that writes to a database
 * @author Amrit Tuladhar
 *
 */
public class SimiliarityWriterToDB implements SimilarityWriter {

    private DatabaseImpl databaseImpl;
    private String similarityTable;

    public SimiliarityWriterToDB(DatabaseImpl databaseImpl,
            String similarityTable) {
        this.databaseImpl = databaseImpl;
        if (!databaseImpl.openConnection()) {
            System.out.println("Could not open database connection.");
            System.exit(1);
        }
        this.similarityTable = similarityTable;
    }

    public void write(int movieId1, int movieId2, double similarity) throws Exception{
        String sql = "INSERT INTO " + similarityTable + "(mid1, mid2, similarity) VALUES(" +
        movieId1 + ", " + movieId2 + ", " + similarity + ");";
        databaseImpl.updateDB(sql);
    }

    public void close() throws Exception {
        this.databaseImpl.closeConnection();
    }
}