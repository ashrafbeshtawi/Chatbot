package de.dailab.oven.database.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import de.dailab.oven.database.AbstractDatabaseTest;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.database.validate.UserValidator;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.User;
import de.dailab.oven.model.data_model.filters.RecipeFilter;
import zone.bot.vici.Language;

public class IndividualOvenSettingsQueryTest extends AbstractDatabaseTest {
	
	private Graph graph;
	private IndividualOvenSettingsQuery iOSQ;
	private Query query;
	private UserQuery userQuery;
	private static final UserValidator VALIDATOR = new UserValidator();
	
	@Override
	public void initialize()
            throws DatabaseException, ConfigurationException {
		this.graph = this.getGraph();
		query = new Query(this.graph);
		this.iOSQ = new IndividualOvenSettingsQuery(this.graph);
		this.userQuery = new UserQuery(this.graph);
	}

	@After
	public void close() {	
		if(this.iOSQ != null) {
			this.iOSQ.close();			
		}
	}
	
	@Test
	public void getAverageTempTimeServingFactorsValidTest() {
		
		User user = new User();
		String userName = "tmpovensettingsuser"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.GERMAN);
		assertTrue(VALIDATOR.isValid(user));
		RecipeFilter recipeFilter = new RecipeFilter();
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		recipeFilter.setMaxNumberOfRecipesToParsePerLanguage(10);
		
