package transcript.recommender;

import transcript.algorithms.memorybased.FilterAndWeight;
import transcript.memreader.TranscriptMemHelper;

/**
 * Sets up a basic memory-based recommender for transcript data.
 * 
 * It uses correlation with default voting.  Since speed
 * is less of an issue with transcript data, and data is
 * so much sparser, we gain a lot by using this complex
 * method.
 * 
 * @author lewda
 */
public class CorrelationRecommender extends AbstractRecommender {
    
    // The recommender
    private FilterAndWeight faw;

    /**
     * Create a memory-based recommender using the file
     * as the TranscriptMemReader object.
     * @param file the TranscriptMemReader serialized object
     */
    public CorrelationRecommender(String file) {
        tmh = new TranscriptMemHelper(file);
        faw = new FilterAndWeight(tmh, FilterAndWeight.CORRELATION_DEFAULT_VOTING + FilterAndWeight.SAVE_WEIGHTS);
    }
    
    /**
     * Create a memory-based recommender using a TranscriptMemReader object.
     * @param tmh the TranscriptMemReader object
     */
    public CorrelationRecommender(TranscriptMemHelper tmh) {
        this.tmh = tmh;
        faw = new FilterAndWeight(tmh, FilterAndWeight.CORRELATION_DEFAULT_VOTING  + FilterAndWeight.SAVE_WEIGHTS);
    }

    public double recommend(int sid, String course) {
        return faw.recommend(sid, course);
    }
    
    public void resort() {
        super.resort();
        faw.reset();
    }
}