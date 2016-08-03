package netflix.ui;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import transcript.data.Entry;

public class TranscriptParser extends HTMLEditorKit {
    
    /**
     * 
     */
    private static final long serialVersionUID = -243923576327460432L;
    
    private String fileName;
    private final String[] IDS_TO_CHECK = {"VAR_STC_COURSE_NAME_", "VAR_STC_VERIFIED_GRADE_", "VAR_STC_CMPL_CRED_"};
    private int courseNum;
    private int currentState;
    private boolean lookForText;
    private ArrayList<Entry> coursesTaken = new ArrayList<Entry>();
    private String curDept, curGrade;
    private int curCNum;
    /**
     * Constructor
     * @param fileName Name of transcript file to parse
     */
    public TranscriptParser(String fileName) {
        this.fileName = fileName;
        this.courseNum = 1;
        this.currentState = 0;
        this.lookForText = false;
    }
    /**
     * Call to obtain a HTMLEditorKit.Parser object.
     * 
     * @return A new HTMLEditorKit.Parser object.
     */
    public HTMLEditorKit.Parser getParser()
    {
        return super.getParser();
    }

    /**
     * Parses a transcript file and extracts courses etc.
     */
    public ArrayList<Entry> parse() throws IOException{
        int len = (int)(new File(fileName).length());
        char buf[] = new char[len];
        FileReader fileReader = new FileReader(fileName);
        fileReader.read(buf);
        String data = new String(buf);
        StringReader r = new StringReader(data);

        HTMLEditorKit.Parser transcriptParser = getParser();
        transcriptParser.parse(r, new MyParserCallback(), true);
        /*System.out.println("Courses taken: ");
        for (Entry courseTaken : coursesTaken) {
            System.out.println("X" + courseTaken.getDept() + "X" + 
                    " X" + courseTaken.getCnum() + "X" + 
                    " X" + courseTaken.getFullCourse() + "X");
        }*/
        fileReader.close();
        return coursesTaken;
    }

    /**
     * Callback class required for HTMLParser to work
     */
    private class MyParserCallback extends HTMLEditorKit.ParserCallback {
        public void handleText(char[] data, int pos) {
            if (lookForText) {
                lookForText = false;
                //System.out.println("Text found.");
                String text = new String(data);
                switch(currentState) {
                case 1:
                    int posOfDot = text.indexOf(".");
                    if (posOfDot >= 0) {
                        curDept = text.substring(0, posOfDot).trim();
                        curCNum = Integer.parseInt(text.substring(posOfDot+1,posOfDot+4).trim());
                    }
                    break;
                case 2:
                    curGrade = text.trim();
                    coursesTaken.add(new Entry(0, 0, "", curDept, curCNum, "", curGrade, ""));
                    break;
                default:
                    break;
                }
            }
        }

        public void handleComment(char[] data, int pos) {

            //System.out.println("Comment(" + data.length + " chars)");
        }

        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if (t==HTML.Tag.P) {
                //System.out.println("Current state: " + currentState);
                String idToCheck = IDS_TO_CHECK[currentState] + courseNum;
                String incomingId = (String)a.getAttribute(HTML.Attribute.ID);
                //System.out.println("  id to check: " + idToCheck);
                //System.out.println("  incoming: " + incomingId);
                if (idToCheck.equalsIgnoreCase(incomingId)) {
                    lookForText = true;
                    currentState++;
                    if (currentState >= IDS_TO_CHECK.length) {
                        currentState = 0;
                        courseNum++;
                    }
                } else {
                    lookForText = false;
                }
            }
            //System.out.println("Tag start(<" + t.toString() + ">, " + a.getAttributeCount() + " attrs)");

        }

        public void handleEndTag(HTML.Tag t, int pos) {


            //System.out.println("Tag end(</" + t.toString() + ">)");
        }

        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {

            //System.out.println("Tag(<" + t.toString() + ">, " + a.getAttributeCount() + " attrs)");
        }

        public void handleError(String errorMsg, int pos){
            //System.out.println("Parsing error: " + errorMsg + " at " + pos);
        }
    }

}

