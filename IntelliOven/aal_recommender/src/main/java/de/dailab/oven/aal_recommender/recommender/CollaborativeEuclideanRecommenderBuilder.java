package de.dailab.oven.aal_recommender.recommender;

import javax.annotation.Nonnull;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollaborativeEuclideanRecommenderBuilder implements RecommenderBuilder {

	@Nonnull
	private static final Logger LOGGER = LoggerFactory.getLogger(CollaborativeEuclideanRecommenderBuilder.class);
	
	@Override
	public Recommender buildRecommender(final DataModel dataModel) throws TasteException {
		final double threshold = 0.8;

		LOGGER.info("Generating euclidean similarity");

		final UserSimilarity euclideanSimilarity = new EuclideanDistanceSimilarity(dataModel);
		
		LOGGER.info("Calculating Neighborhoud with Threshhold: {}", threshold);

		final UserNeighborhood eucNeighbours = new ThresholdUserNeighborhood(threshold, euclideanSimilarity,dataModel);
		
		LOGGER.info("Creating Recommender...");

		return new GenericUserBasedRecommender(dataModel, eucNeighbours, euclideanSimilarity);
	}

}
