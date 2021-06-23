package de.dailab.oven.database.query;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.model.data_model.*;
import org.jsoup.internal.StringUtil;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

import de.dailab.oven.database.configuration.Graph;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.database.parse.RecipeParser;
import de.dailab.oven.database.validate.RecipeValidator;
import de.dailab.oven.model.data_model.filters.RecipeFilter;
import de.dailab.oven.model.database.NodeLabel;
import zone.bot.vici.Language;

public class RecipeQuery extends AQuery{

	@Nonnull
	private static final String WITH_MATCH = " WITH * MATCH(";
	@Nonnull
	private static final Logger LOGGER = Logger.getLogger(RecipeQuery.class.getName());
	@Nonnull
	private final CategoryQuery categoryQuery;
	@Nonnull
	private final ImageQuery imageQuery;
	@Nonnull
	private final IngredientQuery ingredientQuery;
		
	/**
	 * Initialize empty to set Graph later on
	 */
	public RecipeQuery() {this(null);}
	
	/**
	 * Initialize RecipeQuery with the graph to query
	 * @param graph	The graph to query
	 */
	public RecipeQuery(@Nullable final Graph graph) {
		super(graph);
		this.categoryQuery = (CategoryQuery) getQuery(CategoryQuery.class);
		this.imageQuery = (ImageQuery) getQuery(ImageQuery.class);
		this.ingredientQuery = (IngredientQuery) getQuery(IngredientQuery.class);
	}
	
