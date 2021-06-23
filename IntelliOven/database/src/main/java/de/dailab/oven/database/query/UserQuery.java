package de.dailab.oven.database.query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jsoup.internal.StringUtil;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.model.database.RelationshipType;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.database.parse.CategoryParser;
import de.dailab.oven.database.parse.HouseholdParser;
import de.dailab.oven.database.parse.IngredientParser;
import de.dailab.oven.database.parse.LanguageParser;
import de.dailab.oven.database.parse.UserParser;
import de.dailab.oven.database.validate.UserValidator;
import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.data_model.HouseholdLabel;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.User;
import de.dailab.oven.model.database.NodeLabel;
import zone.bot.vici.Language;

public class UserQuery extends AQuery{

	private static final String COOKED_KEY = "cooked";

	private static final String LIKES_ING_KEY = "likesing";

	private static final String WITH_MERGE = " WITH * MERGE(";

	private static final String USER_NAME = "userName";

	private static final String LANGUAGE_KEY = "languages";

	private static final String RATING_KEY = "ratings";

	private static final String MATCH_USER_ID = "MATCH(user) WHERE ID(user)=";
	
	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(UserQuery.class.getName());
	@Nonnull
	private final IdQuery idQuery;
	@Nonnull
	private final IngredientQuery ingredientQuery;
	@Nonnull
	private final CategoryQuery categoryQuery;
	@Nonnull
	private static final UserParser PARSER = new UserParser();
	@Nonnull
	private static final LanguageParser L_PARSER = new LanguageParser();
	@Nonnull
	private static final CategoryParser C_PARSER = new CategoryParser();
	@Nonnull
	private static final HouseholdParser H_PARSER = new HouseholdParser();
	
	/**
	 * Initialize empty to set Graph later on
	 */
	public UserQuery() {this(null);}
	
	/**
	 * Initialize UserQuery with the graph to query
	 * @param graph	The graph to query
	 */
	public UserQuery(final Graph graph) {
		super(graph);
		this.idQuery = (IdQuery) getQuery(IdQuery.class);
		this.ingredientQuery = (IngredientQuery) getQuery(IngredientQuery.class);
		this.categoryQuery = (CategoryQuery) getQuery(CategoryQuery.class);
	}
	
	//--------------
	//PUBLIC METHODS
	//--------------
	
	@Nullable
	private User createUser(@Nonnull User user) throws InputException {
			
		//check if user name exists
		final User foundUser = getUser(user.getName());

		if(foundUser != null) {
			throw new InputException("User name " + user.getName() + " does already exist");
		}
		
		User tmp = putUserNode(user);
		
		if(tmp!= null) {
			
			StringBuilder queryString = new StringBuilder();
			queryString.append(MATCH_USER_ID);
			queryString.append(tmp.getId());
			queryString.append(getMergePrefersCategoryString(user));
			queryString.append(getMergeUserRatingString(user));
			queryString.append(getMergeSpokenLanguagesString(user));
			queryString.append(getMergeHouseholdString(user));
			queryString.append(getMergeCookedRecipeString(user, null));
			runWriteQueryString(queryString.toString());
			
			//Add ingredients
			List<Ingredient> likes = this.ingredientQuery.putIngredients(user.getLikesIngredients().stream().collect(Collectors.toList()));
			if(likes != null) {
				List<Long> likesIDs = new ArrayList<>();
				likes.forEach(l -> likesIDs.add(l.getID()));
				this.ingredientQuery.addRelationships(likesIDs, user.getId(), RelationshipType.LIKES);
			}
			
			List<Ingredient> incompatible = this.ingredientQuery.putIngredients(user.getIncompatibleIngredients().stream().collect(Collectors.toList()));
			
			if(incompatible != null) {
				List<Long> incompatibleIDs = new ArrayList<>();
				incompatible.forEach(l -> incompatibleIDs.add(l.getID()));
				this.ingredientQuery.addRelationships(incompatibleIDs, user.getId(), RelationshipType.IS_INCOMPATIBLE_WITH);
			}
			
			return getUser(tmp.getId());			
		}
		
		return null;	
	}
	
