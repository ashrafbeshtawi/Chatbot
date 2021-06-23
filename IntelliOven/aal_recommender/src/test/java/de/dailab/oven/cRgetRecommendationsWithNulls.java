/**
 * 
 */
package de.dailab.oven;

import org.junit.Test;

import de.dailab.oven.aal_recommender.recommender.CollaborativeRecommender;

/**
 * @author Chrismw
 *
 */
public class cRgetRecommendationsWithNulls {

	/**
	 * Tests 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void test() {
		
		final CollaborativeRecommender collabRecommender = new CollaborativeRecommender();
		collabRecommender.getRecommendations(null, 0, null, null);
	}

}
