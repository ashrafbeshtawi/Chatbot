package de.dailab.oven.aal_recommender.recommender;

import javax.annotation.Nonnull;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollaborativePearsonRecommenderBuilder implements RecommenderBuilder {

	@Nonnull
	private static final Logger LOGGER = LoggerFactory.getLogger(CollaborativePearsonRecommenderBuilder.class);
	
	@Override
	public Recommender buildRecommender(final DataModel dataModel) throws TasteException {
		final double threshold = 0.9;

		LOGGER.info("Generating pearson similarity");

		final UserSimilarity pearsonSimilarity = new PearsonCorrelationSimilarity(dataModel);
		
		LOGGER.info("Calculating Neighborhoud with Threshhold: {}", threshold);

		final UserNeighborhood pearNeighbours = new ThresholdUserNeighborhood(threshold, pearsonSimilarity,dataModel);
		
		LOGGER.info("Creating Recommender...");

		return new GenericUserBasedRecommender(dataModel, pearNeighbours,
				pearsonSimilarity);
	}

}
