import java.util.*;

public class Anonymizer {
	
	HashSet<Student> students;
	public final int k = 5;
	
	public Anonymizer() {
		
	}
	
	
	private ArrayList<HashSet<Student>> cover(ArrayList<HashSet<Student>> C) {
		
		
		ArrayList<HashSet<Student>> pi = new ArrayList<HashSet<Student>>();
		HashSet<Student> maxSet = new HashSet<Student>();
		HashSet<Student> D = new HashSet<Student>();
		HashSet<Student> diff = new HashSet<Student>();
		
		
		double r;
		double maxRatio = 0;
		
		diff.addAll(students);
		
		while(D.size() != students.size()) {
			
			for(HashSet<Student> S : C) {
				
				r = computeMinSimilarity(S);
				r /= computeIntersectSize(S, diff);
				
				if(r > maxRatio)
				{
					maxRatio = r;
					maxSet = S;
				}
			}
			
			D.addAll(maxSet);
			pi.add(maxSet);		
			diff.removeAll(maxSet);
			
			
		}		
	
		return pi;
	
	}
	
	private int computeIntersectSize(HashSet<Student> S, HashSet<Student> diff) {
		
		int count = 0; 
		
		for(Student student : S)
		{
			if(diff.contains(student))
				count++;
		}
		
		return count;
	}
	
	
	private void reduce(ArrayList<HashSet<Student>> partition) {
		
		HashSet<Student> temp;
		ArrayList<HashSet<Student>> tempList = new ArrayList<HashSet<Student>>();
		tempList.addAll(partition);
		Iterator<Student> iter;
		
		for(HashSet<Student> S1 : tempList) {
			for(HashSet<Student> S2 : tempList) {
				if(!S1.equals(S2)) {
					
					for(Student student : S1) {
					
						if(S2.contains(student)) {
							if(S1.size() > k && S1.size() > S2.size())
								S1.remove(student);
							else if(S2.size() > k && S2.size() > S1.size())
								S2.remove(student);
							else {
								temp = new HashSet<Student>();
								temp.addAll(S1);
								temp.addAll(S2);
								partition.remove(S1);
								partition.remove(S2);
								partition.add(temp);	
							}
							break;
						}
					}		
				}
			}
		}	
	}
	
	
	
	private int computeMinSimilarity(HashSet<Student> S) {
		
		int minSimilarity = 1000000;
		int currentSimilarity;
		
		for(Student student1 : S) {
			
			for(Student student2 : S) {
				
				currentSimilarity = student1.computeSimilarity(student2);
				if(currentSimilarity < minSimilarity)
					minSimilarity = currentSimilarity;
				
			}
		}
				
		return minSimilarity;
		
	}
		
	private ArrayList<HashSet<Student>> computeCollection() {
		
		ArrayList<HashSet<Student>> collection = new ArrayList<HashSet<Student>>();
		HashSet<Student> subset;
		int sim, count = 0, count2 = 0, count3 = 0;
		
		for(Student c : students) {
			if(count % 100 == 0)
				System.out.println("count: " + count);
				
			count2 = 0;
			for(Student c1 : students) {
				
				subset = new HashSet<Student>();
				sim = c.computeSimilarity(c1);	
			
				count3 = 0;
				for(Student v : students) {
					if(c.computeSimilarity(v) >= sim)
						subset.add(v);
						
					count3++;
						
				}
				
				if(subset.size() >= k)
					collection.add(subset);
					
				count2++;
			}
		
			count++;
		}
		
		return collection;
	}
	
	private ArrayList<HashSet<Student>> computeCollection2() {

		ArrayList<HashSet<Student>> collection = new ArrayList<HashSet<Student>>();
		HashSet<Student> subset;
		int sim, count = 0, count2 = 0, count3 = 0;


			for(Student c : students) {
				if(count % 100 == 0)
					System.out.println("count: " + count);

				count2 = 0;
				for(int i = 1; i <= 87; i++) {


					subset = new HashSet<Student>();

					count3 = 0;
					for(Student v : students) {

						if(87 - c.computeSimilarity(v) <= i)
								subset.add(v);

						count3++;	
					}

					if(subset.size() >= k)
						collection.add(subset);

					count2++;
				}

				count++;
			}

			return collection;
		}
		
	
	public static void main(String[] args) {
		
		
		
		Timer227 timer = new Timer227();
		Anonymizer anon = new Anonymizer();
		
		
		//Set up the students array so that each entry is a
		//student who has taken more than once class. 
		Entry[] e = Parser.parseData(args[0]);		
		anon.students = Transformer.transformAndGatherData(e);
		anon.students = Transformer.removeSingles(anon.students);
	
		System.out.println(anon.students.size());
	
		timer.start();
	
		ArrayList<HashSet<Student>> temp = anon.computeCollection2();
		
		timer.stop();
		System.out.println("Time to compute collections: " + timer.getTime());
		System.out.println("Number of collections " + temp.size());
		
		timer.resetTimer();
		
		timer.start();
		ArrayList<HashSet<Student>> temp2 = anon.cover(temp);
		timer.stop();
		
		System.out.println("Time to compute cover: " + timer.getTime());
		System.out.println("Size of partition " + temp2.size());
	
		timer.resetTimer();
		
		timer.start();
		anon.reduce(temp2);
		
		timer.stop();
		
		System.out.println("Time to reduce: " + timer.getTime());
	
	}
	
}
