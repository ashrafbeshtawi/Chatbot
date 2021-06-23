package de.dailab.oven.aal_recommender.recommender;

import javax.annotation.Nonnull;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollaborativeUncenteredCosineRecommenderBuilder implements RecommenderBuilder {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(CollaborativeUncenteredCosineRecommenderBuilder.class);
	
	@Override
	public Recommender buildRecommender(final DataModel dataModel) throws TasteException {
		final double threshold = 0.9;

		LOG.info("Generating uncentered cosine similarity");
		final UserSimilarity uncCosineSimilarity = new UncenteredCosineSimilarity(dataModel);
		
		LOG.info("Calculating Neighborhoud with Threshhold: {}", threshold);
		final UserNeighborhood uncCosineNeighbours = new ThresholdUserNeighborhood(threshold, uncCosineSimilarity,dataModel);
		
		LOG.info("Creating Recommender...");
		return new GenericUserBasedRecommender(dataModel, uncCosineNeighbours,	uncCosineSimilarity);
		
	}
}
