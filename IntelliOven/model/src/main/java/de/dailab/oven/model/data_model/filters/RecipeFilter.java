package de.dailab.oven.model.data_model.filters;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.data_model.FoodLabel;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.User;
import zone.bot.vici.Language;

/**
 * Helper class for setting a filter for a recipe query
 * @author Tristan Schroer
 * @version 2.0.0
 */
public class RecipeFilter {
	
	@Nonnull
	private Set<Language> recipeLanguages = new HashSet<>();
	@Nonnull
	private String recipeName = ""; 
	@Nonnull
	private Set<Category> requiredCategories = new HashSet<>();
	@Nonnull
	private Set<Category> possibleCategories = new HashSet<>();
	@Nonnull
	private Set<Category> excludedCategories = new HashSet<>();
	@Nonnull
	private Set<Ingredient> requiredIngredients = new HashSet<>();
	@Nonnull
	private Set<Ingredient> possibleIngredients = new HashSet<>();
	@Nonnull
	private Set<Ingredient> excludedIngredients = new HashSet<>();
	@Nonnull
	private Set<String> possibleAuthors = new HashSet<>();
	@Nonnull
	private Set<String> excludedAuthors = new HashSet<>();
	@Nonnull
	private Duration cookedWithin = Duration.ZERO;
	@Nonnull
	private FoodLabel isFoodLabel = FoodLabel.UNDEF;
	@Nullable
	private Long recipeId = null;
	@Nullable
	private Integer originalServings = null;
	@Nullable
	private Integer limit = null;
	
	/**
	 * Set the languages the recipes can / should be written in
	 * @param recipeLanguages The given set of languages
	 */
	public void setRecipeLanguages(@Nullable final Set<Language> recipeLanguages) {
		if(recipeLanguages != null) {
			recipeLanguages.remove(null);
			recipeLanguages.remove(Language.UNDEF);
			this.recipeLanguages = recipeLanguages;
		}
	}
	
	/**
	 * Add a language the recipe can / should be written in
	 * @param recipeLanguage The language to add
	 */
	public void addRecipeLanguage(@Nullable final Language recipeLanguage) {
		if(recipeLanguage != null && !recipeLanguage.equals(Language.UNDEF)) 
			this.recipeLanguages.add(recipeLanguage);
	}
	
	/**
	 * @return 	The set of languages the recipes can / should be written in<br>
	 * 			Empty set in case no specific language has been added
	 */
	@Nonnull
	public Set<Language> getRecipeLanguages() {
		return this.recipeLanguages;
	}
	
	/**
	 * @param recipeName A name a recipe should contain
	 */
	public void setRecipeName(@Nullable final String recipeName) {
		if(recipeName != null)
			this.recipeName = recipeName;
	}
	
	/**
	 * @return 	A name a recipe should contain<br>
	 * 			Empty string in case it has not been set
	 */
	@Nonnull
	public String getRecipeName() {
		return this.recipeName;
	}
	
	/**
	 * Set the categories the recipes must have
	 * @param categories	The required categories
	 */
	public void setRequiredCategories(@Nullable final Set<Category> categories) {
		if(categories != null) {
			categories.remove(null);
			this.requiredCategories = categories;			
		}
	}
	
	/**
	 * Add a category the recipes must have
	 * @param category The required category
	 */
	public void addRequiredCategory(@Nullable final Category category) {
		if(category != null)
			this.requiredCategories.add(category);
	}
	
	/**
	 * @return The set of all categories the recipes must have
	 */
	@Nonnull
	public Set<Category> getRequiredCategories() {
		return this.requiredCategories;
	}
	
	/**
	 * Set the categories the recipes can have ("bbq" is a nice category, but it isn't a hard requirement)
	 * @param categories The possible categories
	 */
	public void setPossibleCategories(@Nullable final Set<Category> categories) {
		if(categories != null) {
			categories.remove(null);
			this.possibleCategories = categories;
		}
	}
	
	/**
	 * Add a category the recipes can have ("bbq" is a nice category, but it isn't a hard requirement)
	 * @param category The possible category
	 */
	public void addPossibleCategory(@Nullable final Category category) {
		if(category != null)
			this.possibleCategories.add(category);
	}
	
	/**
	 * @return 	Set the categories the recipes can have ("bbq" is a nice category, but it isn't a hard requirement)<br>
	 * 			Empty set in case there is no such category set
	 */
	@Nonnull
	public Set<Category> getPossibleCategories() {
		return this.possibleCategories;
	}
	
