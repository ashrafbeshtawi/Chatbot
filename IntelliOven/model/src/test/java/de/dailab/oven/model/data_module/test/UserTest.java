package de.dailab.oven.model.data_module.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.data_model.User;
import zone.bot.vici.Language;

public class UserTest {
	
	User testUser;
	
	@Before
	public void initialize() {
		testUser = new User();
	}
	
	@Test
	public void idTest() {
		assertEquals(-1, testUser.getId());
		long id = 12345;
		testUser.setId(id);
		assertEquals(id, testUser.getId());
	}
	
	@Test
	public void nameTest() {
		assertEquals("", testUser.getName());
		testUser.setName("Max Mustermann");
		assertEquals("max mustermann", testUser.getName());
		testUser.setName(null);
		assertEquals("max mustermann", testUser.getName());
	}
	
	@Test
	public void preferredCategoriesTest() {
		Set<Category> testCategories = new HashSet<>();
		testCategories.add(new Category("cat1"));
		testCategories.add(new Category("cat2"));
		testCategories.add(new Category("cat3"));
		
		Set<String> emptySet = new HashSet<>();
		
		
		assertEquals(emptySet, testUser.getPreferredCategories());
		
		testUser.addPreferredCategories(null);
		assertEquals(emptySet, testUser.getPreferredCategories());
		
		testUser.addPreferredCategories(testCategories);
		assertEquals(testCategories, testUser.getPreferredCategories());
		
		testUser.addPreferredCategory(null);
		assertEquals(testCategories, testUser.getPreferredCategories());
		
		testUser.addPreferredCategory(new Category("cat3"));
		assertEquals(testCategories, testUser.getPreferredCategories());
		
		testCategories.add(new Category("cat4"));
		testUser.addPreferredCategory(new Category("cat4"));
		assertEquals(testCategories, testUser.getPreferredCategories());
		
		testUser.removePreferredCategory(null);
		assertEquals(testCategories, testUser.getPreferredCategories());
		
		testUser.removePreferredCategory(new Category("cat5"));
		assertEquals(testCategories, testUser.getPreferredCategories());
		
		testCategories.remove(new Category("cat4"));
		testUser.removePreferredCategory(new Category("cat4"));
		assertEquals(testCategories, testUser.getPreferredCategories());
	}
	
//	@Test
	public void likesIngredientsTest() {
		Set<String> testIngredients = new HashSet<>();
		testIngredients.add("ing1");
		testIngredients.add("ing2");
		testIngredients.add("ing3");
		
		Set<String> emptySet = new HashSet<>();
		
		
		assertEquals(emptySet, testUser.getLikesIngredients());
		
		testUser.setLikedIngredients(null);
		assertEquals(emptySet, testUser.getLikesIngredients());
		
//		testUser.setLikedIngredients(testIngredients);
//		assertEquals(testIngredients, testUser.getLikesIngredients());
//		assertEquals(false, testUser.isLikesIngredientsFromDatabase());
//		testUser.setAllDataFromDatabase();
		
//		testUser.addLikedIngredient(null);
//		assertEquals(testIngredients, testUser.getLikesIngredients());
//		assertEquals(true, testUser.isLikesIngredientsFromDatabase());
		
		testUser.addLikedIngredient("ing3");
		assertEquals(testIngredients, testUser.getLikesIngredients());
		
		testIngredients.add("ing4");
		testUser.addLikedIngredient("ing4");
		assertEquals(testIngredients, testUser.getLikesIngredients());
		
//		testUser.removeLikedIngredient(null);
//		assertEquals(testIngredients, testUser.getLikesIngredients());
//		assertEquals(true, testUser.isLikesIngredientsFromDatabase());
		
		testUser.removeLikedIngredient("ing5");
		assertEquals(testIngredients, testUser.getLikesIngredients());
		
		testIngredients.remove("ing4");
		testUser.removeLikedIngredient("ing4");
		assertEquals(testIngredients, testUser.getLikesIngredients());
	}
	
//	@Test
	public void incompatibleIngredientsTest() {
		Set<String> testIngredients = new HashSet<>();
		testIngredients.add("ing1");
		testIngredients.add("ing2");
		testIngredients.add("ing3");
		
		Set<String> emptySet = new HashSet<>();
		
		
		assertEquals(emptySet, testUser.getIncompatibleIngredients());
		
		testUser.setIncompatibleIngredients(null);
		assertEquals(emptySet, testUser.getIncompatibleIngredients());
		
//		testUser.setIncompatibleIngredients(testIngredients);
//		assertEquals(testIngredients, testUser.getIncompatibleIngredients());
//		assertEquals(false, testUser.isIncompatibleIngredientsFromDatabase());
//		testUser.setAllDataFromDatabase();
		
//		testUser.addIncompatibleIngredient(null);
//		assertEquals(testIngredients, testUser.getIncompatibleIngredients());
//		assertEquals(true, testUser.isIncompatibleIngredientsFromDatabase());
//		
		testUser.addIncompatibleIngredient("ing3");
		assertEquals(testIngredients, testUser.getIncompatibleIngredients());
		
		testIngredients.add("ing4");
		testUser.addIncompatibleIngredient("ing4");
		assertEquals(testIngredients, testUser.getIncompatibleIngredients());
		
//		testUser.removeIncompatibleIngredient(null);
//		assertEquals(testIngredients, testUser.getIncompatibleIngredients());
//		assertEquals(true, testUser.isIncompatibleIngredientsFromDatabase());
//		
		testUser.removeIncompatibleIngredient("ing5");
		assertEquals(testIngredients, testUser.getIncompatibleIngredients());
		
		testIngredients.remove("ing4");
		testUser.removeIncompatibleIngredient("ing4");
		assertEquals(testIngredients, testUser.getIncompatibleIngredients());
	}
	
