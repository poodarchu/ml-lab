package netflix.algorithms.memorybased.database;

/**
 * WARNING: THIS CLASS IS DEPRECATED DUE TO THE MUCH-INCREASED SPEED
 * OF MEMREADER ACCESS.  NOTHING HERE IS UP TO DATE AS OF 11/07/06.
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import netflix.db.Database;
import netflix.utilities.Pair;

public class MyDatabase extends Database  {

	protected String usersName;
	
	public MyDatabase() {
		super();
	}
	
	public MyDatabase(String dbName, String ratingsName,
			String moviesName, String usersName) {
		super(dbName, ratingsName, moviesName);
		this.usersName = usersName;
	}

	/**
	 * Gets a rating for a uid and mid
	 * @param uid the user id
	 * @param mid the movie id
	 * @return a rating
	 */
	public int getRatingForUserAndMovie(int uid, int mid){
		int rating = -99;
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT rating FROM " + ratingsName + " "
				+ "WHERE uid = " + uid + " AND mid = " + mid 
				+ ";");
			rs.next();
			rating = rs.getInt(1);
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return rating;
	}
	
	/**
	 * Gets the average rating given to a movie
	 * @param mid the movie id
	 * @return the average rating
	 */
	public double getAverageRatingForMovie(int mid){
		double avgRating = 0;
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT AVG(rating) FROM " + ratingsName + " "
				+ "WHERE mid = " + mid + ";");
			rs.next();
			avgRating = rs.getDouble(1);
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return avgRating;
	}
	
	/**
	 * Gets the average rating given by a user
	 * @param uid the user id
	 * @return the average rating
	 */
	public double getAverageRatingForUser(int uid){
		double avgRating = 0;
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT avgrating FROM " + usersName + " "
				+ "WHERE uid = " + uid + ";");
			rs.next();
			avgRating = rs.getDouble(1);
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return avgRating;
	}
	
	/**
	 * Gets a list of users who have seen a particular movie
	 * @param mid the movie id
	 * @return a list of uids
	 */
	public ArrayList<Integer> getUsersWhoSawMovie(int mid){
		ArrayList<Integer> users = new ArrayList<Integer>();		
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT uid FROM " + ratingsName + " " 
										+ "WHERE mid = " + mid + ";");
			while (rs.next())
				users.add(rs.getInt(1));
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return users;
	}
	
	
	/**
	 * Gets a list of users who have seen a particular movie
	 * @param mid the movie id
	 * @return a list of uids
	 */
	public ArrayList<Integer> getMoviesSeenByUser(int uid){
		ArrayList<Integer> movies = new ArrayList<Integer>();		
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT mid FROM " + ratingsName + " " 
										+ "WHERE uid = " + uid + ";");
			while (rs.next())
				movies.add(rs.getInt(1));
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return movies;
	}
	
	/**
	 * Gets the ratings for all movies seen by a user
	 * @param uid the user id
	 * @return a list of ratings
	 */
	public ArrayList<Integer> getRatingsForMoviesSeenByUser(int uid){
		ArrayList<Integer> users = new ArrayList<Integer>();		
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT rating FROM " + ratingsName + " "
					+ "WHERE uid = " + uid + ";");
			while (rs.next())
				users.add(rs.getInt(1));
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return users;
	}
	
	/**
	 * For two users, it returns all movies they rated in common
	 * @param uid1 the first user id
	 * @param uid2 the second user id
	 * @return common ratings
	 */
	public ArrayList<Pair> getCommonRatings(int uid1, int uid2){
		ArrayList<Pair> list = new ArrayList<Pair>();
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT R1.rating, R2.rating " 
					+ "FROM " + ratingsName + " R1 "
					+ "INNER JOIN " + ratingsName + " R2 " 
					+ "ON R1.mid = R2.mid "
					+ "WHERE R1.uid = " + uid1 + " AND R2.uid = " + uid2 + ";");
			while (rs.next())
				list.add(new Pair(rs.getInt(1), rs.getInt(2)));
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return list;
	}
	
	/**
	 * Returns the number of users in the database
	 * @return the number of users in the database
	 */
	public int getNumUsers() {
		int numUsers = 0;
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + ratingsName + " "
				+ "GROUP BY uid;");
			numUsers = rs.getFetchSize();
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return numUsers;
	}
	
	/**
	 * Gets all movies and the number of times they have been rated
	 * @return all movies and the number of times they have been rated
	 */
	public HashMap<Integer, Integer> getMovieRatingNums() {
		HashMap<Integer, Integer> movies = new HashMap<Integer, Integer>();
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT mid, COUNT(*) FROM " 
					+ ratingsName + " GROUP BY mid;");
			while (rs.next())
				movies.put(rs.getInt(1), rs.getInt(2));
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return movies;
	}
	
	/**
	 * For testing
	 * @param testTable
	 * @return
	 */
	public ArrayList<Pair> getTestingData(String testTable){
		ArrayList<Pair> list = new ArrayList<Pair>();
		try{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT uid, mid " 
						+ "FROM " + testTable + ";");
			while (rs.next())
				list.add(new Pair(rs.getInt(1), rs.getInt(2)));
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return list;
	}
}
