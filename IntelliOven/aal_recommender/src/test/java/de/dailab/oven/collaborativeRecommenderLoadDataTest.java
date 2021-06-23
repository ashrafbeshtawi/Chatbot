/**
 * 
 */
package de.dailab.oven;

import static org.junit.Assert.fail;

import org.junit.Test;

import de.dailab.oven.aal_recommender.recommender.CollaborativeRecommender;

/**
 * @author Chrismw
 *
 */
public class collaborativeRecommenderLoadDataTest {

	@Test
	public void test() {
		final CollaborativeRecommender collabRecommender = new CollaborativeRecommender();
		try{
		      collabRecommender.loadData();
		      //Even if the path were wrong, the code would correctly catch its own exception
		   }
		   catch(final Exception e){
		      fail("Should not have thrown any exception");
		   }
	}

}
