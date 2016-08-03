package transcript.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility functions for writing Entry[] and
 * TransformedEntry[] files, as flat text.
 * 
 * @author lewda
 */
public class Writer {
    
    /**
     * Writes an array of TransformedEntry[] to a file.
     * @param data the data to write
     * @param fileName the file to write to
     */
    public static void write(TransformedEntry[] data, String fileName) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            for (TransformedEntry te : data) {
                bw.write(te.toString() + "\n");
            }
            bw.close();
        }
        catch (IOException e) {
            System.out.println("Error writing test file:\n" + e);
        }
    }
    
    /**
     * Writes an array of Entry[] to a file.
     * @param data the data to write
     * @param fileName the file to write to
     */
    public static void write(Entry[] data, String fileName) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            for (Entry e : data) {
                bw.write(e.toString() + "\n");
            }
            bw.close();
        }
        catch (IOException e) {
            System.out.println("Error writing test file:\n" + e);
        }
    }
}
