package netflix.algorithms.modelbased.writer;

import java.io.*;

import netflix.utilities.IntDoublePair;

import cern.colt.map.*;
import cern.colt.list.*;

/**
 * @author leahsteinberg
 * Crazy conglomeration of implementations in order to leverage existing code from 
 * Amrit's work on building the item-item similarity tables as well as in-memory
 * storage and access methods.
 */
public class UserSimKeeper implements SimilarityWriter, Serializable{

	private static final long serialVersionUID = 6475421128517362006L;

	/* An entry in the similarity list looks like: key=userID, 
	 * value = list of <userID, similarity> pairs. 
	 */
	private OpenIntObjectHashMap similarities;
	
	public UserSimKeeper() {
		similarities = new OpenIntObjectHashMap();
	}
	
	/**
	 * Converted to write similarities between USERS - keeps old form to work w/
	 * ItemBasedModelBuilder
	 */
	public void write(int movieId1, int movieId2, double similarity) throws Exception {
		writeUserSim(movieId1, movieId2, similarity);
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @author steinbel
	 * Records the similarity between two users along with their ids.  The similarity
	 * is recorded under the list for the first user only.
	 * @param uid1 - the userID of the first user
	 * @param uid2 - the userID of the second user
	 * @param similarity - the similarity between the two users
	 * @throws Exception
	 */
	public void writeUserSim(int uid1, int uid2, double similarity) throws Exception{
		ObjectArrayList sims;
		if (similarities.containsKey(uid1))
			sims = (ObjectArrayList) similarities.get(uid1);
		else
			sims = new ObjectArrayList();
		
		sims.add(new IntDoublePair(uid2, similarity));
		similarities.put(uid1, sims);
	}
	
	/**
	 * @author steinbel, based on serialize method in memreader.MemReader
	 * Serializes the similarity lists for later access.
	 * @param fileName - file to write the serialized object to
	 * @param writer - the UserSimKeeper object to serialize
	 */
	public static void serialize(String fileName, UserSimKeeper writer) {
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			ObjectOutputStream objectOut = new ObjectOutputStream(out);
			objectOut.writeObject(writer);
			objectOut.close();
			out.close();
		} catch(FileNotFoundException e) { e.printStackTrace(); }
		catch(IOException e) { e.printStackTrace(); }
	}

	/**
	 * @author steinbel, based on deserializze method in memreader.MemReader
	 * Deseralizes the similarity list.
	 * @param fileName - file with the serialized UserSimKeeper object
	 * @return - the deseralized UserSimKeeper
	 */
	public static UserSimKeeper deserialize(String fileName) {
		try {
			FileInputStream in = new FileInputStream(fileName);
			ObjectInputStream objectIn = new ObjectInputStream(in);
			UserSimKeeper simKeeper = (UserSimKeeper)objectIn.readObject();
			objectIn.close();
			in.close();
			return simKeeper;
		} catch (ClassNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		return null;
	}

	/**
	 * @author steinbel
	 * Returns the list of similar users and similarities for this user.
	 * @param userID - the target user
	 * @return - a list of <uid, similarity> pairs, null if none recorded
	 */
	public ObjectArrayList getSimilarities(int userID) {
		if (! similarities.containsKey(userID) )
			return null;
		else
			return (ObjectArrayList)similarities.get(userID);
	}
}
