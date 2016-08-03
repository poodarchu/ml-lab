package netflix.recommender;
import java.text.DecimalFormat;

import netflix.memreader.MemHelper;

/**
 * Uses a simple global average for recommendations. In
 * other words, predicts that user a will rate movie m
 * with the average rating of all other users who have 
 * rated movie m. 
 *
 * I know that other people have computed this elsewhere, but
 * I wanted to test it out with the MemReader/AbstractRecommender
 * framework to make sure that everything is working well
 */
public class AverageRecommender extends AbstractRecommender {

    
    private MemHelper helper;

    public AverageRecommender(String memReaderFile) {
        helper = new MemHelper(memReaderFile);
    }

    public double recommend(int uid, int mid, String date) {
        return Math.round(helper.getAverageRatingForMovie(mid) * 10) / 10.0;
    }

    public static void main(String[] args) {
		String base = "/recommender/netflix.dat";
		AverageRecommender ga = new AverageRecommender(base);
		DecimalFormat format = new DecimalFormat("000");
		
		for(int i = 1; i < 243; i++) {
			String inFile = "/recommender/qualifying/qualifying_" + format.format(i) + ".txt";
			String outFile = "/recommender/qualifying/avg/results_" + format.format(i) + ".txt";

			ga.recommendFile(inFile, outFile);
		}
    }



}
