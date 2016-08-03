package netflix.utilities;

import java.util.*;

/**
 * This class can be used to profile program execution 
 * by keeping track of the time spent in each method 
 * and the average time per call. To use this class
 * create a global MethodTimer object, and call
 * the start method at the beginning of every method
 * and stop at the end. At the end of executing the 
 * timings can be retrieved using the printTimings 
 * function. 
 *
 * @author Ben Sowell
 */
public class MethodTimer {

    /**
     * The Timing class tracks a single method. It 
     * stores the total amount of time spent in that
     * method as well as the min and max time per 
     * call and the number of calls. 
     */
    class Timing {
        
        public String name;
        public int numCalls;
        public long startTime, totalTime, maxTime, minTime;
        
        /** 
         * Default constructor. Initializes 
         * everything to 0 or null. 
         */
        public Timing() {
            name = null;
            numCalls = 0;
            startTime = totalTime = maxTime = 0;
            minTime = Long.MAX_VALUE;
        }
        
        /**
         * Constructor. Creates a new Timing
         * object for the specified method and 
         * the specified start time. 
         *
         * @param  name  The name of this method. 
         * @param  startTime  The current start time. 
         */
        public Timing(String name, long startTime) {
            this.name = name;
            numCalls = 0;
            this.startTime = startTime;
        }

        /**
         * Adds the time for another method call to
         * this Timing object. Updates the total time
         * and the min and max times for a call. 
         * 
         * @param  endTime  The end time (in ns) for
         *                  current invocation of this
         *                  method. 
         */
        public void addTime(long endTime) {
            numCalls++;
            long time = endTime - startTime;
            totalTime += time;
            if(time > maxTime) {
                maxTime = time;
            }
            
            if(time < minTime) {
                minTime = time;
            }
        }

        /**
         * Returns the average time per call for this method. 
         *
         * @return The average time per call for this method. 
         */
        public double getAvgTime() {
            return (double) totalTime / (double) numCalls;
        }

        /**
         * Returns a String containing the number
         * of times this method has been called, the
         * average time per call, and the min and max
         * time for a call. 
         *
         * @return A String containing details about this 
         *         method. 
         */
        public String formatTiming() {
            return (name + " " + numCalls + " " 
                    + (totalTime/ (long) 1000000) + " " 
                    + (minTime/ (long) 1000000) + " " 
                    + (maxTime/ (long) 1000000) + " " 
                    + (getAvgTime() / (double) 1000000));
        }
    }

    private Hashtable<String, Timing> times;

    /**
     * Default constructor. 
     */
    public MethodTimer(){
        times = new Hashtable<String, Timing>();
    }


    /**
     * Starts a timer for the specfied method. If 
     * this is the first time that the method has
     * been called, a new Timing object is created, 
     * otherwise the existing one is modfied. 
     *
     * @param  methodName  The method to time. 
     */
    public void start(String methodName) {
        long startTime = System.nanoTime();

        if(times.containsKey(methodName)) {
            times.get(methodName).startTime = startTime;
        }
        else {
            times.put(methodName, new Timing(methodName, startTime));
        }
    }

    /**
     * Ends the tiemr for the specified method. 
     *
     * @throws RuntimeException if the specified method name does
     *         not have an associated Timing object. 
     */
    public void stop(String methodName) throws RuntimeException {
        long stopTime = System.nanoTime();

        if(!times.containsKey(methodName)) {
            throw new RuntimeException("Unknown method name.");
        }
        else {
            times.get(methodName).addTime(stopTime);
        }
    }
    
    /**
     * Prints all the timings managed by this MethodTimer. 
     */
    public void printTimes() {
        System.out.println("name numCalls totalTime minTime maxTime avgTime");

        for(String method : times.keySet()) {
            System.out.println(times.get(method).formatTiming());
        }
    }
}
