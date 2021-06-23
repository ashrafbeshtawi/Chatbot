package de.dailab.oven.database.validate;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import zone.bot.vici.Language;

/**
 * Class for verifying languages
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class LanguageValidator extends AValidator{

	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(LanguageValidator.class.getName());
	@Nonnull
	private static final String LANGUAGE_KEY = "Language";
	@Nonnull
	private static final String LANGUAGE_UNDEF = LANGUAGE_KEY + ".UNDEF";
	
	/**
	 * Checks if language is not NULL and supported
	 * @param language	The language to test
	 * @return			True if language is valid<br>False otherwise
	 */
	public boolean isValid(@Nullable Language language) {
		//Check if language is NULL
		if(language == null) {
			logNull(LOGGER, LANGUAGE_KEY);
			return false;
		}
		
		//Check if language is supported
		if(language == Language.UNDEF) {
			logInvalid(LOGGER, LANGUAGE_UNDEF, LANGUAGE_KEY, LANGUAGE_KEY, "");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if all languages are valid
	 * @param languages	The languages to test
	 * @return			True if all languages are valid<br>False otherwise or set is empty or null
	 */
	public boolean isValid(Set<Language> languages) {
		//Check if languages is NULL
		if(languages == null) {
			logNull(LOGGER, LANGUAGE_KEY + "s");
			return false;
		}
		
		else if(languages.isEmpty()) {
			logInvalid(LOGGER, LANGUAGE_KEY + "s", LANGUAGE_KEY, LANGUAGE_KEY, "");
			return false;
		}
		
		//Check each language
		for(Language language : languages) {
			if(!isValid(language))
				return false;
		}
		
		return true;
	}

	/**
	 * @param languageObject	{@link Language} or Set of {@link Language} to test
	 * @return 					<tt>True</tt> if languageObject is valid<br><tt>False</tt> otherwise
	 */
	@Override
	public <T> boolean isValid(T languageObject) {
	
		if(languageObject == null) {
			logNull(LOGGER, Language.class.getSimpleName());
			return false;
		}
		
		if(isCorrectObject(languageObject, HashSet.class, LOGGER)) {
			Set<?> set = (Set<?>) languageObject;
			
			if(set.isEmpty()) return true;
			
			if(isCorrectObject(set.iterator().next(), Language.class, LOGGER)) {
				Set<Language> languages = new HashSet<>();
				set.forEach(c -> languages.add((Language) c));
				return isValid(languages);
			}
		}

		if(isCorrectObject(languageObject, Language.class, LOGGER)) 
			return isValid((Language) languageObject);
		
		return false;
	}
}