package de.dailab.oven.database.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.dailab.oven.database.validate.model.NameRequest;

public class NameValidatorTest {

	private static final NameValidator NAME_VALIDATOR = new NameValidator();
	
	@Test
	public void nullTest() {
		assertFalse(NAME_VALIDATOR.isValid(null, "Var", 4));
		assertTrue(NAME_VALIDATOR.isValid("name", null, 4));
		assertTrue(NAME_VALIDATOR.isValid("name", "Var", null));
	}

	@Test
	public void emptyTest() {
		assertFalse(NAME_VALIDATOR.isValid("", "Var", 4));
		assertFalse(NAME_VALIDATOR.isValid("name", "", 5));
		assertTrue(NAME_VALIDATOR.isValid("name", "", 4));
		assertTrue(NAME_VALIDATOR.isValid("name", "Var", -1));
	}
	
	@Test
	public void validTest() {
		assertTrue(NAME_VALIDATOR.isValid("name", "Var", 4));
	}
	@Test
	public void nameRequestTest() {
		assertFalse(NAME_VALIDATOR.isValid(new NameRequest("User", "",3)));
	}
}