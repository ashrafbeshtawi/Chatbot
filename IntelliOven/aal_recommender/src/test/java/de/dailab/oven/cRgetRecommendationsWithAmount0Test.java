/**
 * 
 */
package de.dailab.oven;

import org.junit.Test;

import de.dailab.oven.aal_recommender.recommender.CollaborativeRecommender;
import de.dailab.oven.model.data_model.User;

/**
 * @author Chrismw
 *
 */
public class cRgetRecommendationsWithAmount0Test {

	@Test(expected = IllegalArgumentException.class)
	public void test() {
		
		final CollaborativeRecommender collabRecommender = new CollaborativeRecommender();
		final User user = new User();
		collabRecommender.getRecommendations(user, 0, null, null);
	}

}
