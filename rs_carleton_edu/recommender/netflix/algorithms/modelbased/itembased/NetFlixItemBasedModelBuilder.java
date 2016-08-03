package netflix.algorithms.modelbased.itembased;

import netflix.algorithms.modelbased.itembased.method.AdjCosineSimilarityMethod;
import netflix.algorithms.modelbased.itembased.method.SimilarityMethod;
import netflix.algorithms.modelbased.reader.DataReader;
import netflix.algorithms.modelbased.reader.DataReaderFromMem;
import netflix.algorithms.modelbased.writer.SimilarityWriter;
import netflix.algorithms.modelbased.writer.SimilarityWriterToFile;
import netflix.memreader.MemHelper;

public class NetFlixItemBasedModelBuilder {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            String memHelperFile = "/recommender/netflixNoProbe.dat";
            String outputFile = "/recommender/netflixsimNoProbe.txt";
            if (args.length > 0)
                memHelperFile = args[0];
            if (args.length > 1)
                outputFile = args[1];
            
            DataReader netflixDataReader = new DataReaderFromMem(
                    new MemHelper(memHelperFile));
            SimilarityWriter netflixSimWriter = new SimilarityWriterToFile(
            outputFile);
            SimilarityMethod netflixSimAdjCosMethod = new AdjCosineSimilarityMethod();
            netflixSimAdjCosMethod.setNumMinUsers(3);
            
            ItemBasedModelBuilder netflixModelBuilder = new ItemBasedModelBuilder(
                    netflixDataReader, netflixSimWriter, netflixSimAdjCosMethod);
            netflixModelBuilder.buildModel();

            netflixDataReader.close();
            netflixSimWriter.close();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
