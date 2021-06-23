package de.dailab.oven.database.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

import de.dailab.oven.model.data_model.*;
import org.junit.Test;

import zone.bot.vici.Language;

public class RecipeValidatorTest {

	private static final RecipeValidator RECIPE_VALIDATOR = new RecipeValidator();
	
	@Test
	public void isAuthorValidTest() {
		assertFalse(RECIPE_VALIDATOR.isAuthorValid(null));
		assertTrue(RECIPE_VALIDATOR.isAuthorValid("Author"));
	}
	
	@Test
	public void isLanguageValidTest() {
		assertFalse(RECIPE_VALIDATOR.isLanguageValid(Language.UNDEF));
		assertTrue(RECIPE_VALIDATOR.isLanguageValid(Language.ENGLISH));
	}
	
	@Test
	public void isRecipeNameValidTest() {
		assertFalse(RECIPE_VALIDATOR.isRecipeNameValid(null));
		assertTrue(RECIPE_VALIDATOR.isRecipeNameValid("Recipe name"));
	}
	
	@Test
	public void isCategoriesValidTest() {
		assertFalse(RECIPE_VALIDATOR.isCategoriesValid(null));
		assertTrue(RECIPE_VALIDATOR.isCategoriesValid(new HashSet<Category>()));
	}
	
	@Test
	public void isDurationValidTest() {
		assertFalse(RECIPE_VALIDATOR.isDurationValid(null));
		assertFalse(RECIPE_VALIDATOR.isDurationValid(Duration.ZERO.minusHours(1)));
		assertTrue(RECIPE_VALIDATOR.isDurationValid(Duration.ZERO));
	}
	
	@Test
	public void isDurationsValidTest() {
		assertFalse(RECIPE_VALIDATOR.isDurationsValid(null));
		
		Map<String, Duration> testMap = new HashMap<>();
		assertTrue(RECIPE_VALIDATOR.isDurationsValid(testMap));
		
		testMap.put(null, null);
		assertFalse(RECIPE_VALIDATOR.isDurationsValid(testMap));
		
		testMap.put(null, Duration.ZERO);
		assertFalse(RECIPE_VALIDATOR.isDurationsValid(testMap));
		
		testMap.remove(null);
		testMap.put("Test duration", null);
		assertFalse(RECIPE_VALIDATOR.isDurationsValid(testMap));
		
		testMap.put("Test duration", Duration.ZERO);
		assertTrue(RECIPE_VALIDATOR.isDurationsValid(testMap));
	}
	
	@Test
	public void isFoodLabelValidTest() {
		assertFalse(RECIPE_VALIDATOR.isFoodLabelValid(null));
		assertTrue(RECIPE_VALIDATOR.isFoodLabelValid(FoodLabel.GREEN));
	}
	
	@Test
	public void isIdValidTest() {
		assertFalse(RECIPE_VALIDATOR.isIdValid(-2l));
		assertTrue(RECIPE_VALIDATOR.isIdValid(null));
		assertTrue(RECIPE_VALIDATOR.isIdValid(100l));
	}
	
	@Test
	public void isImagePathValidTest() {
		assertFalse(RECIPE_VALIDATOR.isImagePathValid(null));
		assertTrue(RECIPE_VALIDATOR.isImagePathValid(""));
		assertFalse(RECIPE_VALIDATOR.isImagePathValid("invalidTest.txt"));
		
		String sep = File.separator;
		String dir = Paths.get("").toAbsolutePath().toString() + sep + "src" + sep + "test" + sep 
				+ "resources" + sep;
		
		String imagePath = dir + "testImage.png";
		assertTrue(RECIPE_VALIDATOR.isImagePathValid(imagePath));
	}
	
