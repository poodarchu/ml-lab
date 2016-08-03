package netflix.recommender;

import netflix.algorithms.memorybased.memreader.FilterAndWeight;
import netflix.memreader.MemHelper;

/**
 * Sets up a basic memory-based recommender for movie data.
 * 
 * It uses vector similarity with inverse user frequency.  It
 * is a good trade-off between speed (needed for movie recommendations)
 * and quality of recommendations.
 * 
 * @author lewda
 */
public class CorrelationRecommender extends AbstractRecommender {
    
    // The recommender
	private FilterAndWeight faw;

    /**
     * Create a memory-based recommender using the file
     * as the MemReader object.
     * @param file the MemReader serialized object
     */
	public CorrelationRecommender(String memReaderFile) {
        mh = new MemHelper(memReaderFile);
		faw = new FilterAndWeight(mh, FilterAndWeight.CORRELATION);
	}
    
    /**
     * Create a memory-based recommender using a MemReader object.
     * @param file the MemReader serialized objec
     */
    public CorrelationRecommender(MemHelper mh) {
        this.mh = mh;
        faw = new FilterAndWeight(this.mh, FilterAndWeight.CORRELATION);
    }
	
	public double recommend(int uid, int mid, String date) {
        double d = faw.recommend(uid, mid);
        
        if(d > 5.0)
            d = 5.0;
        else if (d < 1.0)
            d = 1.0;
        
		return d;
    }
    
    public void resort() {
        super.resort();
        faw.reset();
    }
}

