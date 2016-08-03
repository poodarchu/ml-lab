package netflix.algorithms.modelbased.itembased;

import netflix.algorithms.modelbased.itembased.method.AdjCosineSimilarityMethod;
import netflix.algorithms.modelbased.itembased.method.SimilarityMethod;
import netflix.algorithms.modelbased.reader.DataReader;
import netflix.algorithms.modelbased.reader.DataReaderFromDB;
import netflix.algorithms.modelbased.writer.SimilarityWriter;
import netflix.algorithms.modelbased.writer.SimilarityWriterToFile;

public class MovieLensDBItemBasedModelBuilder {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            DataReader movielensDataReader = new DataReaderFromDB(
                    new DatabaseImpl("movielens", "ratings", "movies", "users"));
            SimilarityWriter movielensSimWriter = new SimilarityWriterToFile(
            "/home/tuladhaa/movielens_pearson_item_sim.txt");
            SimilarityMethod movielensSimAdjCosineMethod = new AdjCosineSimilarityMethod();

            ItemBasedModelBuilder movielensModelBuilder = new ItemBasedModelBuilder(
                    movielensDataReader, movielensSimWriter, movielensSimAdjCosineMethod);
            movielensModelBuilder.buildModel();

            movielensDataReader.close();
            movielensSimWriter.close();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
