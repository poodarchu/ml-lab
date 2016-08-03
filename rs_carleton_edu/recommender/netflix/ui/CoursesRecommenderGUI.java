package netflix.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;

import netflix.utilities.RankingUtilities;
import transcript.data.Entry;
import transcript.memreader.TranscriptMemHelper;
import transcript.recommender.AbstractRecommender;
import transcript.recommender.AssociationRecommender;
import transcript.recommender.CorrelationRecommender;
import transcript.utilities.Converter;
import cern.colt.list.ObjectArrayList;

/**
 * A GUI for a Courses Recommender using a serialized object with historical transcript data
 * @author tuladhaa
 *
 */
public class CoursesRecommenderGUI extends RecommenderGUI implements ActionListener {

    /**
     * Constants
     */
    private static final long serialVersionUID = 89104961997112915L;
    private static final int COURSE_DESC_LENGTH = 25;

    final int TRANSCRIPTINPUT_WIDTH = 300;
    final int TRANSCRIPTINPUT_HEIGHT = 150;
    /**
     * Id for the new student
     */
    private ArrayList<Entry> coursesTaken = new ArrayList<Entry>();
    private int maxStudId;
    private TranscriptMemHelper tmh;
    private AbstractRecommender recommender;
    String[] grades = {"A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F"};

    private String remIntroBtnRemove = "Remove intro classes";
    private String remIntroBtnShow = "Include intro classes";
    private JButton remIntroBtn = new JButton(remIntroBtnRemove);
    private boolean showIntroClasses = true;
    private boolean showGrades = true;
    /**
     * Constructor
     * @param transcriptMemReaderFile
     * @param allCoursesFile
     */
    public CoursesRecommenderGUI(String transcriptMemReaderFile, String allCoursesFile) {
        this.setResizable(false);
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setTitle("Courses Recommender");
        this.tmh = new TranscriptMemHelper(transcriptMemReaderFile);
        this.recommender = new CorrelationRecommender(tmh);
        this.maxStudId = tmh.getNumberOfStuds() + 1;
        this.newUserId = maxStudId;
        getAllCourses(allCoursesFile);
        itemsToCheck.addAll(allItems);

        algorithmsDesc.add("Correlation");
        algorithmsDesc.add("Association Rules");

        inputFileDirection = "Transcript file:";
        parseStatusMessageYes = "Done parsing transcript file.";
        parseStatusMessageNo = "Transcript file not parsed yet.";
        addItemBtnLabel = "Add Course";
        toggleOutputBtnLabelMy = "View my courses";
        toggleOutputBtnLabelRec = "View my recommendations";

        addCommonComponentsToPane(this.getContentPane());
        customizeComponents(this.getContentPane());
    }

    /* (non-Javadoc)
     * @see netflix.ui.RecommenderGUI#customizeComponents(java.awt.Container)
     */
    protected void customizeComponents(Container pane) {
        // TODO Auto-generated method stub
        pane.setSize(TRANSCRIPTINPUT_WIDTH, TRANSCRIPTINPUT_HEIGHT);

        layout.putConstraint(SpringLayout.WEST, remIntroBtn,
                5, SpringLayout.EAST, toggleOutputBtn);
        layout.putConstraint(SpringLayout.NORTH, remIntroBtn,
                5, SpringLayout.SOUTH, algorithmsList);
        remIntroBtn.setEnabled(false);
        remIntroBtn.addActionListener(this);
        pane.add(remIntroBtn);
        inputFileInstruct.setText("Transcipt file (for new user):");
        addItemList.setRenderer(new CourseListRenderer());
        addAllCoursesToList();
        addRatingsToList(grades);

        initializeComponents(CoursesRecommenderGUI.this);

    }

    /**
     * Gets all the courses from our courses-name.txt 
     * so the recommender can use it to recommend courses
     * @param allCoursesFile File with list of all courses
     */
    private void getAllCourses(String allCoursesFile) {
        allItems = new ArrayList<Item>();
        if (!allCoursesFile.equals("")) {
            allItems = RankingUtilities.readCourseFile(allCoursesFile);
        }
    }

