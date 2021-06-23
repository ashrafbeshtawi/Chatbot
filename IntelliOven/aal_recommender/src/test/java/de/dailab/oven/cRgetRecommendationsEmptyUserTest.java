package de.dailab.oven;

import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.dailab.oven.aal_recommender.recommender.CollaborativeRecommender;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.User;

public class cRgetRecommendationsEmptyUserTest {
	/**
	 * Since the user doesn't exist, the result should be null
	 */
	@Test
	public void test() {
		
		final CollaborativeRecommender collabRecommender = new CollaborativeRecommender();
		final User user = new User();
		final String[] desired = {"Carrot"};
		final String[] unwanted = {"Broccoli", "Chicken"};
		final Recipe[] res = collabRecommender.getRecommendations(user, 1, desired, unwanted);
		assertNull(res);
	}

}
