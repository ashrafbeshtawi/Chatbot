package de.dailab.oven.recipe_services.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.User;

public class RecipeSorter {

	@Nonnull
	public List<Recipe> sortRecipesByRatings(@Nullable final List<Recipe> recipes, @Nullable final User user) {
		return sortRecipesByRatings(new HashSet<>(recipes), user);
	}
	
	@Nonnull
	public List<Recipe> sortRecipesByRatings(@Nullable final Set<Recipe> recipes, @Nullable final User user) {
		final List<Recipe> sorted = new ArrayList<>();
		
		if(recipes == null || recipes.isEmpty())
			return sorted;
		
		sorted.addAll(recipes);
		
		if(user == null || user.getId() == -1)
			Collections.shuffle(sorted);
			
		else
			Collections.sort(sorted, (a, b) -> sort(user, a, b));
		
		return sorted;
	}
	
	private int sort(@Nonnull final User user, @Nonnull final Recipe first, @Nonnull final Recipe second) {
		final int firstRating = first.getUserRatings().getOrDefault(user.getId(), 0);
		final int secondRating = second.getUserRatings().getOrDefault(user.getId(), 0);
		
		if(firstRating > secondRating)
			return -1;
		else if(firstRating == secondRating)
			return 0;

		return 1;
	}
}
