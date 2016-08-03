package transcript.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 * This class utilizes the power of the transcript data
 * manipulation code.  It takes the compsdata.txt file that Dave
 * has and turns it into a fully-functional 
 * 
 * In a way this code has a dual purpose - both to use the code,
 * but to also demonstrate how the code can be properly used
 * in any context.
 * 
 * @author lewda
 */
public class Main {
	public static void main (String[] args) {
        //Read in the compsdata.txt file
        Entry[] data = Parser.parseData(args[0]);
        
        //Transform the data
        TransformedEntry[] tdata = Transformer.transform(data);
        
        //"Tidy up" the data
        tdata = Transformer.tidyData(tdata);
        
        //Clear out memory space, in case it is needed
        data = null;
        System.gc();
        
        //Perturb the data (optional step)
        //Perturber.perturb(tdata, 3, true);
        
        //Now write the file back to disk, so it can later be used
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(
                    args[1]));
            for (TransformedEntry te : tdata) {
                bw.write(te.toString() + "\n");
            }
            bw.close();
        }
        catch (IOException e) {
            System.out.println("Error writing file:\n" + e);
        }
        
        //At this point, there are two options.
        //1. You can create a training/test set using CreateTestSet
        //2. You can load the data into a TranscriptMemReader
        //Have fun!
	}
}
