package netflix.ui;

import java.util.Comparator;

public class ItemComparator implements Comparator<Item> {

    /**
     * Sorts on:
     * 1. rating
     * 2. id
     * 3. description
     */
    public int compare(Item a, Item b) {
        if (a.rating < b.rating)
            return 1;
        else if (a.rating > b.rating)
            return -1;
        
        if(a.id.compareTo(b.id) < 0)
            return 1;
        else if(a.id.compareTo(b.id) > 0)
            return -1;
        
        if(a.description.compareTo(b.description) < 0)
            return 1;
        else if(a.description.compareTo(b.description) > 0)
            return -1;
        
        return 0;
    }

}