	@Nullable
	private StatementResult runWriteQueryString(@Nonnull String str) {
		
		Statement statement = new Statement(str);
		try(Session writeSession = this.graph.openWriteSession()) {
			return writeSession.run(statement);
		} catch (Exception e) {
			return null;
		}
	}
	
	private String getMergeHouseholdString(final User user) {
		StringBuilder queryString = new StringBuilder();

		String household = user.getHousehold().toString().toLowerCase();
		queryString.append(WITH_MERGE);
		queryString.append(household);
		queryString.append(":Household {name: '");
		queryString.append(household);
		queryString.append("'}) WITH * MERGE(user)-[:BELONGS_TO]-(");
		queryString.append(household);
		queryString.append(") ");

	
		return queryString.toString();
	}
	
	private String getMergeUserRatingString(final User user) {

		StringBuilder queryString = new StringBuilder();
		String recipeKey;
		String ratedKey;
		
		for(final Entry<Long, Integer> entry: user.getRecipeRatings().entrySet()) {
			recipeKey = "ratedRecipe" + entry.getKey();
			ratedKey  = "recipeRating" + entry.getKey();
			queryString.append(" WITH * MATCH(");
			queryString.append(recipeKey);
			queryString.append(":Recipe) WHERE ID(");
			queryString.append(recipeKey);
			queryString.append(") = ");
			queryString.append(entry.getKey());
			queryString.append(" WITH * MERGE (user)-[");
			queryString.append(ratedKey);
			queryString.append(":RATED]->(");
			queryString.append(recipeKey);
			queryString.append(") ON CREATE SET ");
			queryString.append(ratedKey);
			queryString.append(".rating = ");
			queryString.append(entry.getValue());
			queryString.append(" SET ");
			queryString.append(ratedKey);
			queryString.append(".rating = ");
			queryString.append(entry.getValue());
			queryString.append(" ");
		}
		
		return queryString.toString();
	}
	
	private String getMergeCookedRecipeString(@Nonnull final User user, @Nullable User stored) {
		
		final StringBuilder queryString = new StringBuilder();
		
		final Set<Long> newRecipes = user.getCookedRecipeIDs();
		final Set<Long> oldRecipes = new HashSet<>();
		
		if(stored != null) {
			oldRecipes.addAll(stored.getCookedRecipeIDs());
			oldRecipes.removeAll(newRecipes);
			newRecipes.removeAll(stored.getCookedRecipeIDs());
		}
		
		if(!newRecipes.isEmpty()) {
			queryString.append(" WITH * MATCH (cookedrecipe:Recipe) WHERE ID(cookedrecipe) IN [");
			queryString.append(StringUtil.join(newRecipes, ","));
			queryString.append("] WITH * MERGE (user)-[:COOKED]->(cookedrecipe) ");
		}
		
		if(!oldRecipes.isEmpty()) {
			queryString.append(" WITH * MATCH (oldcooked:Recipe) WHERE ID(oldcooked) IN [");
			queryString.append(StringUtil.join(oldRecipes, ","));
			queryString.append("] WITH * MATCH (user)-[cookiedo:COOKED]->(oldcooked) DELETE cookiedo ");
		}
		
		return queryString.toString();
	}
	
	private String getMergePrefersCategoryString(final User user) {
		
		StringBuilder queryString = new StringBuilder();
		String category;
		String var;
		
		for(final Category c: user.getPreferredCategories()) {
			category = c.getName().toLowerCase();
			var = adjustToVariable(category);
			queryString.append(WITH_MERGE);
			queryString.append(var);
			queryString.append(":Category {name: '");
			queryString.append(category);
			queryString.append("'}) WITH * MERGE(user)-[:PREFERS]-(");
			queryString.append(var);
			queryString.append(") ");
		}
		
		return queryString.toString();
	}

