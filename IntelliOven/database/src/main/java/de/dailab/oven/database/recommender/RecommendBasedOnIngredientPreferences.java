package de.dailab.oven.database.recommender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import de.dailab.oven.model.data_model.filters.RecipeFilter;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.*;
@Deprecated
public class RecommendBasedOnIngredientPreferences {
	
	private Set<Ingredient> preferredIngredients = new HashSet<>();
	private List<Recipe> recipes = new ArrayList<>();
	private final Map<Recipe, Integer> recipeRatings = new HashMap<>();
	private final Set<Recipe> passedRecipes = new HashSet<>();
	private Query query;
	private static final Logger LOGGER = Logger.getLogger(RecommendBasedOnIngredientPreferences.class.getName());
	private User user;
	
	
	public RecommendBasedOnIngredientPreferences(final User user, final Query query, RecipeFilter recipeFilter) {
		if(user != null) {
			this.user = user;
			this.preferredIngredients = user.getLikesIngredients();  			
		}
		if(recipeFilter == null)
			recipeFilter = new RecipeFilter();
		
		if(query != null){
			this.query = query;
		   		    
		    if(user!= null) {
		    	recipeFilter.addUserPreferences(user);
		    }
		    final List<Recipe> tmpRecipes;
			try {
				tmpRecipes = this.query.getRecipe(recipeFilter);
				if(tmpRecipes != null) {
					this.recipes = tmpRecipes;		    	
				}
			} catch (final Exception e) {
				LOGGER.log(Level.INFO, e.getLocalizedMessage());
			}
		}
	}
	
	public RecommendBasedOnIngredientPreferences(final Set<String> likedIngredients, final List<Recipe> recipes) {
		this.preferredIngredients = new HashSet<>();
		likedIngredients.forEach(i -> this.preferredIngredients.add(new Ingredient(i, this.user.getCurrentlySpokenLanguage())));
		this.recipes = recipes;
	}
	
	public Set<Recipe> recommendTopFive() {
		
		for(final Recipe recipe : this.recipes) {
			this.recipeRatings.put(recipe, 0);
			for (final IngredientWithAmount entry : recipe.getIngredients()) {
				if(this.preferredIngredients.contains(entry.getIngredient())){
					this.recipeRatings.compute(recipe, (k, v) -> (v + 1));
				}
			}
		}
		
		int maxRating = this.preferredIngredients.size();
		
		return getTopFive(maxRating);
	}

	private Set<Recipe> getTopFive(int maxRating) {
		final Set<Recipe> topFive = new HashSet<>();

		while(maxRating >= 0 && topFive.size() < 5) {
			for(final Map.Entry<Recipe, Integer> entry : this.recipeRatings.entrySet()) {
				if(entry.getValue() == maxRating) {
					topFive.add(entry.getKey());
				}
				if(topFive.size() == 5) {
					break;
				}
			}
			maxRating -= 1;
		}
		
		return topFive;
	}
	
	
	
	@Nullable
	public Recipe recommendSingleRecipe() {
		final Recipe recipe = null;
		final Set<Recipe> allRecipes = new HashSet<>();
		allRecipes.addAll(this.recipes);
		allRecipes.removeAll(this.passedRecipes);
		final int size = allRecipes.size();
		int i = 0;
		final int item = ThreadLocalRandom.current().nextInt(0, size);
		for(final Recipe r : allRecipes) {
			if(i == item) {
				this.passedRecipes.add(r);
				return r;
			}
			i++;
		}
		return recipe;
	}
}
