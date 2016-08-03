package netflix.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * An example of how to extend Database.java to work for you!
 * @author steinbel (original code)
 * @author lewda (decoupling)
 *
 */
public class DatabaseExtensionExample extends Database {
	
	/**
	 * @author steinbel
	 * Prints top x rows from the MovieID table.  (Baby method for testing
	 * connection to db.)
	 * @param x - the number of rows to retrieve.
	 */
  	public void printTopXMovies(int x){
		try{
			String query = "USE recommender;";
			Statement stmt = con.createStatement();
			stmt.executeQuery(query);
			query = "SELECT * FROM movie LIMIT " + x + ";";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				System.out.println("movieID " + rs.getInt(1) +
					" name " + rs.getString(3) +
					" release date " + rs.getDate(2).toString());
			}
			stmt.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	/**
	 * @author steinbel
	 * Queries the db for movies seen by a particular user.
	 * @param userID - the id of the user whose movie list we want
	 * @return ArrayList<Integer> the movie ids of the movies this user has seen 
	 */
	public ArrayList<Integer> getMoviesForUser(int userID){
		ArrayList<Integer> movies = new ArrayList<Integer>();		
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT mid FROM " + ratingsName + " "
			+ "WHERE uid = " + userID + ";");
			/* Now we have to get the results out of the ResultSet and
			 * into a more useable format, like an ArrayList.  Since we're
			 * only pulling movies for one user, an ArrayList should fit the
			 * results.
			 */
			while (rs.next())
				movies.add(rs.getInt(1));
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return movies;
	}

	/**
	 * @author steinbel
	 * Gets the rating by the user for a movie.
	 * @param userID - the id of the user in question
	 * @param movieID - the id of the movie in question
	 * @return int the rating of user with userID for movie with movieID
	 *		-99 indicates no rating
	 */
	public int getRatingForUserAndMovie(int userID, int movieID){
		int rating = -99;
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT rating FROM " + ratingsName + " "
				+ "WHERE uid = " + userID + " AND mid = " + movieID 
				+ ";");
			rs.next();
			rating = rs.getInt(1);
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return rating;
	}

	/**
	 * @author steinbel
	 * Returns the ids of users who have rated the movie passed in and the ratings
	 * they gave that movie.
	 * @param movieID - the movie we're searching on
	 * @return TreeMap<Integer, Integer> a list of users and ratings.  The userid is
	 *		the key and the rating is the value in the TreeMap.
	 */
	public TreeMap<Integer, Integer> getUsersAndRatingsForMovie(int movieID){
		TreeMap<Integer, Integer> list = new TreeMap<Integer, Integer>();
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT uid, rating FROM " + ratingsName + " "
				+ "WHERE mid = " + movieID + ";");
			while (rs.next())
				list.put(rs.getInt(1), rs.getInt(2));
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return list;
	}
}