	private String getMergeSpokenLanguagesString(final User user) {
		
		StringBuilder queryString = new StringBuilder();
		String language;
		String var;
		
		if(!user.getSpokenLanguages().contains(user.getCurrentlySpokenLanguage()))
			user.addLanguageToSpokenLanguages(user.getCurrentlySpokenLanguage().getLangCode2());
		
		for(final Language l: user.getSpokenLanguages()) {
			
			language = l.getLangCode2().toLowerCase();
			var = adjustToVariable(language);
			queryString.append(WITH_MERGE);
			queryString.append(var);
			queryString.append(":Language {name: '");
			queryString.append(language);
			queryString.append("'}) WITH * MERGE(user)-[:SPEAKS]-(");
			queryString.append(var);
			queryString.append(") ");
		}
		
		return queryString.toString();
	}

	private String getChangedUserName(@Nonnull User current, @Nonnull User stored) throws InputException {
		StringBuilder queryString = new StringBuilder();
		
		if(!current.getName().equalsIgnoreCase(stored.getName())) {
			
			User tmp = getUser(current.getName());
			
			if(tmp != null) {
				throw new InputException("User name " + current.getName() + " does already exist");
			}
			
			queryString.append(" SET user.name = '");
			queryString.append(current.getName());
			queryString.append("' ");
		}
		
		return queryString.toString();
	}
	
	private String getChangedPreferredCategories(@Nonnull User current, @Nonnull User stored) {
		StringBuilder queryString = new StringBuilder();
		
			
		User tmp = new User();
		
		Set<Category> c1 = current.getPreferredCategories();
		Set<Category> c2 = stored.getPreferredCategories();
		
		//Get new categories
		c1.removeAll(c2); 
		//Get deleted categories
		c2.retainAll(c1);
		
		tmp.addPreferredCategories(c1);
		queryString.append(getMergePrefersCategoryString(tmp));
		queryString.append(getDeletePreferredRelationship(c2));
	
		return queryString.toString();
	}
	
	private String getDeletePreferredRelationship(@Nonnull final Set<Category> categories) {
		StringBuilder queryString = new StringBuilder();
		
		String var;
		String varP;
		for(Category c : categories) {
			var = adjustToVariable(c.getName());
			varP = "prefs" + var;
			queryString.append(" WITH * MATCH(");
			queryString.append(var);
			queryString.append(":Category) WHERE ID(");
			queryString.append(var);
			queryString.append(") = ");
			queryString.append(c.getID());
			queryString.append(" WITH * MATCH(user)-[");
			queryString.append(varP);
			queryString.append(":PREFERS]-(");
			queryString.append(var);
			queryString.append(") DELETE ");
			queryString.append(varP);
			queryString.append(" ");
		}
	
		return queryString.toString();
	}
	
	private String getMergeSpokenLanguages(@Nonnull User current, @Nonnull User stored) {
		StringBuilder queryString = new StringBuilder();
	
		
		if(!current.getSpokenLanguages().contains(current.getCurrentlySpokenLanguage()))
			current.addLanguageToSpokenLanguages(current.getCurrentlySpokenLanguage().getLangCode2());
		
		Set<Language> l1 = current.getSpokenLanguages();
		Set<Language> l2 = stored.getSpokenLanguages();
		
		//Get new languages
		l1.removeAll(l2); 
		//Get deleted categories
		l2.retainAll(l1);
			
		User tmp = new User();
		tmp.setSpokenLanguages(l1);
		
		queryString.append(getMergeSpokenLanguagesString(tmp));
		
		String var;
		for(Language l : l2) {
			var = l.getLangCode2().toLowerCase();
			queryString.append( "WITH * MATCH(");
			queryString.append(var);
			queryString.append(":Language) WHERE l.name ='");
			queryString.append(var);
			queryString.append("' MATCH(user)-[s:SPEAKS]-(");
			queryString.append(var);
			queryString.append(") DELETE s ");
		}
	
		return queryString.toString();
	}
	