	/**
	 * Retrieves all recipes matching the filter from the database
	 * @param recipeFilter	The filter (new filter if all recipes should be loaded)
	 * @return				The list of parsed recipes<br>
	 * 						Empty list in case no match has been found<br>
	 * 						{@code Null} in case of lost database connection
	 */
	@Nullable
	public List<Recipe> getRecipe(@Nullable final RecipeFilter recipeFilter) {
		
		if(recipeFilter == null) return new ArrayList<>();
		
		//Add all languages if none is set
		if(recipeFilter.getRecipeLanguages().isEmpty()) {
			recipeFilter.addRecipeLanguage(Language.ENGLISH);
			recipeFilter.addRecipeLanguage(Language.GERMAN);
			recipeFilter.addRecipeLanguage(Language.TURKISH);
		}
		
		final Set<Long> allIDs = new HashSet<>();
		
		if(recipeFilter.getRecipeId() != null && recipeFilter.getRecipeId() >= 0l) {
			allIDs.add(recipeFilter.getRecipeId());
			return getRecipesByIds(allIDs, recipeFilter);
		}
		
		allIDs.addAll(getAllRecipeIDs());
			
		if(allIDs.isEmpty()) return null;

		final CountDownLatch latch = new CountDownLatch(3);
		
		final Set<Long> requiredIDs = POOL.submit(getRequiredIds(allIDs, recipeFilter, latch)).invoke();
		final Set<Long> possibleIDs = POOL.submit(getPossibleIds(allIDs, recipeFilter, latch)).invoke();
		final Set<Long> excludedIDs = POOL.submit(getExcludedIds(recipeFilter, latch)).invoke();
				
		try {
			latch.await();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		if(requiredIDs == null) allIDs.clear();
		
		else if(!requiredIDs.isEmpty())
			allIDs.retainAll(requiredIDs);
		
		if(!possibleIDs.isEmpty())
			allIDs.retainAll(possibleIDs);
		
		if(!excludedIDs.isEmpty())
			allIDs.removeAll(excludedIDs);
			
		return getRecipesByIds(allIDs, recipeFilter);
	}
	
	/**
	 * Retrieves all required IDs for recipe querying
	 * @param allIDs		All IDs to work with (all recipe IDs)
	 * @param recipeFilter	The current recipe filter
	 * @param latch			The latch to count down
	 * @return				The set of all required IDs<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRequiredIds(@Nonnull final Set<Long> allIDs, @Nonnull final RecipeFilter recipeFilter, @Nonnull final CountDownLatch latch){
		return () -> {
			final CountDownLatch subLatch = new CountDownLatch(7);
			
			final Set<Long> ids = new HashSet<>();
			ids.addAll(allIDs);

			final List<Set<Long>> idSets = new ArrayList<>();
			idSets.add(POOL.submit(getRecipeIDsByLanguages(subLatch, recipeFilter)).invoke());
			idSets.add(POOL.submit(getRecipeIDsByRequiredIngredients(subLatch, recipeFilter.getRequiredIngredients())).invoke());
			idSets.add(POOL.submit(getRecipeIDsByRequiredCategories(subLatch, recipeFilter.getRequiredCategories())).invoke());
			idSets.add(POOL.submit(getRecipeIDsByName(subLatch, recipeFilter)).invoke());
			idSets.add(POOL.submit(getRecipeIDsByDuration(subLatch, recipeFilter)).invoke());
			idSets.add(POOL.submit(getRecipeIDsByFoodlabel(subLatch, recipeFilter)).invoke());
			idSets.add(POOL.submit(getRecipeIDsByOriginalServings(subLatch, recipeFilter)).invoke());	
			
			try {
				subLatch.await();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}			

			if(idSets.contains(null)) {
				latch.countDown();
				return null;
			}
			
			for(final Set<Long> set : idSets) {
				if(!set.isEmpty()) ids.retainAll(set);
			}
			
			if(ids.size() == allIDs.size())
				ids.clear();
			

			latch.countDown();
			
			return ids;
		};
	}
	
	/**
	 * Retrieves all possible IDs for recipe querying
	 * @param allIDs		All IDs to work with (all recipe IDs)
	 * @param recipeFilter	The current recipe filter
	 * @param latch			The latch to count down
	 * @return				The set of all possible IDs
	 */
	@Nonnull
	private Callable<Set<Long>> getPossibleIds(@Nonnull final Set<Long> allIDs, @Nonnull final RecipeFilter recipeFilter, @Nonnull final CountDownLatch latch){
		return () -> {
			final CountDownLatch subLatch = new CountDownLatch(1);
			
			final Set<Long> ids = new HashSet<>();

			ids.addAll(allIDs);
			final Set<Long> ingredientIDs = POOL.submit(getRecipeIDsByPossibleIngredients(subLatch, recipeFilter)).invoke();
			final Set<Long> categoryIDs = POOL.submit(getRecipeIDsByPossibleCategories(subLatch, recipeFilter)).invoke();
			final Set<Long> authorIDs = POOL.submit(getRecipeIDsByPossibleAuthors(subLatch, recipeFilter)).invoke();
			try {
				subLatch.await();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}	
			if(ingredientIDs != null) ids.retainAll(ingredientIDs);
			if(categoryIDs != null) ids.retainAll(categoryIDs);
			if(authorIDs != null) ids.retainAll(authorIDs);
			
			while(ids.contains(null)) {ids.remove(null);}
			
			if(ids.size() == allIDs.size())
				ids.clear();
			
			latch.countDown();
			
			return ids;
		};
	}
	
	/**
	 * Retrieves all excluded IDs for recipe querying
	 * @param allIDs		All IDs to work with (all recipe IDs)
	 * @param recipeFilter	The current recipe filter
	 * @param latch			The latch to count down
	 * @return				The set of all excluded IDs
	 */
	@Nonnull
	private Callable<Set<Long>> getExcludedIds(@Nonnull final RecipeFilter recipeFilter, @Nonnull final CountDownLatch latch){
		return () -> {
			final CountDownLatch subLatch = new CountDownLatch(3);
			final Set<Long> ids = new HashSet<>();
			final Set<Long> ingredientIDs = POOL.submit(getRecipeIDsByExcludedIngredients(subLatch, recipeFilter)).invoke();
			final Set<Long> categoryIDs = POOL.submit(getRecipeIDsByExcludedCategories(subLatch, recipeFilter)).invoke();
			final Set<Long> authorIDs = POOL.submit(getRecipeIDsByExcludedAuthors(subLatch, recipeFilter)).invoke();
			
			try {
				subLatch.await();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}	
			if(ingredientIDs != null) ids.addAll(ingredientIDs);
			if(categoryIDs != null) ids.addAll(categoryIDs);
			if(authorIDs != null) ids.addAll(authorIDs);
			
			while(ids.contains(null)) {ids.remove(null);}

			latch.countDown();
			return ids;
		};
	}	
		
	/**
	 * @return A set with all recipe IDs found<br>Empty in case of lost database connection or empty database
	 */
	@Nonnull
	private Set<Long> getAllRecipeIDs() {
		
		final StringBuilder queryString = new StringBuilder();
		
		queryString.append("MATCH (recipe:Recipe) ");
		queryString.append("RETURN ID(recipe) ");
				
		return performIdQuery(queryString);
	}

	/**
	 * Runs the given query string which should end with {@code RETURN ID(n)}
	 * @param queryString	The query string to perform
	 * @return				All IDs as set retrieved from the statement result
	 */
	@Nonnull
	private Set<Long> performIdQuery(final StringBuilder queryString) {
		
		final Set<Long> ids = new HashSet<>();
		
		final Statement statement = new Statement(queryString.toString());
		try(final Session readSession = this.graph.openReadSession()) {
			
			final StatementResult result = readSession.run(statement);
			if(result.hasNext())
				result.list().forEach(r -> ids.add(r.get(0).asLong()));
					
		} catch (final Exception e) {
			logLostConnection(LOGGER);
		}
		
		while(ids.contains(null)) {ids.remove(null);}
		
		return ids;
	}
	
	/**
	 * Retrieves all recipes within the given set of IDs
	 * @param ids		The IDs to query
	 * @param filter	The filter containing the limit of returns
	 * @return			The parsed recipes
	 */
	@Nonnull
	private List<Recipe> getRecipesByIds(@Nonnull final Set<Long> ids, @Nonnull final RecipeFilter filter){
		
		final Set<Recipe> recipes = new HashSet<>();
		
		if(ids.isEmpty()) return new ArrayList<>();
		
		final StringBuilder queryString = new StringBuilder();
		queryString.append("MATCH(recipe:Recipe) WHERE ID(recipe) IN [");
		queryString.append(StringUtil.join(ids, ","));
		queryString.append("] ");
		queryString.append("WITH* MATCH(recipe)-[:WRITTEN_IN]->(language:Language) ");
		queryString.append("WITH* MATCH(recipe)<-[:AUTHORED]-(author:Author) ");
		queryString.append("WITH* MATCH(recipe)-[contains:CONTAINS]->(ingredient:Ingredient) ");
		queryString.append("WITH* MATCH(recipe)-[:BELONGS_TO]->(category:Category) ");
		queryString.append("WITH* MATCH(recipe)-[:IS]->(foodlabel:Foodlabel) ");
		queryString.append("WITH* OPTIONAL MATCH (user:User)-[rated:RATED]->(recipe) ");
		queryString.append("WITH* OPTIONAL MATCH (recipe)-[takes:TAKES]->(duration:Duration) ");
		queryString.append("WITH* RETURN recipe, ");
		queryString.append("collect(category.name) AS categories,");
		queryString.append("author.name, ");
		queryString.append("language.name, ");
		queryString.append("foodlabel.name, ");
		queryString.append("collect([ingredient, contains]) AS ingredients, ");
		queryString.append("collect([ID(user), rated.rating]) AS ratings, ");
		queryString.append("collect([duration, takes]) AS durations");
		
		final Integer limit = filter.getMaxNumberOfRecipesToParsePerLanguage();
		
		if(limit != null && limit > 0) {
			queryString.append(" LIMIT ");
			queryString.append(limit);
		}
		
		recipes.addAll(executeQueryString(queryString.toString()));
		
		recipes.remove(null);
				
		return recipes.stream().collect(Collectors.toList());
	}
	
	@Nullable
	public Recipe putSingleRecipe(@Nullable final Recipe recipe) throws InputException{
		if(recipe == null) return null;
		
		if(!new RecipeValidator().isValid(recipe)) throw new InputException("recipe invalid");
		
		final Recipe dublicate = getDuplicate(recipe);
		
		return putRecipeNode(recipe, dublicate);
	}
	
	public void putMultipleRecipes(@Nullable final List<Recipe> recipes) {
		if(recipes == null) return;
		for(final Recipe r : recipes) {
			try {
				putSingleRecipe(r);
			} catch (final InputException iE) {
				LOGGER.log(Level.INFO, iE.getLocalizedMessage(), iE.getCause());
			}
		}
	}

	/**
	 * @param recipe Recipe to check for a duplicate
	 * @return Recipe found in database; Null in case nothing has been found
	 */
	@Nullable
	public Recipe getDuplicate(@Nonnull final Recipe recipe) {
		final RecipeFilter filter = new RecipeFilter();
		
		if(recipe.getId() >= 0l)
			filter.setRecipeId(recipe.getId());
		else {
			filter.setRecipeName(recipe.getName());
			filter.addPossibleAuthor(recipe.getAuthor());
			filter.addRecipeLanguage(recipe.getLanguage());
			filter.setMaxNumberOfRecipesToParsePerLanguage(1);			
		}
		
		final List<Recipe> foundRecipe = getRecipe(filter);
		
		if(foundRecipe == null || foundRecipe.isEmpty()) return null;
		
		return foundRecipe.get(0);
	}
	

	/**
	 * Merges the recipe node into the database (including author and language node)
	 * @param recipe	The recipe to merge
	 * @return			The recipe with ID from database
	 */
	@Nullable
	private Recipe putRecipeNode(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {

		final StringBuilder queryString = new StringBuilder();
		
		if(stored == null) {
			queryString.append("MERGE (recipe:Recipe {name: '");
			queryString.append(recipe.getName());
			queryString.append("'}) ");
		}
		else {
			queryString.append("MATCH (recipe:Recipe) WHERE ID(recipe) = ");
			queryString.append(recipe.getId());
			queryString.append(" ");
		}
		
		queryString.append(getMergeAuthorString(recipe, stored));
		queryString.append(getMergeLanguageString(recipe, stored));
		queryString.append(getMergeNameString(recipe, stored));
		queryString.append(getMergeURLString(recipe, stored));
		queryString.append(getMergeOriginalServingsString(recipe, stored));
		queryString.append(getMergeInstructionsString(recipe, stored));
		queryString.append(getMergeOvenSettingsString(recipe, stored));
		queryString.append(getMergeImagePathString(recipe, stored));
		queryString.append(getMergeFoodLabelString(recipe, stored));
		queryString.append(getMergeUserRatingString(recipe, stored));
		queryString.append(getMergeDurationsString(recipe, stored));
		queryString.append(getMergeCategoriesString(recipe, stored));
		queryString.append(getMergeIngredientsString(recipe, stored));
		queryString.append("RETURN ID(recipe)");
		
		final Set<Long> ids = performIdQuery(queryString);
		
		if(ids.isEmpty()) return null;
		
		final RecipeFilter filter = new RecipeFilter();
		filter.setRecipeId(ids.iterator().next());
		
		final List<Recipe> recipes = getRecipe(filter);
		
		if(recipes == null || recipes.isEmpty()) return null;
		
		return recipes.get(0);
	}

	/**
	 * Generates a string for merging a recipes durations if necessary
	 * @param recipe	The recipe to put
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes
	 */
	@Nonnull
	private String getMergeDurationsString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		final StringBuilder queryString = new StringBuilder();
		
		Map<String, Duration> newDurations = new HashMap<>();
		final Map<String, Duration> oldDurations = new HashMap<>();
		
		if(stored != null) {
			final Map<String, Duration> d1 = recipe.getDurations();
			final Map<String, Duration> d2 = stored.getDurations();
			for(final Entry<String, Duration> duration : d1.entrySet()) {
				if(!d2.containsKey(duration.getKey()) || !d2.get(duration.getKey()).equals(duration.getValue())) 
					newDurations.put(duration.getKey(), duration.getValue());
			}
			for(final Entry<String, Duration> duration : d2.entrySet()) {
				if(!d1.containsKey(duration.getKey())) 
					oldDurations.put(duration.getKey(), duration.getValue());
			}
		}
		else 
			newDurations = recipe.getDurations();
		
		String durationKey;
		String takesKey;
		for(final Entry<String, Duration> e : newDurations.entrySet()) {
			durationKey = adjustToVariable(e.getKey()) + "duration";
			takesKey = adjustToVariable(e.getKey()) + "takes";
			queryString.append(" WITH * MERGE(");
			queryString.append(durationKey);
			queryString.append(":Duration {name: '");
			queryString.append(e.getKey());
			queryString.append("'})<-[");
			queryString.append(takesKey);
			queryString.append(":TAKES]-(recipe) ON CREATE SET ");
			queryString.append(takesKey);
			queryString.append(".time = '");
			queryString.append(e.getValue().toString());
			queryString.append("' SET ");
			queryString.append(takesKey);
			queryString.append(".time = '");
			queryString.append(e.getValue().toString());
			queryString.append("' ");
		}
		
		for(final Entry<String, Duration> e : oldDurations.entrySet()) {
			durationKey = adjustToVariable(e.getKey()) + "duration";
			takesKey = adjustToVariable(e.getKey()) + "takes";
			queryString.append(WITH_MATCH);
			queryString.append(durationKey);
			queryString.append(":Duration) WHERE ");
			queryString.append(durationKey);
			queryString.append(".name = '");
			queryString.append(e.getKey());
			queryString.append("' WITH * MATCH(");
			queryString.append(durationKey);
			queryString.append(")-[");
			queryString.append(takesKey);
			queryString.append(":TAKES]-(recipe) DELETE ");
			queryString.append(takesKey);
			queryString.append(" ");
		}
			
		return queryString.toString();
	}
	
	/**
	 * Generates a string for merging a recipes image if necessary
	 * @param recipe	The recipe to put
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes
	 */
	@Nonnull
	private String getMergeImagePathString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		final StringBuilder queryString = new StringBuilder();
		
		boolean changed = true;
		
		if(stored != null) {
			if(recipe.getImage() != null) {
				final RenderedImage tmp = this.imageQuery.loadImage(stored.getId(), NodeLabel.RECIPE);
				changed = !tmp.equals(recipe.getImage());
			}
			else if(recipe.getImageFile() != null) {
				final File tmp = this.imageQuery.loadImageFile(stored.getId(), NodeLabel.RECIPE);
				changed = !tmp.equals(recipe.getImageFile());
			}
			else
				changed = !stored.getImagePath().isEmpty();
		}
		
		
		if(changed) {
			queryString.append(" WITH * SET recipe.imagePath = '");
			queryString.append(putRecipeImage(recipe, stored).getImagePath());
			queryString.append("' ");
		}
		
		return queryString.toString();	
	}
	
