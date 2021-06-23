package de.dailab.oven.database.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import de.dailab.oven.model.data_model.Category;

public class CategoryValidatorTest {

	private static final CategoryValidator CATEGORY_VALIDATOR = new CategoryValidator();
	
	@Test
	public void nullTest() {
		Category nullCateory = null;
		assertFalse(CATEGORY_VALIDATOR.isValid(nullCateory));
		
		Set<Category> nullCategories = null;
		assertFalse(CATEGORY_VALIDATOR.isValid(nullCategories));
	}

	@Test
	public void emptyTest() {
		assertFalse(CATEGORY_VALIDATOR.isValid(new Category("")));
		assertTrue(CATEGORY_VALIDATOR.isValid(new HashSet<Category>()));
	}
	
	@Test
	public void invalidTest() {
		assertFalse(CATEGORY_VALIDATOR.isValid(new Category("n")));
		Set<Category> categories = new HashSet<>();
		categories.add(new Category("t"));
		assertFalse(CATEGORY_VALIDATOR.isValid(categories));
	}
	
	@Test
	public void validTest() {
		assertTrue(CATEGORY_VALIDATOR.isValid(new Category("Ei")));
		Set<Category> categories = new HashSet<>();
		categories.add(new Category("Öl"));
		assertTrue(CATEGORY_VALIDATOR.isValid(categories));	
	}
}