	private String getMergeRecipeRatings(@Nonnull User current, @Nonnull User stored) {
		StringBuilder queryString = new StringBuilder();
		
		if(!current.getSpokenLanguages().contains(current.getCurrentlySpokenLanguage()))
			current.addLanguageToSpokenLanguages(current.getCurrentlySpokenLanguage().getLangCode2());
		
		Map<Long, Integer> r1 = current.getRecipeRatings();
		Map<Long, Integer> r2 = stored.getRecipeRatings();
		Map<Long, Integer> newR = new HashMap<>();
		
		for(Entry<Long, Integer> rating : r1.entrySet()) {
			if(!r2.containsKey(rating.getKey()) || Long.compare(r2.get(rating.getKey()), rating.getValue()) != 0) 
				newR.put(rating.getKey(), rating.getValue());
		}
		
		User tmp = new User();
		tmp.setRecipeRatings(newR);
		queryString.append(getMergeUserRatingString(tmp));
	
		return queryString.toString();
	}
	
	private List<Ingredient> mergeIngredients(@Nonnull Set<Ingredient> current, @Nonnull Set<Ingredient> stored, @Nonnull User user, @Nonnull RelationshipType relationship) {
		
		current.removeAll(stored);
		stored.removeAll(current);
		
		List<Ingredient> currentN = this.ingredientQuery.putIngredients(current.stream().collect(Collectors.toList()));
		
		if(currentN != null) {
			List<Long> iDs = new ArrayList<>();
			currentN.forEach(i -> iDs.add(i.getID()));
			this.ingredientQuery.addRelationships(iDs, user.getId(), relationship);
		}
		
		for(Ingredient i : stored) {
			this.ingredientQuery.removeRelationship(user.getId(), i.getID(), relationship);
		}
		
		return currentN;
	}
	
	public User putUser(User user) throws InputException {
		
		//Validate input
		if(!new UserValidator().isValid(user)) throw new InputException("invalid user");
		
		//If it is a new user, add it
		if(user.getId() < 0l) {return createUser(user);}
		
		
		//With id check we know, if the user does already exist in database 
		else {
			
			final User userFromDatabase = getUser(user.getId());
			
			if(userFromDatabase == null) return createUser(user);
			
			StringBuilder queryString = new StringBuilder();
			queryString.append(MATCH_USER_ID);
			queryString.append(user.getId());
			queryString.append(getChangedUserName(user, userFromDatabase));
			queryString.append(getChangedPreferredCategories(user, userFromDatabase));
			queryString.append(getMergeSpokenLanguages(user, userFromDatabase));
			queryString.append(getMergeRecipeRatings(user, userFromDatabase));
			queryString.append(getMergeCookedRecipeString(user, userFromDatabase));
			queryString.append(" RETURN ID(user)");
			
			if(!queryString.toString().contentEquals(MATCH_USER_ID + user.getId() + " RETURN ID(user)")) {
				try(Session writeSession = this.graph.openWriteSession()){
					StatementResult r = writeSession.run(new Statement(queryString.toString()));
					System.out.println(queryString.toString());
					if(r.hasNext() && r.list().get(0).get(0).asLong() != user.getId()) return null;
					
				} catch (Exception e) {
					e.printStackTrace();
					logLostConnection(LOGGER);
					return null;
				}
			}
			
			mergeIngredients(user.getIncompatibleIngredients(), userFromDatabase.getIncompatibleIngredients(), user, RelationshipType.IS_INCOMPATIBLE_WITH);	
			mergeIngredients(user.getLikesIngredients(), userFromDatabase.getLikesIngredients(), user, RelationshipType.LIKES);
			
		}
		
		return user;
	}
	
