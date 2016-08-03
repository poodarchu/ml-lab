package netflix.utilities;

/**
 * The Timer227 class provides the means to measure the execution 
 * time of a portion of another Java program. It computes all elapsed
 * times in nanoseconds, though the accuracy is dependent on the 
 * underlying system. 
 *
 * A Timer227 object can be used to time nonconsecutive sections
 * of code. In other words, every time that start and stop are called, 
 * the elapsed time will be added to the currentTime stored by the 
 * Timer227 object. This makes it easy to avoid timing non-essential
 * methods like input and output. The resetTimer method clears the
 * stored currentTime, and the timer can then be used again. 
 *
 * Note: This class requires Java 1.5 for the nanoTime method. 
 *
 * @author Ben Sowell
 */
public class Timer227 
{
    private long startTime, currentTime;


    /**
     * Constructor. Initializes startTime
     * and currentTime. 
     */
    public Timer227() 
    {
        resetTimer();
    }

    /**
     * Starts the timer. 
     */
    public void start()
    {
        startTime = System.nanoTime();
    }
	
    /**
     * Stops the timer and adds
     * the elapsed time to currentTime. 
     */
    public void stop()
    {
        long stopTime = System.nanoTime();
        currentTime = currentTime + (stopTime - startTime);
    }
	
    /** 
     * Resets the timer by setting startTime 
     * and currentTime to 0.
     */
    public void resetTimer()
    {	
        startTime = 0;
        currentTime = 0;
    }

    /**
     * Returns the elapsed time measured in nanoseconds.
     * Though in all cases this method will return a 
     * time in nanoseconds, its accuracy will depend 
     * on the underlying system. 
     *
     * @return  The number of elapsed nanoseconds
     */
    public long getNanoTime()
    {
        return currentTime;
    }

    /**
     * Returns the elapsed time rounded to the nearest 
     * millisecond. 
     *
     * @return  The number of elapsed milliseconds
     */
    public long getMilliTime()
    {
        return currentTime / (long) 1000000;
    }

    /**
     * Returns the elapsed time rounded to the nearest
     * second. 
     *
     * @return  The number of elapsed seconds. 
     */
    public long getTime()
    {	
        return currentTime / (long) 1000000000;
    }
}
