package de.dailab.oven.model.data_model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import zone.bot.vici.Language;

public class User
{

	@Nonnull
	private String name = "";
	private long id = -1;
    @Nonnull
    private Set<Category> preferredCategories = new HashSet<>();
    @Nonnull
    private Set<Ingredient> likesIngredients = new HashSet<>();
    @Nonnull
    private Set<Ingredient> incompatibleIngredients = new HashSet<>();
    @Nonnull
    private Map<Long, Integer> recipeRatings = new HashMap<>();
    @Nonnull
    private Language currentlySpokenLanguage = Language.UNDEF;
    @Nonnull
    private Set<Language> spokenLanguages = new HashSet<>();
    @Nonnull
    private HouseholdLabel household = HouseholdLabel.GUEST;
    @Nonnull
    private Set<Long> cookedRecipeIDs = new HashSet<>();
    
    
    /**
     * Set the user name
     * @param name as string
     */
    public void setName(@Nullable String name){
    	if(name != null)
    		this.name = name.toLowerCase();
    }
    
    /**
     * @return the user name as string
     */
    @Nonnull
    public String getName() {
    	return this.name;
    }
    
    /**
     * Set the users ID.
     * Do not set this ID (if somehow possible) except the ID was loaded out of the database
     * @param id as long
     */
    public void setId(long id){
    	this.id = id;
    }
    
    /**
     * @return The users ID which is currently set within the instance
     */
    public long getId() {
    	return this.id;
    }
    
    /**
     * Adds the category to the set of preferred categories
     * @param category which shall be added to the set of preferred categories
     */
    public void addPreferredCategory(@Nullable Category category){
		if(category != null) 
			this.preferredCategories.add(category);
    }
    
    /**
     * Set the list (set) of categories the user prefers (copies all values)
     * @param preferredCategories set of categories the user prefers
     */
    public void addPreferredCategories(@Nullable Set<Category> preferredCategories) {
    	if(preferredCategories != null)
    		this.preferredCategories.addAll(preferredCategories);
    }
    
    /**
     * Removes the given category from the list of categories the user prefers
     * @param category as string which shall be removed from the list of categories the user prefers
     */
    public void removePreferredCategory(@Nullable Category category)   {
    	this.preferredCategories.remove(category);
    }
    
    /**
     * @return returns the set of the categories the user prefers
     */
    @Nonnull
    public Set<Category> getPreferredCategories() {
    	return this.preferredCategories;
    }
    
    /**
     * Add the given ingredient to the list of ingredients the user likes
     * @param ingredient as string which shall be added to the list of ingredients the user likes
     */
    @Deprecated
    public void addLikedIngredient(@Nullable String ingredient){
    	if(ingredient != null && !ingredient.isEmpty()) {
    		Ingredient newIngredient = new Ingredient(ingredient, this.currentlySpokenLanguage);
    		
    		if(!this.likesIngredients.contains(newIngredient))
    			this.likesIngredients.add(newIngredient);
    	}
    }

    /**
     * Add the given ingredient to the list of ingredients the user likes
     * @param ingredient as string which shall be added to the list of ingredients the user likes
     */
    public void addLikedIngredient(@Nullable Ingredient ingredient){
    	if(ingredient != null)
    		this.likesIngredients.add(ingredient);
    }
    
    /**
     * Set the list (set) of ingredients the user likes
     * @param likesIngredients set of ingredients as strings
     */
    public void setLikedIngredients(@Nullable Set<Ingredient> likesIngredients) {
    	if(likesIngredients != null)
    		this.likesIngredients.addAll(likesIngredients);
    }
    
    /**
     * Removes the given ingredient from the list of liked ingredients
     * @param ingredient as string which shall be removed from the list of ingredients the user likes
     */
    @Deprecated
    public void removeLikedIngredient(@Nullable String ingredient)
    {
    	if(ingredient != null && !ingredient.isEmpty()) {
    		Ingredient newIngredient = new Ingredient(ingredient, this.currentlySpokenLanguage);
    		if(this.likesIngredients.contains(newIngredient)) {
    			this.likesIngredients.remove(newIngredient);
    		}    		
    	}
    }
    
