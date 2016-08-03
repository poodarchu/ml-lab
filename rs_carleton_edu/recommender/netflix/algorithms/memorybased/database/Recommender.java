package netflix.algorithms.memorybased.database;

import java.sql.SQLException;
import java.util.ArrayList;

import netflix.rmse.RMSECalculator;
import netflix.utilities.Pair;

/**
 * Contains main and methods for garnering recommendations.
 * 
 * CURRENT USEAGE:
 * java Recommender <uid> <mid> <database> <ratings> <movies> <users>
 * OR 
 * java Recommender <database> <ratings> <movies> <users> <testtable>
 * 
 * WARNING: THIS CLASS IS DEPRECATED DUE TO THE MUCH-INCREASED SPEED
 * OF MEMREADER ACCESS.  NOTHING HERE IS UP TO DATE AS OF 11/07/06.
 * 
 * @author lewda
 *
 */
public class Recommender {
	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();
		
		if(args.length == 0) {
			System.out.println("Useage: ");
			//System.out.println("java Recommender <uid> <mid> <database> <ratings> <movies> <users>");
			//System.out.println("OR");
			System.out.println("java Recommender <database> <ratings> <movies> <users> <testtable>");
			System.out.println("OR");
			System.out.println("java Recommender <database> <ratings> <movies> <users> <testtable> <options>");
		}
		
		// Process one recommendation for a user
		/*
		if(args.length == 6) {
			Filter f = new Filter(args[2], args[3], args[4], args[5]);
			System.out.println("Using database " + args[2] + ", ratingsName = " 
			                   + args[3] + ", moviesName = "  + args[4]);
			System.out.println("Processing recommendation for user " + args[0] 
			                   + " for movie " + args[1] + "...");
			System.out.println("Recommendation: "
			                   + f.recommend(Integer.parseInt(args[0]),
			                   		 	     Integer.parseInt(args[1])));
		}*/
		
		// Go through a whole test table of recommendations
		// Used with movielens database, primarily
		if(args.length == 5 || args.length == 6) {
			Filter f = new Filter();
			int options = Weight.CORRELATION + Weight.SAVE_WEIGHTS;
			if(args.length == 6)
				options = Integer.parseInt(args[5]);
			
			f = new Filter(args[0], args[1], args[2], args[3], options);

			MyDatabase db = new MyDatabase(args[0], args[4], args[2], args[3]);
			db.openConnection();
			RMSECalculator calc = new RMSECalculator(db, args[4]);
			RMSECalculator calcGlobal = new RMSECalculator(db, args[4]);
			ArrayList<Pair> testSet = db.getTestingData(args[4]);
			double avgTime = 0;
			int avgNum = 0, rec, gAvg;
			long startTime2, endTime2;
			
			
			System.out.println("Using database = " + args[0] + ", ratingsName = " 
			                   + args[1] + ", moviesName = "  + args[2]
			                   + ", testTable = " + args[4]);
			System.out.print("Using ");
			Weight.printOptions(options);
			//System.out.println("0 out of " + size + " entries processed.");
			
			for(Pair pair : testSet) {
				startTime2 = System.currentTimeMillis();
				rec = f.recommend(pair.a, pair.b);
				endTime2 = System.currentTimeMillis();
				gAvg = (int)Math.round(f.getDB().getAverageRatingForMovie(pair.b));
				avgNum++;
				avgTime += (endTime2 - startTime2);
				try {
					calc.add(pair.a, pair.b, rec);
					calcGlobal.add(pair.a, pair.b, gAvg);
				}
				catch (SQLException e) { e.printStackTrace(); }
				
				/*
				System.out.println("Processing recommendation for user " + pair.a 
				        + " for movie " + pair.b + "...");
				System.out.println("Recommendation: "
						+ rec);
				System.out.println("Global average: " + gAvg);
				System.out.println("Actual value: "
						+ db.getRatingForUserAndMovie(pair.a, pair.b));
				System.out.println("Calculation time: " + (endTime2 - startTime2) + " ms");
				System.out.println("Average calc time: " + (avgTime / avgNum) + " ms");
				System.out.println("Current RMSE: " + calc.rmse());
				System.out.println("Current Global RMSE: " + calcGlobal.rmse());
				*/
			}
			
			System.out.println("Final RMSE: " + calc.rmse());
			System.out.println("Final Global RMSE: " + calcGlobal.rmse());
			System.out.println("Final Average calc time: " + (avgTime / avgNum) + " ms");
			
			db.closeConnection();
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Time taken: " + (endTime - startTime) + " ms.");
	}
}