	/**
	 * Set the categories the recipes must not have
	 * @param categories	The categories to exclude
	 */
	public void setExcludedCategories(@Nullable final Set<Category> categories) {
		if(categories != null) {
			categories.remove(null);
			this.excludedCategories = categories;
		}
	}
	
	/**
	 * Add a category the recipes must not have
	 * @param category	The category to exclude
	 */
	public void addExcludedCategory(@Nullable final Category category) {
		if(category != null)
			this.excludedCategories.add(category);
	}
	
	/**
	 * @return Set of categories the recipes must not have
	 */
	@Nonnull
	public Set<Category> getExcludedCategories() {
		return this.excludedCategories;
	}
	
	/**
	 * Set which ingredients the recipes must include
	 * @param ingredients	The set of ingredients
	 */
	public void setRequiredIngredients(@Nullable final Set<Ingredient> ingredients) {
		if(ingredients != null)
			this.requiredIngredients = ingredients;
	}
	
	/**
	 * Add an ingredient which the recipes must include
	 * @param ingredient	The ingredient to add
	 */
	public void addRequiredIngredient(@Nullable final Ingredient ingredient) {
		if(ingredient != null)
			this.requiredIngredients.add(ingredient);
	}
	
	/**
	 * @return Set of ingredients the recipes must include
	 */
	@Nonnull
	public Set<Ingredient> getRequiredIngredients() {
			return this.requiredIngredients;
	}
	
	/**
	 * Set the set of ingredients the recipe can include (not each will be in each recipe, but at least one)
	 * @param ingredients The possible ingredients
	 */
	public void setPossibleIngredients(@Nullable final Set<Ingredient> ingredients) {
		if(ingredients != null)
			this.possibleIngredients = ingredients;
	}
	
	/**
	 * Add an ingredient which the recipes can include
	 * @param ingredient	The ingredient to add
	 */
	public void addPossibleIngredient(@Nullable final Ingredient ingredient) {
		this.possibleIngredients.add(ingredient);
	}
	
	/**
	 * @return Set of ingredients the recipes can include (at least one)
	 */
	@Nonnull
	public Set<Ingredient> getPossibleIngredients() {
		return this.possibleIngredients;
	}
	
	/**
	 * Set which ingredients must not be included in the recipes
	 * @param ingredients The ingredients to exclude
	 */
	public void setExcludedIngredients(@Nullable final Set<Ingredient> ingredients) {
		if(ingredients != null)
			this.excludedIngredients = ingredients;
	}
	
	/**
	 * Add an ingredient which the recipes must not have
	 * @param ingredient	The ingredient to exclude
	 */
	public void addExcludedIngredient(@Nullable final Ingredient ingredient) {
		if(ingredient != null)
			this.excludedIngredients.add(ingredient);
	}
	
	/**
	 * @return The set of ingredients which the recipes must not include	
	 */
	@Nonnull
	public Set<Ingredient> getExcludedIngredients() {
		return this.excludedIngredients;
	}
	
	/**
	 * Add a set of authors the recipes can be authored by (at least one)
	 * @param authors	The possible recipe authors
	 */
	public void setPossibleAuthors(@Nullable final Set<String> authors) {
		if(authors != null)
			this.possibleAuthors = authors;
	}
	
	/**
	 * Add an author the recipes can be authored by
	 * @param author The possible recipes author
	 */
	@Nullable
	public void addPossibleAuthor(@Nullable final String author) {
		if(author != null && !author.isEmpty())
			this.possibleAuthors.add(author);
	}
	
	/**
	 * @return The set of possible recipe authors (at least one match)
	 */
	@Nonnull
	public Set<String> getPossibleAuthors() {
		return this.possibleAuthors;
	}
	
	/**
	 * Set the authors the recipes should not be from
	 * @param authors	The set of unwanted authors
	 */
	public void setExcludedAuthors(@Nullable final Set<String> authors) {
		if(authors != null)
			this.excludedAuthors = authors;
	}
	
	/**
	 * Add an author the recipes should not be from
	 * @param author	Unwanted author
	 */
	public void addExcludedAuthor(@Nullable final String author) {
		if(author != null)
			this.excludedAuthors.add(author);
	}
	
	/**
	 * @return The set of recipe authors to exclude
	 */
	@Nonnull
	public Set<String> getExcludedAuthors() {
		return this.excludedAuthors;
	}
	
	/**
	 * Set the total duration the recipes should be cooked within
	 * @param duration	The maximum duration
	 */
	public void setCookedWithin(@Nullable final Duration duration) {
		if(duration != null && !duration.isNegative())
			this.cookedWithin = duration;
	}
	
