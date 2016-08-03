





public class Test {

	public static void main(String[] args) {
		Student a = new Student("1", "MATH");
		Student b = new Student("2", "CS");
		
		a.addClass(1999, "SP", "MATH", "103", "01", "Gail", "A+");
		b.addClass(2000, "SP", "CS", "103", "01", "Gail", "A+");
		
		System.out.println(a.computeSimilarity(b));
		
		
		
	}

}
