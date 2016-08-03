package netflix.algorithms.memorybased.memreader;

import netflix.memreader.MemHelper;
import netflix.rmse.RMSECalculator;
import cern.colt.list.IntArrayList;

/**
 * Dan's personal testing class.  Not recommended for people
 * who are not Dan.
 * 
 * @author lewda
 *
 */
public class Recommender {
    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        if (args.length != 3) {
            System.out.println("Useage: ");
            System.out
                    .println("java Recommender <memreader> <memtest> <options>");
            System.exit(1);
        }

        //Load memreader base, test files
        System.out.println("Base: " + args[0] + "; Test: " + args[1]);
        MemHelper mh = new MemHelper(args[0]);
        MemHelper testmh = new MemHelper(args[1]);
        
        //Load FAW
        FilterAndWeight f = new FilterAndWeight(mh, Integer.parseInt(args[2]));
        System.out.print("Using ");
        FilterAndWeight.printOptions(Integer.parseInt(args[2]));

        //Start up RMSE count
        RMSECalculator rmse = new RMSECalculator();
        RMSECalculator movrmse = new RMSECalculator();
        RMSECalculator usermse = new RMSECalculator();

        // For each user, make recommendations
        IntArrayList users = testmh.getListOfUsers(), movies;
        double rating, movavg, useavg;
        int uid, mid, actual;

        for (int i = 0; i < users.size(); i++) {
            uid = users.getQuick(i);
            movies = testmh.getMoviesSeenByUser(uid);

            for (int j = 0; j < movies.size(); j++) {
                mid = MemHelper.parseUserOrMovie(movies.getQuick(j));
                actual = testmh.getRating(uid, mid);
                rating = f.recommend(uid, mid);
                movavg = mh.getAverageRatingForMovie(mid);
                useavg = mh.getAverageRatingForUser(uid);

                if (rating < 0)
                    rating = useavg;
                if (rating > 5)
                    rating = 5;
                else if (rating < 1)
                    rating = 1;

                rmse.add(actual, rating);
                movrmse.add(actual, movavg);
                usermse.add(actual, useavg);

                //                System.out.println("Processing user " + uid + " for movie "
                //                        + mid + "...");
                //                System.out.println("Actual: " + testmh.getRating(uid, mid)
                //                        + "; Rec: " + rating + "; MovGlobal: " + movavg
                //                        + "; UserGlobal: " + useavg);
                //                System.out.println("RMSE: " + rmse.rmse());
            }
        }

        //Print results
        long endTime = System.currentTimeMillis();
        System.out.println();
        System.out.println("Final RMSE: " + rmse.rmse());
        System.out.println("Final Movie Avg RMSE: " + movrmse.rmse());
        System.out.println("Final User Avg RMSE: " + usermse.rmse());
        System.out.println("Total time taken: " + (endTime - startTime)
                + " ms.");

        System.out.println(rmse.rmse() + " (" + (endTime - startTime) + " ms)");
    }
}