	/*
	 * Add time stamp to the cooked relationship and creates the relationship if necessary
	 * @param user User who cooked the recipe
	 * @param recipeId The recipes ID
	 * @param date The date when it has been cooked
	 * @throws Exception Exception thrown by neo4j in case of lost connection
	 * @throws InputException Throws InputException in case of invalid recipe ID
     * @throws Exception in case of connection to database is lost
	 * @important This function can't be implemented without using statement.withParameters!
	 */
	public void addCookedRecipe(final User user, final long recipeId, final LocalDate date) throws InputException, DatabaseException {
		
		Boolean isValid = new IdQuery(this.graph).isNodeIdValid(recipeId, NodeLabel.RECIPE);
		
		if(!Boolean.TRUE.equals(isValid)) {
			throw new InputException("ID " + recipeId + " does not match to a recipe");
		}
		
		final Statement statement = new Statement(
				"MATCH (user:User), (recipe:Recipe) WHERE ID(user)=$userId AND ID(recipe)=$recipeId "
				+ "MERGE (user)-[cooked:COOKED]-(recipe) "
				+ "ON CREATE SET cooked.dates = [] "
				+ "SET cooked.dates = cooked.dates + $date"
				);
		
		try(final Session writeSession = this.graph.openWriteSession()) {
			writeSession.run(statement.withParameters(Values.parameters(
					"userId", user.getId(), 
					"recipeId", recipeId, 
					"date", date
					)));			
		}
	
	}

	/**
	 * Pass "" and -1 to get all users
	 * @param userName					user name to query
	 * @param userId					user ID to query
	 * @return							The found users
	 * @throws InputException			<strong>Not thrown anymore</strong>
	 * @throws InterruptedException		<strong>Not thrown anymore</strong>
	 * @deprecated Use dedicated methods instead
	 */
	@Deprecated
	public List<User> getUser(final String userName, final long userId) throws InputException, InterruptedException {
		final List<User> users = new ArrayList<>();
		
		if(userId >= 0l) users.add(getUser(userId));
		
		else if(userName != null && !userName.isEmpty()) users.add(getUser(userName));
		
		else users.addAll(getAllUsers());
		
		users.remove(null);
		
		return users;
	}

	/**
	 * @param id	The users ID
	 * @return		<tt>Null</tt> in case ID is <tt>null</tt> or ID does not match a user<br>
	 * 				The retrieved user otherwise
	 */
	@Nullable
	public User getUser(@Nullable Long id) {
		
		if(id == null || id < 0l) return null;
		
		StringBuilder queryString = new StringBuilder();
		queryString.append(MATCH_USER_ID);
		queryString.append(id);
		return getUser(queryString);
	}
	
	/**
	 * @param id	The users name
	 * @return		<tt>Null</tt> in case userName is <tt>null</tt>, empty or name does not match a user<br>
	 * 				The retrieved user otherwise
	 */
	@Nullable
	public User getUser(@Nullable String userName) {
		if(userName == null || userName.isEmpty()) return null;
		
		StringBuilder queryString = new StringBuilder();
		queryString.append("MATCH(user:User) WHERE user.name ='");
		queryString.append(userName.toLowerCase());
		queryString.append("' ");
		
		return getUser(queryString);
	}
	
