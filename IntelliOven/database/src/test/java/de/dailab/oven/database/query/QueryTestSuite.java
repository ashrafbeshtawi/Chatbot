package de.dailab.oven.database.query;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	IdQueryTest.class,
	ImageQueryTest.class,
	IndividualOvenSettingsQueryTest.class,
	IngredientQueryTest.class,
	NutritionQueryTest.class
})

public class QueryTestSuite {}
