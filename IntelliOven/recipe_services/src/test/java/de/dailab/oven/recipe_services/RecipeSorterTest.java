package de.dailab.oven.recipe_services;

import org.junit.Test;

import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.User;
import de.dailab.oven.recipe_services.common.RecipePrinter;
import de.dailab.oven.recipe_services.common.RecipeSorter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

import org.junit.Before;

public class RecipeSorterTest {
	
	Recipe first;
	Recipe second;
	Recipe third;
	Logger logger;
	
	@Before
	public void initialize() {
		this.first = new Recipe();
		this.second = new Recipe();
		this.third = new Recipe();
		this.logger = Logger.getGlobal();
	}
	
	@Test
	public void sorterTest() {
		this.first.setAuthor("Max Mustermann");
		this.second.setAuthor("Max Mustermann");
		this.third.setAuthor("Max Mustermann");

		this.first.setLanguage("de");
		this.second.setLanguage("de");
		this.third.setLanguage("de");

		this.first.setName("Lecker Möhrchen");
		this.second.setName("Lecker Banane");
		this.third.setName("Lecker Nix");
		
		final Map<Long, Integer> testRatings1 = new HashMap<>();
		testRatings1.put((long) 1, 3);

		this.first.setUserRatings(testRatings1);
		
		final Map<Long, Integer> testRatings2 = new HashMap<>();
		testRatings2.put((long) 1, 2);

		this.second.setUserRatings(testRatings2);
		
		List<Recipe> recipes = new ArrayList<>();
		recipes.add(this.third);
		recipes.add(this.second);
		recipes.add(this.first);
		
		final User testUser = new User();
		testUser.setId(1l);
		System.out.println("USER_ID: " + testUser.getId());
		recipes = new RecipeSorter().sortRecipesByRatings(recipes, testUser);
		for(final Recipe recipe : recipes) {
			System.out.println(RecipePrinter.recipeToString(recipe));
		}
		
		assertEquals(this.first, recipes.get(0));
	}
		
}