	/**
	 * @return The total duration the recipes should be cooked within
	 */
	@Nonnull
	public Duration getCookedWithin() {
		return this.cookedWithin;
	}
	
	/**
	 * Set the food label the recipe should have
	 * @param foodLabel
	 */
	public void setIsFoodLabel(@Nullable final FoodLabel foodLabel) {
		if(foodLabel != null)
			this.isFoodLabel = foodLabel;
	}
	
	/**
	 * @return The food label the recipe should have
	 */
	public FoodLabel getIsFoodLabel() {
		return this.isFoodLabel;
	}
	
	/**
	 * Set the recipes ID the recipe should have.
	 * <strong>Important:</strong> if this attribute is set all other attributes will be ignored
	 * @param recipeId The ID to query
	 */
	public void setRecipeId(@Nullable final Long recipeId) {
		this.recipeId = recipeId;
	}
	
	/**
	 * @return The ID which will be queried later on<br>{@code Null} by default
	 */
	@Nullable
	public Long getRecipeId() {
		return this.recipeId;
	}
	
	/**
	 * Set the amount of original set servings for the recipe so that Amounts will be even most likely
	 * @param originalServings The wanted servings (portions)
	 */
	public void setOriginalServings(@Nullable final Integer originalServings) {
		this.originalServings = originalServings;
	}
	
	/**
	 * @return The servings to query<br>{@code Null} by default
	 */
	@Nullable
	public Integer getOriginialServings() {
		return this.originalServings;
	}
	
	/**
	 * Set the maximum amount of recipes which should be retrieved for each language
	 * @param limit	The maximum amount of recipes
	 */
	public void setMaxNumberOfRecipesToParsePerLanguage (@Nullable final Integer limit) {
		this.limit = limit;
	}
	
	/**
	 * @return  The maximum number of recipes to query per language<br>{@code Null} by default
	 */
	@Nullable
	public Integer getMaxNumberOfRecipesToParsePerLanguage () {
		return this.limit;
	}
	
	/**
	 * Adds the user preferences to the filter (basic)
	 * @param user	The user who initializes the query
	 */
	public void addUserPreferences(@Nullable final User user) {
		if(user != null) {
			if(user.getCurrentlySpokenLanguage() != Language.UNDEF) {
				this.recipeLanguages.add(user.getCurrentlySpokenLanguage());				
			}
			
			for(final Ingredient ingredient : user.getIncompatibleIngredients()) {
				this.excludedIngredients.add(ingredient);
			}
			
			for(final Ingredient ingredient : user.getLikesIngredients()) {
				this.possibleIngredients.add(ingredient);
			}
			
			this.requiredCategories.addAll(user.getPreferredCategories());
		}
	}
	
	/**
	 * Resets all values to default
	 */
	public void reset() {
		this.recipeLanguages = new HashSet<>();
		this.recipeName = ""; 
		this.requiredCategories = new HashSet<>();
		this.possibleCategories = new HashSet<>();
		this.excludedCategories = new HashSet<>();
		this.requiredIngredients = new HashSet<>();
		this.possibleIngredients = new HashSet<>();
		this.excludedIngredients = new HashSet<>();
		this.possibleAuthors = new HashSet<>();
		this.excludedAuthors = new HashSet<>();
		this.cookedWithin = Duration.ZERO;
		this.isFoodLabel = FoodLabel.UNDEF;
		this.recipeId = null;
		this.originalServings = null;
		this.limit = null;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("RecipeFilter settings: {\n");
		sb.append("RequiredIngredients: ").append(this.requiredIngredients).append("\n");
		sb.append("PossibleIngredients: ").append(this.possibleIngredients).append("\n");
		sb.append("ExcludedIngredients: ").append(this.excludedIngredients).append("\n");
		final Duration totalDuration = getCookedWithin();
		sb.append("CookedWithin: ").append(totalDuration == Duration.ZERO ? "[Not specified]" : totalDuration.toMinutes()+" minutes").append("\n");
		final Set<String> rC = this.requiredCategories.stream().map(Category::getName).collect(Collectors.toSet());
		final Set<String> pC = this.possibleCategories.stream().map(Category::getName).collect(Collectors.toSet());
		final Set<String> eC = this.excludedCategories.stream().map(Category::getName).collect(Collectors.toSet());
		sb.append("RequiredCategories: ").append(String.join(", ", rC)).append("\n");
		sb.append("PossibleCategories: ").append(String.join(", ", pC)).append("\n");
		sb.append("ExcludedCategories: ").append(String.join(", ", eC)).append("\n");
		return sb.append("}").toString();
	}
}
