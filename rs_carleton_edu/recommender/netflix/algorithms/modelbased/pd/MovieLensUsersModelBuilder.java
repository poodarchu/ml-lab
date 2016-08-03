package netflix.algorithms.modelbased.pd;

import netflix.algorithms.modelbased.itembased.ItemBasedModelBuilder;
import netflix.algorithms.modelbased.itembased.method.*;
import netflix.algorithms.modelbased.reader.*;
import netflix.algorithms.modelbased.writer.*;
import netflix.memreader.MemHelper;

public class MovieLensUsersModelBuilder {

	/**
	 * @author leahsteinberg
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			for (int i=1; i<6; i++) {
				String memHelperFile = "u"+ i + "base.dat";
				String outputFile = "movielens_" + i +".dat";
				//todo: args options
				DataReader dr = new DataReaderFromMem(new MemHelper(memHelperFile));
				SimilarityWriter sw = new UserSimKeeper();
				//TODO: allow change here
				SimilarityMethod sm = new AdjCosineSimilarityMethod();
				sm.setNumMinMovies(3);
				ItemBasedModelBuilder userModelBuilder = 
					new ItemBasedModelBuilder(dr, sw, sm);
				userModelBuilder.setFileName(outputFile);
				userModelBuilder.buildModel(true, true);
				dr.close();
				//don't need to close the simKeeper because we only serialize with it and
				//that closes its own writers
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

}
