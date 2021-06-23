package de.dailab.oven.data_acquisition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dailab.oven.database.AbstractDatabaseTest;

import org.junit.Ignore;
import org.junit.Test;

import de.dailab.oven.data_acquisition.controller.ImportExportController;
import de.dailab.oven.data_acquisition.crawler.URLCrawler;
import de.dailab.oven.model.data_model.filters.RecipeFilter;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.recipe_services.nutrition_evaluator.NutritionsOnCall;
import de.dailab.oven.recipe_services.unit_recalculator.UnitRecalculator;

public class DataflowTest extends AbstractDatabaseTest {
	Query query;

//
//	@Before
//	public void initialize() {
//		query = new Query();
//	}
//


	@Override
	public void initialize() {

	}

	private final Map<String, Long> timestamps = new HashMap();
	
	public Map<String, Long> getTimestamps() {
		return this.timestamps;
	}
	
	public void dataflowSingleRecipe() throws Exception {
		this.timestamps.put("start", System.currentTimeMillis());
		
		final URLCrawler urlCrawler = new URLCrawler("pfannkuchen", "de");
		this.timestamps.put("initialized urlcrawler", System.currentTimeMillis());
		
		final List<String> urls = urlCrawler.getUrls();
		this.timestamps.put("time to get urls", System.currentTimeMillis());
		
		final ImportExportController controller = new ImportExportController("https://www.chefkoch.de/rezepte/1167071222777296/Chili-con-Carne.html");
		this.timestamps.put("initialized recipeanalyzer", System.currentTimeMillis());
		
		List<Recipe> recipes = controller.getOnlineRecipesByUrls();
		this.timestamps.put("time to download, recalculate and get nutrition for one recipe", System.currentTimeMillis());
		
		final UnitRecalculator ur = new UnitRecalculator();
		this.timestamps.put("initialized unit recalculator", System.currentTimeMillis());
		
		ur.recalculate(recipes.get(0));
		this.timestamps.put("recalculated", System.currentTimeMillis());
		
		final NutritionsOnCall noc = new NutritionsOnCall();
		this.timestamps.put("initialized nutritions on call", System.currentTimeMillis());
		
		noc.evaluateNutritions(recipes.get(0));
		this.timestamps.put("got nutrition", System.currentTimeMillis());
		
		final Query query = new Query();
		this.timestamps.put("initialized graph", System.currentTimeMillis());
		
		final Recipe recipe = recipes.get(0);
		recipe.setLanguage("de");
		query.putSingleRecipe(recipe);
		this.timestamps.put("time to put recipe into database", System.currentTimeMillis());
		
		final RecipeFilter recipeFilter = new RecipeFilter();
//		recipeFilter.setRecipeName("chili con carne");
//		recipeFilter.addRecipeLanguage(Language.GER);
//		recipeFilter.setRecipeId(3788);
		this.timestamps.put("initialzied recipe filter", System.currentTimeMillis());
		
		recipes = query.getRecipe(recipeFilter);
		this.timestamps.put("time to get and parse ".concat(Integer.toString(recipes.size())).concat(" recipes"), System.currentTimeMillis());
		
		
		System.out.println(this.timestamps);
		
	}

