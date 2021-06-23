/**
 * 
 */
package de.dailab.oven;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import de.dailab.oven.aal_recommender.recommender.CollaborativeRecommender;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.User;

/**
 * @author Chrismw
 *
 */
public class cRgetRecommendationsAllNormal {

	@Ignore
	@Test
	public void test() {
		final CollaborativeRecommender cr = new CollaborativeRecommender();
		final User user = new User();
		
		user.setId(1533);
		final String[] desired = {"chicken"};
		final String[] unwanted = {"broccoli", "orange"};
		final Recipe[] res = cr.getRecommendations(user, 4, desired, unwanted);
		System.out.println("Amount of recipes: " + res.length);
		for (final Recipe r : res) {
			System.out.println(r.getName());
		}
		assertTrue("Amount of recommendations doesn't equal expected value.", 4 >= res.length);
	}
}