    /**
     * Removes the given ingredient from the list of liked ingredients
     * @param ingredient as string which shall be removed from the list of ingredients the user likes
     */
    public void removeLikedIngredient(@Nullable Ingredient ingredient) {
    	if(ingredient != null)
    		this.likesIngredients.remove(ingredient);
    }
    
    /**
     * @return Set of strings of the ingredients the user likes
     */
    @Nonnull
    public Set<Ingredient> getLikesIngredients() {
    	return this.likesIngredients;
    }
    
    /**
     * Add the ingredient as string to the set of ingredients which the user does not like or is allergic to
     * @param ingredient ingredient as string which shall be added to the set of incompatible ingredients
     */
    @Deprecated
    public void addIncompatibleIngredient(@Nullable String ingredient){
    	if(ingredient != null && !ingredient.isEmpty()) {
    		Ingredient newIngredient = new Ingredient(ingredient, this.currentlySpokenLanguage);
			this.incompatibleIngredients.add(newIngredient);
    	}
    }

    /**
     * Add the ingredient as string to the set of ingredients which the user does not like or is allergic to
     * @param ingredient ingredient as string which shall be added to the set of incompatible ingredients
     */
    public void addIncompatibleIngredient(@Nullable Ingredient ingredient){
    	if(ingredient != null)
    		this.incompatibleIngredients.add(ingredient);
    }
    
    /**
     * Set the list (set) of incompatible ingredients
     * @param incompatibleIngredients Set of strings of the ingredients names which the user is allergic to or does not like
     */
    public void setIncompatibleIngredients(@Nullable Set<Ingredient> incompatibleIngredients) {
    	if(incompatibleIngredients != null)
    		this.incompatibleIngredients.addAll(incompatibleIngredients);
    }
    
    /**
     * Removes the given ingredient from list of incompatible ingredients
     * @param ingredient ingredient as string which shall be removed
     */
    @Deprecated
    public void removeIncompatibleIngredient(@Nullable String ingredient)
    {
    	if(ingredient != null && !ingredient.isEmpty()) {
    		Ingredient newIngredient = new Ingredient(ingredient, this.currentlySpokenLanguage);
    		if(this.incompatibleIngredients.contains(newIngredient))
    			this.incompatibleIngredients.remove(newIngredient);    		
    	}
    }

    /**
     * Removes the given ingredient from list of incompatible ingredients
     * @param ingredient ingredient as string which shall be removed
     */
    public void removeIncompatibleIngredient(@Nullable Ingredient ingredient)
    {
		this.incompatibleIngredients.remove(ingredient);    			 		
    }
    
    /**
     * @return ingredients as strings which the user does not like or is allergic to
     */
    @Nonnull
    public Set<Ingredient> getIncompatibleIngredients() {
    	return this.incompatibleIngredients;
    }
    
    /**
     * Add a rating for a recipe
     * @param recipeId long which is the recipe ID
     * @param rating integer from -10 to 10 which displays the rating 
     */
    public void addRecipeRating(long recipeId, int rating){
		if(this.recipeRatings.containsKey(recipeId)) {
			if(this.recipeRatings.get(recipeId) != rating)
				this.recipeRatings.replace(recipeId, rating);				
		}
		else {
			this.recipeRatings.put(recipeId, rating);
		}
    }
    
    /**
     * Set the map of recipe ratings.
     * @param recipeRatings the map of ratings  
     */
    public void setRecipeRatings(@Nullable Map<Long, Integer> recipeRatings) {
    	if(recipeRatings != null) {
    		this.recipeRatings.putAll(recipeRatings);
    	}
    }
    
    /**
     * @return Map of recipe ratings. Long = recipe ID; Integer = rating
     */
    @Nonnull
    public Map<Long, Integer> getRecipeRatings(){
    	return this.recipeRatings;
    }
 
    /**
     * Removes a recipe rating
     * @param recipeId the ID of the recipe which rating shall be removed
     */
    public void removeRecipeRating(long recipeId) {
    	if(this.recipeRatings.containsKey(recipeId)) {
    		this.recipeRatings.remove(recipeId);
    	}
    }
    
