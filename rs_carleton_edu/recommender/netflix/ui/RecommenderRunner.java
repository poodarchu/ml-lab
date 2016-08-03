package netflix.ui;

import java.util.ArrayList;

/**
 * A sample driver program to start a recommender GUI
 * @author tuladhaa
 *
 */
public class RecommenderRunner {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java RecommenderRunner <MemHelperFile> [all courses / movies file] [c/m]");
        }
        String memHelperFile = args[0];
        String descriptionsFile = (args.length > 1) ? args[1] : "";
        boolean isRecommendMovies = (args.length > 2) ? (args[2].equals("m")) : false;
        
        ArrayList<Item> moviesToRate = new ArrayList<Item>();
        String[] movies = {"Test", "Test test", "Test", "Test test test"};
        for (int i=0; i<movies.length; i++) {
            moviesToRate.add(new Item("", movies[i], 0.0));
        }
        RecommenderGUI userInputFrame = null;
        if (isRecommendMovies) {
            userInputFrame = new MoviesRecommenderGUI(memHelperFile, descriptionsFile);
        } else {
            userInputFrame = new CoursesRecommenderGUI(memHelperFile, descriptionsFile);
        }
        userInputFrame.setVisible(true);
    }
}
