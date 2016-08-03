package netflix.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;
/**
 * @author steinbel
 * @author lewda (slight modifications)
 * Informal tester class for db access.
 */
public class HelloDB{

	public static void main(String[] args){

		try{
			DatabaseExtensionExample db = new DatabaseExtensionExample();
			boolean result = db.openConnection();
			System.out.println("opened successfully? " + result);
			if (result){
				System.out.println("------testing db.queryDB()---------");
				ResultSet rs = db.queryDB("SELECT * FROM movies LIMIT 8;");
				if (rs.next())
					System.out.println(rs.getInt(1));

				System.out.println("------testing db.updateDB()--------");
				int made = db.updateDB("CREATE TABLE trial  (silly int);");
				int insert = db.updateDB("INSERT INTO trial VALUES (3);");
				int gone = db.updateDB("DROP TABLE trial;");
				System.out.println("made " + made + " insert " + insert +
						" gone " + gone);

				System.out.println("-----testing db.getMoviesForUser()----");
				ArrayList<Integer> movies = db.getMoviesForUser(30878);
				//limit to five because there are a lot
				for (int i=0; i<5; i++)
					System.out.println("movie id " + movies.get(i));

				System.out.println("-----testing db.getRatingForUserAndMovie()------");
				System.out.println(db.getRatingForUserAndMovie(30878, 
						movies.get(movies.size()-1)));

				System.out.println("-----testing db.getUsersAndRatingsForMovie()----");	
				TreeMap<Integer, Integer> map = db.getUsersAndRatingsForMovie(1);
				//limit to five again for same reason
				for (int i=0; i<5; i++){
					int key = map.firstKey();
					System.out.println("User " + key + " gave rating "
							+ map.remove(key));
				}
				System.out.println("closed successfully? " + 
						db.closeConnection());
			}

		} catch (SQLException e) {e.printStackTrace();}
	}	

}