	@Test
	public void recipeRatingsTest() {
		Map<Long, Integer> testRatings = new HashMap<>();
		testRatings.put((long) 1, 2);
		testRatings.put((long) 2, 4);
		testRatings.put((long) 3, 6);
		
		Map<Long, Integer> emptyMap = new HashMap<>();
		
		
		assertEquals(emptyMap, testUser.getRecipeRatings());
		
		testUser.setRecipeRatings(null);
		assertEquals(emptyMap, testUser.getRecipeRatings());
		
		testUser.setRecipeRatings(testRatings);
		assertEquals(testRatings, testUser.getRecipeRatings());
		
		testUser.addRecipeRating(1, 3);
		testRatings.replace((long) 1, 3);
		assertEquals(testRatings, testUser.getRecipeRatings());
		assertEquals(3, testUser.getRecipeRatings().get((long) 1).intValue());
		
		testUser.addRecipeRating(4, -2);
		testRatings.put((long) 4, -2);
		assertEquals(testRatings, testUser.getRecipeRatings());
		assertTrue(testUser.getRecipeRatings().containsKey((long) 4));
		
		testUser.removeRecipeRating(0);
		assertEquals(testRatings, testUser.getRecipeRatings());
		
		testRatings.remove((long) 4);
		testUser.removeRecipeRating(4);
		assertEquals(testRatings, testUser.getRecipeRatings());
		assertFalse(testUser.getRecipeRatings().containsKey((long) 4));
	}
	
	@Test
	public void spokenLanguagesTest() {
		
		assertEquals(Language.UNDEF, testUser.getCurrentlySpokenLanguage());
		Language nullLanguage = null;
		testUser.setCurrentlySpokenLanguage(nullLanguage);
		assertEquals(Language.UNDEF, testUser.getCurrentlySpokenLanguage());
		
		testUser.setCurrentlySpokenLanguage("NoLanguage");
		assertEquals(Language.UNDEF, testUser.getCurrentlySpokenLanguage());
		
		testUser.setCurrentlySpokenLanguage("de");
		assertEquals(Language.GERMAN, testUser.getCurrentlySpokenLanguage());
		
		Set<Language> testLanguages = new HashSet<>();
		testLanguages.add(Language.GERMAN);
		
		testUser.removeLanguageFromSpokenLanguages("NoLanguage");
		testUser.removeLanguageFromSpokenLanguages("");
		testUser.removeLanguageFromSpokenLanguages(nullLanguage);
		assertEquals(testLanguages, testUser.getSpokenLanguages());
		
		Set<Language> emptySet = new HashSet<>();
		testUser.removeLanguageFromSpokenLanguages("de");
		assertEquals(emptySet, testUser.getSpokenLanguages());
		
		testUser.setSpokenLanguages(testLanguages);
		testLanguages.add(Language.ENGLISH);
		assertFalse(testUser.getSpokenLanguages().contains(Language.ENGLISH));
		assertTrue(testUser.getSpokenLanguages().contains(Language.GERMAN));
	}
}