	@Ignore
	@Test
	public void DataflowSingleRecipeTest() throws Exception{
		this.dataflowSingleRecipe();
	}

/**	
	
//	@Test
	public void ConfigurationReaderTest() throws ConfigurationException {
		ConfigurationReader configReader = new ConfigurationReader("config.json", Connectivity.LOCAL);
		String uri = configReader.getUri();
		String password = configReader.getPassword();
		String user = configReader.getUser();

		assertTrue(uri.contentEquals("bolt://localhost:7687"));
		assertTrue(password.contentEquals("aal2019"));
		assertTrue(user.contentEquals("neo4j"));
	}
	
//	@Test
	public void GraphConnectionTest() throws Exception{
		this.graph = new Graph();
		if(this.connectionType == Connectivity.UNDEF) {
			throw new ConfigurationException("Connection Type must be chosen");
		}
		//If the following fails, Neo4J will throw an exception by itself
		else if(this.connectionType == Connectivity.LOCAL) {
			this.graph.connectToLocalGraph();
		}
		else {
			this.graph.connectToRemoteGraph();
		}
		this.graph.close();
	}
	
	@Test
	public void queryTest() throws Exception {
		Query query = new Query(this.connectionType);
		query.close();
	}
	
//	@Test
	public void IdQueryTest() throws Exception {
		long start = System.currentTimeMillis();
		this.graph = new Graph();
		if(this.connectionType == Connectivity.LOCAL) {
			this.graph.connectToLocalGraph();
		}
		else {
			this.graph.connectToRemoteGraph();
		}
		Session readSession = this.graph.openReadSession();
		long timestamp = System.currentTimeMillis() - start;
		System.out.println(timestamp);
		IdQuery idQuery = new IdQuery(readSession, 426);
		timestamp = System.currentTimeMillis() - start;
		System.out.println(timestamp);
		List<String> labels = idQuery.getNodeLabelsForId();
		timestamp = System.currentTimeMillis() - start;
		System.out.println(timestamp);
		boolean validLabelExists = false;
		String[] validLabels = {"User", "Recipe", "Ingredient", "Category"};
		for(String label : validLabels) {
			if(labels.contains(label)) {
				validLabelExists = true;
				break;
			}
		}
		timestamp = System.currentTimeMillis() - start;
		System.out.println(timestamp);
		assertTrue(validLabelExists);
		String[] validTypes = {"CONTAINS", "BELONGS_TO", "PREFERS", "IS_INCOMPATIBLE_WITH", "RATED"};
		idQuery.setIdToQuery(253);
		timestamp = System.currentTimeMillis() - start;
		System.out.println(timestamp);
		String relationshipType = idQuery.getRelationshipTypeForId();
		timestamp = System.currentTimeMillis() - start;
		System.out.println(timestamp);
		boolean isValidType = false;
		for(String type : validTypes) {
			if(type.contentEquals(relationshipType)) {
				isValidType = true;
				break;
			}
		}
		assertTrue(isValidType);
		timestamp = System.currentTimeMillis() - start;
		System.out.println(timestamp);
		timestamp = System.currentTimeMillis() - start;
		System.out.println(timestamp);
		System.out.println(System.currentTimeMillis() - start);
		this.graph.close();
	}

	//GGF CHECK ERGÄNZEN
//	@Test
	public void QueryPutUserTest() throws Exception {
		Query query = new Query(this.connectionType);
		
		User user = new User();
		user.setName("Mark");
		user.addPreferredCategory("testCategory1");
		user.addPreferredCategory("testCategory2");
		user.addIncompatibleIngredient("testingredient3");
//		user.addRecipeRating(463, 9);
		user.setCurrentlySpokenLanguage("engl");
		query.putUser(user);
		query.close();
		
	}
	
	//GGF CHECK ERGÄNZEN
//	@Test
	public void QueryPutRecipeTest() throws Exception {
		Query query = new Query(this.connectionType);
		
		Recipe recipe = new Recipe();
		recipe.setAuthor("Testauthor");
		recipe.setLanguage("engl");
		recipe.addCategory("testCategory1");
		recipe.addCategory("testCategory2");
		recipe.addCategory("test3Category3");
		recipe.addDurationToListOfDurations("total", 1, 30);
		recipe.addDurationToListOfDurations("preparation", 0, 30);
		recipe.addDurationToListOfDurations("baking", 1, 30);
		recipe.setFoodLabel(FoodLabel.GREEN);
		Ingredient ingredient1 = new Ingredient();
		ingredient1.setName("testIngredient1");
		ingredient1.setLanguage("testIngredient1");
		Amount amount1 = new Amount(32, "gramm");
		recipe.addIngredientToListOfIngredients(ingredient1, amount1);
		Ingredient ingredient2 = new Ingredient();
		ingredient2.setName("testIngredient2");
		Amount amount2 = new Amount(10, "gramm");
		recipe.addIngredientToListOfIngredients(ingredient2, amount2);
		recipe.addInstruction("Do this");
		recipe.addInstruction("Do that");
		recipe.setName("TestRecipe1");
		recipe.setOriginalServings(42);
		recipe.setUrl("http://www.nocooking.com/");
//		recipe.addUserRating(3238, 10);
		System.out.println(recipe.getDuration());
		query.putSingleRecipe(recipe);
		query.close();
	}

//	@Test
	public void clearGraph() throws Exception{
		Query query = new Query(this.connectionType);
		query.clearGraph();
		query.close();
	}
	
//	@Test
	//This test is specifically for Arcelik - the foodlabel and other variables of the recipes must not point to null 
	public void testArcelikParserToDatabase() throws Exception {
		final File baseDirectory = new File("C:\\Users\\Tristan Schroer\\OneDrive\\Studium\\3 - I\\7 - Semester\\Bachelor-Arbeit\\RECIPES-19-07-19\\RECIPES-19-07-19");
		final List<Recipe> recipes = ArcelikParser.parseRecipesInDirectory(baseDirectory);
		Query query = new Query(this.connectionType);
		System.out.println(recipes.size());
		try {
			query.putMultipleRecipes(recipes);			
		} catch (Exception e ) {
			e.printStackTrace();
		}
		query.close();
	}
	
//	@Test
	public void fillDatabase() throws Exception
	{
		List<Recipe> recipes = new ArrayList<>();
		
		String[] singleSearchTerm = {"bolognese"};
		String[] searchTermsForMeat = {"fish", "chicken", "chili con carne", "beef"};
		String[] searchTermsForVegetarian = {"vegetarisch", "vegetarian", "vegan", "banana", "apple", "lemon"};
		String[] searchTermsForPastry = {"cake", "cookies", "cupcake", "casserole", "pizza", "pie", "recipe"};
		String[] searchTermsForOtherRecipes = {"party", "snacks", "noodle", "cheese", "pasta"};
		String[] languagesToSearchIn = {"de", "en"};
		

		List<String[]> searchTerms = new ArrayList<>();
		searchTerms.add(singleSearchTerm);
		searchTerms.add(searchTermsForPastry);
		searchTerms.add(searchTermsForMeat);
		searchTerms.add(searchTermsForVegetarian);
		searchTerms.add(searchTermsForOtherRecipes);
		
		
		int count = 0;
		int count2 = 1;
		for(String [] termKind : searchTerms) {
			for(String searchTerm : termKind) {
				for(String language : languagesToSearchIn) {
					RecipeAnalyzer recipeAnalyzer = new RecipeAnalyzer(searchTerm, language);
					try {
						recipes.addAll(recipeAnalyzer.getOnlineRecipesByName());	
					} catch (Exception e ) {
						e.printStackTrace();
					}
					
				}
				count++;
				System.out.println("Finished search term " + count + " of " + termKind.length + " in " + count2 + " termKind of " + searchTerms.size());
			}
			count = 0;
			System.out.println("Finished termKind " + count2 + " of " + searchTerms.size());
			count2++;
		}
		System.out.println("Downloaded " + recipes.size() + " recipes");
		Query query = new Query(this.connectionType);
		try {
			query.putMultipleRecipes(recipes);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
		query.close();
	}
	
//	@Test
	public void putSingleRecipeFromChefkochTest() throws Exception {
		String url = "https://www.chefkoch.de/rezepte/2040561330436979/Cinnamon-Rolls-with-Cream-Cheese-Frosting.html";
		RecipeAnalyzer recipeAnalyzer = new RecipeAnalyzer(url);
		Recipe recipe = new Recipe();
		recipe = recipeAnalyzer.getOnlineRecipesByUrls().get(0);
		System.out.println();
		Query query = new Query(this.connectionType);
		query.putSingleRecipe(recipe);
		query.close();
	}


	@Test
	public void getRecipeByIdTest() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.setRecipeId(5779);
				
		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	
	@Test
	public void getRecipeByNameTest() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.setRecipeName("chicken");
				
		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	
	@Test
	public void getRecipeByRequiredCategories() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.addRequiredCategory("party");
		recipeFilter.addRequiredCategory("kinder");
		
		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	
	@Test
	public void getRecipeByPossibleCategories() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.addPossibleCategory("party");
		recipeFilter.addPossibleCategory("kinder");
		
		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	
	@Test
	public void getRecipeByCategoriesToAvoid() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.addExcludedCategory("party");
		recipeFilter.addExcludedCategory("kinder");

		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	
	@Test
	public void getRecipeRequiredIngredients() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.addRequiredIngredient("salt");
		recipeFilter.addRequiredIngredient("water");
	
		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	
	@Test
	public void getRecipeByPossibleIngredients() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.addPossibleIngredient("salt");
		recipeFilter.addPossibleIngredient("water");
		
		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	
	@Test
	public void getRecipeByIngredientstoAvoid() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.addExcludedIngredient("salt");
		recipeFilter.addExcludedIngredient("water");
		recipeFilter.addExcludedIngredient("bütün hindi");
		
		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	
	@Test
	public void getRecipeByPossibleAuthors() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.addPossibleAuthor("chefkoch");

		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	
	@Test
	public void getRecipeByAuthorsToAvoid() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();

		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.addExcludedAuthor("chefkoch");

		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	
	@Test
	public void getRecipeByDuration() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.setCookedWithin(Duration.ZERO.plusMinutes(60));

		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	
	@Test
	public void getRecipeByFoodLabel() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.setIsFoodLabel(FoodLabel.YELLOW);
		
		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}

	@Test
	public void getRecipeByOriginalServings() throws Exception {
		Query query = new Query(this.connectionType);
		RecipeFilter recipeFilter = new RecipeFilter();
		
		recipeFilter.addRecipeLanguage(Language.EN);
		recipeFilter.addRecipeLanguage(Language.TR);
		recipeFilter.addRecipeLanguage(Language.GER);
		
		recipeFilter.setOriginalServings(4);

		try {
			List<Recipe> recipes = query.getRecipe(recipeFilter);
			Recipe recipe = recipes.get(0);
			System.out.println("Parsed recipes: " + recipes.size());
			printRecipe(recipe);
		} catch (InputException e) {
			System.out.println(e);
		}
		query.close();
	}
	**/
}
