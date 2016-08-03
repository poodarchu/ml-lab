package netflix.algorithms.memorybased.rectree;

import java.util.*;
import netflix.memreader.*;
import netflix.utilities.*;
import cern.colt.list.*;
import cern.colt.map.*;
import cern.colt.function.*;

/**
 * Class to convert a hashtable from uid to cluster 
 * to an array of IntArrayLists representing each 
 * cluster. We make this a separate class so that 
 * it can be used with the forEachPair method in the 
 * OpenIntIntHashMap class in the colt package. 
 * Cerns code to iterate through a hashtable is 
 * almost certainly faster than mine would be. 
 *
 * @author Ben Sowell
 */
public class ClusterCollection implements IntIntProcedure {
        
    private ArrayList<IntArrayList> clusters;
    private int[] count;
    private double[] sum;
    private MemHelper helper;

    /**
     *
     * @param  k  Number of clusters. 
     */
    public ClusterCollection(int k, MemHelper helper) {
        clusters = new ArrayList<IntArrayList>(k);
        count = new int[k];
        sum = new double[k];
        this.helper = helper;

        for(int i = 0; i < k; i++) {
            clusters.add(new IntArrayList());
            count[i] = 0;
            sum[i] = 0.0;
        }
    }
    
    public ClusterCollection(ArrayList<IntArrayList> clusters, MemHelper helper) {
        this.helper = helper;
        this.clusters = clusters;

    }


    public ArrayList<IntArrayList> getClusters() {
        return clusters;
    }
    
    public IntArrayList getCluster(int cluster) {
        return clusters.get(cluster);
    }

    public double getAverage(int cluster) {
        return sum[cluster] / count[cluster];
    }

    public int size() {
        return clusters.size();
    }

    public int getClusterSize(int cluster) {
        return clusters.get(cluster).size();
    }


    /**
     * Adds user first to cluster second. 
     *
     * @return  true (not used).
     */
    public boolean apply(int first, int second) {
        clusters.get(second).add(first);

        sum[second] += helper.getRatingSumForUser(first);
        count[second] += helper.getNumberOfMoviesSeen(first);
            
        return true;
    }


    public void printClusters() {
        
        for(int i = 0; i < clusters.size(); i++) {
            
            System.out.print("Cluster " + i + ": ");
            
            for(int j = 0; j < clusters.get(i).size(); j++) {

                System.out.print(clusters.get(i).get(j) + " ");

            }
            System.out.println();
        }
    }
}
