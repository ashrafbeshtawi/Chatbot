package de.dailab.oven.aal_recommender.recommender;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.dailab.oven.aal_recommender.data_converter.WordToVectorConverter;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.Amount;
import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.User;
import de.dailab.oven.model.data_model.filters.RecipeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ContentBasedRecommender {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(ContentBasedRecommender.class);

	private String mode = "cosine"; // which similarity to use

	private List<Recipe> recipes;

	private final User user; // User for whom to create a recommendation

	/**
	 * @param recipes Recipes to use for recommendation
	 * @param user    User for whom to create a recommendation
	 * @param mode    which similarity to use, set mode to "cosine" (default) or
	 *                "jaccard"
	 */
	public ContentBasedRecommender(final List<Recipe> recipes, final User user, final String mode) {

		this.recipes = recipes;
		this.user = user;
		if ("jaccard".equals(mode) || "cosine".equals(mode)) {
			this.mode = mode;
		} else if (mode != null) {
			LOG.warn("{} is invalid, default (cosine) will be used", mode);
		}

	}

	/**
	 * @param recipes Recipes to use for recommendation
	 * @param user    User for whom to create a recommendation
	 */
	public ContentBasedRecommender(final List<Recipe> recipes, final User user) {

		this.recipes = recipes;
		this.user = user;

	}

	/**
	 * @param mode which similarity to use, set mode to "cosine" (default) or
	 *             "jaccard"
	 */
	public ContentBasedRecommender(final String mode) {
		this(null, null, mode);
	}

	/**
	 * @param name        name of the recipe. Set to null if not relevant
	 * @param categories  Recipe categories. Set to null if not relevant
	 * @param ingredients recipe ingredients. Set to null if not relevant
	 * @param footlabel   Recipe footlabel. Set to null if not relevant
	 * @param duration    Recipe duration. Set to null if not relevant
	 * @return most similar recipe; Null if connection to database failed or if
	 *         database is empty
	 */
	public Recipe getMostSimilarRecipe(final String name, final ArrayList<String> categories,
			final ArrayList<String> ingredients, final String footlabel, final Duration duration) {

		if (this.recipes == null) {
			try {
				final Query query = new Query();

				final RecipeFilter recipeFilter = new RecipeFilter();
				recipeFilter.addRecipeLanguage(Language.ENGLISH);
				recipeFilter.addExcludedAuthor("food.com"); // or else the query
															// will not work
															// (too many
															// recipes)
				this.recipes = query.getRecipe(recipeFilter);
				query.close();

				if (this.recipes.isEmpty()) {
					LOG.warn("no recipes in database, getting most similar recipe is not possible");
					return null;
				}
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
				return null;
			}
		}

		double maxAll = 0.0;
		Recipe maxRecipe = this.recipes.get(0);

		for (int i = 1; i < this.recipes.size(); i++) {

			double simAll = 0;
			final ArrayList<Double> countVec = new ArrayList<>();
			final ArrayList<Double> countVeci = new ArrayList<>();

			if (ingredients != null) {
				final ArrayList<ArrayList<String>> iLists = new ArrayList<>();
				final ArrayList<String> iListIng = ingredientsToList(this.recipes.get(i).getIngredients());
				iLists.add(ingredients);
				iLists.add(iListIng);
				final WordToVectorConverter w2vI = new WordToVectorConverter(this.recipes.get(0).getLanguage(), iLists);

				countVec.addAll(w2vI.getCountVec(ingredients));
				countVeci.addAll(w2vI.getCountVec(ingredients));
			}

			if (categories != null) {
				final ArrayList<ArrayList<String>> cLists = new ArrayList<>();
				final ArrayList<String> cListIng = new ArrayList<>();
				final List<Category> cList = new ArrayList<>(this.recipes.get(i).getCategories());
				cList.forEach(c -> cListIng.add(c.getName()));
				
				cLists.add(categories);
				cLists.add(cListIng);
				final WordToVectorConverter converter = new WordToVectorConverter(this.recipes.get(0).getLanguage(), cLists);

				countVec.addAll(converter.getCountVec(categories));
				countVeci.addAll(converter.getCountVec(cListIng));
			}
			if (name != null) {
				final ArrayList<ArrayList<String>> nLists = new ArrayList<>();
				final ArrayList<String> nListi = nameToList(this.recipes.get(i).getName());
				nLists.add(nameToList(name));
				nLists.add(nListi);
				final WordToVectorConverter w2vN = new WordToVectorConverter(this.recipes.get(0).getLanguage(), nLists);

				countVec.addAll(w2vN.getCountVec(nameToList(name)));
				countVeci.addAll(w2vN.getCountVec(nListi));
			}
			if ("cosine".equals(this.mode)) {

				simAll = cosineSim(countVec, countVeci);

			}

			if (simAll > maxAll) {
				maxAll = simAll;
				maxRecipe = this.recipes.get(i);
			} else if (simAll == maxAll) {
				final double r = Math.random();
				if (r < 0.5) {
					maxAll = simAll;
					maxRecipe = this.recipes.get(i);
				}
			}
		}

		return maxRecipe;

	}

	private static double cosineSim(final ArrayList<Double> v1, final ArrayList<Double> v2) {

		double result = 0;
		double tmpa = 0;
		double tmpb = 0;
		// dotp
		for (int i = 0; i < v1.size(); i++) {
			result += v1.get(i) * v2.get(i);
			tmpa += Math.pow(v1.get(i), 2);
			tmpb += Math.pow(v2.get(i), 2);
		}
		result /= Math.sqrt(tmpa) * Math.sqrt(tmpb);

		return result;
	}

	private static double jaccardSimilarity(final List<String> r1, final List<String> r2) {

		final ArrayList<String> vector1 = deleteDuplicates(r1);
		final ArrayList<String> vector2 = deleteDuplicates(r2);

		int intersectionCount = 0;
		for(final String s : vector1) {
			if(vector2.contains(s)) {
				intersectionCount++;
			}
		}

		return ((double) (intersectionCount))
				/ ((double) (vector1.size() + vector2.size() - (intersectionCount)));

	}

	/**
	 * @param mode which similarity to use, set mode to "cosine" (default) or
	 *             "jaccard"
	 */
	public void setMode(final String mode) {
		this.mode = mode;
	}

	private static ArrayList<String> deleteDuplicates(final List<String> list) {
		final ArrayList<String> newList = new ArrayList<>();
		for(final String word : list) {
			if(!newList.contains(word)) {
				newList.add(word);
			}
		}
		return newList;
	}

	private static ArrayList<String> ingredientsToList(final Map<Ingredient, Amount> ingredients) {
		final ArrayList<String> list = new ArrayList<>();
		for(final Ingredient i : ingredients.keySet()) {
			list.add(i.getName());
		}
		return list;
	}

	private static ArrayList<String> nameToList(final String name) {
		final ArrayList<String> list = new ArrayList<>();
		for (final String word : name.split(" ")) {
			if (!list.contains(word)) {
				list.add(word);
			}
		}

		return list;
	}

}