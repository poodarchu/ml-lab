package netflix.algorithms.memorybased.rectree;

import java.util.*;
import netflix.memreader.*;
import netflix.utilities.*;
import cern.colt.list.*;
import cern.colt.map.*;

public class RecTree2 {

    private MemHelper helper;
    private final int MAX_ITERATIONS = 20;
    private final int PARTION_MAX_SIZE = 200;
    private final int MAX_DEPTH = 2;


    private ArrayList<IntArrayList> finalClusters;
    private OpenIntIntHashMap uidToCluster;

    /**
     * Builds the RecTree and saves the resulting clusters.
     */
    public RecTree2(MemHelper helper) {
        this.helper = helper;
        finalClusters = new ArrayList<IntArrayList>();
        uidToCluster = new OpenIntIntHashMap();
    }


    public void cluster() {
        finalClusters = constructRecTree(helper.getListOfUsers(), 
                                         0, 
                                         helper.getGlobalAverage());

        IntArrayList cluster;

        for(int i = 0; i < finalClusters.size(); i++) {
            cluster = finalClusters.get(i);

            for(int j = 0; j < cluster.size(); j++) {
                uidToCluster.put(cluster.get(j), i);
            }
        }
    }


    /**
     * Gets the specified cluster by its positional id. 
     * @return  The cluster at location id in the clusters list.
     */
    public IntArrayList getClusterByID(int id) {
        return finalClusters.get(id);
    }

    /**
     * Gets the id for the cluster containing the specified
     * user. 
     * @return  The location of the cluster containing
     *          the specified uid in the clusters list. 
     */
    public int getClusterIDByUID(int uid){
        return uidToCluster.get(uid);
    }

    /**
     * Gets the cluster containing the specified user. 
     * @return  The cluster containing the speficied user. 
     */
    public IntArrayList getClusterByUID(int uid) {
        return finalClusters.get(uidToCluster.get(uid));
    }
   
   

    public ArrayList<IntArrayList> constructRecTree(IntArrayList dataset, 
                                                    int currDepth, 
                                                    double cliqueAverage) {

        ArrayList<IntArrayList> clusters = new ArrayList<IntArrayList>();
        
        if(dataset.size() <= PARTION_MAX_SIZE || currDepth > MAX_DEPTH) {
            clusters.add(dataset);
            return clusters;
        }

        currDepth++;

        ClusterCollection subClusters = kMeans(dataset, 2, cliqueAverage);

        for(int i = 0; i < 2; i++) {
            clusters.addAll(constructRecTree(subClusters.getCluster(i), 
                                             currDepth, subClusters.getAverage(i)));
        }

        return clusters;
    }


    public ClusterCollection kMeans(IntArrayList dataset, int k, 
                                    double cliqueAverage) {

        int count = 0, newCluster = -1, point;
        boolean converged = false;
        OpenIntIntHashMap clusterMap = new OpenIntIntHashMap();

        //Initialize the centroids as k random points in the dataset.
        Centroid[] centroids = chooseRandomCentroids(dataset, k);


//        Centroid[] centroids = new Centroid[2];
//        centroids[0] = new Centroid(1, helper);
//        centroids[1] = new Centroid(6, helper);

//        System.out.println("centroid 0 from: " + centroids[0].startingUid);
//        System.out.println("centroid 1 from: " + centroids[1].startingUid);

        //We can't update the current centroids during each iteration
        //because we need the old values, so we create new Centroid
        //objects to modify. 
        Centroid[] newCentroids;


        //Perform the clustering until the clusters converge or until
        //we reach the maximum number of iterations. 
        while(!converged && count < MAX_ITERATIONS) {

//            System.out.println("count " + count);
            

            converged = true;

            newCentroids = new Centroid[k];
            for(int i = 0; i < k; i++) {
                newCentroids[i] = new Centroid(centroids[i]);
            }

            //For every point in the dataset, find the closest
            //centroid. If this centroid is different from the 
            //points previously assigned cluster then the 
            //algorithm has not converged. 
            for(int i = 0; i < dataset.size(); i++) {
                
                point = dataset.get(i);

                newCluster = findClosestCentroid(point, centroids, cliqueAverage);

                //This is the first pass through the data. We add
                //the point to the appropriate cluster, and update
                //the new version of that clusters centroid. 
                if(!clusterMap.containsKey(point)) {
                    
                    converged = false;
                    clusterMap.put(point, newCluster);

                    //If the centroid was initialized to this point, we don't 
                    //want to add it again. 
                    if(centroids[newCluster].startingUid != point) {
                        //                      System.out.println("Adding " + point + " to " + newCluster);
                        newCentroids[newCluster].addPoint(point, helper);
                    }
                }


                
                //The point has changed clusters. We add the
                //point to the new cluster and modify the centroid
                //for both the new cluster and the old cluster. 
                else if(clusterMap.get(point) != newCluster) {

//                System.out.println("Current cluster is " + clusterMap.get(i));
//                    System.out.println("moving " + point + " to " + newCluster);

                    newCentroids[clusterMap.get(point)].removePoint(point, helper);

//                    System.out.println("newCluster: " + newCluster);
                    newCentroids[newCluster].addPoint(point, helper);

                    converged = false;
                    clusterMap.put(point, newCluster);
                }
            }
            
            //Replace centroids with newCentroids and 
            //recompute the average for each one. 
            centroids = newCentroids;


            //TEMP: Goes through every point and finds the total distance
            //to the centroids. If everything is working correctly, this 
            //number should never increase. 
            double totalError = 0.0;
            int tempCluster;

            for(int i = 0; i < k; i++) {

                centroids[i].findAverage();

                //              System.out.println("Centroid " + i);
//                centroids[i].printRatings();
//                System.out.println("New average: " + centroids[i].getAverage());
//                System.out.println();
                


            }
            
            for(int i=0; i < dataset.size(); i++) {
                
                point = dataset.get(i);
                tempCluster = clusterMap.get(point);
                totalError += 
                    centroids[tempCluster].distanceWithDefault(point, cliqueAverage, helper);

            }




//            System.out.println("Total Error: " + totalError);
            


            
            //increment count
            count++;

        }

        ClusterCollection clusters = new ClusterCollection(k, helper);
        clusterMap.forEachPair(clusters);
        
        return clusters;
    }



    /**
     * Finds the closest centroid to a specified 
     * user. 
     *
     * @param  uid  The user to find a centroid for.
     * @param  centroids  The list of centroids. 
     * @retrun The index of the closest centroid to uid. 
     */
    private int findClosestCentroid(int uid, Centroid[] centroids, 
                                    double cliqueAverage) {

        double distance;
        double min = -1.0;
        int minIndex = -1;

        for(int i = 0; i < centroids.length; i++) {
            distance = centroids[i].distanceWithDefault(uid, cliqueAverage, helper);

//                 System.out.println("distance from " + uid + " to cluster " + i + " is " + distance);

            if(Math.abs(distance) > min) {
                min = distance;
                minIndex = i;
            }
        }
        
        return minIndex;
    }


    /**
     * Randomly chooses k users to serve as intial centroids for 
     * the kMeans algorithm. 
     *
     * @param  dataset  The list uids. 
     * @param  k  The number of centroids (clusters) desired. 
     * @return A List of randomly chosen centroids. 
     */
    private Centroid[] chooseRandomCentroids(IntArrayList dataset, int k) {

        Random rand = new Random();
        Centroid[] centroids = new Centroid[k];

        for(int i = 0; i < k; i++) {
            centroids[i] = new Centroid(rand.nextInt(dataset.size()), helper);
        }

        return centroids;
    }
}