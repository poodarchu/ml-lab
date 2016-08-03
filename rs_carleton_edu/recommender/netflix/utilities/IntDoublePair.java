package netflix.utilities;

import java.io.Serializable;

/**
 * Simple pair consisting of an integer (id) and a double (rating). 
 * The natural ordering ont IntDoublePair objects sorts them by 
 * the double value b. 
 *
 * @author Ben Sowell
 */
public class IntDoublePair implements Comparable<IntDoublePair>, Serializable{
    /**
	 * Serializable added by steinbel to work with PD.
	 */
	private static final long serialVersionUID = -4468069122793756417L;
	public int a;
    public double b;
	
    /**
     * @param a The int value
     * @param b The double value
     */
    public IntDoublePair(int a, double b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Compares IntDoublePair to another
     * IntDoublePair o using the value of b. 
     * 
     * @param o The IntDoublePair to compare to. 
     * @return -1 if this.b < o.b
     *          0 if this.b = o.b
     *          1 if this.b > o.b
     */
    public int compareTo(IntDoublePair o) {
        if(this.b < o.b){
            return -1;
        }
        else if(this.b == o.b){
            return 0;
        }
        else {
            return 1;
        }
    }
    
    /**
     * Tests whether another object is equal 
     * to this one. Consistent with the natural 
     * ordering. 
     *
     * @param obj The object to compare to. 
     * @return true if obj is an instance of 
     *              IntDoublePair and 
     *              this.b = obj.b.
     *         false otherwise.
     */
    public boolean equals(Object obj) {
        if(obj instanceof IntDoublePair) {
            IntDoublePair other = (IntDoublePair) obj;
            return (other.b == this.b);
        }
        return false;
    }

}
