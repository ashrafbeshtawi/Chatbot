package de.dailab.oven.database.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Relationship;

import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.model.database.NodeLabel;

/**
 * Class for setting and getting individual oven settings
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class IndividualOvenSettingsQuery extends AQuery{
	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(IndividualOvenSettingsQuery.class.getName());
	@Nonnull
	private final IdQuery idQuery;
	private static final double DEFAULT_FACTOR = 1.0d;
	private static final double ZERO_FACOTR = 0.0d;
	@Nonnull
	private static final NodeLabel RECIPE_LABEL = NodeLabel.RECIPE;
	@Nonnull 
	private static final NodeLabel USER_LABEL = NodeLabel.USER;
	@Nonnull
	private static final String RECIPE = NodeLabel.RECIPE.toDatabaseLabel();
	@Nonnull
	private static final String RECIPE_ID = "recipeId";
	@Nonnull
	private static final String MATCH_RECIPE_ID = "MATCH(recipe:" + RECIPE + ") WHERE ID(recipe) = $";
	@Nonnull
	private static final String USER = NodeLabel.USER.toDatabaseLabel();
	@Nonnull
	private static final String USER_ID = "userId";
	@Nonnull
	private static final String MATCH_USER_ID = "MATCH(user:" + USER + ") WHERE ID(user) = $";
	@Nonnull
	private static final String WITH = "WITH * ";
	@Nonnull
	private static final String RATED = "rated.";
	@Nonnull
	public static final String TEMP = "tempFactor";
	@Nonnull
	public static final String TIME = "timeFactor";
	@Nonnull
	public static final String SERVING = "servingFactor";
	
	/**
	 * Initialize the class with the graph to query 
	 * @param graph	The graph to query
	 */
	public IndividualOvenSettingsQuery(@Nullable Graph graph) {
		super(graph);
		this.idQuery = (IdQuery) getQuery(IdQuery.class);
	}
	
	/**
	 * Sets the individual time factor for the user and the given recipe 
	 * @param recipeId		The ID for the recipe to set the factor for
	 * @param userId		The ID for the user to set the factor for
	 * @param timeFactor	The factor to set
	 * @return 				True if the factor has been set<br>
	 * 						False if setting failed or graph is NULL<br> 
	 * 						NULL in case of lost database connection.
	 */
	@Nullable
	public Boolean setTimeFactor(@Nullable Long recipeId, @Nullable Long userId, 
			@Nullable Double timeFactor) {
		
		if(this.graph != null)
			return setSingleSetting(recipeId, userId, timeFactor, TIME);			
		
		return false;
	}
	
	/**
	 * Get the individual time factor for the user and the given recipe 
	 * @param recipeId	The ID for the recipe to get the factor for
	 * @param userId	The ID for the user to get the factor for
	 * @return	1.0d if graph is NULL, variable is not set or the factor has not been set yet<br>
	 * 			NULL in case of lost database connection<br>
	 * 			0.0d in case any ID does not match to the expected node label<br>
	 * 			The gotten factor otherwise
	 */
	@Nullable
	public Double getTimeFactor(@Nullable Long recipeId, @Nullable Long userId) {
		if(this.graph != null) 
			return getSingleSetting(recipeId, userId, TIME);
		
		return DEFAULT_FACTOR;
	}
	
	/**
	 * Sets the individual temperature factor for the user and the given recipe 
	 * @param recipeId		The ID for the recipe to set the factor for
	 * @param userId		The ID for the user to set the factor for
	 * @param tempFactor	The factor to set
	 * @return				True if the factor has been set<br>
	 * 						False if setting failed or graph is NULL<br> 
	 * 						NULL in case of lost database connection.
	 */
	@Nullable
	public Boolean setTempFactor(@Nullable Long recipeId, @Nullable Long userId, 
			@Nullable Double tempFactor) {
		
		if(this.graph != null) 
			return setSingleSetting(recipeId, userId, tempFactor, TEMP);			
		
		return false;
	}

	/**
	 * Get the individual temperature factor for the user and the given recipe 
	 * @param recipeId	The ID for the recipe to get the factor for
	 * @param userId	The ID for the user to get the factor for
	 * @return	1.0d if graph is NULL, variable is not set or the factor has not been set yet<br>
	 * 			NULL in case of lost database connection<br>
	 * 			0.0d in case any ID does not match to the expected node label<br>
	 * 			The gotten factor otherwise
	 */
	@Nullable
	public Double getTempFactor(@Nullable Long recipeId, @Nullable Long userId) {
		if(this.graph != null) 
			return getSingleSetting(recipeId, userId, TEMP);
		
		return DEFAULT_FACTOR;
	}
	
	/**
	 * Sets the individual serving factor for the user and the given recipe 
	 * @param recipeId		The ID for the recipe to set the factor for
	 * @param userId		The ID for the user to set the factor for
	 * @param servingFactor	The factor to set
	 * @return				True if the factor has been set<br>
	 * 						False if setting failed or graph is NULL<br> 
	 * 						NULL in case of lost database connection.
	 */
	@Nullable
	public Boolean setServingFactor(@Nullable Long recipeId, @Nullable Long userId, 
			@Nullable Double servingFactor) {
		
		if(this.graph != null) 
			return setSingleSetting(recipeId, userId, servingFactor, SERVING);			
		
		return false;
	}
	
	/**
	 * Get the serving temperature factor for the user and the given recipe 
	 * @param recipeId	The ID for the recipe to get the factor for
	 * @param userId	The ID for the user to get the factor for
	 * @return	1.0d if graph is NULL, variable is not set or the factor has not been set yet<br>
	 * 			NULL in case of lost database connection<br>
	 * 			0.0d in case any ID does not match to the expected node label<br>
	 * 			The gotten factor otherwise
	 */
	@Nullable
	public Double getServingFactor(@Nullable Long recipeId, @Nullable Long userId) {
		if(this.graph != null) 
			return getSingleSetting(recipeId, userId, SERVING);
		
		return DEFAULT_FACTOR;
	}
	
	/**
	 * Sets the individual oven settings with the given factors
	 * @param recipeId		The ID for the recipe to set the factors for
	 * @param userId		The ID for the user to set the factors for
	 * @param timeFactor	The time factor to set
	 * @param tempFactor	The temperature factor to set
	 * @param servingFactor	The serving factor to set
	 * @return				True if the factors have been set<br>
	 * 						False if setting failed or graph is NULL<br> 
	 * 						NULL in case of lost database connection.
	 */
	@Nullable
	public Boolean setIndividualOvenSettings(@Nullable Long recipeId, @Nullable Long userId, 
			@Nullable Double timeFactor, @Nullable Double tempFactor, 
			@Nullable Double servingFactor) {
		
		Boolean validRecipeID = this.idQuery.isNodeIdValid(recipeId, RECIPE_LABEL);
		Boolean validUserID = this.idQuery.isNodeIdValid(userId, USER_LABEL);
		
		if(this.graph == null || timeFactor == null || tempFactor == null || servingFactor == null)
			return false;
		
		if(Boolean.TRUE.equals(validRecipeID) 
				&& Boolean.TRUE.equals(validUserID)) {
			
			try (Session writeSession = this.graph.openWriteSession()){
			
				Statement statement = new Statement(
						MATCH_RECIPE_ID + RECIPE_ID + " "
								+ WITH + MATCH_USER_ID + USER_ID + " "
								+ WITH + "MERGE (recipe)<-[rated:RATED]-(user) "
								+ "ON CREATE SET " + RATED + TIME + " = 1.0, "
								+ RATED + TEMP + " = 1.0, "
								+ RATED + SERVING + " = 1.0 "
								+ "SET " + RATED + TIME + " = $" + TIME + ", "
								+ RATED + TEMP + " = $" + TEMP + ", "
								+ RATED + SERVING + " = $" + SERVING + " "
								+ WITH + "RETURN " 
									+ RATED + TIME + ", " 
									+ RATED + TEMP + ", " 
									+ RATED + SERVING);
				
				Record record = writeSession.run(statement.withParameters(Values.parameters(
						RECIPE_ID, recipeId,
						USER_ID, userId,
						TIME, timeFactor,
						TEMP, tempFactor,
						SERVING, servingFactor
						))).single();	
				
				double setTimeFactor 	= record.get(0).asDouble();
				double setTempFactor 	= record.get(1).asDouble();
				double setServingFactor = record.get(2).asDouble();
				
				
				return (Math.abs(setTimeFactor - timeFactor) < 0.001
						&& Math.abs(setTempFactor - tempFactor) < 0.001
						&& Math.abs(setServingFactor - servingFactor) < 0.001);
				
			} catch(Exception e) {
				logLostConnection(LOGGER);
				return null;
			}	
			
		}
		return false;
	}
	
	/**
	 * Get the oven settings map for a specific user and recipe
	 * @param recipeId 	The ID for the recipe to get the factors for
	 * @param userId	The ID for the user to get the factors for
	 * @return 	Empty map in case graph is NULL<br>
	 * 			NULL in case of lost database connection<br>
	 * 			Map with all default values (1.0d) if relationship does not exist<br>
	 * 			Map with partly default values (1.0d) if the specific factor has not been set<br>
	 * 			Map with all 0.0d values in case any ID does not match the expected node label<br>
	 * 			Map with all factors otherwise
	 */
	@Nullable
	public Map<String, Double> getIndividualOvenSettings(@Nullable Long recipeId, @Nullable Long userId) {
		
		Map<String, Double> factorMap = new HashMap<>();
		
		if(this.graph == null) {
			LOGGER.log(Level.INFO, "Graph is NULL. Empty map is returned");
			return factorMap;
		}
		
		Boolean validRecipeID = this.idQuery.isNodeIdValid(recipeId, RECIPE_LABEL);
		Boolean validUserID = this.idQuery.isNodeIdValid(userId, USER_LABEL);
		
		
		if(Boolean.TRUE.equals(validRecipeID)
				&& Boolean.TRUE.equals(validUserID)) {

			try (Session readSession = this.graph.openReadSession()){
				Statement statement = new Statement(
						MATCH_RECIPE_ID + RECIPE_ID + " "
								+ WITH + MATCH_USER_ID + USER_ID + " "
								+ WITH + "MATCH (recipe)<-[rated:RATED]-(user) "
								+ WITH + "RETURN rated");
				
				StatementResult result = readSession.run(statement.withParameters(Values.parameters(
						RECIPE_ID, recipeId,
						USER_ID, userId
						)));
				
				if(!result.hasNext())
					return getDefaultFactorMap();

				Record record = result.list().get(0);
				Relationship rated = getRelationship(record.get(0));
				
				if(rated == null) {
					LOGGER.log(Level.INFO, "Default factors 1.0d is returned since of: {0}", 
							"Casting to relationship was not possible");
					
					return getDefaultFactorMap();				
				}
				
				factorMap.put(TIME, getFactorFromRelationShip(rated, TIME));
				factorMap.put(TEMP, getFactorFromRelationShip(rated, TEMP));
				factorMap.put(SERVING, getFactorFromRelationShip(rated, SERVING));
				
				return factorMap;
			
			} catch(Exception e) {
				logLostConnection(LOGGER);
				return null;
			}
				
		}		
		
		return getZeroFactorMap();
	}

	/**
	 * Get the average oven settings map for a specific user based on the 50% best rated recipes
	 * @param userId	The ID for the user to get the factors for
	 * @return 	Empty map in case graph is NULL<br>
	 * 			NULL in case of lost database connection<br>
	 * 			Map with all default values (1.0d) if relationships do not exist<br>
	 * 			Map with partly default values (1.0d) if the specific factor has not been set<br>
	 * 			Map with all 0.0d values in case ID does not match to a user<br>
	 */
	@Nullable
	public Map<String, Double> getAverageTempTimeServingFactor(@Nullable Long userId) {
		Map<String, Double> avgFactors = new HashMap<>();
		
		if(this.graph == null) {
			LOGGER.log(Level.INFO, "Graph is NULL. Empty map is returned");
			return avgFactors;
		}
		
		Boolean validUserID = this.idQuery.isNodeIdValid(userId, USER_LABEL);
		
		if(validUserID != null && validUserID) {

			try (Session readSession = this.graph.openReadSession()){
			
				Statement statement = new Statement(
						"MATCH (u:User)-[rated:RATED]-(re:Recipe) WHERE ID(u) = $" + USER_ID + " "
								+ "AND " + RATED + "rating IS NOT NULL "
								+ "RETURN rated ORDER BY " + RATED + "rating DESCENDING"
						);
				
				StatementResult result = readSession.run(statement.withParameters(Values.parameters(
						USER_ID, userId
						)));
				
				int numberOfRatings = 0;
				List<Record> records = new ArrayList<>();
				if(result.hasNext()) {
					records = result.list();			
					if(records.size() < 2) 
						numberOfRatings = 2;
					
					else 
						numberOfRatings = records.size();
					
				}
				else 			
					return getDefaultFactorMap();
				
		
				int half = numberOfRatings / 2;
				
				return getAvgFactorMap(avgFactors, records, half);
				
			} catch(Exception e) {
				logLostConnection(LOGGER);
				return null;
			}
					
		}

		return getZeroFactorMap();
	}

	/**
	 * Tries to parse the records and adds the factors to the passed map
	 * @param avgFactors 	The current factor map
	 * @param records		The records from a database request
	 * @param half			The current half to divide the result with
	 * @return				The new computed average factor map
	 */
	private Map<String, Double> getAvgFactorMap(Map<String, Double> avgFactors, List<Record> records, int half) {
		
		double tempFactor 		= 0.0d;
		double timeFactor 		= 0.0d;
		double servingFactor 	= 0.0d;
		
		Record record;
		Relationship rated = null;
		
		if(!records.isEmpty()) {
			for(int i = 0; i < half; i++) {
				record = records.get(i);
				rated = getRelationship(record.get(0));
				timeFactor 		+= getFactorFromRelationShip(rated, TIME);
				tempFactor 		+= getFactorFromRelationShip(rated, TEMP);
				servingFactor 	+= getFactorFromRelationShip(rated, SERVING);
			}
		}
		
		avgFactors.put(TEMP, getDefaultOrFactorAverage(tempFactor, half));
		avgFactors.put(TIME, getDefaultOrFactorAverage(timeFactor, half));
		avgFactors.put(SERVING, getDefaultOrFactorAverage(servingFactor, half));
		
		return avgFactors;
	}
	
	/**
	 * Sets the individual factor for the user and the given recipe 
	 * @param recipeId	The ID for the recipe to set the factor for
	 * @param userId	The ID for the user to set the factor for
	 * @param factor	The factor value to set
	 * @param variable	The factors key which shall be used
	 * @return	True if the factor has been set, False if not. NULL in case of lost<br>
	 * 						database connection.
	 */
	@Nullable
	private Boolean setSingleSetting(@Nullable Long recipeId, @Nullable Long userId, 
			@Nullable Double factor, @Nullable String variable) {
		
		if(variable == null || variable.isEmpty() || factor == null) 
			return false;
		
		Boolean validRecipeID = this.idQuery.isNodeIdValid(recipeId, RECIPE_LABEL);
		Boolean validUserID = this.idQuery.isNodeIdValid(userId, USER_LABEL);

		if(Boolean.TRUE.equals(validRecipeID) 
				&& Boolean.TRUE.equals(validUserID)) {
		
			try (Session writeSession = this.graph.openWriteSession()){
				Statement statement = new Statement(
						MATCH_RECIPE_ID + RECIPE_ID + " "
							+ WITH + MATCH_USER_ID + USER_ID + " "
							+ WITH + "MERGE (recipe)<-[rated:RATED]-(user) "
							+ "ON CREATE SET " + RATED + variable + "= 1.0 "
							+ "SET " + RATED + variable +" = $factor "
							+ WITH + "RETURN " + RATED + variable);
				
				double setValue = writeSession.run(statement.withParameters(Values.parameters(
						RECIPE_ID, recipeId,
						USER_ID, userId,
						"factor", factor
						))).single().get(0).asDouble();
				
				return Math.abs(setValue - factor) < 0.001;

			} catch(Exception e) {
				logLostConnection(LOGGER);
				return null;
			}
		
		}		
		return false;
	}
	
	/**
	 * Get the individual oven setting based on the passed variable, user ID and recipe ID
	 * @param recipeId	The ID for the recipe to set the factor for
	 * @param userId	The ID for the user to set the factor for
	 * @param variable	The factors key which shall be used
	 * @return	1.0d if graph is NULL, variable is not set or the factor has not been set yet<br>
	 * 			NULL in case of lost database connection<br>
	 * 			0.0d in case any ID does not match to the expected node label<br>
	 * 			The gotten factor otherwise
	 */
	@Nullable
	private Double getSingleSetting(@Nullable Long recipeId, @Nullable Long userId, @Nullable String variable) {
		
		if(variable == null || variable.isEmpty()) {
			logDefaultFactor("Variable has not been set");
			return DEFAULT_FACTOR;
		}
		
		else if(this.graph == null) {
			logDefaultFactor("Graph is NULL");
			return DEFAULT_FACTOR;
		}
		
		double factor = 0.0d;
		
		Boolean validRecipeID = this.idQuery.isNodeIdValid(recipeId, RECIPE_LABEL);
		Boolean validUserID = this.idQuery.isNodeIdValid(userId, USER_LABEL);
		
		if(Boolean.TRUE.equals(validRecipeID) 
				&& Boolean.TRUE.equals(validUserID)) {
			
			try (Session readSession = this.graph.openReadSession()){
				
				Statement statement = new Statement(
						MATCH_RECIPE_ID + RECIPE_ID + " "
								+ WITH  + MATCH_USER_ID + USER_ID + " "
								+ WITH + "MATCH (recipe)<-[rated:RATED]-(user) "
								+ WITH + "RETURN rated");
				
				Record record = readSession.run(statement.withParameters(Values.parameters(
						RECIPE_ID, recipeId,
						USER_ID, userId
						))).single();
				
				Relationship rated = getRelationship(record.get(0));
				
				if(rated == null)
					return DEFAULT_FACTOR;
				
				factor = getFactorFromRelationShip(rated, variable);
				
			} catch(Exception e) {
				logLostConnection(LOGGER);
				return null;
			}
		}		
		
		return factor;
	}
	
	/**
	 * Initializes a default factor map
	 * @return Map with all factors = 1.0d
	 */
	@Nonnull
	private Map<String, Double> getDefaultFactorMap() {
		Map<String, Double> defaultMap = new HashMap<>();
		
		defaultMap.put(TEMP, DEFAULT_FACTOR);
		defaultMap.put(TIME, DEFAULT_FACTOR);
		defaultMap.put(SERVING, DEFAULT_FACTOR);
		
		return defaultMap;
	}
	
	/**
	 * Initializes a zero factor map
	 * @return Map with all factors = 0.0d
	 */
	@Nonnull
	private Map<String, Double> getZeroFactorMap() {
		Map<String, Double> zeroMap = new HashMap<>();
		
		zeroMap.put(TEMP, ZERO_FACOTR);
		zeroMap.put(TIME, ZERO_FACOTR);
		zeroMap.put(SERVING, ZERO_FACOTR);
		
		return zeroMap;
	}
	
	/**
	 * Returns either the calculated factor or default factor if factor is 0.0d
	 * @param factor		Factor to observe
	 * @param avgFactor		The number to divide the factor by
	 * @return				Default factor or average factor
	 */
	private double getDefaultOrFactorAverage(double factor, int avgFactor) {
		if(Math.abs(factor) < 0.001d)
			return DEFAULT_FACTOR;
		else 
			return factor/avgFactor;
	}
	
	/**
	 * Tries to cast the given value to a relationship
	 * @param value	The value which is expected to be a relationship
	 * @return		The relationship or NULL in case of failure
	 */
	@Nullable
	private Relationship getRelationship(@Nullable Value value) {
		
		if(value != null) {
			try {
				return value.asRelationship();
			} catch (Exception e) {
				LOGGER.log(Level.INFO, "Could not cast value {0} as relationship", value);
			}			
		}
		
		return null;
	}
	
	/**
	 * Tries to extract the factor with the given key from the relationship
	 * @param relationship	The relationship to extract the factor from
	 * @param factor		The factors key
	 * @return				The factor or default factor 1.0d in case of failure
	 */
	private double getFactorFromRelationShip(@Nullable Relationship relationship, @Nonnull String factor) {
		
		if(relationship != null && relationship.containsKey(factor)) {
			try {
				return relationship.get(factor).asDouble();									
			} catch(Exception e) {
				logDefaultFactor(e.getLocalizedMessage());
			}
		}
		
		return DEFAULT_FACTOR;
	}
	
	/**
	 * Logs that a default factor is returned
	 * @param message	The message why it is returned
	 */
	private void logDefaultFactor(@Nullable String message) {
		if(message == null)
			message = "unknown";

		LOGGER.log(Level.INFO, "Default factor 1.0d is returned since of: {0}", message);
	}
}
