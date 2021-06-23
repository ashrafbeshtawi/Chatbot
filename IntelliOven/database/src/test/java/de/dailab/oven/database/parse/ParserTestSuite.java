package de.dailab.oven.database.parse;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	IngredientParserTest.class,
	LanguageParserTest.class,
	NutritionParserTest.class
})

public class ParserTestSuite {}