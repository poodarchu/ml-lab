package netflix.db;

/**
 * This class provides basic db connection and access.
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class Database {

	//class variables
	protected Connection con;
	protected String dbName;
	protected String ratingsName;
	protected String moviesName;

	/**
	 * Default constructor.
	 * 
	 * Sets up a connection to the database "recommender", using
	 * the table name "ratings" for ratings and "movies" for movies.
	 */
	public Database() {
		dbName = "recommender";
		ratingsName = "ratings";
		moviesName = "movies";
	}
	
	/**
	 * More in-depth constructor for database
	 * 
	 * @param dbName 
	 * @param ratingsName 
	 * @param moviesName 
	 */
	public Database(String dbName, String ratingsName, String moviesName) {
		this.dbName = dbName;
		this.ratingsName = ratingsName;
		this.moviesName = moviesName;
	}
	
	/**
	 * @author steinbel - modified from Enchilada
	 * Opens the connection to the MySQL db "recommender".  If password changes
	 * are made, they should be made in here - password and db name are hard-
	 * coded in at present.
	 * @return boolean true on successful connection, false if problems
	 */
	public boolean openConnection(){
		boolean success = false;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://" +
			"localhost:3306/" + dbName, "recommender", "recommender");
			success = true;

		} catch (Exception e){
			System.err.println("Error getting connection.");
			e.printStackTrace();
		}

		return success;
	}

	/**
	 * @author steinbel - lifted from Enchilada
	 * Closes the connection to the db.
	 * @return boolean true on successful close, false if problems
	 */
	public boolean closeConnection(){
		boolean success = false;
		try{
			con.close();
			success = true;
		} catch (Exception e){
			System.err.println("Erorr closing the connection.");
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * @author steinbel - lifted from Enchilada
	 * Returns the connection to the db.
	 * @return Connection con
	 */
	public Connection getConnection(){
		return con;
	}

	/**
	 * @author steinbel
	 * All-purpose method for custom queries.
	 * Given a string containing a query, execute that query in the db.
	 * Return any results in the form of a ResultSet.
	 * @param query - the string containing the well-formed MySQL query
	 * @return ResultSet containing the results of the query or null if error
	 */
	public ResultSet queryDB(String query){
		ResultSet rs = null;
		try{
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			/* NOTE: cannot manually close Statment here or we
			 * lose the ResultSet access.
			 * May want to change this into a CachedRowSet to
			 * deal with that.  Also, what about memory limitations?
			 */
		} catch(SQLException e){ e.printStackTrace(); }
		return rs;
	}

	/**
	 * @author steinbel
	 * All-purpose method for INSERTS, DELETES and other similar SQL updates.
	 * Given a string containing an SQL statement, executes that update in the db.
	 * @param sqlString - the string containing the well-formed MySQL statement
	 * @return int indicating the number of rows affected by the statement
	 *		-99 indicates failure to execute update
	 */
	public int updateDB(String sqlString){
		int rowsAffected = -99;
		try{
			Statement stmt = con.createStatement();
			rowsAffected = stmt.executeUpdate(sqlString);
			stmt.close();
		} catch(SQLException e){ e.printStackTrace(); }
		return rowsAffected;
	}

	/**
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @param dbName the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * @return the moviesName
	 */
	public String getMoviesName() {
		return moviesName;
	}

	/**
	 * @param moviesName the moviesName to set
	 */
	public void setMoviesName(String moviesName) {
		this.moviesName = moviesName;
	}

	/**
	 * @return the ratingsName
	 */
	public String getRatingsName() {
		return ratingsName;
	}

	/**
	 * @param ratingsName the ratingsName to set
	 */
	public void setRatingsName(String ratingsName) {
		this.ratingsName = ratingsName;
	}
}