/*
 Results (run by Dan)

 FINAL RESULTS
 GLOBAL: ua = 1.0427, ub = 1.0498
 CORR: ua = 0.9556 (0.9536, 0.2%), ub = 0.9721 (0.9694, 0.3%)
 CORR_DEF: ua = 0.9679 (0.9639, 0.4%), ub = 0.9854  (0.9824, 0.3%)
 VS: ua = 0.9726 (0.9680, 0.5%), ub = 0.9890 (0.9849, 0.4%)
 VS_IUF: ua = 0.9694 (0.9653, 0.4%), ub = 0.9855 (0.9812, 0.4%)
 
 % increase when using case amplification tests
 CORR
 1.0: 0.9556/0.9721
 1.1: 0.9549/0.9714
 1.2: 0.9544/0.9706
 1.3: 0.9540/0.9701
 1.4: 0.9537/0.9697
 1.5: 0.9536/0.9694 - Optimal
 1.6: 0.9537/0.9694
 1.7: 0.9538/0.9694
 1.8: 0.9541/0.9695
 1.9: 0.9545/0.9697
 2.0: 0.9550/0.9701
 2.1: 0.9556/0.9706
 2.2: 0.9562/0.9712
 2.3: 0.9573/0.9719
 2.4: 0.9582/0.9727
 2.5: 0.9592/0.9736
 2.6: 0.9602/0.9745
 2.7: 0.9614/0.9756
 2.8: 0.9626/0.9768
 2.9: 0.9640/0.9780
 3.0: 0.9654/0.9793
 3.1: 0.9668/0.9807
 3.2: 0.9682/0.9821
 3.3: 0.9698/0.9837
 3.4: 0.9714/0.9853
 3.5: 0.9730/0.9871
 3.6: 0.9747/0.9888
 3.7: 0.9765/0.9905
 3.8: 0.9782/0.9923
 3.9: 0.9800/0.9941
 4.0: 0.9823/0.9959
 4.1: 0.9841/0.9977
 4.2: 0.9860/0.9998
 4.3: 0.9879/1.0017
 4.4: 0.9899/1.0037
 4.5: 0.9918/1.0056
 4.6: 0.9938/1.0075
 4.7: 0.9957/1.0094
 4.8: 0.9976/1.0113
 4.9: 0.9996/1.0127
 5.0: 1.0015/1.0147
   
 CORR_DEF
 1.0: 0.9679/0.9854
 1.1: 0.9673/0.9848
 1.2: 0.9668/0.9843
 1.3: 0.9664/0.9839
 1.4: 0.9660/0.9835
 1.5: 0.9657/0.9832
 1.6: 0.9653/0.9829
 1.7: 0.9650/0.9827
 1.8: 0.9648/0.9825
 1.9: 0.9645/0.9824
 2.0: 0.9643/0.9823
 2.1: 0.9641/0.9823
 2.2: 0.9640/0.9823 - Optimal
 2.3: 0.9639/0.9824
 2.4: 0.9639/0.9825
 2.5: 0.9638/0.9826 - Optimal
 2.6: 0.9639/0.9828
 2.7: 0.9639/0.9830
 2.8: 0.9640/0.9832
 2.9: 0.9641/0.9835
 3.0: 0.9643/0.9838
 3.1: 0.9645/0.9841
 3.2: 0.9647/0.9842
 3.3: 0.9650/0.9846
 3.4: 0.9653/0.9851
 3.5: 0.9656/0.9856
 3.6: 0.9660/0.9860
 3.7: 0.9664/0.9865
 3.8: 0.9668/0.9872
 3.9: 0.9673/0.9877
 4.0: 0.9678/0.9883
 4.1: 0.9683/0.9888
 4.2: 0.9688/0.9888
 4.3: 0.9695/0.9894
 4.4: 0.9700/0.9900
 4.5: 0.9706/0.9907
 4.6: 0.9713/0.9914
 4.7: 0.9719/0.9921
 4.8: 0.9726/0.9929
 4.9: 0.9735/0.9936
 5.0: 0.9742/0.9944
  
 VS
 1.0: 0.9726/0.9890
 1.1: 0.9721/0.9884
 1.2: 0.9716/0.9879
 1.3: 0.9711/0.9875
 1.4: 0.9707/0.9871
 1.5: 0.9703/0.9867
 1.6: 0.9699/0.9864
 1.7: 0.9696/0.9861
 1.8: 0.9693/0.9858
 1.9: 0.9690/0.9856
 2.0: 0.9688/0.9854
 2.1: 0.9686/0.9852
 2.2: 0.9684/0.9851
 2.3: 0.9682/0.9850
 2.4: 0.9681/0.9849
 2.5: 0.9680/0.9849 - OPTIMAL
 2.6: 0.9680/0.9849 - OPTIMAL
 2.7: 0.9680/0.9849 - OPTIMAL
 2.8: 0.9679/0.9850
 2.9: 0.9680/0.9850
 3.0: 0.9680/0.9851
 3.1: 0.9681/0.9853
 3.2: 0.9682/0.9854
 3.3: 0.9683/0.9856
 3.4: 0.9685/0.9858
 3.5: 0.9687/0.9860
 3.6: 0.9689/0.9863
 3.7: 0.9691/0.9865
 3.8: 0.9693/0.9868
 3.9: 0.9696/0.9872
 4.0: 0.9699/0.9875
 4.1: 0.9702/0.9879
 4.2: 0.9705/0.9882
 4.3: 0.9709/0.9886
 4.4: 0.9713/0.9891
 4.5: 0.9717/0.9895
 4.6: 0.9721/0.9900
 4.7: 0.9725/0.9904
 4.8: 0.9729/0.9909
 4.9: 0.9734/0.9914
 5.0: 0.9739/0.9920
 
 VS_IUF
 1.0: 0.9694/0.9855
 1.1: 0.9687/0.9848
 1.2: 0.9681/0.9842
 1.3: 0.9675/0.9836
 1.4: 0.9670/0.9831
 1.5: 0.9666/0.9826
 1.6: 0.9662/0.9822
 1.7: 0.9659/0.9819
 1.8: 0.9656/0.9817
 1.9: 0.9655/0.9815
 2.0: 0.9653/0.9813
 2.1: 0.9653/0.9812 - OPTIMAL
 2.2: 0.9653/0.9812 - OPTIMAL
 2.3: 0.9653/0.9813
 2.4: 0.9654/0.9814
 2.5: 0.9656/0.9815
 2.6: 0.9658/0.9817
 2.7: 0.9661/0.9820
 2.8: 0.9665/0.9823
 2.9: 0.9669/0.9827
 3.0: 0.9673/0.9831
 3.1: 0.9678/0.9836
 3.2: 0.9683/0.9842
 3.3: 0.9689/0.9848
 3.4: 0.9695/0.9854
 3.5: 0.9702/0.9861
 3.6: 0.9709/0.9868
 3.7: 0.9716/0.9876
 3.8: 0.9724/0.9884
 3.9: 0.9732/0.9893
 4.0: 0.9741/0.9902
 4.1: 0.9750/0.9912
 4.2: 0.9759/0.9921
 4.3: 0.9768/0.9932
 4.4: 0.9777/0.9942
 4.5: 0.9788/0.9953
 4.6: 0.9798/0.9964
 4.7: 0.9808/0.9975
 4.8: 0.9818/0.9987
 4.9: 0.9829/0.9999
 5.0: 0.9839/1.0011

 Mo' Stats (a little out of date)

 uabase.dat, uatest.dat
 Final Movie Avg RMSE: 1.0426551486028641
 Final User Avg RMSE: 1.043136049763304

 CORR: 0.9580911220072544 (22329 ms)
 CORR + AMP: 0.9611315740701232 (22672 ms)
 CORR_DEF: 0.9683476957094102 (102266 ms)
 CORR_DEF + AMP: 0.9642022637430787 (102344 ms)
 VS: 0.9726443480742439 (15390 ms)
 VS + AMP: 0.9680442526453057 (16781 ms)
 VS_INVERSE: 0.9693993454781678 (17938 ms)
 VS_INVERSE + AMP: 0.9656094320071958 (18719 ms)
 
 ubbase.dat, ubtest.dat
 Final Movie Avg RMSE: 1.049805311209651
 Final User Avg RMSE: 1.0603832330289955
 
 CORR: 0.9744091921571956 (22078 ms)
 CORR + AMP: 0.9760577702313714 (22562 ms)
 CORR_DEF: 0.9845585932655959 (99172 ms)
 CORR_DEF + AMP: 0.9811092776278237 (100328 ms)
 VS: 0.9889517701691805 (15078 ms)
 VS + AMP: 0.9849120577994519 (16454 ms)
 VS_INVERSE: 0.9855307773042519 (17343 ms)
 VS_INVERSE + AMP: 0.9815064545214766 (19265 ms)
 */
