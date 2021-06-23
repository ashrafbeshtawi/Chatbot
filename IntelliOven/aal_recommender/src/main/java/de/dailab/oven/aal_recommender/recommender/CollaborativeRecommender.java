package de.dailab.oven.aal_recommender.recommender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import de.dailab.oven.model.data_model.filters.RecipeFilter;
import org.apache.commons.io.FileUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;

import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class CollaborativeRecommender {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(CollaborativeRecommender.class);

	private static final String PATH = "aal_recommender_data"; // path to interactions.csv folder
	private FileDataModel datamodel; // datamodel containing interactions

	private File file; // interactions.csv

	public CollaborativeRecommender() {
		try {

			final File directory = new File(PATH);
			if (!directory.exists()) {
				directory.mkdir();
			}

			final File interactionsFile = new File(PATH + "/interactions.csv");
			if (!interactionsFile.isFile()) {
				final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
				final InputStream is = classloader.getResourceAsStream("data/interactions.csv");
				if(interactionsFile.createNewFile())
					FileUtils.copyInputStreamToFile(is, interactionsFile);

				is.close();
			}
			this.file = interactionsFile;
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}

		// this works also, but only for 1 file
		// this.file = new
		// File(getClass().getClassLoader().getResource("data/interactions.csv").getFile());

	}

	/**
	 * Creates a datamodel from all interaction files in set directory
	 */
	public void loadData() {
		try {
			LOG.debug("Loading Dataset");
			this.datamodel = new FileDataModel(this.file);

		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * This function will use euclidean similarity
	 * 
	 * @param user               for whom to create recommendations
	 * @param amount             how many recommendations to create
	 * @param desiredIngredients set to null if not needed
	 * @param avoidedIngredients set to null if not needed
	 * @return amount number of recipe recommendations; null if connection to
	 *         database failed
	 */
	public Recipe[] getRecommendations(final User user, final int amount, final String[] desiredIngredients,
			final String[] avoidedIngredients) {

		final RecommenderBuilder recommenderBuilder = new CollaborativeEuclideanRecommenderBuilder();

		return getRecommendations(user, recommenderBuilder, amount, desiredIngredients, avoidedIngredients);
	}

	/**
	 * @param user               for whom to create recommendations
	 * @param amount             how many recommendations to create
	 * @param recommenderBuilder affects used similarity
	 * @param desiredIngredients set to null if not needed
	 * @param avoidedIngredients set to null if not needed
	 * @return amount number of recipe recommendations; null if connection to
	 *         database failed
	 */
	public Recipe[] getRecommendations(final User user, final RecommenderBuilder recommenderBuilder, final int amount,
			final String[] desiredIngredients, final String[] avoidedIngredients) {

		if (user == null) {
			throw new IllegalArgumentException("The called User does not exist!");
		} else if (amount == 0) {
			throw new IllegalArgumentException(
					"The function is only applicable for an amount of recommendations greater zero");
		} else {

			if (this.datamodel == null) {
				loadData();
			}

			try {

				final UserBasedRecommender recommender = (UserBasedRecommender) recommenderBuilder
						.buildRecommender(this.datamodel);

				LOG.debug("Generating recommendations...");
				final List<RecommendedItem> recommendations = recommender.recommend(user.getId(), 200);

				LOG.debug("Applying filter and returning Recommendations");
				final Query query = new Query();

				final RecipeFilter recipeFilter = new RecipeFilter();

				int matches = 0; //Recipes fitting conditions
				int count = 0; //checked Recipes

				final Recipe[] results;
				if (recommendations.size() >= amount) {
					results = new Recipe[amount];
				} else {
					results = new Recipe[recommendations.size()];
				}

				// get recipes from database based on the recipeID
				while (matches < amount && recommendations.size() > count) {
					final long id = recommendations.get(count).getItemID();
					recipeFilter.setRecipeId(id);
					final List<Recipe> recipes = query.getRecipe(recipeFilter);

					if (!recipes.isEmpty()) {
						
						if (!filterRecipe(recipes.get(0), desiredIngredients, avoidedIngredients)) {
							count++;
							continue;
						}
						results[matches] = recipes.get(0);
						matches++;
					}
					count++;
				}

				query.close();

				if (matches < recommendations.size()) {
					final Recipe[] results_shorter = new Recipe[matches];
					for (int i = 0; i < matches; i++) {
						results_shorter[i] = results[i];
					}
					return results_shorter;
				}

				return results;

			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
				return null;
			}
		}
	}

	/**
	 * appends evaluations to evaluation.txt file in the same directory as the
	 * interactions.csv files.
	 * 
	 * @param recommenderBuilder affects used similarity
	 * @param threshold items whose preference value is at least this value are considered "relevant" for the purposes of computations
	 * @param evaluationPercentage affects how many users will be used
	 * @return IRStatistics containing quality measures of given recommenderBuilder
	 */
	public IRStatistics evaluateRecommendations(final RecommenderBuilder recommenderBuilder, final double threshold,
			final double evaluationPercentage) {
		if (this.datamodel == null) {
			loadData();
		}
		try (final FileWriter pw = new FileWriter(PATH + "/evaluation.txt", true)) {
			final GenericRecommenderIRStatsEvaluator eval = new GenericRecommenderIRStatsEvaluator();
			final IRStatistics result = eval.evaluate(recommenderBuilder, null, this.datamodel, null, 5, threshold,
					evaluationPercentage);
			pw.append("-----threshold:").append(String.valueOf(threshold)).append(" evaluationPercentage:").append(String.valueOf(evaluationPercentage)).append("-----\n\n");
			pw.append("F1 score: ");
			pw.append(String.valueOf(result.getF1Measure())).append("\n");
			pw.append("Precision: ");
			pw.append(String.valueOf(result.getPrecision())).append("\n");
			pw.append("Recall: ");
			pw.append(String.valueOf(result.getRecall())).append("\n");
			pw.append("Reach: ");
			pw.append(String.valueOf(result.getReach())).append("\n");
			pw.append("Fall Out: ");
			pw.append(String.valueOf(result.getFallOut())).append("\n");
			pw.append("Normalized Discounted Cumulative Gain: ");
			pw.append(String.valueOf(result.getNormalizedDiscountedCumulativeGain())).append("\n\n");
			LOG.debug("DONE {}", threshold);
			return result;

		} catch (final TasteException | IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * use this method to inform the recommender of new interactions. Creates a new
	 * interaction file with given interaction or appends to an existing file
	 * 
	 * @param user     who rated a new recipe
	 * @param recipeID that was rated
	 * @param rating
	 * 
	 */
	public void refreshDatamodel(final User user, final long recipeID, final int rating) {
		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd");
		final LocalDateTime now = LocalDateTime.now();
		try (final FileWriter pw = new FileWriter(PATH + "/interactions." + dtf.format(now) + "update.csv", true)) {
			pw.append(String.valueOf(user.getId())).append(",").append(String.valueOf(recipeID)).append(",").append(String.valueOf(rating)).append(".0").append("\n");
			if (this.datamodel != null) {
				this.datamodel.refresh(null);
			}
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}

	}
	/**
	 * use this method to inform the recommender of new interactions. Creates a new
	 * interaction file with given interaction or appends to an existing file
	 * 
	 * @param recipe to check
	 * @param desiredIngredients
	 * @param avoidedIngredients
	 * @return true if recipe contains all desiredIngredients and no avoidedIngredients; else false
	 */
	private boolean filterRecipe(final Recipe recipe, final String[] desiredIngredients, final String[] avoidedIngredients) {
		if (desiredIngredients != null && recipe != null) {

			int containsCount = getNumberOfDiseredIngredients(recipe, desiredIngredients);
			//check if recipe contains all desired ingredients
			if (containsCount < desiredIngredients.length) {
				return false;
			}
		}
		
		return !containsAvoidedIngredients(recipe, avoidedIngredients);
		
	}
	
	private int getNumberOfDiseredIngredients(@Nonnull final Recipe recipe, @Nonnull final String[] desiredIngredients) {
		int containsCount = 0;
		for (final String di : desiredIngredients) {
			for (final Ingredient i : recipe.getIngredients().keySet()) {
				final String[] words = i.getName().split(" ");
				for(final String word : words) {

					if(word.equals(di)) {
						containsCount++;
						break;
					}
				}
			}
		}
		
		return containsCount;
	}
	
	private boolean containsAvoidedIngredients(final Recipe recipe, final String[] avoidedIngredients) {
		if (avoidedIngredients != null) {
			for (final Ingredient i : recipe.getIngredients().keySet()) {
				final String[] words = i.getName().split(" ");
				for(final String word : words) {
					for(final String ai : avoidedIngredients) {
						if(word.contentEquals(ai)) {
							//recipe contains an avoided ingredient
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public DataModel getDatamodel() {
		return this.datamodel;
	}
}