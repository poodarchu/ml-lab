package netflix.algorithms.modelbased.itembased;

import netflix.algorithms.modelbased.itembased.method.AdjCosineSimilarityMethod;
import netflix.algorithms.modelbased.itembased.method.SimilarityMethod;
import netflix.algorithms.modelbased.reader.DataReader;
import netflix.algorithms.modelbased.reader.DataReaderFromMem;
import netflix.algorithms.modelbased.writer.SimilarityWriter;
import netflix.algorithms.modelbased.writer.SimilarityWriterToFile;
import netflix.memreader.MemHelper;

public class MovieLensMemItemBasedModelBuilder {


    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            String memHelperFile = "C:\\uabase.dat";
            String outputFile = "C:\\movielens_itemsim_adjcos_a.txt";
            if (args.length > 0)
                memHelperFile = args[0];
            if (args.length > 1)
                outputFile = args[1];
            DataReader movielensDataReader = new DataReaderFromMem(
                    new MemHelper(memHelperFile));

            SimilarityWriter movielensSimWriter = new SimilarityWriterToFile(
                    outputFile);
            SimilarityMethod movielensSimAdjCosineMethod = new AdjCosineSimilarityMethod();
            movielensSimAdjCosineMethod.setNumMinUsers(3);
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
