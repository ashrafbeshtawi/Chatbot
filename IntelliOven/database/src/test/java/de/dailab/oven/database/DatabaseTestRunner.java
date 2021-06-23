package de.dailab.oven.database;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import de.dailab.oven.database.parse.ParserTestSuite;
import de.dailab.oven.database.query.QueryTestSuite;
//import de.dailab.oven.database.parse.ParserTestSuite;
//import de.dailab.oven.database.query.QueryTestSuite;
import de.dailab.oven.database.validate.ValidatorTestSuite;

public class DatabaseTestRunner {

	private static final Logger LOGGER = Logger.getLogger(DatabaseTestRunner.class.getCanonicalName());
	
	public static void main(String[] args) {
		
		//Define test database
		System.setProperty("oven_database_test_uri", "bolt://130.149.154.16:7688");
		System.setProperty("oven_database_test_user", "neo4j");
		System.setProperty("oven_database_test_pw", "Intell10venTests");
		
		Result result = JUnitCore.runClasses(
				ValidatorTestSuite.class,
				ParserTestSuite.class,
				QueryTestSuite.class
				);
		
		LOGGER.log(Level.INFO, "------DATABASE TESTS RUNNER------");
		
		boolean error = false;
		for(Failure failure : result.getFailures()) {
			LOGGER.log(Level.INFO, failure.toString());
			if(!error) {
				error = true;
			}
		}
		
		if(!error) {
			LOGGER.log(Level.INFO, "No error in tests");
		}
		
		LOGGER.log(Level.INFO, "---------------END---------------");
	}
}