	@Nullable
	public User getUser(@Nonnull StringBuilder queryString) {
		
		queryString.append(getFullUserMatch());
		queryString.append(getFullUserReturn());
		
		Statement statement = new Statement(queryString.toString());
		
		User user = null;
		Record record = null;
		
		try(Session readSession = this.graph.openReadSession()){
			
			StatementResult result = readSession.run(statement);			
			
			if(result.hasNext()) {
				record = result.list().get(0);
			}
					
		} catch (Exception e) {
			logLostConnection(LOGGER);
			return null;
		}
		
		if(record!= null) {
			CountDownLatch latch = new CountDownLatch(1);
			user = POOL.submit(parseFullUserFromSingleRecord(record, latch)).invoke();
			
			try {
				latch.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		return user;
	}
	
	@Nonnull
	public List<User> getAllUsers(){
		
		Set<User> users = new HashSet<>();
		
		StringBuilder queryString = new StringBuilder();
		queryString.append("MATCH(user:User) ");
		queryString.append(getFullUserMatch());
		queryString.append(getFullUserReturn());
		
		Statement statement = new Statement(queryString.toString());

		List<Record> records = null;
		
		try(Session readSession = this.graph.openReadSession()){
			
			StatementResult result = readSession.run(statement);

			if(result.hasNext()) {
				records = result.list();
			}
			
		} catch (Exception e) {
			logLostConnection(LOGGER);
		}
		
		if(records != null) {
			
			CountDownLatch latch = new CountDownLatch(records.size());
			records.forEach(r -> users.add(POOL.submit(parseFullUserFromSingleRecord(r, latch)).invoke()));
			try {
				latch.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		users.remove(null);
		
		return users.stream().collect(Collectors.toList());	
	}
		
	/**
	 * @return Full query string match part for user 
	 */
	@Nonnull
	private String getFullUserMatch() {
		StringBuilder queryString = new StringBuilder();
		queryString.append(" WITH* MATCH(user)-[:SPEAKS]->(l:Language) ");
		queryString.append(" WITH* OPTIONAL MATCH(user)-[rated:RATED]->(recipe:Recipe) ");
		queryString.append(" WITH* OPTIONAL MATCH(user)-[:LIKES]->(likes:Ingredient)-[:IS]->(likeslang:Language) ");
		queryString.append(" WITH* OPTIONAL MATCH(user)-[:IS_INCOMPATIBLE_WITH]->(incomp:Ingredient)-[:IS]->(incomplang:Language) ");
		queryString.append(" WITH* OPTIONAL MATCH(user)-[:PREFERS]->(c:Category) ");
		queryString.append(" WITH* OPTIONAL MATCH(user)-[:BELONGS_TO]->(household:Household) ");
		queryString.append(" WITH* OPTIONAL MATCH(user)-[:COOKED]->(cookedrecipe:Recipe) ");
		return queryString.toString();
	}
	
	/**
	 * @return Full query string return part for user 
	 */
	@Nonnull
	private String getFullUserReturn() {
		StringBuilder queryString = new StringBuilder();
		queryString.append(" RETURN ");
		queryString.append(" user, ");
		queryString.append(" collect(l) AS languages, ");
		queryString.append(" collect([ID(recipe), rated.rating]) AS ratings, ");
		queryString.append(" collect([likes, likeslang]) AS likesing, ");
		queryString.append(" collect([incomp, incomplang]) AS incomping, ");
		queryString.append(" collect(c) AS categories, ");
		queryString.append(" collect(ID(cookedrecipe)) AS cooked, ");
		queryString.append(" household ");
		return queryString.toString();
	}
	
	@Nonnull
	private Map<Long, Integer> parseRatings(Record record){
		
		Map<Long, Integer> parsed = new HashMap<>();
		
		if(!record.containsKey(RATING_KEY)) return parsed;
		
		final int numberOfRatings = record.get(RATING_KEY).size();
		
		for(int i = 0; i < numberOfRatings; i++) {
			try {				
				parsed.put(record.get(RATING_KEY).get(i).get(0).asLong(), record.get(RATING_KEY).get(i).get(1).asInt());								
			} catch (final Exception e) {
				LOGGER.log(Level.FINE, e.getLocalizedMessage(), e.getCause());
			}
		}	
		
		return parsed;
	}
	
	@Nonnull
	private Set<Long> parseCookedRecipes(Record record){
		
		Set<Long> parsed = new HashSet<>();
		
		if(!record.containsKey(COOKED_KEY)) return parsed;
		
		final int numberOfRatings = record.get(COOKED_KEY).size();
		
		for(int i = 0; i < numberOfRatings; i++) {
			try {				
				parsed.add(record.get(COOKED_KEY).get(i).get(0).asLong());								
			} catch (final Exception e) {
				//Do nothing
			}
		}	
		
		parsed.remove(null);
		
		return parsed;
	}
	
	/**
	 * @param record	The record to parse
	 * @return			The set of languages within the record
	 */
	@Nonnull
	private Set<Language> parseSpokenLanguages(@Nullable Record record){
		
		if(record == null || !record.containsKey(LANGUAGE_KEY)) return new HashSet<>();
		
		Set<Language> languages = new HashSet<>();

		List<Object> lObjects = record.get(LANGUAGE_KEY).asList();
		
		lObjects.forEach(l-> languages.add(L_PARSER.parseLanguageFromNode((Node) l))); 
		
		return languages;
	}
	
	//--------------
	//PRIVATE QUERIES
	//--------------

	@Nullable
	private User putUserNode(@Nonnull final User user) {
		
		try (final Session writeSession = this.graph.openWriteSession()) {
			
			final Statement statement = new Statement(
					"MERGE (user:User {name: $userName}) "
					+ "RETURN ID(user)"
					);
			
			StatementResult result = writeSession.run(statement.withParameters(Values.parameters(
					USER_NAME, user.getName().toLowerCase()
					)));
			
			user.setId(result.list().get(0).get(0).asLong());
			
		} catch (final DatabaseException e) {
			
			LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
			return null;
		}
		
		return user;
	}
	
	/**
	 * @param record	Record to parse User out of
	 * @param latch		Latch to for counting down
	 * @return			User from database
	 */
	private Callable<User> parseFullUserFromSingleRecord(final Record record, final CountDownLatch latch) {
		return () -> {		

				User user = PARSER.parseUserFromNode(record.get("user").asNode());
				if(user == null) {
					latch.countDown();
					return null;
				}
				
				
				user.setSpokenLanguages(parseSpokenLanguages(record));
				user.setRecipeRatings(parseRatings(record));
				user.setLikedIngredients(parseIngredients(record, LIKES_ING_KEY));
				user.setIncompatibleIngredients(parseIngredients(record, "incomping"));
				user.setCookedRecipeIDs(parseCookedRecipes(record));
				List<Node> categories = new ArrayList<>();
				record.get("categories").asList().forEach(c -> categories.add((Node)c));
				user.addPreferredCategories(C_PARSER.parseCategoriesFromNodes(categories));
				try {
					user.setHousehold(HouseholdLabel.GUEST.getLabel(H_PARSER.parseHouseholdFromNode(record.get("household").asNode()).getName()));					
				} catch (Exception e) {
					LOGGER.log(Level.FINE, e.getLocalizedMessage(), e.getCause());
				}
				
				latch.countDown();
				return user;
		};
	}
	
	@Nonnull
	private Set<Ingredient> parseIngredients(@Nonnull Record record, @Nonnull String key){
		Set<Ingredient> ingredients = new HashSet<>();
		if(record.containsKey(key)) {
			
			int size = record.get(key).asList().size();
			
			IngredientParser iP = new IngredientParser();
			for(int i = 0; i < size; i++) {
				try {
					ingredients.add(iP.parseIngredientFromNode(record.get(key).get(i).get(0).asNode(), record.get(key).get(i).get(1).asNode()));					
				} catch (Exception e) {
					//Do nothing
				}
			}
		}
		
		return ingredients;
	}
	
	/**
	 * Delete the given user from database
	 * @param user	User to delete
	 */
	public void deleteUser(@Nonnull User user) {
		
		try (Session writeSession = this.graph.openWriteSession()){
			Statement statement = new Statement("MATCH (user:User) WHERE ID(user)=" + user.getId() + " DETACH DELETE user");
			writeSession.run(statement);
			
		} catch (Exception e) {
			LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
		}

	}

	/**
	 * 
	 * @param user			User to add
	 * @return				The added User
	 * @throws InputException 
	 * @deprecated use putUser(user) instead
	 */
	@Nullable
	@Deprecated
	public User putAndGetUser(final User user) throws InputException {
		return putUser(user);
	}
}