    /**
     * Adds all the courses to the list of all courses
     */
    private void addAllCoursesToList() {
        Collections.sort(allItems, new CourseNameComparator());
        if (allItems.size() > 0) {
            for (Item course : allItems) {
                addItemList.addItem(course);
                //addItemList.addItem(makeObj(course.getId() + " " + desc));
            }
        } else {
            Set<String> courses = tmh.getListOfCourses();
            for(String course : courses) {
                addItemList.addItem(makeObj(course));
            }
        }

    }

    /**
     * actionPerformed method for ActionListener extension
     * 
     * @param e ActionEvent object that describes the action
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == browseBtn) {
            String transcriptFile = getInputFile(CoursesRecommenderGUI.this);
            inputFileField.setText(transcriptFile);

        } else if (e.getSource() == parseInputFileBtn) {
            long start = 0, end = 0;
            start = System.currentTimeMillis();
            parseStatusLabel.setText("Parsing transcript...");
            String transcriptFile = inputFileField.getText();
            parseStatus = getTranscriptData(transcriptFile);
            System.out.println("Transcript parse: " + (end - start));
            if (parseStatus) {

                // Parsed transcript means it's a new user
                newUserId = maxStudId;
                start = System.currentTimeMillis();
                addEntries(coursesTaken);
                recommender.resort();
                end = System.currentTimeMillis();
                System.out.println("Adding and resorting: " + (end - start));

                start = System.currentTimeMillis();
                itemsToCheck = findCoursesToCheck(newUserId);
                end = System.currentTimeMillis();
                System.out.println("Finding untaken courses: " + (end - start));
                updateMyItemsText();
                getReadyForRecommendations(false);
            } else
                parseStatusLabel.setText("Error parsing transcript.");

        } else if (e.getSource() == getRecBtn) {
            outputText = "";
            if (itemsToCheckFound) {
                switch(algorithmsList.getSelectedIndex()) {
                case 0:
                    showGrades = true;
                    itemsRecommended = getRecommendations(new CorrelationRecommender(tmh));
                    break;
                case 1:
                    showGrades = false;
                    itemsRecommended = getRecommendations(new AssociationRecommender(tmh));
                    break;
                }
                setOutputText(showIntroClasses);
            }
            displayOutput();

        } else if (e.getSource() == addItemBtn) {
            Item courseSelected = (Item) addItemList.getSelectedItem();
            String courseId = courseSelected.getId();
            String grade = addRatingList.getSelectedItem().toString();
            if (courseId.indexOf(".") > 0) {
                int posOfDot = courseId.indexOf(".");
                String dept = courseId.substring(0, posOfDot);
                int cnum = Integer.parseInt(courseId.substring(posOfDot+1, posOfDot+4));
                Entry newE = new Entry(newUserId, 0, "", dept, cnum, "", grade, "");
                /* public Entry(int sid, int year, String term, String dept, int cnum,
                    String prof, String grade, String major) {*/
                if (addEntry(newE)) {
                    System.out.println("Added " + courseId);
                    itemsToCheck = 
                        removeFromItems(itemsToCheck, new Item(dept + "." + String.valueOf(cnum), "", 0));
                    itemsToCheckFound = true;                        
                    /*for (Item i : itemsToCheck) {
                        System.out.println(i.getId());
                    }*/
                    JOptionPane.showMessageDialog(this, "Added " + courseId + " successfully.", 
                            "Added course", JOptionPane.INFORMATION_MESSAGE);
                    updateMyItemsText(newE);
                    getRecBtn.setEnabled(true);
                    toggleOutputBtn.setEnabled(true);
                } else {
                    System.out.println("Problem adding " + courseId);
                    JOptionPane.showMessageDialog(this, "Could not add " + courseId + ".", 
                            "Could not add course", JOptionPane.INFORMATION_MESSAGE);
                }
            } 
        } else if (e.getSource() == toggleOutputBtn) {
            if (toggleOutputBtn.getText().equals (toggleOutputBtnLabelMy)) {
                displayMyItems();
            } else {
                if (outputText.equals("")) {
                    outputTextArea.setText("No recommendations generated yet.\nPlease click Get Recommendations first.");
                    toggleOutputBtn.setText(toggleOutputBtnLabelMy);
                } else {
                    displayOutput();
                }
            }
        } else if (e.getSource() == curUserSetBtn) {
            try {
                myItemsText = "";
                int userId = Integer.parseInt(curUserTextField.getText());
                newUserId = userId;
                ObjectArrayList coursesTakenObjects = tmh.getCoursesTakenByStud(newUserId);
                coursesTaken = new ArrayList<Entry>();
                for (int i=0;i<coursesTakenObjects.size();i++) {
                    Entry en = (Entry) coursesTakenObjects.getQuick(i);
                    coursesTaken.add(en);
                }
                itemsToCheck = findCoursesToCheck(newUserId);
                updateMyItemsText();            
                getReadyForRecommendations(true);
            } catch(NumberFormatException nFE) {
                JOptionPane.showMessageDialog(this, "Invalid user Id", 
                        "Invalid userId", JOptionPane.INFORMATION_MESSAGE);
            }

        } else if (e.getSource() == remIntroBtn) {

            if (itemsRecommended.size()>0) {
                if (showIntroClasses) {
                    setOutputText(false);
                    showIntroClasses = false;
                    remIntroBtn.setText(remIntroBtnShow);
                } else {
                    setOutputText(true);
                    showIntroClasses = true;
                    remIntroBtn.setText(remIntroBtnRemove);
                }
            }
        }
    }

    /**
     * Sets outputtext
     * @param showIntroClasses if true, output text will include intro classes
     */
    private void setOutputText(boolean showIntroClasses) {
        if (itemsRecommended.size()>0) {
            outputText="";
            for (Item i : itemsRecommended) {
                String cId = i.getId();
                String cNumString = cId.substring(cId.indexOf(".")+1);
                if (cNumString.substring(0,1).equals("1")) {
                    if (showIntroClasses) 
                        outputText += formatItemForList(i, showGrades) + "\n";
                } else {
                    outputText +=formatItemForList(i, showGrades) + "\n";
                }
            }
            displayOutput();
        }
    }

    /**
     * Reads and parses a transcript file. Stores the courses found in coursesTaken
     * @return true if data successfully parsed, false otherwise
     */
    private boolean getTranscriptData(String transcriptFileName) {
        coursesTaken = new ArrayList<Entry>();
        try {
            // Parsing a transcript means new student
            this.newUserId = maxStudId;

            System.out.print("Parsing transcript...");
            TranscriptParser transcriptParser = new TranscriptParser(transcriptFileName);
            coursesTaken = transcriptParser.parse();
            System.out.println("done.");
            updateMyItemsText();
        } catch (FileNotFoundException fNFE) {
            return false;

        } catch (IOException iOE) {
            return false;
        }
        return true;

    }

    /* (non-Javadoc)
     * @see netflix.ui.RecommenderGUI#getRecommendations(transcript.recommender.AbstractRecommender)
     */
    private ArrayList<Item> getRecommendations(AbstractRecommender recommender) {
        outputTextArea.setText("Working...");
        long start=0, end=0;

        // Add courses taken, and find what courses haven't been taken. 
        // We don't want to do this more than once for a transcript
        // so we set a boolean
        if (!itemsToCheckFound) {
            start = System.currentTimeMillis();
            itemsToCheck = findCoursesToCheck(newUserId);
            itemsToCheckFound = true;
            end = System.currentTimeMillis();
            System.out.println("Getting untaken courses: " + (end-start));
        }
        start = System.currentTimeMillis();
        recommender.resort();
        recommender.rankCourses(newUserId, itemsToCheck);
        itemsRecommended = itemsToCheck;
        end = System.currentTimeMillis();
        System.out.println("Ranking: " + (end - start));
        remIntroBtn.setEnabled(true);
        return itemsRecommended;
    }

    /**
     * Adds an item to the recommender with the current newUserId
     * @param i Item to add
     * @return true if item has been successfully added, false otherwise
     */
    protected boolean addItem(Item i) {
        String id = i.getId();
        int ind = id.indexOf(".");
        try {
            if (ind>-1) {
                String dept = id.substring(0, ind);
                int cnum = Integer.parseInt(id.substring(ind+1));
                // Entry(int sid, int year, String term, String dept, int cnum, String prof, String grade, String major)
                Entry course = new Entry(newUserId, 0, "", dept, cnum, "", Converter.numToGrade((int) i.getRating()), "");
                return recommender.add(course);
            }
        } catch (NumberFormatException nFE) {
            return false;
        }
        return false;
    }
    /**
     * Add an individual entry to the recommender. It will consist of at least a course and
     * grade for a particular student
     * @param course Course to add
     * @return true if course is succesfully added
     */
    private boolean addEntry(Entry course) {
        course.setSid(newUserId);
        return recommender.add(course);
    }
    /**
     * Adds an ArrayList of entries to the recommender
     * @param courses ArrayList of entries to add
     */
    private void addEntries(ArrayList<Entry> courses) {
        for (Entry course : courses) {
            if (!addEntry(course))
                System.out.println("Could not add " + course.getFullCourse());
        }
    }
    /**
     * Finds the courses to check (i.e., courses that haven't been taken by a student)
     * @param studId
     * @return
     */
    private ArrayList<Item> findCoursesToCheck(int studId) {
        if (allItems.size() > 0) {
            return recommender.getUnratedCourses(studId, allItems);
        }
        return recommender.getUnratedCourses(studId);
    }
    /* (non-Javadoc)
     * @see netflix.ui.RecommenderGUI#updateMyItemsText()
     */
    protected void updateMyItemsText() {
        myItemsText = "";
        for (Entry c : coursesTaken) {
            myItemsText += formatItemForList(new Item(c.getFullCourse(), 
                    getCourseName(c.getFullCourse()), 
                    c.getGradeAsInt()), true) + "\n";
            //System.out.println(c.getFullCourse());
        }

    }

    /**
     * Adds a course to the user's courses
     * @param addCourse
     */
    private void updateMyItemsText(Entry addCourse) {
        myItemsText += 
            formatItemForList(new Item
                    (addCourse.getFullCourse(), getCourseName(addCourse.getFullCourse()), addCourse.getGradeAsInt()),
                    true) +  
                    "\n";
    }

    private String getCourseName(String courseId) {
        if (allItems.size() > 0) {
            for (Item allItem : allItems)
                if (allItem.getId().equals(courseId)) {
                    return allItem.getDescription();
                }
        }
        return "";
    }
    protected class CourseNameComparator implements Comparator<Item> {
        /**
         * Sorts on:
         * 1. id (descending)
         * 2. description (ascending)
         * 3. rating
         */
        public int compare(Item a, Item b) {
            if(a.id.compareTo(b.id) < 0)
                return -1;
            else if(a.id.compareTo(b.id) > 0)
                return 1;

            if(a.description.compareTo(b.description) < 0)
                return -1;
            else if(a.description.compareTo(b.description) > 0)
                return 1;


            if (a.rating < b.rating)
                return 1;
            else if (a.rating > b.rating)
                return -1;
            return 0;
        }
    }
    /**
     * ListRenderer for rendering items in the list of all classes
     * @author tuladhaa
     *
     */
    private class CourseListRenderer extends JLabel implements ListCellRenderer {

        public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
            // TODO Auto-generated method stub
            Item course = (Item) arg1;
            String desc = (course.getDescription().length() > COURSE_DESC_LENGTH) ?
                    course.getDescription().substring(0,COURSE_DESC_LENGTH) + "..." :
                        course.getDescription();
                    setText(course.getId() + " " + desc);
                    return this;
        }

    }

    /**
     * Formats an Item for displaying in a list
     * @param i
     * @return
     */
    protected String formatItemForList(Item i, boolean showGrades) {
        String desc = i.getDescription();
        if (desc.length() > DESCDISPLAYLENGTH) {
            desc = desc.substring(0, DESCDISPLAYLENGTH);
        } else {
            int l = DESCDISPLAYLENGTH - desc.length();
            for (int c=1;c<=l;c++) {
                desc += " ";
            }
        }
        int g = (int) i.getRating();
        if (g > 12) g = 12;
        if (g < 1) g = 1;
        String grade = (showGrades) ? Converter.numToGrade(g) : "";
        //System.out.println(i.getId() + " " + desc + " " + i.getRating());
        return i.getId() + "  " + desc + "  " + grade;
    }
}
