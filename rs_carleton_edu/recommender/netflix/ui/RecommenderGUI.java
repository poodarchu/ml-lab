package netflix.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 * A generic GUI for a recommender system, to be extended by Movies and Courses GUI classes
 * @author tuladhaa
 *
 */
public abstract class RecommenderGUI extends JFrame {

    private static final Dimension MAX_BTN_DIM = new Dimension(100, 1);
    protected static final int DESCDISPLAYLENGTH = 40;
    protected final int FRAME_WIDTH = 456;
    protected final int FRAME_HEIGHT = 683;
    /**
     * Current user
     */
    protected JLabel curUserLabel = new JLabel("Set current user id");
    protected JTextField curUserTextField;
    protected JButton curUserSetBtn = new JButton();
    protected JLabel curUserStatusLabel;
    /**
     * Add item
     */
    protected JComboBox addItemList;
    protected JComboBox addRatingList;
    protected JButton addItemBtn;
    /**
     * Get recommendations
     */
    protected JButton getRecBtn;
    protected JComboBox algorithmsList;
    /**
     * Input file
     */
    protected JLabel inputFileInstruct = new JLabel();
    protected JTextField inputFileField = new JTextField();
    protected JButton browseBtn;
    
    protected JButton parseInputFileBtn = new JButton("Parse");
    protected JLabel parseStatusLabel = new JLabel();
    protected boolean parseStatus = false;
    protected JTextArea outputTextArea = new JTextArea();
    protected JScrollPane scrollPane;
    protected ArrayList<String> algorithmsDesc = new ArrayList<String>();
    protected JButton toggleOutputBtn = new JButton();
    protected int NEW_STUD_ID = 10000;

    protected final int LEFT_MARGIN = 10;
    protected final int PADDING = 5;
    protected final int TOP_MARGIN = 10;
    
    // Messages
    protected String inputFileDirection = "";
    protected String parseStatusMessageNo = "";
    protected String parseStatusMessageYes = "";
    protected String addItemBtnLabel = "";
    protected String toggleOutputBtnLabelMy = "";
    protected String toggleOutputBtnLabelRec = "";

    protected SpringLayout layout = new SpringLayout();
    
    /**
     * Boolean to indicate whether we have found items to rate or not.
     * This is what tells us whether we are ready to generate recommendations.
     */
    protected boolean itemsToCheckFound = false;
    protected ArrayList<Item> allItems = new ArrayList<Item>();
    protected ArrayList<Item> itemsToCheck = new ArrayList<Item>();
    protected ArrayList<Item> itemsRecommended = new ArrayList<Item>();
    protected int newUserId;
    protected String myItemsText = "", outputText = "";
    public RecommenderGUI() {
        super("User Input");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    protected Object makeObj(final String item)  {
        return new Object() { public String toString() { return item; } };
    }

    /**
     * Uses a JFileChooser to get the transcript file
     * @param
     * @return Transcript file spec chosen by user
     */
    protected String getInputFile(Component parent) {
        String oldInputText = inputFileField.getText();
        JFileChooser transcriptFileChooser = new JFileChooser();
        int returnVal = transcriptFileChooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String result = transcriptFileChooser.getSelectedFile().getAbsolutePath();
            if (result.equals("")) return oldInputText;
            return result;
        }
        return "";
    }

