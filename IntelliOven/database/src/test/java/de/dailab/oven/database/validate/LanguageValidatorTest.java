package de.dailab.oven.database.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import zone.bot.vici.Language;

public class LanguageValidatorTest {

	private static final LanguageValidator LANGUAGE_VALIDATOR = new LanguageValidator();
	
	@Test
	public void isValidLanguageTest() {
		Language nullLanguage = null;
		Language invalid = Language.UNDEF;
		Language valid = Language.GERMAN;
		
		assertFalse(LANGUAGE_VALIDATOR.isValid(nullLanguage));
		assertFalse(LANGUAGE_VALIDATOR.isValid(invalid));
		assertTrue(LANGUAGE_VALIDATOR.isValid(valid));
	}
}
