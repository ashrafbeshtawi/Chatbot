package de.dailab.oven.database.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Nutrition;
import de.dailab.oven.model.data_model.Unit;
import zone.bot.vici.Language;

public class IngredientValidatorTest {

	private static final IngredientValidator INGREDIENT_VALIDATOR = new IngredientValidator();
	
	//TODO: add ID check
	
	@Test
	public void isValidIngredientTest() {
		Ingredient nullIngredient = null;
		Ingredient invalidName = new Ingredient("t", Language.ENGLISH);
		Ingredient invalidLanguage = new Ingredient("test", Language.UNDEF);
		Ingredient invalidNutrition = new Ingredient("test", null, Language.ENGLISH);
		Map<Nutrition, Amount> invalidN = new HashMap<>();
		invalidN.put(Nutrition.ENERGY, new Amount(-5.0f, Unit.UNDEF));
		invalidNutrition.setNutrition(invalidN);
		Ingredient valid = new Ingredient("test", Language.ENGLISH);
		
		assertFalse(INGREDIENT_VALIDATOR.isValid(nullIngredient));
		assertFalse(INGREDIENT_VALIDATOR.isValid(invalidName));
		assertFalse(INGREDIENT_VALIDATOR.isValid(invalidLanguage));
		assertFalse(INGREDIENT_VALIDATOR.isValid(invalidNutrition));
		assertTrue(INGREDIENT_VALIDATOR.isValid(valid));
	}
	
	@Test
	public void isValidIngredientSetTest() {
		Set<Ingredient> nullSet = null;
		Set<Ingredient> emptySet = new HashSet<>();
		
		Ingredient invalidName = new Ingredient("t", Language.ENGLISH);
		Ingredient valid1 = new Ingredient("test", Language.ENGLISH);
		Ingredient valid2 = new Ingredient("test2", Language.ENGLISH);

		Set<Ingredient> invalidSet = new HashSet<>();
		invalidSet.add(invalidName);
		invalidSet.add(valid1);
		
		Set<Ingredient> validSet = new HashSet<>();
		validSet.add(valid1);
		validSet.add(valid2);
		
		assertFalse(INGREDIENT_VALIDATOR.isValid(nullSet));
		assertTrue(INGREDIENT_VALIDATOR.isValid(emptySet));
		assertFalse(INGREDIENT_VALIDATOR.isValid(invalidSet));
		assertTrue(INGREDIENT_VALIDATOR.isValid(validSet));
	}

}