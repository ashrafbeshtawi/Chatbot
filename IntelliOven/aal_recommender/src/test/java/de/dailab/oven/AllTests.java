package de.dailab.oven;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ collaborativeRecommenderLoadDataTest.class, cRgetRecommendationsAllNormal.class,
		cRgetRecommendationsEmptyUserTest.class, cRgetRecommendationsWithAmount0Test.class,
		cRgetRecommendationsWithNulls.class })
public class AllTests {

}
