package de.dailab.oven.database.validate;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	AmountValidatorTest.class,
	CategoryValidatorTest.class,
	IdValidatorTest.class,
	IngredientValidatorTest.class,
	LanguageValidatorTest.class,
	NameValidatorTest.class,
	NutritionValidatorTest.class,
	RatingValidatorTest.class,
	RecipeValidatorTest.class,
	UserValidatorTest.class
})

public class ValidatorTestSuite {}
