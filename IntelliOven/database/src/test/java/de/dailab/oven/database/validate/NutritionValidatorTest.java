package de.dailab.oven.database.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;

import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Nutrition;
import de.dailab.oven.model.data_model.Unit;

public class NutritionValidatorTest {

	private static final NutritionValidator NUTRITION_VALIDATOR = new NutritionValidator();
	
	@Test
	public void isValidNutritionTest() {
		Nutrition nullNutrition = null;
		assertFalse(NUTRITION_VALIDATOR.isValid(nullNutrition));
		assertTrue(NUTRITION_VALIDATOR.isValid(Nutrition.CARBOHYDRATE));
	}
	
	@Test
	public void isValidNutritionEntryTest() {
		Nutrition nullNutrition = null;
		Nutrition validNutrition = Nutrition.CARBOHYDRATE;
		
		Amount validAmount = new Amount(5.0f, Unit.GRAM);
		Amount invalidAmount = new Amount(-5.0f, Unit.CENTIMETER);
		
		Entry<Nutrition, Amount> nullEntry = null;
		assertFalse(NUTRITION_VALIDATOR.isValid(nullEntry));
		
		Map<Nutrition, Amount> nullMap = new HashMap<>();
		nullMap.put(nullNutrition, null);
		nullMap.put(validNutrition, null);
		for(Entry<Nutrition, Amount> entry : nullMap.entrySet()) {
			assertFalse(NUTRITION_VALIDATOR.isValid(entry));
		}
		nullMap.put(nullNutrition, validAmount);
		for(Entry<Nutrition, Amount> entry : nullMap.entrySet()) {
			assertFalse(NUTRITION_VALIDATOR.isValid(entry));
		}
		
		Map<Nutrition, Amount> invalidMap = new HashMap<>();
		invalidMap.put(validNutrition, invalidAmount);
		for(Entry<Nutrition, Amount> entry : invalidMap.entrySet()) {
			assertFalse(NUTRITION_VALIDATOR.isValid(entry));
		}
		
		Map<Nutrition, Amount> validMap = new HashMap<>();
		validMap.put(validNutrition, validAmount);
		for(Entry<Nutrition, Amount> entry : validMap.entrySet()) {
			assertTrue(NUTRITION_VALIDATOR.isValid(entry));
		}
	}
	
	@Test
	public void isValidMapTest() {
		Map<Nutrition, Amount> nullMap1 = null;
		assertFalse(NUTRITION_VALIDATOR.isValid(nullMap1));
		
		Nutrition nullNutrition = null;
		Nutrition validNutrition1 = Nutrition.CARBOHYDRATE;
		Nutrition validNutrition2 = Nutrition.FAT;
		
		Amount validAmount = new Amount(5.0f, Unit.GRAM);
		Amount invalidAmount = new Amount(-5.0f, Unit.CENTIMETER);
		
		Map<Nutrition, Amount> nullMap = new HashMap<>();
		nullMap.put(nullNutrition, null);
		nullMap.put(validNutrition1, null);
		assertFalse(NUTRITION_VALIDATOR.isValid(nullMap));
		
		Map<Nutrition, Amount> invalidMap = new HashMap<>();
		invalidMap.put(validNutrition1, invalidAmount);
		invalidMap.put(validNutrition2, validAmount);
		assertFalse(NUTRITION_VALIDATOR.isValid(invalidMap));
		
		Map<Nutrition, Amount> validMap = new HashMap<>();
		validMap.put(validNutrition1, validAmount);
		assertTrue(NUTRITION_VALIDATOR.isValid(validMap));
	}
}