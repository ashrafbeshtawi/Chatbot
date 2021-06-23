package de.dailab.oven.database;

import de.dailab.oven.database.configuration.ConfigurationLoader;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.database.query.RecipeQuery;
import de.dailab.oven.database.recommender.RecommendBasedOnIngredientPreferences;
import de.dailab.oven.model.data_model.*;
import de.dailab.oven.model.data_model.filters.RecipeFilter;
import org.jsoup.internal.StringUtil;
import org.junit.Test;
import zone.bot.vici.Language;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class DatabaseTest extends AbstractDatabaseTest {
	
	Query query;
	HouseholdController house;
	
	@Override
	public void initialize()
            throws DatabaseException, ConfigurationException {
		this.query = new Query();
		this.house = new HouseholdController(this.query.getGraph());
	}


	@Test
	public void testLanguage() {
		System.out.println("test1");
		final User user = new User();
		user.setCurrentlySpokenLanguage("de");
	}
	
//	@Test
//	public void queryTest() throws Exceptiony {
//		Query query = new Query(super.getGraph());
//		query.close();
//	}

	//GGF CHECK ERGÄNZEN
//	@Test
	public void QueryPutUserTest() throws Exception {
		
		final User user = new User();
		user.setName("Mark");
		user.addPreferredCategory(new Category("testCategory1"));
		user.addPreferredCategory(new Category("testCategory2"));
		user.addIncompatibleIngredient("testingredient3");
		user.addLikedIngredient("testingredient1");
//		user.addRecipeRating(463, 9);
		user.setCurrentlySpokenLanguage("engl");
		this.query.putUser(user);
		this.query.close();
		
	}
	
//	@Test
	public void putUserFromDatabaseTest() {
		try {
			final List<User> users = this.query.getUser("", -1);
			final User user = users.get(0);
			System.out.println(user.getIncompatibleIngredients());
			user.addIncompatibleIngredient(new Ingredient("hefe", Language.GERMAN));
			this.query.putUser(user);
			System.out.println(this.query.getUser(user.getId()).getIncompatibleIngredients());
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void putUserWithAllergyTest() {
		try {
			final List<User> users = this.query.getUser("usernae", -1);
			final User user = users.get(0);
			//System.out.println(user.getName());
			
//			user.addLanguageToSpokenLanguages(Language.ENGLISH.getLangCode2());
			user.setCurrentlySpokenLanguage(Language.ENGLISH.getLangCode2());
			//System.out.println(user.getIncompatibleIngredients().size());
//			user.addIncompatibleIngredient("beef");
//			query.putUser(user);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//GGF CHECK ERGÄNZEN
	@Test
	public void QueryPutRecipeTest() throws Exception {
		System.out.println("putRecipeTest");
		final Recipe recipe = new Recipe();
		recipe.setAuthor("Testauthor");
		recipe.setLanguage("en");
		recipe.addCategory(new Category("testCategory1"));
		recipe.addCategory(new Category("testCategory2"));
		recipe.addCategory(new Category("testCategory3"));
		recipe.addDurationToListOfDurations("total", 1, 30);
		recipe.addDurationToListOfDurations("preparation", 0, 30);
		recipe.addDurationToListOfDurations("baking", 1, 30);
		recipe.setFoodLabel(FoodLabel.GREEN);
		final Ingredient ingredient1 = new Ingredient("testIngredient1", recipe.getLanguage());
		final IngredientWithAmount ingredientWithAmount1 = new IngredientWithAmount(ingredient1, 32, Unit.GRAM);
		recipe.addIngredientToListOfIngredients(ingredientWithAmount1);
		final Ingredient ingredient2 = new Ingredient("testIngredient2", recipe.getLanguage());
		final IngredientWithAmount ingredientWithAmount2 = new IngredientWithAmount(ingredient2, 10, Unit.GRAM);
		recipe.addIngredientToListOfIngredients(ingredientWithAmount2);
		recipe.addInstruction("Do this");
		recipe.addInstruction("Do that");
		recipe.setName("TestRecipe1");
		recipe.setOriginalServings(42);
		recipe.setUrl("http://www.nocooking.com/");
//		recipe.addUserRating(3238, 10);
		this.query.putSingleRecipe(recipe);
		this.query.close();
	}

//	@Test
	public void clearGraph() throws Exception{
		final Query query = new Query();
		query.clearGraph();
		query.close();
	}

	@Test
	public void getRecipeByIdTest() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeById");
		
		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		
		recipeFilter.setRecipeId(3788l);
				
		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeByNameTest() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByName");
		
//		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
//		recipeFilter.addRecipeLanguage(Language.GERMAN);
		
		recipeFilter.setRecipeName("avcı usulü sülün");
				
		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeByRequiredCategories() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByRequiredCategories");
		
//		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
//		recipeFilter.addRecipeLanguage(Language.GERMAN);
		
		recipeFilter.addRequiredCategory(new Category("vegan"));
//		recipeFilter.addRequiredCategory("kinder");
//		recipeFilter.addRequiredIngredient("et");
		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				for(final Recipe recipe : recipes) {
					//System.out.println(RecipePrinter.recipeToString(recipe));
				}
//				Recipe recipe = recipes.get(0);
//				//System.out.println(RecipePrinter.recipeToString(recipe);
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeByPossibleCategories() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByPossibleCategories");
		
		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
		recipeFilter.addRecipeLanguage(Language.GERMAN);
			
		recipeFilter.addPossibleCategory(new Category("party"));
		recipeFilter.addPossibleCategory(new Category("kinder"));
		
		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeByCategoriesToAvoid() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByCategoriesToAvoid");
		
		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
		recipeFilter.addRecipeLanguage(Language.GERMAN);
				
//		recipeFilter.addExcludedCategory("vegan");
		recipeFilter.addExcludedCategory(new Category("meat"));

		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeRequiredIngredients() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByRequiredIngredients");
		
//		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
//		recipeFilter.addRecipeLanguage(Language.GERMAN);
		
//		recipeFilter.addRequiredIngredient("lammfleisch");
//		recipeFilter.addRequiredIngredient("kırıntıları");
	
		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeByPossibleIngredients() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByPossibleIngredients");
		
		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		
//		recipeFilter.addPossibleIngredient("salt");
//		recipeFilter.addPossibleIngredient("water");
//		
		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeByIngredientstoAvoid() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByIngredientsToAvoid");
		
		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
		recipeFilter.addRecipeLanguage(Language.GERMAN);
//		
//		recipeFilter.addExcludedIngredient("salt");
//		recipeFilter.addExcludedIngredient("water");
//		recipeFilter.addExcludedIngredient("bütün hindi");
//		
		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeByPossibleAuthors() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByPossibleAuthors");
		
		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		
		recipeFilter.addPossibleAuthor("chefkoch");

		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeByAuthorsToAvoid() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByAuthorsToAvoid");

		recipeFilter.addRecipeLanguage(Language.GERMAN);
			
		recipeFilter.addExcludedAuthor("chefkoch");

		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeByDuration() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByDuration");
		
		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		
		recipeFilter.setCookedWithin(Duration.ZERO.plusMinutes(60));

		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeByFoodLabel() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByFoodLabel");
		
		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
		recipeFilter.addRecipeLanguage(Language.GERMAN);
		
		recipeFilter.setIsFoodLabel(FoodLabel.YELLOW);
		
		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}

	@Test
	public void getRecipeByOriginalServings() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByOriginalServings");
		
		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
		recipeFilter.addRecipeLanguage(Language.GERMAN);
				
		recipeFilter.setOriginalServings(4);

		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
	}
	
	@Test
	public void getRecipeByLimit() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("getRecipeByLimit");
		
		recipeFilter.addRecipeLanguage(Language.ENGLISH);
		recipeFilter.addRecipeLanguage(Language.TURKISH);
		recipeFilter.addRecipeLanguage(Language.GERMAN);
			
		recipeFilter.setMaxNumberOfRecipesToParsePerLanguage(10);

		try {
			final List<Recipe> recipes = this.query.getRecipe(recipeFilter);
			//System.out.println("Parsed recipes: " + recipes.size());
			if(!recipes.isEmpty()) {
				final Recipe recipe = recipes.get(0);
				//System.out.println(RecipePrinter.recipeToString(recipe));
			}
		} catch (final InputException e) {
			//System.out.println(e);
		}
		
		recipeFilter.reset();
	}
	
	@Test
	public void recommenderTest() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		System.out.println("rectest 1");
		final User user = new User();
		user.addLikedIngredient("salz");
		user.addLikedIngredient("wasser");
		user.addLikedIngredient("zucker");
		user.addLikedIngredient("zimtpulver");
		
		
//		try {
//			List<Recipe> recipes = query.getRecipe(recipeFilter);
//			if(!recipes.isEmpty()) {
				final RecommendBasedOnIngredientPreferences rbo = new RecommendBasedOnIngredientPreferences(user, this.query, recipeFilter);
				final Set<Recipe> topFive = rbo.recommendTopFive();
				for(final Recipe recipe : topFive) {
					//System.out.println(RecipePrinter.recipeToString(recipe));
				}
//			}
//		} catch (InputException e) {
//			//System.out.println(e);
//		}
	}
	
	@Test
	public void recommenderTest2() throws Exception {
		final RecipeFilter recipeFilter = new RecipeFilter();
		final User user = new User();
		System.out.println("rectest 2");
		user.addLikedIngredient("salz");
		user.addLikedIngredient("wasser");
		user.addLikedIngredient("zucker");
		user.addLikedIngredient("zimtpulver");
		
		
//		try {
//			List<Recipe> recipes = query.getRecipe(recipeFilter);
//			if(!recipes.isEmpty()) {
				final RecommendBasedOnIngredientPreferences rbo = new RecommendBasedOnIngredientPreferences(user, this.query, recipeFilter);
				final Set<Recipe> topFive = rbo.recommendTopFive();
				for(final Recipe recipe : topFive) {
					//System.out.println(RecipePrinter.recipeToString(recipe));
				}
////			}
//		} catch (InputException e) {
//			//System.out.println(e);
//		}
	}
	
	@Test
	public void testConfigLoader() {
		System.out.println("configLtTest");
		final ConfigurationLoader configLoader = new ConfigurationLoader();
		//System.out.println(configLoader.getUri());
		//System.out.println(configLoader.getUser());
		//System.out.println(configLoader.getPw());
	}
	
	@Test
	public void testAddCookedRecipe() throws Exception {
		System.out.println("cooked recipe");
	 	final Query query = new Query();
	 	final RecipeFilter recipeFilter = new RecipeFilter();
	 	recipeFilter.setMaxNumberOfRecipesToParsePerLanguage(1);
	 	final List<Recipe> recipes = query.getRecipe(recipeFilter);
	 	if(!recipes.isEmpty()) {
	 		final Recipe recipe = recipes.get(0);
	 		User user = new User();
	 		user.setName("testUser");
	 		user.addLanguageToSpokenLanguages("german");
	 		try{
	 			query.putUser(user);
	 		} catch (final InputException iE) {
	 			//System.out.println(iE.getLocalizedMessage());
	 		}
	 		final List<User> users = query.getUser("testname", -1);
	 		if (!users.isEmpty()){
	 			user = users.get(0);
	 			final LocalDate date = LocalDate.now();
	 			query.addCookedRecipe(user, recipe.getId(), date);
	 		}
	 		else {
	 			//System.out.println("User doesn't exist");
	 		}
	 	}
	}
	
	@Test
	public void getAllUsersTest() {
		List<User> users = new ArrayList<>();
		try {
			users = this.query.getUser("", -1);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		System.out.println(users.size());
		for(final User user : users) {
			//System.out.println(user.getRecipeRatings());
		}
	}

//	@Test
	public void getByUserNameTest() {
		List<User> users = new ArrayList<>();
		try {
			users = this.query.getUser("", -1);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		for(final User u: users) {
			//System.out.println(u.getName());
		}
		try {
			final User user = this.query.getUser(users.get(0).getName(), -1).get(0);
			assertTrue(user != null);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
//	@Test
	public void addAndgetUserTest() {
		final String userName = "ge ";
		final User user = new User();
		final User se = new User();
		final User er = new User();
		final User es = new User();
		user.setCurrentlySpokenLanguage(Language.GERMAN.getLangCode3());
		se.setCurrentlySpokenLanguage(Language.GERMAN.getLangCode3());
		er.setCurrentlySpokenLanguage(Language.GERMAN.getLangCode3());
		es.setCurrentlySpokenLanguage(Language.GERMAN.getLangCode3());
		
		user.setName(userName + 5);
		se.setName(userName + 2);
		er.setName(userName + 3);
		es.setName(userName + 4);
		
		final List<User> users = new ArrayList<User>();
		users.add(user);
		users.add(es);
		users.add(er);
		users.add(se);
		
		User newUser = null;
		try {
			for(final User u : users) {
				newUser = new UserController(this.query.getGraph()).addAndGetUser(u);
				final LocalDateTime wait = LocalDateTime.now().plusSeconds(5);
//				while (LocalDateTime.now().isBefore(wait)) {}
				if(newUser == null) {
					//System.out.println("null");
					final List<User> uss = this.query.getUser(u.getName(), -1);
					if(!users.isEmpty()) {
						newUser = uss.get(0);						
					}
					if(newUser == null) {
						//System.out.println("null here");
					}
					else {
						//System.out.println("got");
						new UserController(this.query.getGraph()).deleteUser(newUser);
					}
				} else {
					//System.out.println(newUser.getId());
					try {
						new UserController(this.query.getGraph()).deleteUser(newUser);
					} catch (final DatabaseException e) {
						//System.out.println("Deleting failed");
					}
				}				
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			final User user12 = this.query.getUser("user 2", 6508).get(0);
			//System.out.println(user12.getRecipeRatings());
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
//	@Test
	public void getUserTest() {
//		User user = this.query.getUser(null);
//		assertEquals(null, user);
//		
//		user = this.query.getUser(0l);
//		assertEquals(null, user);

//		try {
//			user = this.query.getUser("", 4l).get(0);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		try {
//			List<User> u = this.query.getUser("xx", 1123456123542l);
//			assertTrue(u.isEmpty());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		User user2 = new User();
		user2.setName("getUserTest" + System.currentTimeMillis());
		user2.setCurrentlySpokenLanguage(Language.ENGLISH);
		user2.addIncompatibleIngredient(new Ingredient("salz", Language.GERMAN));
		user2.addLikedIngredient(new Ingredient("pfeffer", Language.GERMAN));
		user2.addRecipeRating(7349, 5);
		user2.addPreferredCategory(new Category("meat"));
		try {
			this.query.putUser(user2);		
			user2 = this.query.getUser(user2.getName(), -1l).get(0);
			System.out.println(user2.getId());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			user2.addLanguageToSpokenLanguages("de");
			this.query.putUser(user2);			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	@Test
	public void getSettingsTest() {
		System.out.println("setting test");
		try {
			final Map<String, Double> settings = this.query.getAverageTempTimeServingFactor(2330);
			//System.out.println(settings);
		} catch (final DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void getHouseHoldTest() {
		System.out.println("household test");
		System.out.println(this.house.getHouseholds());
		System.out.println(this.house.getHousehold(HouseholdLabel.HOUSEHOLD));
		System.out.println(this.house.getGroceryStock());
		this.house.addGroceryStockItem(new GroceryStockItem(new Ingredient("salz", Language.GERMAN),1, Unit.PIECES, LocalDateTime.now().plusDays(4)));
		this.house.addShoppingListItem(new ShoppingListItem(new Ingredient("salz", Language.GERMAN),1, Unit.PIECES, LocalDateTime.now().plusDays(4)));
		
		System.out.println(this.house.getGroceryStock());
		System.out.println(this.house.getShoppingList());
	}
	
	@Test
	public void getRatingsTest() {
		System.out.println("getRatingTest");

		try {
			final User u = this.query.getUser("", 5537).get(0);
//			final User u2 = this.query.getUser(5537l);
//			System.out.println(u2.getRecipeRatings());
			//System.out.println(u.getRecipeRatings());
		} catch (final Exception e) {
			//System.out.println(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void setTest() {
		System.out.println("SET test");
		RecipeFilter filter = new RecipeFilter();
		filter.setMaxNumberOfRecipesToParsePerLanguage(1);
		filter.addRecipeLanguage(Language.ENGLISH);
//		filter.addPossibleAuthor("arçelik");
//		filter.addExcludedIngredient(new Ingredient("sugar", Language.ENGLISH));
//		filter.setIsFoodLabel(FoodLabel.YELLOW);
		new RecipeQuery(getGraph()).getRecipe(filter);
		
		List<String> strings = new ArrayList<String>();
		strings.add("hallo du");
		strings.add("nein");
		strings.add("ja ja");
		
		List<String> strings2 = new ArrayList<String>();
		strings2.add("hallo du");
		strings2.add("nein");
		strings2.add("ja ja");
		
		
		
		System.out.println(StringUtil.join(strings, ","));
		System.out.println(strings.equals(strings2));
	}
}
