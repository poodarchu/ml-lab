package netflix.ui;

public class Item {
    protected String id;
    protected String description;
    protected double rating;
    
    public Item(){}
    public Item(String id, String description, double origRating) {
        this.id = id;
        this.description = description;
        this.rating = origRating;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getId() {
        return id;
    }
    public double getRating() {
        return rating;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }
    
    public int getIdAsInt() {
        return Integer.parseInt(id);
    }
    
    public String toString() {
        return "#" + id + ": " + description + ": " + rating;
    }
}