		try {
			user = this.userQuery.putAndGetUser(user);
			assertTrue(user.getId() != -1);
			List<Recipe> recipesToRate = this.query.getRecipe(recipeFilter);
			assertEquals(10, recipesToRate.size());
			for(Recipe recipe: recipesToRate) {
				assertTrue(this.iOSQ.setIndividualOvenSettings(recipe.getId(), 
						user.getId(), 5.0d, 10.0d, 15.0d));
				recipe.addUserRating(user.getId(), 10);
				this.query.putSingleRecipe(recipe);
			}
			
			Map<String, Double> avgFactors = this.iOSQ.getAverageTempTimeServingFactor(user.getId());
			double time = avgFactors.get(IndividualOvenSettingsQuery.TIME);
			double temp = avgFactors.get(IndividualOvenSettingsQuery.TEMP);
			double serving = avgFactors.get(IndividualOvenSettingsQuery.SERVING);
			assertTrue(Math.abs(time - 5.0d) < 0.001);
			assertTrue(Math.abs(temp - 10.0d) < 0.001);
			assertTrue(Math.abs(serving - 15.0d) < 0.001);
			this.userQuery.deleteUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getAverageTempTimeServingFactorsNoRelationshipsTest() {
		User user = new User();
		String userName = "tmpovensettingsuser"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.GERMAN);
		assertTrue(VALIDATOR.isValid(user));
		try {
			user = this.userQuery.putAndGetUser(user);
			assertTrue(user.getId() != -1);
			
			Map<String, Double> avgFactors = this.iOSQ.getAverageTempTimeServingFactor(user.getId());
			double time = avgFactors.get(IndividualOvenSettingsQuery.TIME);
			double temp = avgFactors.get(IndividualOvenSettingsQuery.TEMP);
			double serving = avgFactors.get(IndividualOvenSettingsQuery.SERVING);
			assertTrue(Math.abs(time - 1.0d) < 0.001);
			assertTrue(Math.abs(temp - 1.0d) < 0.001);
			assertTrue(Math.abs(serving - 1.0d) < 0.001);
			this.userQuery.deleteUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getAverageTempTimeServingFactorsPartlyValidTest() {
		
		User user = new User();
		String userName = "tmpovensettingsuser"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.GERMAN);
		RecipeFilter recipeFilter = new RecipeFilter();
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		recipeFilter.setMaxNumberOfRecipesToParsePerLanguage(10);
		assertTrue(VALIDATOR.isValid(user));
		try {
			user = this.userQuery.putAndGetUser(user);
			assertTrue(user.getId() != -1);
			List<Recipe> recipesToRate = this.query.getRecipe(recipeFilter);
			assertEquals(10, recipesToRate.size());
			for(Recipe recipe: recipesToRate) {
				assertTrue(this.iOSQ.setTempFactor(recipe.getId(), user.getId(), 10.0d));
				assertTrue(this.iOSQ.setServingFactor(recipe.getId(), user.getId(), 15.0d));
				recipe.addUserRating(user.getId(), 10);
				this.query.putSingleRecipe(recipe);
			}
			
			Map<String, Double> avgFactors = this.iOSQ.getAverageTempTimeServingFactor(user.getId());
			double time = avgFactors.get(IndividualOvenSettingsQuery.TIME);
			double temp = avgFactors.get(IndividualOvenSettingsQuery.TEMP);
			double serving = avgFactors.get(IndividualOvenSettingsQuery.SERVING);
			assertTrue(Math.abs(time - 1.0d) < 0.001);
			assertTrue(Math.abs(temp - 10.0d) < 0.001);
			assertTrue(Math.abs(serving - 15.0d) < 0.001);
			this.userQuery.deleteUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getAverageTempTimeServingFactorsInValidTest() {
		
		try {	
			Map<String, Double> avgFactors = this.iOSQ.getAverageTempTimeServingFactor(-1l);
			double time = avgFactors.get(IndividualOvenSettingsQuery.TIME);
			double temp = avgFactors.get(IndividualOvenSettingsQuery.TEMP);
			double serving = avgFactors.get(IndividualOvenSettingsQuery.SERVING);
			assertTrue(Math.abs(time) < 0.001);
			assertTrue(Math.abs(temp) < 0.001);
			assertTrue(Math.abs(serving) < 0.001);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void resetGraphTest() {
		assertTrue(this.iOSQ.getGraph() != null);
		this.iOSQ.setGraph(null);
		assertEquals(null, this.iOSQ.getGraph());
		this.iOSQ.setGraph(this.graph);
		assertEquals(this.graph, this.iOSQ.getGraph());
	}
	
	@Test
	public void getIndividualOvenSettingsValidTest() {
		User user = new User();
		String userName = "tmpovensettingsuser"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.GERMAN);
		RecipeFilter recipeFilter = new RecipeFilter();
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		recipeFilter.setMaxNumberOfRecipesToParsePerLanguage(1);
		assertTrue(VALIDATOR.isValid(user));
		try {
			user = this.userQuery.putAndGetUser(user);
			assertTrue(user.getId() != -1);
			Recipe recipeToRate = this.query.getRecipe(recipeFilter).get(0);
			assertTrue(recipeToRate != null);	
			assertTrue(this.iOSQ.setIndividualOvenSettings(recipeToRate.getId(), 
					user.getId(), 5.0d, 10.0d, 15.0d));
			recipeToRate.addUserRating(user.getId(), 10);
			this.query.putSingleRecipe(recipeToRate);
		
			Map<String, Double> factors = this.iOSQ.getIndividualOvenSettings(
					recipeToRate.getId(), user.getId());
			
			double time = factors.get(IndividualOvenSettingsQuery.TIME);
			double temp = factors.get(IndividualOvenSettingsQuery.TEMP);
			double serving = factors.get(IndividualOvenSettingsQuery.SERVING);
			assertTrue(Math.abs(time - 5.0d) < 0.001);
			assertTrue(Math.abs(temp - 10.0d) < 0.001);
			assertTrue(Math.abs(serving - 15.0d) < 0.001);
			
			this.userQuery.deleteUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getIndividualOvenSettingsNoRelationshipTest() {
		User user = new User();
		String userName = "tmpovensettingsuser"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.GERMAN);
		
		RecipeFilter recipeFilter = new RecipeFilter();
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		recipeFilter.setMaxNumberOfRecipesToParsePerLanguage(1);
		assertTrue(VALIDATOR.isValid(user));
		try {
			user = this.userQuery.putAndGetUser(user);
			assertTrue(user.getId() != -1);
			Recipe recipeToRate = this.query.getRecipe(recipeFilter).get(0);
			assertTrue(recipeToRate != null);	
			
			Map<String, Double> factors = this.iOSQ.getIndividualOvenSettings(recipeToRate.getId(), user.getId());
			double time = factors.get(IndividualOvenSettingsQuery.TIME);
			double temp = factors.get(IndividualOvenSettingsQuery.TEMP);
			double serving = factors.get(IndividualOvenSettingsQuery.SERVING);
			assertTrue(Math.abs(time - 1.0d) < 0.001);
			assertTrue(Math.abs(temp - 1.0d) < 0.001);
			assertTrue(Math.abs(serving - 1.0d) < 0.001);
			this.userQuery.deleteUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getIndividualOvenSettingsPartlyValidTest() {
		User user = new User();
		String userName = "tmpovensettingsuser"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.GERMAN);
		
		RecipeFilter recipeFilter = new RecipeFilter();
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		recipeFilter.setMaxNumberOfRecipesToParsePerLanguage(1);
		assertTrue(VALIDATOR.isValid(user));
		try {
			user = this.userQuery.putAndGetUser(user);
			assertTrue(user.getId() != -1);
			Recipe recipeToRate = this.query.getRecipe(recipeFilter).get(0);
			assertTrue(recipeToRate != null);	
			assertTrue(this.iOSQ.setTimeFactor(recipeToRate.getId(), user.getId(), 5.0d));
			recipeToRate.addUserRating(user.getId(), 10);
			this.query.putSingleRecipe(recipeToRate);
			
			Map<String, Double> factors = this.iOSQ.getIndividualOvenSettings(recipeToRate.getId(), user.getId());
			double time = factors.get(IndividualOvenSettingsQuery.TIME);
			double temp = factors.get(IndividualOvenSettingsQuery.TEMP);
			double serving = factors.get(IndividualOvenSettingsQuery.SERVING);
			assertTrue(Math.abs(time - 5.0d) < 0.001);
			assertTrue(Math.abs(temp - 1.0d) < 0.001);
			assertTrue(Math.abs(serving - 1.0d) < 0.001);
			this.userQuery.deleteUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getIndividualOvenSettingsInvalidTest() {
		User user = new User();
		String userName = "tmpovensettingsuser"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.GERMAN);
		assertTrue(VALIDATOR.isValid(user));
		RecipeFilter recipeFilter = new RecipeFilter();
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		recipeFilter.setMaxNumberOfRecipesToParsePerLanguage(1);
		
		try {
			user = this.userQuery.putAndGetUser(user);
			assertTrue(user.getId() != -1);
			Recipe recipeToRate = this.query.getRecipe(recipeFilter).get(0);
			assertTrue(recipeToRate != null);	
			assertTrue(this.iOSQ.setTimeFactor(recipeToRate.getId(), user.getId(), 5.0d));
			recipeToRate.addUserRating(user.getId(), 10);
			this.query.putSingleRecipe(recipeToRate);
			
			Map<String, Double> factors = this.iOSQ.getIndividualOvenSettings(user.getId(), user.getId());
			double time = factors.get(IndividualOvenSettingsQuery.TIME);
			double temp = factors.get(IndividualOvenSettingsQuery.TEMP);
			double serving = factors.get(IndividualOvenSettingsQuery.SERVING);
			assertTrue(Math.abs(time) < 0.001);
			assertTrue(Math.abs(temp) < 0.001);
			assertTrue(Math.abs(serving) < 0.001);
			
			factors = this.iOSQ.getIndividualOvenSettings(recipeToRate.getId(), recipeToRate.getId());
			time = factors.get(IndividualOvenSettingsQuery.TIME);
			temp = factors.get(IndividualOvenSettingsQuery.TEMP);
			serving = factors.get(IndividualOvenSettingsQuery.SERVING);
			assertTrue(Math.abs(time) < 0.001);
			assertTrue(Math.abs(temp) < 0.001);
			assertTrue(Math.abs(serving) < 0.001);
			this.userQuery.deleteUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getSetSingleSettingsValidTest() {
		User user = new User();
		String userName = "tmpovensettingsuser"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.GERMAN);
		assertTrue(VALIDATOR.isValid(user));
		RecipeFilter recipeFilter = new RecipeFilter();
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		recipeFilter.setMaxNumberOfRecipesToParsePerLanguage(1);
		
		try {
			user = this.userQuery.putAndGetUser(user);
			assertTrue(user.getId() != -1);
			Recipe recipeToRate = this.query.getRecipe(recipeFilter).get(0);
			assertTrue(recipeToRate != null);	
			assertTrue(this.iOSQ.setTimeFactor(recipeToRate.getId(), user.getId(), 5.0d));
			assertTrue(this.iOSQ.setTempFactor(recipeToRate.getId(), user.getId(), 10.0d));
			assertTrue(this.iOSQ.setServingFactor(recipeToRate.getId(), user.getId(), 15.0d));
			
			recipeToRate.addUserRating(user.getId(), 10);
			this.query.putSingleRecipe(recipeToRate);
			
			
			double time = this.iOSQ.getTimeFactor(recipeToRate.getId(), user.getId());
			double temp = this.iOSQ.getTempFactor(recipeToRate.getId(), user.getId());
			double serving = this.iOSQ.getServingFactor(recipeToRate.getId(), user.getId());
			assertTrue(Math.abs(time - 5.0d) < 0.001);
			assertTrue(Math.abs(temp - 10.0d) < 0.001);
			assertTrue(Math.abs(serving - 15.0d) < 0.001);
	
			this.userQuery.deleteUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getSetSingleSettingsInValidTest() {
		User user = new User();
		String userName = "tmpovensettingsuser"; 
		user.setName(userName);
		user.setCurrentlySpokenLanguage(Language.GERMAN);
		assertTrue(VALIDATOR.isValid(user));
		RecipeFilter recipeFilter = new RecipeFilter();
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		recipeFilter.setMaxNumberOfRecipesToParsePerLanguage(1);
		
		try {
			user = this.userQuery.putAndGetUser(user);
			assertTrue(user.getId() != -1);
			Recipe recipeToRate = this.query.getRecipe(recipeFilter).get(0);
			assertTrue(recipeToRate != null);	
			
			assertFalse(this.iOSQ.setTimeFactor(recipeToRate.getId(), recipeToRate.getId(), 5.0d));
			assertFalse(this.iOSQ.setTimeFactor(user.getId(), user.getId(), 5.0d));
			assertTrue(this.iOSQ.setTimeFactor(recipeToRate.getId(), user.getId(), 5.0d));
			
			assertFalse(this.iOSQ.setTempFactor(recipeToRate.getId(), recipeToRate.getId(), 10.0d));
			assertFalse(this.iOSQ.setTempFactor(user.getId(), user.getId(), 10.0d));
			assertTrue(this.iOSQ.setTempFactor(recipeToRate.getId(), user.getId(), 10.0d));
			
			assertFalse(this.iOSQ.setServingFactor(recipeToRate.getId(), recipeToRate.getId(), 15.0d));
			assertFalse(this.iOSQ.setServingFactor(user.getId(), user.getId(), 15.0d));
			assertTrue(this.iOSQ.setServingFactor(recipeToRate.getId(), user.getId(), 15.0d));
			
			recipeToRate.addUserRating(user.getId(), 10);
			this.query.putSingleRecipe(recipeToRate);
			
			
			double time = this.iOSQ.getTimeFactor(recipeToRate.getId(), recipeToRate.getId());
			double temp = this.iOSQ.getTempFactor(recipeToRate.getId(), recipeToRate.getId());
			double serving = this.iOSQ.getServingFactor(recipeToRate.getId(), recipeToRate.getId());
			assertTrue(Math.abs(time) < 0.001);
			assertTrue(Math.abs(temp) < 0.001);
			assertTrue(Math.abs(serving) < 0.001);
	
			
			time = this.iOSQ.getTimeFactor(user.getId(), user.getId());
			temp = this.iOSQ.getTempFactor(user.getId(), user.getId());
			serving = this.iOSQ.getServingFactor(user.getId(), user.getId());
			assertTrue(Math.abs(time) < 0.001);
			assertTrue(Math.abs(temp) < 0.001);
			assertTrue(Math.abs(serving) < 0.001);
	
			this.userQuery.deleteUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void nullTest() {
		IndividualOvenSettingsQuery nullQuery = new IndividualOvenSettingsQuery(null);
		assertEquals(null, nullQuery.getGraph());
		assertTrue(nullQuery.getAverageTempTimeServingFactor(100l).isEmpty());
		assertTrue(nullQuery.getIndividualOvenSettings(100l, 100l).isEmpty());
		assertTrue(Math.abs(nullQuery.getTimeFactor(100l, 100l) - 1.0d) < 0.001d);
		assertTrue(Math.abs(nullQuery.getTempFactor(100l, 100l) - 1.0d) < 0.001d);
		assertTrue(Math.abs(nullQuery.getServingFactor(100l, 100l) - 1.0d) < 0.001d);
		assertFalse(nullQuery.setIndividualOvenSettings(100l, 100l, 1.0, 1.0, 1.0));
		assertFalse(nullQuery.setTimeFactor(100l, 100l, 1.0));
		assertFalse(nullQuery.setTempFactor(100l, 100l, 1.0));
		assertFalse(nullQuery.setServingFactor(100l, 100l, 1.0));
	}
}