	@Test
	public void isIngredientsValidTest() {
		assertFalse(RECIPE_VALIDATOR.isIngredientsValid(null));
		assertFalse(RECIPE_VALIDATOR.isIngredientsValid(new ArrayList<>()));
		
		Ingredient ingredient = new Ingredient("test ingredient", Language.ENGLISH);
		IngredientWithAmount ingredientWithAmount = new IngredientWithAmount(ingredient, 5.0f, Unit.GRAM);
		assertTrue(RECIPE_VALIDATOR.isIngredientsValid(Collections.singletonList(ingredientWithAmount)));
	}
	
	@Test
	public void isInstructionsValid() {
		assertFalse(RECIPE_VALIDATOR.isInstructionsValid(null));
		assertFalse(RECIPE_VALIDATOR.isInstructionsValid(new ArrayList<String>()));
		
		List<String> instructions = new ArrayList<>();
		instructions.add("Do a test move");
		
		assertTrue(RECIPE_VALIDATOR.isInstructionsValid(instructions));
	}
	
	@Test
	public void isOriginalServingsValid() {
		assertFalse(RECIPE_VALIDATOR.isOriginalServingsValid(null));
		assertFalse(RECIPE_VALIDATOR.isOriginalServingsValid(0));
		assertTrue(RECIPE_VALIDATOR.isOriginalServingsValid(1));
	}
	
	@Test
	public void isUrlValidTest() {
		assertFalse(RECIPE_VALIDATOR.isUrlValid(null));
		assertTrue(RECIPE_VALIDATOR.isUrlValid(""));
		assertFalse(RECIPE_VALIDATOR.isUrlValid("invalid./"));
		assertTrue(RECIPE_VALIDATOR.isUrlValid("http://google.de"));
	}
	
	@Test
	public void isUserRatingsValid() {
		assertFalse(RECIPE_VALIDATOR.isUserRatingsValid(null));
		assertTrue(RECIPE_VALIDATOR.isUserRatingsValid(new HashMap<>()));;
	}
	
	@Test
	public void isRecipeValidTest() {
		Recipe recipe = null;
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe = new Recipe();
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setAuthor("T");
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setAuthor("Author");
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setLanguage(Language.ENGLISH);
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setName("Recipe name");
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		Set<Category> invalidCategories = new HashSet<>();
		invalidCategories.add(new Category("T"));
		recipe.setCategories(invalidCategories);
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.removeCategory(new Category("T"));
		recipe.addCategory(new Category("Category"));
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setDuration(Duration.ZERO.minusHours(1));
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setDuration(Duration.ZERO);
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.addDurationToListOfDurations("total", Duration.ZERO.minusHours(1));
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		recipe.removeDurationFromListOfDurations("total");
		
		recipe.addDurationToListOfDurations("total", Duration.ZERO.plusHours(1));
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setFoodLabel(null);
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setFoodLabel(FoodLabel.GREEN);
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setId(-20);
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setId(100);
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setImagePath("invalid.7");
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.setImagePath("");
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		Ingredient ingredient = new Ingredient("test ingredient", Language.ENGLISH);
		final List<IngredientWithAmount> l = new LinkedList<>();
		l.add(new IngredientWithAmount(ingredient, -5.0f, Unit.GRAM));
		recipe.setIngredients(l);
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
		recipe.removeIngredient(ingredient);
		ingredient = new Ingredient("test ingredient", Language.ENGLISH);
		recipe.setIngredients(Collections.singletonList(new IngredientWithAmount(ingredient, 5.0f, Unit.GRAM)));
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));

		List<String> instructions = new ArrayList<>();
		instructions.add("Do a test move");
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));

		recipe.setOriginalServings(-1);
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));

		recipe.setOriginalServings(2);
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));

		recipe.setUrl("invalid/-.");
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));

		recipe.setUrl("https://google.de");
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));

		Map<Long, Integer> invalidRatings = new HashMap<>();
		invalidRatings.put(-10l, 2);
		recipe.setUserRatings(invalidRatings);
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));

		recipe.setUserRatings(new HashMap<Long, Integer>());
		assertFalse(RECIPE_VALIDATOR.isValid(recipe));
		
	}
}
