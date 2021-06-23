package de.dailab.oven.data_acquisition;

import de.dailab.oven.data_acquisition.controller.ImportExportController;
import de.dailab.oven.data_acquisition.parser.ArcelikParser;
import de.dailab.oven.database.AbstractDatabaseTest;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.Recipe;
import org.junit.Before;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DataAcquisitionTest extends AbstractDatabaseTest {
	Query query;

	@Before
	public void initialize()
            throws DatabaseException, ConfigurationException {
		this.query = new Query();
	}
	
//	@Test
	//This test is specifically for Arcelik - the foodlabel and other variables of the recipes must not point to null 
	public void testArcelikParserToDatabase() throws Exception {
		/**
		 * @ToDo: Upload Arcelik recipes and change folder
		 */
		if(!(new File("/home/pi/RECIPES-19-07-19").exists())) {
			System.out.println("cant reach folder");
		}
 		final File baseDirectory = new File("file:///home/pi/RECIPES-19-07-19");
//		final File baseDirectory = new File("C:\\Users\\Tristan Schroer\\OneDrive\\Studium\\3 - I\\7 - Semester\\Bachelor-Arbeit\\RECIPES-19-07-19\\RECIPES-19-07-19");
		final List<Recipe> recipes = ArcelikParser.parseRecipesInDirectory(baseDirectory);
		System.out.println(recipes.size());
		try {
			this.query.putMultipleRecipes(recipes);
		} catch (final Exception e ) {
			e.printStackTrace();
		}
		this.query.close();
	}
	
//	@Test
	public void fillDatabase() throws Exception
	{
		final List<Recipe> recipes = new ArrayList<>();
		
		final String[] singleSearchTerm = {"bolognese", "burger" , "fries", "soup", "ham", "bbq", "asian" , "duck" , "pork" , "honey" , "sweet" , "sausage"};
//		String[] searchTermsForMeat = {"fish", "chicken", "chili con carne", "beef"};
//		String[] searchTermsForVegetarian = {"vegetarisch", "vegetarian", "vegan", "banana", "apple", "lemon"};
//		String[] searchTermsForPastry = {"cake", "cookies", "cupcake", "casserole", "pizza", "pie", "oven"};
//		String[] searchTermsForOtherRecipes = {"party", "snacks", "noodle", "cheese", "pasta"};
//		String[] languagesToSearchIn = {"de", "en"};
		final String[] languagesToSearchIn = {"en"};

		final List<String[]> searchTerms = new ArrayList<>();
		searchTerms.add(singleSearchTerm);
//		searchTerms.add(searchTermsForPastry);
//		searchTerms.add(searchTermsForMeat);
//		searchTerms.add(searchTermsForVegetarian);
//		searchTerms.add(searchTermsForOtherRecipes);
		
		
		int count = 0;
		int count2 = 1;
		for(final String [] termKind : searchTerms) {
			for(final String searchTerm : termKind) {
				for(final String language : languagesToSearchIn) {
					final ImportExportController controller = new ImportExportController(searchTerm, language);
					try {
						recipes.addAll(controller.getOnlineRecipesByName());	
					} catch (final Exception e ) {
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
		final Query query = new Query();
		int counter = 0;
		for(final Recipe r: recipes) {
			try {
				query.putSingleRecipe(r);			
			} catch (final Exception e) {
				counter += 1;
				System.out.println(counter  + " recipes of " + recipes.size() + " not parsed.");
				System.out.println(r.getUrl());
				System.out.println(r.getName());
				e.printStackTrace();
			}	
		}
		System.out.println("Done");
		query.close();
	}
	
	//@Test
	public void putSingleRecipeFromChefkochTest() throws Exception {
		final String url = "https://www.chefkoch.de/rezepte/2040561330436979/Cinnamon-Rolls-with-Cream-Cheese-Frosting.html";
		final ImportExportController controller = new ImportExportController(url);
		Recipe recipe = new Recipe();
		recipe = controller.getOnlineRecipesByUrls().get(0);
		System.out.println();
		final Query query = new Query();
		query.putSingleRecipe(recipe);
		query.close();
	}

}