    /**
     * Set the language the user speaks currently. The language is automatically added to the list of spoken languages
     * Use language code 2 or language code 3
     * @param language The language the user speaks currently as string
     */
    @Deprecated
    public void setCurrentlySpokenLanguage(@Nullable String language) {
		if(language != null) {
			language = language.toLowerCase();
			this.currentlySpokenLanguage = Language.getLanguage(language);
			if(this.currentlySpokenLanguage != Language.UNDEF) {			
				addLanguageToSpokenLanguages(this.currentlySpokenLanguage.getLangCode2());
			}			
		}
    }

    /**
     * Set the language the user speaks currently. The language is automatically added to the list of spoken languages
     * Use language code 2 or language code 3
     * @param language The language the user speaks currently as string
     */
    public void setCurrentlySpokenLanguage(@Nullable Language language) {
		if(language != null) {
			this.currentlySpokenLanguage = language;
			if(this.currentlySpokenLanguage != Language.UNDEF) {			
				addLanguageToSpokenLanguages(this.currentlySpokenLanguage.getLangCode2());
			}			
		}
    }
    
    /**
     * @return the language the user speaks currently as string
     */
    @Nonnull
    public Language getCurrentlySpokenLanguage() {
    	return this.currentlySpokenLanguage;
    }
    
    /**
     * This function does not proof that the languages within the given set are supported.
     * Use addLanguageToSpokenLanguages instead.
     * @param spokenLanguages Set of strings of languages the user speaks. 
     */
    public void setSpokenLanguages(@Nullable Set<Language> spokenLanguages) {
    	if(spokenLanguages != null && !spokenLanguages.isEmpty()) {
    		this.spokenLanguages.addAll(spokenLanguages);
    	}
    }
    
    /**
     * Adds the language to the list of languages which the user speaks. Makes sure, that the language is supported.
     * Use language code 2 or language code 3
     * @param language as String
     */
    public void addLanguageToSpokenLanguages(@Nullable String language) {
		if(language != null) {
			language = language.toLowerCase();
			Language newLanguage = Language.getLanguage(language);
			if(newLanguage != Language.UNDEF && !this.spokenLanguages.contains(newLanguage)) {
				this.spokenLanguages.add(newLanguage);
			}			
		}
    }
    
    /**
     * Removes the given language from the set of spoken languages
     * @param language as String which shall be removed
     */
    public void removeLanguageFromSpokenLanguages(@Nullable Language language) {
    	if(language != null) {
    		for(Language oldLanguage : this.spokenLanguages) {
    			if(oldLanguage == language) {
    				this.spokenLanguages.remove(oldLanguage);
    			}
    		}    		
    	}
    }
    
    /**
     * Removes the given language from the set of spoken languages
     * @param language as String which shall be removed
     */
    @Deprecated
    public void removeLanguageFromSpokenLanguages(@Nullable String language) {
    	if(language != null) {
    		language = language.toLowerCase();
    		Language newLanguage = Language.getLanguage(language);
    		for(Language oldLanguage : this.spokenLanguages) {
    			if(oldLanguage == newLanguage) {
    				this.spokenLanguages.remove(oldLanguage);
    			}
    		}    		
    	}
    }
    
    /**
     * @return Set of languages as strings the user speaks
     */
    @Nonnull
    public Set<Language> getSpokenLanguages() {
    	return this.spokenLanguages;
    }
    
    public HouseholdLabel getHousehold() {
		return this.household;
	}
    
    public void setHousehold(@Nullable HouseholdLabel household) {
		if(household != null && !household.equals(this.household)) {
			this.household = household;
		}
	}
    
    public void setCookedRecipeIDs(@Nullable Set<Long> recipeIDs) {
    	if(recipeIDs != null) 
    		this.cookedRecipeIDs = recipeIDs;
    }
    
    public void addCookedRecipeID(@Nullable Long recipeID) {
    	if(recipeID != null && Long.compare(recipeID, 0l) >= 0)
    		this.cookedRecipeIDs.add(recipeID);
    	
    }
    
    public Set<Long> getCookedRecipeIDs() {
		return this.cookedRecipeIDs;
	}
}