	/**
	 * Generates a string for merging a recipes oven settings if necessary
	 * @param recipe	The recipe to put
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes
	 */
	@Nonnull
	private String getMergeOvenSettingsString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		final StringBuilder queryString = new StringBuilder();
		
		
		if(stored == null || !recipe.getOvenSettings().equals(stored.getOvenSettings())) {
			//Ensure correct sort
			final Map<Integer, String> map = recipe.getOvenSettings();
			
			final List<String> settings = new ArrayList<>();
			
			for(int i = 0; i < map.size(); i++) {
				settings.add(map.get(i));
			}
			
			if(settings.isEmpty()) 
				queryString.append("WITH * SET recipe.ovenSettings = []");				
			
			
			else {
				queryString.append(" WITH * SET recipe.ovenSettings = ['");
				queryString.append(StringUtil.join(settings, "', '"));
				queryString.append("'] ");					
			}
		}
		
		return queryString.toString();	
		
	}
	
	/**
	 * Generates a string for merging a recipes instructions if necessary
	 * @param recipe	The recipe to put
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes
	 */
	@Nonnull
	private String getMergeInstructionsString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		final StringBuilder queryString = new StringBuilder();
		
		if(stored == null || !recipe.getInstructions().equals(stored.getInstructions())) {
			queryString.append(" WITH * SET recipe.instructions = ['");
			queryString.append(StringUtil.join(recipe.getInstructions(), "', '"));
			queryString.append("'] ");	
		}
		
		return queryString.toString();	
		
	}
	
	/**
	 * Generates a string for merging a recipes original servings if necessary
	 * @param recipe	The recipe to put
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes
	 */
	@Nonnull
	private String getMergeOriginalServingsString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		final StringBuilder queryString = new StringBuilder();
		
		if(stored == null || recipe.getOriginalServings() != stored.getOriginalServings()) {
			queryString.append(" WITH * SET recipe.originalServings = ");
			queryString.append(recipe.getOriginalServings());
			queryString.append(" ");	
		}
		
		return queryString.toString();	
	}
	
	/**
	 * Generates a string for merging a recipe URL if necessary
	 * @param recipe	The recipe to put
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes
	 */
	@Nonnull
	private String getMergeURLString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		final StringBuilder queryString = new StringBuilder();
		
		if(stored == null || !recipe.getUrl().equalsIgnoreCase(stored.getUrl())) {
			queryString.append(" WITH * SET recipe.url = '");
			queryString.append(recipe.getUrl());
			queryString.append("' ");	
		}
		
		return queryString.toString();	
	}
	
	/**
	 * Generates a string for merging a recipe name if necessary
	 * @param recipe	The recipe to put
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes or stored is {@code Null}
	 */
	@Nonnull
	private String getMergeNameString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		final StringBuilder queryString = new StringBuilder();
		
		if(stored != null && !recipe.getName().equalsIgnoreCase(stored.getName())) {
			queryString.append(" WITH * SET recipe.name = '");
			queryString.append(recipe.getName().toLowerCase());
			queryString.append("' ");	
		}
		
		return queryString.toString();	
	}
	
	/**
	 * Generates a string for merging a recipe with the (new) author as well as the delete part if necessary
	 * @param recipe	The recipe to put
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes
	 */
	@Nonnull
	private String getMergeAuthorString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		
		final StringBuilder queryString = new StringBuilder();
		if(stored == null || !recipe.getAuthor().contentEquals(stored.getAuthor())) {
			queryString.append(" WITH * MERGE(author:Author {name : '");
			queryString.append(recipe.getAuthor().toLowerCase());
			queryString.append("'}) ");
			queryString.append(" WITH * MERGE (author)-[:AUTHORED]->(recipe) ");
			
			if(stored != null) {
				queryString.append(" WITH * MATCH(authorOld:Author) WHERE authorOld.name = '");
				queryString.append(stored.getAuthor());
				queryString.append("' WITH * MATCH(authorOld)-[authored:AUTHORED]->(recipe) ");
				queryString.append(" DELETE authored ");
			}
				
		}
		return queryString.toString();
	}
	
	/**
	 * Generates a string for merging a recipe with the (new) language as well as the delete part if necessary
	 * @param recipe	The recipe to put
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes
	 */
	@Nonnull
	private String getMergeLanguageString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		
		final StringBuilder queryString = new StringBuilder();
		if(stored == null || recipe.getLanguage() != stored.getLanguage()) {
			queryString.append(" WITH * MERGE(rlanguage:Language {name : '");
			queryString.append(recipe.getLanguage().getLangCode2().toLowerCase());
			queryString.append("'}) ");
			queryString.append(" WITH * MERGE (recipe)-[:WRITTEN_IN]->(rlanguage) ");
			
			if(stored != null) {
				queryString.append(" WITH * MATCH(languageOld:Language) WHERE languageOld.name = '");
				queryString.append(stored.getLanguage().getLangCode2().toLowerCase());
				queryString.append("' WITH * MATCH(recipe)-[written:WRITTEN_IN]->(languageOld) ");
				queryString.append(" DELETE written ");
			}
				
		}
		return queryString.toString();
		
	}
	
	/**
	 * Generates a string for merging the given food label to the recipe
	 * @param recipe	The recipe of interest
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes
	 */
	@Nonnull
	private String getMergeFoodLabelString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		final StringBuilder queryString = new StringBuilder();
		
		if(stored == null || !recipe.getFoodLabel().equals(stored.getFoodLabel())) {
			queryString.append(" WITH * MERGE(f:Foodlabel {name: '");
			queryString.append(recipe.getFoodLabel().toString().toLowerCase());
			queryString.append("'})");
			queryString.append(" WITH * MERGE(recipe)-[:IS]->(f) ");
			
			if(stored != null) {
				queryString.append(" WITH * MATCH(foodlabelO:Foodlabel) WHERE foodlabelO.name ='");
				queryString.append(stored.getFoodLabel().toString().toLowerCase());
				queryString.append("' WITH * MATCH (recipe)-[delfood:IS]->(foodlabelO) ");
				queryString.append(" DELETE delfood ");
			}
		}
		
		return queryString.toString();
	}
	
	/**
	 * Generates a string for merging the given user ratings for the recipe
	 * @param recipe	The recipe of interest
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes
	 */
	@Nonnull
	private String getMergeUserRatingString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {

		Map<Long, Integer> newRatings = new HashMap<>();
		if(stored != null) {
			final Map<Long, Integer> r1 = recipe.getUserRatings();
			final Map<Long, Integer> r2 = stored.getUserRatings();
			for(final Entry<Long, Integer> rating : r1.entrySet()) {
				if(!r2.containsKey(rating.getKey()) || Long.compare(r2.get(rating.getKey()), rating.getValue()) != 0) 
					newRatings.put(rating.getKey(), rating.getValue());
			}
		}
		else {
			newRatings = recipe.getUserRatings();
		}
		
		final StringBuilder queryString = new StringBuilder();
		String userKey;
		String ratedKey;

		for(final Entry<Long, Integer> entry: newRatings.entrySet()) {
			userKey = "user" + entry.getKey();
			ratedKey  = "recipeRating" + entry.getKey();
			queryString.append(WITH_MATCH);
			queryString.append(userKey);
			queryString.append(":User) WHERE ID(");
			queryString.append(userKey);
			queryString.append(") = ");
			queryString.append(entry.getKey());
			queryString.append(" WITH * MERGE (recipe)<-[");
			queryString.append(ratedKey);
			queryString.append(":RATED]-(");
			queryString.append(userKey);
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
	
	/**
	 * Generates a string for merging the given categories for the recipe
	 * @param recipe	The recipe of interest
	 * @param stored	The stored recipe ({@code Null} in case there is none)
	 * @return			The generated string<br>Empty string in case of no changes
	 */
	@Nonnull
	private String getMergeCategoriesString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		final StringBuilder queryString = new StringBuilder();
		
		final List<String> newCategories = new ArrayList<>();
		final Set<Category> oldCategories = new HashSet<>();
		
		if(stored != null) {
			final Set<Category> oldC = stored.getCategories();
			final Set<Category> newC = recipe.getCategories();
			for(final Category c : newC) {
				if(!oldC.contains(c))
					newCategories.add(c.getName().toLowerCase());
			}
			for(final Category c: oldC) {
				if(!newC.contains(c))
					oldCategories.add(c);
			}
		}
		
		else
			recipe.getCategories().forEach(c -> newCategories.add(c.getName().toLowerCase()));
		
		String cString;
		int count = 0;
		for(final String str : newCategories) {
			cString = "cat" + adjustToVariable(str) + count;
			queryString.append(" WITH * MERGE(");
			queryString.append(cString);
			queryString.append(":Category {name: '");
			queryString.append(cString);
			queryString.append("'})<-[:BELONGS_TO]-(recipe) ");
			count++;
		}
		
		if(!oldCategories.isEmpty()) {
			final List<Long> ids = new ArrayList<>();
			oldCategories.forEach(c -> ids.add(c.getID()));
			queryString.append(" WITH * MATCH (oldcategory:Category) WHERE ID(oldcategory) IN [");
			queryString.append(StringUtil.join(ids, ", "));
			queryString.append("] WITH* MATCH(recipe)-[oldbelong:BELONGS_TO]->(oldcategory) DELETE oldbelong ");
		}
		
		return queryString.toString();
	}

	private static boolean containsEqualIngAndAmount(@Nonnull final IngredientWithAmount ingredient, @Nonnull final List<IngredientWithAmount> list) {
		for(final IngredientWithAmount ing : list) {
			if(ing.getIngredient().equals(ingredient.getIngredient()) && Math.abs(ing.getQuantity()-ingredient.getQuantity()) < 0.0001 && ing.getUnit() == ingredient.getUnit()) {
				return true;
			}
		}
		return false;
	}
	
	@Nonnull
	private String getMergeIngredientsString(@Nonnull final Recipe recipe, @Nullable final Recipe stored) {
		final StringBuilder queryString = new StringBuilder();
		
		final List<IngredientWithAmount> newAmounts = new LinkedList<>();

		final List<IngredientWithAmount> oldAmounts = new LinkedList<>();
		
		
		if(stored == null) 
			newAmounts.addAll(recipe.getIngredients());
		
		else {
			final List<IngredientWithAmount> i1 = recipe.getIngredients();
			final List<IngredientWithAmount> i2 = stored.getIngredients();
			for(final IngredientWithAmount ing : i1) {
				if(!containsEqualIngAndAmount(ing, i2))
					newAmounts.add(ing);
			}
			for(final IngredientWithAmount ing : i2) {
				if(!containsEqualIngAndAmount(ing, i1))
					oldAmounts.add(ing);
			}		
		}

		final List<Ingredient> newIngredients = this.ingredientQuery.putIngredients(newAmounts.stream().map(IngredientWithAmount::getIngredient).collect(Collectors.toList()));
		
		String ingKey;
		String amountKey;
		int count = 0;
		for(int i=0; i < newAmounts.size(); i++) {
			assert newIngredients != null;
			final Ingredient ingredient = newIngredients.get(i);
			if(ingredient == null) {
				continue;
			}
			ingKey = adjustToVariable(ingredient.getName()) + count;
			amountKey = ingKey + "amount";
			queryString.append(WITH_MATCH);
			queryString.append(ingKey);
			queryString.append(":Ingredient) WHERE ID(");
			queryString.append(ingKey);
			queryString.append(") = ");
			queryString.append(ingredient.getID());
			queryString.append(" WITH * MERGE(recipe)-[");
			queryString.append(amountKey);
			queryString.append(":CONTAINS]->(");
			queryString.append(ingKey);
			queryString.append(") ON CREATE SET ");
			getSetSingleAmountString(queryString, newAmounts, amountKey, i, ingredient);
			queryString.append("] SET ");
			getSetSingleAmountString(queryString, newAmounts, amountKey, i, ingredient);
			queryString.append("] ");
			count++;
		}
		
		if(!oldAmounts.isEmpty()) {
			final List<Long> ids = new ArrayList<>();
			oldAmounts.forEach(i -> ids.add(i.getIngredient().getID()));
			queryString.append(" WITH * MATCH(olding:Ingredient)<-[oldamount:CONTAINS]-(recipe) WHERE ID(olding) IN [");
			queryString.append(StringUtil.join(ids, ","));
			queryString.append("] DELETE oldamount ");
		}
		
		return queryString.toString();
	}

	private void getSetSingleAmountString(final StringBuilder queryString, final List<IngredientWithAmount> newAmounts,
			String amountKey, int i, final Ingredient ingredient) {
		
		queryString.append(amountKey);
		queryString.append(".amount = ");
		queryString.append(newAmounts.get(i).getQuantity());
		queryString.append(", ");
		queryString.append(amountKey);
		queryString.append(".unit = '");
		queryString.append(newAmounts.get(i).getUnit().toString().toLowerCase());
		queryString.append("', ");
		queryString.append(amountKey);
		queryString.append(".attributes = [");
		queryString.append(StringUtil.join(ingredient.getAttributes(), ","));
	}
	
	/**
	 * Retrieves all recipes connected with the given set of excluded categories
	 * @param latch				The latch to count down
	 * @param filter			The {@link RecipeFilter} containing the excluded categories
	 * @return					The Set of recipe IDs which are connected to the categories
	 * 							<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByExcludedCategories(@Nonnull final CountDownLatch latch, @Nonnull final RecipeFilter filter){
		return getRecipeIDsByConnectedCategories(latch, filter.getExcludedCategories());
	}
	
	/**
	 * Retrieves all recipes connected with the given set of possible categories
	 * @param latch				The latch to count down
	 * @param filter			The {@link RecipeFilter} containing the possible categories  
	 * @return					The Set of recipe IDs which are connected to the categories
	 * 							<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByPossibleCategories(@Nonnull final CountDownLatch latch, @Nonnull final RecipeFilter filter){
		return getRecipeIDsByConnectedCategories(latch, filter.getPossibleCategories());
	}
	
	/**
	 * Retrieves all recipes connected with the given set of categories
	 * @param latch			The latch to count down
	 * @param categories	The categories to query
	 * @return				The Set of recipe IDs which are connected to the categories
	 * 						<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByConnectedCategories(@Nonnull final CountDownLatch latch, @Nullable final Set<Category> categories){
		return () -> {
			final Set<Long> recipeIDs = new HashSet<>();
			if(categories.isEmpty()) {
				latch.countDown();
				return recipeIDs;
			}
			final List<Node> nodes = this.categoryQuery.getConnectedNodes(categories.stream().collect(Collectors.toList()), NodeLabel.RECIPE);
			
			if(nodes != null)
				nodes.forEach(n -> recipeIDs.add(n.id()));
			
			latch.countDown();
			if(categories.isEmpty()) return null;
			return recipeIDs;
		};
	}
	
	/**
	 * Retrieves all recipe IDs which are strongly connected to the passed set of categories 
	 * @param latch			The latch to count down
	 * @param categories	The set of required categories which each recipe should belong to
	 * @return				The set of recipe IDs which are strongly connected to the required categories
	 * 						<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByRequiredCategories(@Nonnull final CountDownLatch latch, @Nullable final Set<Category> categories){
		return () -> {
			final Set<Long> recipeIDs = new HashSet<>();
			if(categories.isEmpty()) {
				latch.countDown();
				return recipeIDs;
			}
			recipeIDs.addAll(this.categoryQuery.getStrongConnectedIds(categories.stream().collect(Collectors.toList()), NodeLabel.RECIPE));
			recipeIDs.remove(null);
			latch.countDown();
			if(categories.isEmpty()) return null;
			return recipeIDs;
		};
	}
	
	/**
	 * Retrieves all recipes connected with the given set of excluded ingredients
	 * @param latch				The latch to count down
	 * @param filter			The {@link RecipeFilter} containing the excluded ingredients  
	 * @return					The Set of recipe IDs which are connected to the ingredients<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByExcludedIngredients(@Nonnull final CountDownLatch latch, @Nonnull final RecipeFilter filter){
		return getRecipeIDsByConnectedIngredients(latch, filter.getExcludedIngredients());
	}

	/**
	 * Retrieves all recipes connected with the given set of possible ingredients
	 * @param latch				The latch to count down
	 * @param filter			The {@link RecipeFilter} containing the possible ingredients  
	 * @return					The Set of recipe IDs which are connected to the ingredients<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByPossibleIngredients(@Nonnull final CountDownLatch latch, @Nonnull final RecipeFilter filter){
		return getRecipeIDsByConnectedIngredients(latch, filter.getPossibleIngredients());
	}
	
	/**
	 * Retrieves all recipes connected with the given set of ingredients
	 * @param latch			The latch to count down
	 * @param ingredients	The ingredients to query
	 * @return				The Set of recipe IDs which are connected to the ingredients<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByConnectedIngredients(@Nonnull final CountDownLatch latch, @Nullable final Set<Ingredient> ingredients){
		return () -> {
			final Set<Long> recipeIDs = new HashSet<>();
			if(ingredients.isEmpty()) {
				latch.countDown();
				return recipeIDs;
			}
			
			final List<Node> nodes = this.ingredientQuery.getConnectedNodes(ingredients.stream().collect(Collectors.toList()), NodeLabel.RECIPE);
			
			if(nodes != null)
				nodes.forEach(n -> recipeIDs.add(n.id()));
			
			latch.countDown();
			if(recipeIDs.isEmpty()) return null;
			return recipeIDs;
		};
	}
	
	/**
	 * Retrieves all recipe IDs which are strongly connected to the passed set of ingredients 
	 * @param latch			The latch to count down
	 * @param ingredients	The set of required ingredients which each recipe should contain
	 * @return				The set of recipe IDs which are strongly connected to the required ingredients
	 * 						<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByRequiredIngredients(@Nonnull final CountDownLatch latch, @Nullable final Set<Ingredient> ingredients){
		return () ->{
			final Set<Long> recipeIDs = new HashSet<>();
			if(ingredients.isEmpty()) {
				latch.countDown();
				return recipeIDs;
			}
			recipeIDs.addAll(this.ingredientQuery.getStrongConnectedIds(ingredients.stream().collect(Collectors.toList()), NodeLabel.RECIPE));
			recipeIDs.remove(null);
			latch.countDown();
			if(recipeIDs.isEmpty()) return null;
			return recipeIDs;
		};
	}

	@Nullable
	private Callable<Set<Long>> getRecipeIDsByPossibleAuthors(@Nonnull final CountDownLatch latch, @Nonnull final RecipeFilter filter){
		return getRecipeIDsByAuthors(latch, filter.getPossibleAuthors());
	}

	@Nullable
	private Callable<Set<Long>> getRecipeIDsByExcludedAuthors(@Nonnull final CountDownLatch latch, @Nonnull final RecipeFilter filter){
		return getRecipeIDsByAuthors(latch, filter.getExcludedAuthors());
	}
	
	/**
	 * Retrieves all recipe IDs which are connected with the given set of authors
	 * @param latch		The latch to count down
	 * @param authors	The set of authors to query
	 * @return			All matching recipe IDs<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByAuthors(@Nonnull final CountDownLatch latch, @Nullable final Set<String> authors) {
		return () -> {
			final Set<Long> recipeIDs = new HashSet<>();

			if(authors != null && !authors.isEmpty()) {
				
				final StringBuilder queryString = new StringBuilder();
				queryString.append("MATCH(r:Recipe)<-[:AUTHORED]-(a:Author) WHERE (a.name = '");
				queryString.append(StringUtil.join(authors, "' OR a.name = '"));
				queryString.append("') ");
				queryString.append(" RETURN ID(r)");
								
				recipeIDs.addAll(performIdQuery(queryString));
				
				if(recipeIDs.isEmpty()) {
					latch.countDown();
					return null;
				}
				
			}
			latch.countDown();
			return recipeIDs;
		};
	}
		
	/**
	 * Retrieves all recipe IDs which are cooked within the given duration
	 * @param latch		The latch to count down
	 * @param filter	The filter containing the maximum duration
	 * @return			All matching recipe IDs<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByDuration(@Nonnull final CountDownLatch latch, @Nonnull final RecipeFilter filter){
		return () -> {
			final Set<Long> recipeIDs = new HashSet<>();
			
			final Duration cookedWithin = filter.getCookedWithin();
			
			if(!cookedWithin.equals(Duration.ZERO)) {
				
				final StringBuilder queryString = new StringBuilder();
				queryString.append("MATCH(d:Duration {name: 'total'})<-[t:TAKES]-(r:Recipe) ");
				queryString.append("WHERE datetime() + duration(t.time) <= datetime() + duration('");
				queryString.append(cookedWithin.toString());
				queryString.append("') RETURN ID(r)");
				
				recipeIDs.addAll(performIdQuery(queryString));
				
				if(recipeIDs.isEmpty()) {
					latch.countDown();
					return null;
				}
				
			}

			latch.countDown();
			return recipeIDs;
		};
	}
	
	/**
	 * Retrieves all recipe IDs with the given FoodLabel
	 * @param latch		The latch to count down
	 * @param filter	The filter containing the FoodLabel of interest
	 * @return			All matching recipe IDs<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByFoodlabel(@Nonnull final CountDownLatch latch, @Nonnull final RecipeFilter filter){
		return () -> {
			final Set<Long> recipeIDs = new HashSet<>();
				
			final FoodLabel label = filter.getIsFoodLabel();
			
			if(label != FoodLabel.UNDEF) {
								
				final String name = label.toString().toLowerCase();
				final StringBuilder queryString = new StringBuilder();
				queryString.append("MATCH (f:Foodlabel) WHERE f.name= '");
				queryString.append(name);
				queryString.append("' WITH * MATCH(f)<-[:IS]-(r:Recipe) RETURN ID(r)");
				
				recipeIDs.addAll(performIdQuery(queryString));
								
				if(recipeIDs.isEmpty()) {
					latch.countDown();
					return null;
				}

			}
			
			latch.countDown();
			return recipeIDs;
		};
	} 
	
	/**
	 * Retrieves all recipe IDs with the given preset original servings
	 * @param latch		The latch to count down
	 * @param filter	The filter containing the original servings of interest
	 * @return			The retrieved recipe IDs<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByOriginalServings(@Nonnull final CountDownLatch latch, @Nonnull final RecipeFilter filter){
		return () -> {
			final Set<Long> recipeIDs = new HashSet<>();
			
			final Integer servings = filter.getOriginialServings();
			
			if(servings != null) {
				final StringBuilder queryString = new StringBuilder();
				queryString.append("MATCH (r:Recipe) WHERE r.originalServings = ");
				queryString.append(servings);
				queryString.append(" RETURN ID(r)");
				
				recipeIDs.addAll(performIdQuery(queryString));
				
				if(recipeIDs.isEmpty()) {
					latch.countDown();
					return null;
				}
			}
			latch.countDown();
			return recipeIDs;
		};
	} 
	
	/**
	 * Retrieves all recipes containing the preset name
	 * @param latch		The latch to count down
	 * @param filter	The filter containing the name of interest
	 * @return			The retrieved recipe IDs<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByName(@Nonnull final CountDownLatch latch, @Nonnull final RecipeFilter filter){
		return () -> {				
			final Set<Long> recipeIDs = new HashSet<>();
			
			final String name = filter.getRecipeName();
			
			final StringBuilder queryString = new StringBuilder();
			
			if(!name.isEmpty()) {
				
				queryString.append("MATCH (r:Recipe) WHERE r.name CONTAINS('");
				queryString.append(name);
				queryString.append("') RETURN ID(r)");
				
				recipeIDs.addAll(performIdQuery(queryString));
				
				if(recipeIDs.isEmpty()) {
					latch.countDown();
					return null;
				}
			}
			
			latch.countDown();
			return recipeIDs;
		};
	} 
	
	/**
	 * Retrieves all recipe IDs for the given languages
	 * @param latch		The latch to count down
	 * @param filter	The filter containing the languages
	 * @return			Set of IDs matching the requirements<br>{@code Null} in case filter was set but nothing matched
	 */
	@Nullable
	private Callable<Set<Long>> getRecipeIDsByLanguages(@Nonnull final CountDownLatch latch, @Nonnull final RecipeFilter filter){
		return () -> {			
			final Set<Long> recipeIDs = new HashSet<>();
			
			final Set<Language> languages = filter.getRecipeLanguages();
			
			final CountDownLatch subLatch = new CountDownLatch(languages.size());
			
			languages.forEach(l -> recipeIDs.addAll(POOL.submit(getRecipeIDsByLanguage(subLatch, l)).invoke()));
			
			try {
				subLatch.await();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}	

			latch.countDown();
			if(recipeIDs.isEmpty()) return null;
			return recipeIDs;
		};
	} 
	
	/**
	 * Retrieves all recipe IDs for the given language
	 * @param latch		The latch to count down
	 * @param language	The language to query
	 * @return			Set of IDs matching the requirements
	 */
	@Nonnull
	private Callable<Set<Long>> getRecipeIDsByLanguage(@Nonnull final CountDownLatch latch, @Nonnull final Language language){
		return () -> {
			final Set<Long> recipeIDs = new HashSet<>();

			if(!language.equals(Language.UNDEF)) {
			
				final StringBuilder queryString = new StringBuilder();
				queryString.append("MATCH(recipe:Recipe)");
				queryString.append("-[:WRITTEN_IN]->(language:Language) WHERE language.name='" + language.getLangCode2() + "' ");
				queryString.append("RETURN ID(recipe)");
				
				recipeIDs.addAll(performIdQuery(queryString));
				
				recipeIDs.remove(null);
			}
			
			latch.countDown();
			
			return recipeIDs;
		};
	} 
	
	/**
	 * Parses the records to recipes
	 * @param records	The records to parse
	 * @return			A list of parsed recipes
	 */
	private List<Recipe> parseRecipesFromStatementResult(final List<Record> records) {
		final List<Recipe> recipes = new ArrayList<>();
		
		final CountDownLatch latch = new CountDownLatch(records.size());
		
		for(final Record record : records) {
			if(record != null) {
				try {
					recipes.add(POOL.submit(new RecipeParser(record, latch)).invoke());									
				} catch (final Exception e) {LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());}
			}
		}

		try {
			latch.await();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return recipes;
	}
	
	/**
	 * Stores the recipes image in case it exists and sets the path to the image file
	 * @param recipe The {@link Recipe} for which an  image should be stored for
	 * @return The recipe with the set image path
	 */
	@Nonnull
	private Recipe putRecipeImage(@Nonnull final Recipe recipe, @Nullable Recipe stored) {
		
		String imagePath = "";
		
		if(stored != null && !stored.getImagePath().isEmpty() && !recipe.updateImage())
			return recipe;
		
		if(recipe.getImageFile() != null) {
			try {
				imagePath = this.imageQuery.saveImage(recipe.getImageFile(), NodeLabel.RECIPE);
			} catch (final IOException qE) {
				LOGGER.log(Level.INFO, qE.getLocalizedMessage(), qE.getCause());
			}
		}
		
		else if(recipe.getImage() != null) {
			try {
				imagePath = this.imageQuery.saveImage(recipe.getImage(), NodeLabel.RECIPE);
			} catch (final IOException qE) {
				LOGGER.log(Level.INFO, qE.getLocalizedMessage(), qE.getCause());
			}
		}
		
		recipe.setImagePath(imagePath);						
		
		return recipe;
	}
	
	/**
	 * Executes any query string and tries to parse a list of {@link Recipe}s from it
	 * @param queryString	The query string to execute
	 * @return				The list of {@link Recipe}s 
	 */
	@Nonnull
	public List<Recipe> executeQueryString(@Nullable final String queryString) {
	    final List<Recipe> recipes = new ArrayList<>();
	    if(queryString != null && !queryString.isEmpty()) {
	    	try(final Session readSession = this.graph.openReadSession()) {
	    		final StatementResult result = readSession.run(new Statement(queryString));
	    		if(result.hasNext()) {
	    			recipes.addAll(parseRecipesFromStatementResult(result.list()));
	    		}
	    		
	    	} catch(final Exception e) {
	    		LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
	    	}
	    }
	    return recipes;	    	
	}
}