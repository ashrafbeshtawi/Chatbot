package de.dailab.oven.database.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.User;
import zone.bot.vici.Language;

public class UserValidatorTest {

	private static final UserValidator USER_VALIDATOR = new UserValidator();
	
	@Test
	public void isUserNameValidTest() {
		assertFalse(USER_VALIDATOR.isUserNameValid(null));
		assertTrue(USER_VALIDATOR.isUserNameValid("User name"));
	}

	@Test
	public void isIdValidTest() {
		assertFalse(USER_VALIDATOR.isUserIdValid(-2l));
		assertTrue(USER_VALIDATOR.isUserIdValid(null));
		assertTrue(USER_VALIDATOR.isUserIdValid(100l));
	}
	
	@Test
	public void isPreferredCategoriesValidTest() {
		assertFalse(USER_VALIDATOR.isPreferredCategoriesValid(null));
		assertTrue(USER_VALIDATOR.isPreferredCategoriesValid(new HashSet<Category>()));
	}
	
	@Test
	public void isIncompatibleIngredientsValidTest() {
		assertFalse(USER_VALIDATOR.isIncompatibleIngredientsValid(null));
		assertTrue(USER_VALIDATOR.isIncompatibleIngredientsValid(new HashSet<Ingredient>()));
	}

	@Test
	public void isLikedIngredientsValidTest() {
		assertFalse(USER_VALIDATOR.isLikedIngredientsValid(null));
		assertTrue(USER_VALIDATOR.isLikedIngredientsValid(new HashSet<Ingredient>()));
	}
	
	@Test
	public void isSpokenLanguagesValidTest() {
		assertFalse(USER_VALIDATOR.isSpokenLanguagesValid(null));
		
		Set<Language> invalid = new HashSet<>();
		invalid.add(Language.UNDEF);
		assertFalse(USER_VALIDATOR.isSpokenLanguagesValid(invalid));
		
		Set<Language> valid = new HashSet<>();
		valid.add(Language.ENGLISH);
		assertTrue(USER_VALIDATOR.isSpokenLanguagesValid(valid));
	}
	
	@Test
	public void isRecipeRatingsValid() {
		assertFalse(USER_VALIDATOR.isRecipeRatingsValid(null));
		assertTrue(USER_VALIDATOR.isRecipeRatingsValid(new HashMap<>()));;
	}
	
	@Test
	public void isUserValidTest() {
		User user = null;
		assertFalse(USER_VALIDATOR.isValid(user));
		
		user = new User();
		assertFalse(USER_VALIDATOR.isValid(user));
		
		user.setName("n");
		assertFalse(USER_VALIDATOR.isValid(user));
		
		user.setName("User name");
		assertTrue(USER_VALIDATOR.isValid(user));
		
		user.setId(-20);
		assertFalse(USER_VALIDATOR.isValid(user));
		
		user.setId(100);
		assertTrue(USER_VALIDATOR.isValid(user));
		
		Set<Category> invalidCategories = new HashSet<>();
		invalidCategories.add(new Category("t"));
		user.addPreferredCategories(invalidCategories);
		assertFalse(USER_VALIDATOR.isValid(user));
		
		user.removePreferredCategory(new Category("t"));
		user.addPreferredCategories(new HashSet<>());
		assertTrue(USER_VALIDATOR.isValid(user));
		
		Ingredient ingredient = new Ingredient("t", Language.ENGLISH);
		Set<Ingredient> ingredients = new HashSet<>();
		ingredients.add(ingredient);
		user.setIncompatibleIngredients(ingredients);
		assertFalse(USER_VALIDATOR.isValid(user));
		
		user.removeIncompatibleIngredient(ingredient);
		ingredient = new Ingredient("test ingredient", Language.ENGLISH);
		ingredients = new HashSet<>();
		ingredients.add(ingredient);
		user.setIncompatibleIngredients(ingredients);
		assertTrue(USER_VALIDATOR.isValid(user));
		
		ingredient = new Ingredient("t", Language.ENGLISH);
		ingredients = new HashSet<>();
		ingredients.add(ingredient);
		user.setLikedIngredients(ingredients);
		assertFalse(USER_VALIDATOR.isValid(user));
		
		user.removeLikedIngredient(ingredient);
		ingredient = new Ingredient("test ingredient", Language.ENGLISH);
		ingredients = new HashSet<>();
		ingredients.add(ingredient);
		user.setLikedIngredients(ingredients);
		assertTrue(USER_VALIDATOR.isValid(user));
		//
		user.removeLanguageFromSpokenLanguages(Language.ENGLISH);
		Set<Language> invalid = new HashSet<>();
		invalid.add(Language.UNDEF);
		user.setSpokenLanguages(invalid);
		assertFalse(USER_VALIDATOR.isValid(user));

		Map<Long, Integer> invalidRatings = new HashMap<>();
		invalidRatings.put(-10l, 2);
		user.setRecipeRatings(invalidRatings);
		assertFalse(USER_VALIDATOR.isValid(user));

		user.setRecipeRatings(new HashMap<Long, Integer>());
		assertFalse(USER_VALIDATOR.isValid(user));
	}
}