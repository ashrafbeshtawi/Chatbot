package de.dailab.oven.model.data_module.test;


import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;

import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Nutrition;
import zone.bot.vici.Language;



public class IngredientTest {
	
	Ingredient testIngredient;
	Map<Nutrition, Amount> testNutrition;
	
	@Before
	public void initialize() {
		this.testIngredient = new Ingredient("", Language.UNDEF);
		this.testNutrition = new HashMap<>();
	}
	
	@Test
	public void nameTest() {
		assertEquals("", testIngredient.getName());
		testIngredient.setName("Butterbier");
		assertEquals("butterbier", testIngredient.getName());
		testIngredient.setName(null);
		assertEquals("butterbier", testIngredient.getName());
		assertEquals("butterbier", testIngredient.toString());
	}
	
//	@Test
//	public void nutritionTest() {
//		assertEquals(testNutrition, testIngredient.getNutrition());
//		testIngredient.setNutrition(null);
//		assertEquals(testNutrition, testIngredient.getNutrition());
//		
//		Nutrition testNut = Nutrition.CARBOHYDRATE;
//		Amount testAmount = new Amount(2, "mg");
//		testNutrition.put(testNut, testAmount);
//		testIngredient.setNutrition(testNutrition);
//		assertTrue(testIngredient.getNutrition().containsKey(testNut));
//		assertTrue(testIngredient.getNutrition().get(testNut) == testAmount);
//		testNutrition.put(Nutrition.ENERGY, new Amount(3, "ml"));
//		assertFalse(testNutrition == testIngredient.getNutrition());
//		testNutrition = new HashMap<>();
//	}
	
	
	@Test
	public void constructorTest1() {

		Nutrition testNut = Nutrition.CARBOHYDRATE;
		Amount testAmount = new Amount(2, "mg");
		Language testLanguage = Language.GERMAN;
		
		Ingredient testIngredient2 = new Ingredient(null, null, null);
		assertEquals("", testIngredient2.getName());
		assertEquals(testNutrition, testIngredient2.getNutrition());
		assertEquals(Language.UNDEF, testIngredient2.getLanguage());
		
		testNutrition.put(testNut, testAmount);
		testIngredient2 = new Ingredient("Kürbissaft", testNutrition, Language.GERMAN);
		assertEquals("kürbissaft", testIngredient2.getName());
		assertEquals(testNutrition, testIngredient2.getNutrition());
		assertEquals(testLanguage, testIngredient2.getLanguage());
		
		testNutrition = new HashMap<>();
	}
	
	@Test
	public void checkedForNutritionTest() {
		assertFalse(testIngredient.isCheckedForNutrition());
		testIngredient.setCheckedForNutrition(true);
		assertTrue(testIngredient.isCheckedForNutrition());
	}
	
	@Test
	public void languageTest() {
		assertEquals(Language.UNDEF, testIngredient.getLanguage());
		
		Language nullLanguage = null;
		testIngredient.setLanguage(nullLanguage);
		assertEquals(Language.UNDEF, testIngredient.getLanguage());
		
		testIngredient.setLanguage(Language.GERMAN);
		assertEquals(Language.GERMAN, testIngredient.getLanguage());
		
	}
}
