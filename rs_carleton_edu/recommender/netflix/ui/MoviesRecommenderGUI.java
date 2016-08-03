package netflix.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;

import netflix.memreader.MemHelper;
import netflix.recommender.AbstractRecommender;
import netflix.recommender.AssociationRecommender;
import netflix.recommender.CorrelationRecommender;
import netflix.recommender.IncRecommender;
import netflix.recommender.SVDUpdateRecommender;
import netflix.utilities.RankingUtilities;
import cern.colt.list.IntArrayList;


/**
 * A GUI for a Movies Recommender using a serialized object with movie ratings data
 * @author tuladhaa
 *
 */
public class MoviesRecommenderGUI extends RecommenderGUI 
implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 7050644036138613362L;

    private static final int MOVIE_DESC_LENGTH = 25;
    
    final int TRANSCRIPTINPUT_WIDTH = 300;
    final int TRANSCRIPTINPUT_HEIGHT = 150;

    /**
     * Id for the new student
     */
    private ArrayList<Item> moviesRated = new ArrayList<Item>();
    private int maxUserId;
    private MemHelper mh;
    private AbstractRecommender recommender;
    String[] ratings = {"1", "2", "3", "4", "5"};

    private int indexStart = 1;
    
    public MoviesRecommenderGUI(String moviesMemReaderFile, String allMoviesFile) {
        this.setResizable(false);
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setTitle("Movies Recommender");
        this.mh = new MemHelper(moviesMemReaderFile);
        this.recommender = new AssociationRecommender(mh);
        this.maxUserId = mh.getNumberOfUsers() + 1;
        this.newUserId = maxUserId;
        getAllMovies(allMoviesFile);
        itemsToCheck.addAll(allItems);
        
        algorithmsDesc.add("Correlation");
        algorithmsDesc.add("Association Rules");
        algorithmsDesc.add("SVD (with update)");
        algorithmsDesc.add("Incremental SVD");
        //add here
        inputFileDirection = "Movies file:";
        parseStatusMessageYes = "Done parsing movie file.";
        parseStatusMessageNo = "Movie file not parsed yet.";
        addItemBtnLabel = "Add Movie";
        toggleOutputBtnLabelMy = "View my movies";
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
        
        inputFileInstruct.setText("Movies ratings file (new user):");
        parseStatusLabel.setText("Movies file not rated yet:");
        addItemList.setRenderer(new MovieListRenderer());
        addAllMovies();
        addRatingsToList(ratings);
        
        initializeComponents(MoviesRecommenderGUI.this);
    }

    /**
     * Gets all the courses from our courses-name.txt 
     * so the recommender can use it to recommend courses
     * @param allMoviesFile File with list of all courses
     */
    private void getAllMovies(String allMoviesFile) {
        allItems = new ArrayList<Item>();
        if (!allMoviesFile.equals("")) {
            allItems = RankingUtilities.readMovieFile(allMoviesFile);
        }
    }

    /**
     * Adds all movies to the list of all movies in the GUI
     */
    void addAllMovies() {
        Collections.sort(allItems, new MoviesNameComparator());
        if (allItems.size() > 0) {
            for (Item movie : allItems) {
                  addItemList.addItem(movie);
            }
        } else {
            IntArrayList moviesInts = mh.getListOfMovies();
            for(int i=0; i<moviesInts.size(); i++) {
                Item movie = new Item(String.valueOf(i), getMovieName(moviesInts.getQuick(i)), 0);
                addItemList.addItem(movie);
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
            String moviesListFile = getInputFile(MoviesRecommenderGUI.this);
            inputFileField.setText(moviesListFile);

        } else if (e.getSource() == parseInputFileBtn) {
            long start = 0, end = 0;
            start = System.currentTimeMillis();
            parseStatusLabel.setText("Parsing movies...");
            String moviesFile = inputFileField.getText();
            parseStatus = getMoviesFile(moviesFile);
            if (parseStatus) {
              
                // Parsed movie file means it's a new user
                newUserId = maxUserId;
               
                start = System.currentTimeMillis();
                System.gc();
                addEntries(moviesRated); // set by getMoviesFile
                recommender.resort();
                end = System.currentTimeMillis();
                System.out.println("Adding and resorting: " + (end - start));
                
                start = System.currentTimeMillis();
                itemsToCheck = findMoviesToCheck();
                itemsToCheckFound = true;
                end = System.currentTimeMillis();
                System.out.println("Finding unrated movies: " + (end - start));
                updateMyItemsText();
                getReadyForRecommendations(false);
            } else
                parseStatusLabel.setText("Error parsing movie ratings file.");
        } else if (e.getSource() == getRecBtn) {
            outputText = "";
            long start, end;
            start = System.currentTimeMillis();
            recommender.resort();
            end = System.currentTimeMillis();
            System.out.println("Resorting: " + (end - start));
            if (itemsToCheckFound) {
                switch(algorithmsList.getSelectedIndex()) {
                case 0:
                    itemsRecommended = getRecommendations(new CorrelationRecommender(mh));
                    break;
                case 1:
                    itemsRecommended = getRecommendations(new AssociationRecommender(mh));
                    break;
                case 2:
                    itemsRecommended = getRecommendations(
                        new SVDUpdateRecommender(mh, "uabase.svd"));
                    break;
                case 3:
                    itemsRecommended = getRecommendations(new IncRecommender(mh, "incmodel.dat"));
                    break;
                }
                //System.out.println("Recommendations: -----------");
                for (Item course : itemsRecommended) {
                    outputText += course.getId() + " " + course.getDescription() + " " + course.getRating() + "\n";
                    //System.out.println(course.getId() + " " + course.getDescription());
                }
                
            }
            displayOutput();
            
        } else if (e.getSource() == addItemBtn) {
            Item selectedMovie = (Item) addItemList.getSelectedItem();
            int mId = selectedMovie.getIdAsInt(); // addItemList.getSelectedIndex()+indexStart;
            int rating = Integer.parseInt(addRatingList.getSelectedItem().toString());
            if (recommender.add(newUserId, mId, rating)) {
                Item itemToAdd = new Item(String.valueOf(mId), selectedMovie.getDescription(), rating);
                // remove from moviesToCheck
                itemsToCheck = removeFromItems(itemsToCheck, itemToAdd);
                moviesRated.add(itemToAdd);
                JOptionPane.showMessageDialog(MoviesRecommenderGUI.this, "Successfully added movie " + selectedMovie.getDescription(), 
                        "Added movie", JOptionPane.INFORMATION_MESSAGE);
                getRecBtn.setEnabled(true);
                updateMyItemsText(itemToAdd);
                toggleOutputBtn.setEnabled(true);
                itemsToCheckFound = true;
            } else {
                JOptionPane.showMessageDialog(MoviesRecommenderGUI.this, "Error adding " + mId, 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } else if (e.getSource() == toggleOutputBtn) {
            if (toggleOutputBtn.getText().equals (toggleOutputBtnLabelMy)) {
                displayMyItems();
            } else {
                if (outputText.equals("")) {
                    outputTextArea.setText("No recommendations generated yet. \nPlease click Get Recommendations first.");
                    toggleOutputBtn.setText(toggleOutputBtnLabelMy);
                } else {
                    displayOutput();
                }
            }
        } else if (e.getSource() == curUserSetBtn) {
            try {
                int userId = Integer.parseInt(curUserTextField.getText());
                myItemsText = "";
                newUserId = userId;
                IntArrayList moviesRatedInts = mh.getMoviesSeenByUser(newUserId);
                moviesRated = new ArrayList<Item>();
                for (int i=0;i<moviesRatedInts.size();i++) {
                    String movieName = "";
                    int curr = moviesRatedInts.getQuick(i);
                    int mid = MemHelper.parseUserOrMovie(curr);
                    // Try to get movie name from database first
                    try {
                        movieName = mh.getMovieName(mid);
                    } catch (RuntimeException rE) {
                        movieName = "Error, movie not in DB.";
                    }
                    movieName = (movieName.equals("Error, movie not in DB.")) ? getMovieName(mid) : movieName;
                    //    public Item(String id, String description, double origRating)
                    Item m = new Item(Integer.toString(mid), movieName, MemHelper.parseRating(curr));
                    moviesRated.add(m);
                }
                itemsToCheck = findMoviesToCheck();
                updateMyItemsText();
                getReadyForRecommendations(true);
            } catch(NumberFormatException nFE) {
                JOptionPane.showMessageDialog(this, "Invalid user Id", 
                        "Invalid userId", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Reads and parses a list of movies and ratings. Stores the movies found in moviesRated
     * @return true if data successfully parsed, false otherwise
     */
    private boolean getMoviesFile(String moviesRatingsFileName) {
        moviesRated = new ArrayList<Item>();
        String[] split;
        try {
            Scanner sc = new Scanner(new File(moviesRatingsFileName));
            
            while (sc.hasNextLine()) {
                String l = sc.nextLine();
                if (!l.substring(0,1).equals("#")) {
                    split = l.split(",");
                    String mId = ""; double rating = 0; String name = "";
                    if (split.length > 2) { // the input file also contains mIds
                        mId = split[0];
                        rating = Double.parseDouble(split[2]);
                        name = split[1];
                    } else {
                        name = split[0];
                        mId = getMovieId(name);
                        rating = Double.parseDouble(split[1]);
                    }
                    moviesRated.add(new Item(mId, name, rating));
                }
            }
            sc.close();
            return true;
        }
        catch (NumberFormatException nFE) {
            System.out.println("Incorrect file format.");
        }
        catch (IOException e) {
            System.out.println("Error reading movie file:\n" + e);
        }
        return false;
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
            itemsToCheck = findMoviesToCheck();
            itemsToCheckFound = true;
            end = System.currentTimeMillis();
            System.out.println("Getting untaken courses: " + (end-start));
        }
        start = System.currentTimeMillis();
        recommender.rankMovies(newUserId, itemsToCheck);
        itemsRecommended = itemsToCheck;
        end = System.currentTimeMillis();
        System.out.println("Ranking: " + (end - start));
        return itemsRecommended;
    }

    private boolean addItem(Item m) {
        return recommender.add(newUserId, m.getIdAsInt(), (int)m.getRating());
    }
    
    private void addEntries(ArrayList<Item> movies) {
        for (Item m : movies) {
            addItem(m);
        }
    }
    
    /**
     * Gets the movie name given an integer mId
     * @param mId I
     * @return Movie name
     */
    private String getMovieName(int mId) {
        if (allItems.size() > 0)
            for (Item m : allItems) {
                if (m.getId().equals(String.valueOf(mId)))
                    return m.getDescription();
            }
        return "";
    }
    
    /**
     * Gets the id (as a String) given a movie name
     * @param movieName movie name to get id for
     * @return Id as a string
     */
    private String getMovieId(String movieName) {
        if (allItems.size() > 0) {
            for (Item i : allItems) {
                if (i.getDescription().equals(movieName)) 
                    return i.getId();
            }
        }
        return "";
    }
    
    private ArrayList<Item> findMoviesToCheck() {
        if (allItems.size() > 0) {
            return recommender.getUnratedMovies(newUserId, allItems);
        }
        return recommender.getUnratedMovies(newUserId);
    }
    
    /* (non-Javadoc)
     * @see netflix.ui.RecommenderGUI#updateMyItemsText()
     */
    protected void updateMyItemsText() {
        myItemsText = "";
        for(Item m : moviesRated) {
            myItemsText += formatItemForList(m) + "\n";
            //System.out.println(formatItemForList(m));
        }
    }
    
    /**
     * Updates the list of movies rated by the user
     * @param m
     */
    private void updateMyItemsText(Item m) {
        myItemsText += formatItemForList(m) + "\n";
    }

    /**
     * Comparator to sort movies by name
     * @author tuladhaa
     *
     */
    private class MoviesNameComparator implements Comparator<Item> {
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
    
    /**
     * Renderer of movies in the list of all movies
     * @author tuladhaa
     *
     */
    private class MovieListRenderer extends JLabel implements ListCellRenderer {

        public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
            // TODO Auto-generated method stub
            Item movie = (Item) arg1;
            String desc = (movie.getDescription().length() > MOVIE_DESC_LENGTH) ?
                    movie.getDescription().substring(0,MOVIE_DESC_LENGTH) + "..." :
                        movie.getDescription();
            setText(desc);
            return this;
        }
        
    }
}
