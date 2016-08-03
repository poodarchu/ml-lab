import java.security.MessageDigest;
import java.util.ArrayList;


/**
 * Encrypts and encodes part or all of an Entry
 * @author AST
 *
 */
public class Encrypter {
	/**
	 * Encrypts the course part of an array of entries, using MD5 as default encryption algorithm
	 * @param entries
	 * @return
	 */
	public static Entry[] encryptCoursesAndProfs(Entry[] entries) {
		return encryptCoursesAndProfs(entries, "MD5");
	}
	
	/**
	 * Encrypts the course and prof parts of an array of entries, using any encryption algorithm
	 * @param entries
	 * @param digestAlgorithm algorithm to use
	 * @return
	 */
	public static Entry[] encryptCoursesAndProfs(Entry[] entries, String digestAlgorithm) {
		ArrayList<Entry> outputEntries = new ArrayList<Entry>();
		MessageDigest d = null;
		try {
			d =	MessageDigest.getInstance(digestAlgorithm);	

			for (Entry e : entries) {
				outputEntries.add(encryptCoursesAndProfs(e,d));
			}
			return outputEntries.toArray(new Entry[0]);
		} catch (Exception e) {
			System.out.println("Sorry, could not encrypt using " + digestAlgorithm);
			return null;
		}

	}
	
	/**
	 * Encrypts the course part of a single entry using a digest
	 * @param entry
	 * @param digest
	 * @return
	 * @throws Exception
	 */
	private static Entry encryptCoursesAndProfs(Entry entry, MessageDigest digest) throws Exception {
		return new Entry(entry.getSid(), entry.getYear(), entry.getTerm(), 
				encryptAndEncode(entry.getDept(), digest),
				encryptAndEncode(entry.getCnum(), digest),
				encryptAndEncode(entry.getSect(), digest),
				encryptAndEncode(entry.getProf(), digest), entry.getGrade(), entry.getMajor());
	}
	
	/**
	 * Encrypts different parts of an array of entries, depending on what booleans are set to true
	 * Clunky way of doing this, but we don't really need it for now, feel free to replace
	 * @param entries
	 * @param digestAlgorithm
	 * @param sid
	 * @param term
	 * @param dept
	 * @param cnum
	 * @param sect
	 * @param prof
	 * @param grade
	 * @param major
	 * @return
	 */
	public static Entry[] encryptData(Entry[] entries, String digestAlgorithm,
			boolean sid, boolean term, boolean dept, boolean cnum,
			boolean sect, boolean prof, boolean grade, boolean major) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance(digestAlgorithm);
			ArrayList<Entry> outputEntries = new ArrayList<Entry>();
		
			for (Entry e : entries) {
				outputEntries.add(encryptData(e, digest, sid, term, dept, cnum, sect, prof, grade, major));
			}
			return outputEntries.toArray(new Entry[0]);
		} catch (Exception e) {
			System.out.println("Could not create message digest with " + digestAlgorithm);
			return null;
		}
	}
	
	/**
	 * Encrypts different parts of an entry, depending on what booleans are set to true
	 * Clunky way of doing this, but we don't really need it for now, feel free to replace
	 * @param entry
	 * @param digest
	 * @param sid
	 * @param term
	 * @param dept
	 * @param cnum
	 * @param sect
	 * @param prof
	 * @param grade
	 * @param major
	 * @return
	 * @throws Exception
	 */
	private static Entry encryptData(Entry entry, MessageDigest digest, boolean sid, boolean term,
			boolean dept, boolean cnum, boolean sect, boolean prof, boolean grade, boolean major) 
	throws Exception {
		
		String outSid = (sid) ? encryptAndEncode(entry.getSid(), digest) : entry.getSid();
		String outTerm = (term) ? encryptAndEncode(entry.getTerm(), digest) : entry.getTerm();
		String outDept = (dept) ? encryptAndEncode(entry.getDept(), digest) : entry.getDept();
		String outCNum = (cnum) ? encryptAndEncode(entry.getCnum(), digest) : entry.getCnum();
		String outSect = (sect) ? encryptAndEncode(entry.getSect(), digest) : entry.getSect();
		String outProf = (prof) ? encryptAndEncode(entry.getProf(), digest) : entry.getProf();
		String outGrade = (grade) ? encryptAndEncode(entry.getGrade(), digest) : entry.getGrade();
		String outMajor = (major) ? encryptAndEncode(entry.getMajor(), digest) : entry.getMajor();
		
		return new Entry(outSid, entry.getYear(), outTerm, outDept, outCNum, outSect, outProf, outGrade, outMajor);
	}

	/**
	 * Encrypts and encodes a string using the passed MessageDigest
	 * @param toEncrypt
	 * @param d
	 * @return
	 * @throws Exception
	 */
	private static String encryptAndEncode(String toEncrypt, MessageDigest d) throws Exception {
	     d.reset();
	     d.update(toEncrypt.getBytes());
	     byte[] encryptedBytes = d.digest();
	     //encryptedBytes may contain unprintable characters so encode using Base64
	     //sun.* classes are actually internal and should NOT be used in real code
	     //...but works for our purposes :)
	     sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
	     String encoded=encoder.encode(encryptedBytes);
	     return encoded;
	}
}
