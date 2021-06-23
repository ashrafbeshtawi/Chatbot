package de.dailab.oven.database.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

import de.dailab.oven.database.configuration.Configuration;
import de.dailab.oven.database.configuration.ConfigurationLoader;
import de.dailab.oven.database.configuration.DatabaseConfiguration;
import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.User;
import de.dailab.oven.model.data_model.filters.RecipeFilter;



public class Query implements IQuery {

	//-----------
	//FIELD STUFF
	//-----------

    private static final Logger LOG = LoggerFactory.getLogger(Query.class);

	private Graph graph;
	private Session readSession;
	private Session writeSession;
	private RecipeQuery recipeQuery;
	private UserQuery userQuery;
	private final ConfigurationLoader configLoader = new ConfigurationLoader();
	
	/**
	 * @param graph: Graph to use for the queries
	 * @throws DatabaseException Exception which is thrown in case connection can not be established
	 * @throws ConfigurationException Exception thrown if configuration is invalid
	 */
	public Query(@Nonnull final Graph graph) throws DatabaseException, ConfigurationException {
		this.graph = Objects.requireNonNull(graph, "Parameter 'graph' must not be null");
		this.readSession = graph.openReadSession();
		this.writeSession = graph.openWriteSession();
		this.recipeQuery = new RecipeQuery(this.graph);
		this.userQuery = new UserQuery(this.graph);
	}
	
	/**
	 * Connects to a graph based on the environment variables or connects to main graph 
	 * @throws DatabaseException Exception which is thrown in case connection can not be established
	 * @throws ConfigurationException Exception thrown if configuration is invalid
	 */
	public Query() throws DatabaseException, ConfigurationException {
		if(!this.configLoader.getUri().contentEquals("")) {
			this.graph = new Graph(new DatabaseConfiguration(this.configLoader.getUri(), this.configLoader.getUser(), this.configLoader.getPw()));
		}
		else {
			try {
				this.graph = new Graph(Configuration.getInstance().getDatabaseConfiguration());
			} catch (final Exception e) {
			    LOG.error("Establishing connection to Database failed", e);
				return;
			}
		}
		this.readSession = this.graph.openReadSession();
		this.writeSession = this.graph.openWriteSession();
		this.recipeQuery = new RecipeQuery(this.graph);
		this.userQuery = new UserQuery(this.graph);
	}
	
	/**
	 * Use this in between when system is in 'standby'  
	 * @throws DatabaseException Exception which is thrown in case connection can not be established
	 * @throws ConfigurationException Exception thrown if configuration is invalid
	 */
	public void renewSessions() throws DatabaseException, ConfigurationException {
		this.readSession.close();
		this.writeSession.close();
		this.readSession = this.graph.openReadSession();
		this.writeSession = this.graph.openWriteSession();
		this.recipeQuery = new RecipeQuery(this.graph);
		this.userQuery = new UserQuery(this.graph);
	}
	
	/*
	 * Use to close the connection to the graph safely
	 * @throws Exception
	 */
	public void close() throws Exception {
		this.readSession.close();
		this.writeSession.close();
		this.graph.close();
	}
	
	/*
	 * Deletes every relationship and every recipe in the Graph
	 * @throws Exception
	 */	
	public void clearGraph() throws Exception{
		final Statement statement = new Statement(
				"MATCH (n) DETACH DELETE n"
				);
		
		this.writeSession.run(statement);
	}
	
	
	//--------------
	//RECIPE QUERIES
	//--------------
	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void putSingleRecipe(final Recipe recipe) throws Exception {
		this.recipeQuery.putSingleRecipe(recipe);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putMultipleRecipes(final List<Recipe> recipes) throws Exception {
		this.recipeQuery.putMultipleRecipes(recipes);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Recipe checkForRecipeDuplicates(final Recipe recipe) throws Exception {
		return this.recipeQuery.getDuplicate(recipe);
	}
 	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Recipe> getRecipe(final RecipeFilter recipeFilter) throws Exception {
		
		return this.recipeQuery.getRecipe(recipeFilter);
	}
	
	//------------
	//USER QUERIES
	//------------
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putUser(final User user) throws Exception {
		this.userQuery.putUser(user);

	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<User> getUser(final String userName, final long userId) throws Exception{
		return this.userQuery.getUser(userName, userId);
	}
	
	public User getUser(Long id) {return this.userQuery.getUser(id);}
	
	/*
	 * Add time stamp to the cooked relationship and creates the relationship if necessary
	 * @param user
	 * @param recipeId
	 * @param date
	 * @throws Exception
	 */
	public void addCookedRecipe(final User user, final long recipeId, final LocalDate date) throws Exception {
		this.userQuery.addCookedRecipe(user, recipeId, date);
	}
	
	
	//------------------
	//INGREDIENT QUERIES
	//------------------
	
	/*
	 * Adds a replaceable relationship between to ingredients
	 * @param ingred
	 * @param
	 */
	public void addReplaceableRelationship(final Ingredient ingredientToReplace, final Ingredient newIngredient) {

		final Statement statement = new Statement(
				"MATCH (ingredientToReplace:Ingredient) "
					+ "WHERE "
						+ "ingredientToReplace.name = $ingredientToReplaceName "
				+ "MERGE (newIngredient: Ingredient {"
					+ "name: $newIngredientName"
					+ "}) "
				+ "MERGE (ingredientToReplace)-[:REPLACEABLE_BY]->(newIngredient)"
				);
		
		this.writeSession.run(statement.withParameters(Values.parameters(
				"ingredientToReplaceName", ingredientToReplace.getName(),
				"newIngredientName", newIngredient.getName()
				)));
	}

	public void setIndividualOvenSettings(final long recipeID, final long userID, final double timeFactor, final double tempFactor, final double servingFactor) throws InputException, DatabaseException {
		new IndividualOvenSettingsQuery(this.graph)
				.setIndividualOvenSettings(recipeID, userID, timeFactor, tempFactor, servingFactor);
	}

	public Map<String, Double> getAverageTempTimeServingFactor(final long userID) throws DatabaseException {
		return new IndividualOvenSettingsQuery(this.graph).getAverageTempTimeServingFactor(userID);
	}

	public void setTimeFactor(final long recipeID, final long userID, final double timeFactor) throws InputException, DatabaseException {
		new IndividualOvenSettingsQuery(this.graph).setTimeFactor(recipeID, userID, timeFactor);
	}

	public void setTempFactor(final long recipeID, final long userID, final double tempFactor) throws InputException, DatabaseException {
		new IndividualOvenSettingsQuery(this.graph).setTimeFactor(recipeID, userID, tempFactor);
	}
	
	public void setServingFactor(final long recipeID, final long userID, final double servingFactor) throws InputException, DatabaseException {
		new IndividualOvenSettingsQuery(this.graph).setTimeFactor(recipeID, userID, servingFactor);
	}
	
	@Deprecated
	public Session getReadSession() {
		return this.readSession;
	}

	@Deprecated
	public Session getWriteSession() {
		return this.writeSession;
	}

	public Graph getGraph() {
		return this.graph;
	}

	@Nullable
	public List<Recipe> executeRecipeQueryString(final String queryString) throws InputException, InterruptedException, ExecutionException {
	    if(queryString != null && !queryString.contentEquals("")) {
	    	LOG.debug("Execute recipe query: \n {}", queryString);
	        return this.recipeQuery.executeQueryString(queryString);
	    }
	    return null;
	}

	public IngredientQuery getIngredientQuery() {return new IngredientQuery(this.graph);}
	
}