    /**
     * Adds components to the main Course Recommender
     * 
     * @param pane pane to add components to
     */
    protected void addCommonComponentsToPane(Container pane) {

        pane.setLayout(layout);

        curUserLabel.setText("Set Existing User Id from database: ");
        layout.putConstraint(SpringLayout.WEST, curUserLabel, LEFT_MARGIN,
                SpringLayout.WEST, pane);
        layout.putConstraint(SpringLayout.NORTH, curUserLabel, LEFT_MARGIN,
                SpringLayout.NORTH, pane);
        curUserTextField = new JTextField(5);
        layout.putConstraint(SpringLayout.WEST, curUserTextField, PADDING,
                SpringLayout.EAST, curUserLabel);
        layout.putConstraint(SpringLayout.NORTH, curUserTextField, LEFT_MARGIN,
                SpringLayout.NORTH, pane);
        curUserSetBtn.setText("Set");
        curUserSetBtn.setMaximumSize(MAX_BTN_DIM);
        layout.putConstraint(SpringLayout.WEST, curUserSetBtn, PADDING,
                SpringLayout.EAST, curUserTextField);
        layout.putConstraint(SpringLayout.NORTH, curUserSetBtn, LEFT_MARGIN,
                SpringLayout.NORTH, pane);
        
        inputFileInstruct = new JLabel();
        layout.putConstraint(SpringLayout.WEST, inputFileInstruct, LEFT_MARGIN,
                SpringLayout.WEST, pane);
        layout.putConstraint(SpringLayout.NORTH, inputFileInstruct, TOP_MARGIN,
                SpringLayout.SOUTH, curUserSetBtn);
        inputFileField = new JTextField(20);
        layout.putConstraint(SpringLayout.WEST, inputFileField, LEFT_MARGIN,
                SpringLayout.WEST, pane);
        layout.putConstraint(SpringLayout.NORTH, inputFileField, PADDING,
                SpringLayout.SOUTH, inputFileInstruct);
        browseBtn = new JButton("Browse");
        layout.putConstraint(SpringLayout.WEST, browseBtn, PADDING,
                SpringLayout.EAST, inputFileField);
        layout.putConstraint(SpringLayout.NORTH, browseBtn, PADDING,
                SpringLayout.SOUTH, inputFileInstruct);        
        layout.putConstraint(SpringLayout.WEST, parseInputFileBtn, LEFT_MARGIN,
                SpringLayout.WEST, pane);
        layout.putConstraint(SpringLayout.NORTH, parseInputFileBtn, PADDING,
                SpringLayout.SOUTH, browseBtn);
        parseStatusLabel = new JLabel("Transcript not parsed yet");
        layout.putConstraint(SpringLayout.WEST, parseStatusLabel, PADDING,
                SpringLayout.EAST, parseInputFileBtn);
        layout.putConstraint(SpringLayout.NORTH, parseStatusLabel, PADDING,
                SpringLayout.SOUTH, browseBtn);
        /**/
        addItemList = new JComboBox();
        layout.putConstraint(SpringLayout.WEST, addItemList, LEFT_MARGIN,
                SpringLayout.WEST, pane);
        layout.putConstraint(SpringLayout.NORTH, addItemList, TOP_MARGIN,
                SpringLayout.SOUTH, parseInputFileBtn);

        addRatingList = new JComboBox();
        layout.putConstraint(SpringLayout.WEST, addRatingList, PADDING,
                SpringLayout.EAST, addItemList);
        layout.putConstraint(SpringLayout.NORTH, addRatingList, TOP_MARGIN,
                SpringLayout.SOUTH, parseInputFileBtn);

        addItemBtn = new JButton("Add course");
        layout.putConstraint(SpringLayout.WEST, addItemBtn, PADDING,
                SpringLayout.EAST, addRatingList);
        layout.putConstraint(SpringLayout.NORTH, addItemBtn, TOP_MARGIN,
                SpringLayout.SOUTH, parseInputFileBtn);
        /**/
        algorithmsList = new JComboBox();
        for (String algorithm : algorithmsDesc) {
            algorithmsList.addItem(makeObj(algorithm));
        }
        layout.putConstraint(SpringLayout.WEST, algorithmsList, LEFT_MARGIN,
                SpringLayout.WEST, pane);
        layout.putConstraint(SpringLayout.NORTH, algorithmsList, TOP_MARGIN,
                SpringLayout.SOUTH, addItemList);

        getRecBtn = new JButton("Get recommendations");
        getRecBtn.setEnabled(false);
        layout.putConstraint(SpringLayout.WEST, getRecBtn, PADDING,
                SpringLayout.EAST, algorithmsList);
        layout.putConstraint(SpringLayout.NORTH, getRecBtn, TOP_MARGIN,
                SpringLayout.SOUTH, addItemList);

        JLabel recommendationsLabel = new JLabel("Output: ");
        layout.putConstraint(SpringLayout.WEST, recommendationsLabel, LEFT_MARGIN,
                SpringLayout.WEST, pane);
        layout.putConstraint(SpringLayout.NORTH, recommendationsLabel, TOP_MARGIN,
                SpringLayout.SOUTH, getRecBtn);

        toggleOutputBtn = new JButton("View my courses");
        layout.putConstraint(SpringLayout.WEST, toggleOutputBtn, PADDING,
                SpringLayout.EAST, recommendationsLabel);
        layout.putConstraint(SpringLayout.NORTH, toggleOutputBtn, PADDING,
                SpringLayout.SOUTH, getRecBtn);

        outputTextArea = new JTextArea(24, 60);
        outputTextArea.setFont(new Font("Courier", Font.PLAIN, 12));
        outputTextArea.setEditable(false);
        scrollPane = new JScrollPane(outputTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        layout.putConstraint(SpringLayout.WEST, scrollPane, LEFT_MARGIN,
                SpringLayout.WEST, pane);
        layout.putConstraint(SpringLayout.NORTH, scrollPane, 10,
                SpringLayout.SOUTH, recommendationsLabel);

        pane.add(curUserLabel);
        pane.add(curUserTextField);
        pane.add(curUserSetBtn);

        pane.add(inputFileInstruct);
        pane.add(inputFileField);
        pane.add(browseBtn);
        pane.add(parseInputFileBtn);
        pane.add(parseStatusLabel);

        pane.add(algorithmsList);
        pane.add(addItemList);
        pane.add(addRatingList);
        pane.add(addItemBtn);
        pane.add(getRecBtn);
        pane.add(toggleOutputBtn);
        pane.add(recommendationsLabel);
        pane.add(scrollPane);
        // pane.add(getInputTranscriptPanel());
    }

    protected void addRatingsToList(String[] ratings) {
        // TODO Auto-generated method stub
        for (String rating : ratings) {
            addRatingList.addItem(makeObj(rating));
        }

    }

    /**
     * Customizes components for specific recommenders.
     * @param pane
     */
    protected abstract void customizeComponents(Container pane);
    
    protected void initializeComponents(ActionListener component) {
        // Set proper labels
        /*
         *         inputFileDirection = "Movies file:";
        parseStatusMessageYes = "Done parsing movie file.";
        parseStatusMessageNo = "Movie file not parsed yet.";
        addItemBtnLabel = "Add Movie";
        toggleOutputBtnLabelMy = "View my movies";
        toggleOutputBtnLabelRec = "View my recommendations";
        
         */
        parseStatusLabel.setText(parseStatusMessageNo);
        getRecBtn.setEnabled(false);
        toggleOutputBtn.setEnabled(false);
        toggleOutputBtn.setText(toggleOutputBtnLabelMy);
        addItemBtn.setText(addItemBtnLabel);
        
        curUserSetBtn.addActionListener(component);
        browseBtn.addActionListener(component);
        getRecBtn.addActionListener(component);
        parseInputFileBtn.addActionListener(component);
        addItemBtn.addActionListener(component);
        toggleOutputBtn.addActionListener(component);
    }
    /**
     * Formats an Item for displaying in a list
     * @param i
     * @return
     */
    protected String formatItemForList(Item i) {
        String desc = i.getDescription();
        if (desc.length() > DESCDISPLAYLENGTH) {
            desc = desc.substring(0, DESCDISPLAYLENGTH);
        } else {
            int l = DESCDISPLAYLENGTH - desc.length();
            for (int c=1;c<=l;c++) {
                desc += " ";
            }
        }
        //System.out.println(i.getId() + " " + desc + " " + i.getRating());
        return i.getId() + "  " + desc + "  " + i.getRating();
    }
    
    /**
     * Removes, from an arraylist of items, an item that has the same id 
     * as the give item
     * @param itemsList ArrayList of items to remove item from
     * @param itemToRemove Item to remove
     */
    protected ArrayList<Item> removeFromItems(ArrayList<Item> itemsList, Item itemToRemove) {
        boolean remove = false;
        Item itemToRemoveFound = null;
        for (Item i : itemsList) {
            if (i.getId().equals(itemToRemove.getId())) {
                remove = true;
                itemToRemoveFound = i;
                break;
            }
        }
        if (remove) itemsList.remove(itemToRemoveFound);
        return itemsList;
    }
    
    /**
     * Sets variables and GUI elements to be ready for recommendations
     * @param isSetUserId true if a user has been set, false if a text file has been parsed
     */
    protected void getReadyForRecommendations(boolean isSetUserId) {
        itemsToCheckFound = true;
        getRecBtn.setEnabled(true);
        toggleOutputBtn.setEnabled(true);
        if (isSetUserId) {
            inputFileField.setText("");
            parseStatusLabel.setText(parseStatusMessageNo);
        } else {
            curUserTextField.setText("");
            parseStatusLabel.setText(parseStatusMessageYes);
        }
        outputText = "";
        outputTextArea.setText("");
    }
    
    /**
     * Updates the myItemsText variable
     */
    protected abstract void updateMyItemsText();

    /**
     * Displays output and resets the scroll pane to top
     */
    protected void displayOutput() {
        outputTextArea.setText(outputText);
        toggleOutputBtn.setText(toggleOutputBtnLabelMy);
        scrollPane.getVerticalScrollBar().setValue(0);
        outputTextArea.setCaretPosition(0);
    }
    
    /**
     * Displays rated items
     */
    protected void displayMyItems() {
        outputTextArea.setText(myItemsText);
        toggleOutputBtn.setText(toggleOutputBtnLabelRec);
        scrollPane.getVerticalScrollBar().setValue(0);
        outputTextArea.setCaretPosition(0);        
    }
    
    /**
     * A comparator to sort items by their descriptions
     * @author tuladhaa
     *
     */
    protected class NameComparator implements Comparator<Item> {
        /**
         * Sorts on:
         * 1. description (ascending)
         * 2. id (descending)
         * 3. rating
         */
        public int compare(Item a, Item b) {
            if(a.description.compareTo(b.description) < 0)
                return -1;
            else if(a.description.compareTo(b.description) > 0)
                return 1;
            
            if(a.id.compareTo(b.id) < 0)
                return 1;
            else if(a.id.compareTo(b.id) > 0)
                return -1;
            
            if (a.rating < b.rating)
                return 1;
            else if (a.rating > b.rating)
                return -1;
            return 0;
        }
    }
}
