package netflix.recommender;

import java.text.DecimalFormat;

import netflix.algorithms.memorybased.memreader.OptimizedFilterAndWeight;

/**
 * Warning: don't use this code!
 * 
 * This is some very specific code that is used to
 * run a Netflix prize run.  It ran very long, so
 * this class was made to make my life easier (i.e,
 * not have to worry about command line arguments).
 * 
 * @author lewda
 */
public class OptimizedVSRecommender extends AbstractRecommender {

	private OptimizedFilterAndWeight faw;
	
	public OptimizedVSRecommender(String memReaderFile) {
		faw = new OptimizedFilterAndWeight(memReaderFile);
	}
	
	public double recommend(int uid, int mid, String date) {
		return faw.recommend(uid, mid);
	}
	
	public static void main(String[] args) {		
		String base = "/recommender/netflix.dat";
		OptimizedVSRecommender cr = new OptimizedVSRecommender(base);

		DecimalFormat format = new DecimalFormat("000");

		String inFile = "/recommender/qualifying/qualifying_" + format.format(128) + ".txt";
		String outFile = "/recommender/qualifying/results_" + format.format(128) + ".txt";
		cr.recommendFile(inFile, outFile);
		inFile = "/recommender/qualifying/qualifying_" + format.format(148) + ".txt";
		outFile = "/recommender/qualifying/results_" + format.format(148) + ".txt";
		cr.recommendFile(inFile, outFile);
	}
}
