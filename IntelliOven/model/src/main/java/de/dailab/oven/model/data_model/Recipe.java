package de.dailab.oven.model.data_model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.dailab.oven.model.serialization.DurationDeserializer;
import de.dailab.oven.model.serialization.DurationSerializer;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.RenderedImage;
import java.io.File;
import java.time.Duration;
import java.util.*;

@JsonIgnoreProperties(value = { "totalDuration", "updateImage" })
public class Recipe {
	private static final String TOTAL_DURATION_KEY = "total";
	//Merge Attributes
	@Nonnull
	private String author = "unknown";
	@Nonnull
	private Language language = Language.UNDEF;									//Original language the recipe was written in
	@Nonnull
	private String name = "";		
	
	//Further Attributes
	@Nonnull
	private Set<Category> categories = new HashSet<>();							//Categories and tags
	@Nonnull
	private Duration duration = Duration.ZERO;									//The whole time it needs to finish the recipe
	@Nonnull
	private Map<String, Duration> durations = new HashMap<>();					//List of durations since there might be different durations for preparing, baking and so on
	@Nonnull
	private FoodLabel foodLabel = FoodLabel.UNDEF;												
	private long id = -1;														//-1 is default and an invalid ID
	@JsonIgnore
	@Nullable
	private RenderedImage image = null;
	@Nonnull
	private List<IngredientWithAmount> ingredients = new LinkedList<>();
	@Nonnull
	private List<String> instructions = new ArrayList<>();
	private int originalServings = 0;											//Original servings, the recipe has been made for. 0 as invalid default input
	@Nonnull
	private String url = "";													//In case the recipe has been parsed from the Internet
	@Nonnull
	private Map<Long, Integer> userRatings = new HashMap<>();
	@Nonnull
	private String imagePath = "";
	@JsonIgnore
	@Nullable
	private File imageFile = null;
	@Nonnull
	private Map<Integer, String> ovenSettings = new HashMap<>(); 
	@JsonIgnore
	private boolean updateImage = false;
	
	/** 
	 * Sets author to "unknown" if empty string or null is passed, sets the author to the passed string.toLowerCase() otherwise
	 * @param author the recipes author
	 */
	public void setAuthor(@Nullable String author){		
		if(author != null && !author.isEmpty()) 
			this.author = author.toLowerCase();
		
	}
	
	/**
	 * Sets author to unknown if it has not been set before
	 * @return The recipes author
	 */
	@Nonnull
	public String getAuthor(){
		return this.author;
	}

	/**
	 * Set language for this recipe
	 * @param language as string. Use language code 2 or language code 3
	 */
	@Deprecated
	public void setLanguage(@Nullable String language) {
		if(language != null) {
			language = language.toLowerCase();
			Language newLanguage = Language.getLanguage(language);
			if(newLanguage != this.language)
				this.language = newLanguage;			
		}
	}
	
	public void setLanguage(@Nullable Language language) {
		if(language != null && this.language != language)
			this.language = language;
	}
	
	/**
	 * @return The recipes language
	 */
	@Nonnull
	public Language getLanguage() {
		return this.language;
	}
	
	/**
	 * Set the recipes name
	 * @param name Name of the recipe (stored as name.toLowerCase())
	 */
	public void setName(@Nullable String name){
		if(name != null)
			this.name = name.toLowerCase();
	}
	
	/**
	 * @return The recipes name
	 */
	@Nonnull
	public String getName(){
		return this.name;
	}
	
	/**
	 * Sets the categories of this recipe.
	 * @param categories Set of categories
	 */
	public void setCategories(@Nullable Set<Category> categories) {
		if(categories != null) {
			this.categories.clear();
			this.categories.addAll(categories);
		}
	}
	
	/**
	 * Add a category to the set of categories
	 * @param category The category to be added
	 */
	public void addCategory(@Nullable Category category) {
		if(category != null)
			this.categories.add(category);
	}
	
	/**
	 * Removes the given category from the set of categories
	 * @param category Category to remove
	 */
	public void removeCategory(@Nullable Category category) {
		this.categories.remove(category);	
	}
	
	/**
	 * Returns the set of categories which this recipe belongs to
	 * @return Set of categories.
	 */
	@Nonnull
	public Set<Category> getCategories () {
		return this.categories;
	}
	
	/**
	 * Use addDurationToListOfDurations()!
	 * This function is just public for filling the recipe class with data from the database.  
	 * @param duration The total duration for finishing the recipe
	 */
	@JsonDeserialize(using = DurationDeserializer.class)
	public void setDuration(@Nullable Duration duration) {
		if(duration != null && !duration.isNegative()) {
			this.duration = duration;
			addDurationToListOfDurations(TOTAL_DURATION_KEY, duration);
		}
	}

	@JsonSerialize(using = DurationSerializer.class)
	public Duration getDuration() {
		return this.duration;
	}
	
	/**
	 * @return The total duration. Therefore it will be calculated if a new duration has been added, if a duration with key "total", "overall" or "gesamt" this duration will be selected
	 */
	@JsonProperty("totalDuration")
	@JsonSerialize(using = DurationSerializer.class)
	@Nonnull
	public Duration totalDuration() {
		if(this.duration == Duration.ZERO) {
			Duration newDuration = Duration.ZERO;
			for(Map.Entry<String, Duration> entry : this.durations.entrySet()) {
				String key = entry.getKey();
				Duration keyDuration = entry.getValue();
				if(key.contains(TOTAL_DURATION_KEY) || key.contains("overall") || key.contains("gesamt")) {
					setDuration(keyDuration);
					return keyDuration;
				}
				else {
					newDuration = newDuration.plus(keyDuration);
				}
			}
			setDuration(newDuration);
			return newDuration;
		}
		else {
			return this.duration;
		}
	}
	
	/**
	 * Adds the duration to the map of durations in case that the name is not null. hours and minutes will be ignored in case they are negative
	 * @param name Name of the duration (e.g. "preparation time")
	 * @param hours Hours of duration
	 * @param minutes Minutes of duration
	 */
	public void addDurationToListOfDurations(@Nullable String name, int hours, int minutes) {
		if(name != null) {
			Duration newDuration = Duration.ZERO;
			if(hours > 0) {
				newDuration = newDuration.plusHours(hours);				
			}
			if(minutes > 0) {
				newDuration = newDuration.plusMinutes(minutes);				
			}
			if(!newDuration.isNegative() && newDuration != Duration.ZERO)
				this.durations.put(name, newDuration);
		}
	}
	
	/**
	 * Adds the duration to the map of durations in case that the name is not null. duration will be ignored in case it is negative
	 * @param name Name of the duration (e.g. "preparation time")
	 * @param duration Duration which shall be added
	 */
	public void addDurationToListOfDurations(@Nullable String name, @Nullable Duration duration) {
		if(name != null && duration != null && !duration.isNegative())
			this.durations.put(name, duration);
	}
	
	/**
	 * Set all durations
	 * @param durations (preparation time, total,...)
	 */
	@JsonDeserialize(contentUsing = DurationDeserializer.class)
	public void setDurations(@Nullable Map<String, Duration> durations) {
		if(durations != null) {
			this.durations.clear();
			this.durations.putAll(durations);
		}
	}
	
	/**
	 * Returns map of durations and adds the total duration beforehand if it is absent
	 * @return Returns map of durations.
	 */
	@JsonSerialize(contentUsing = DurationSerializer.class)
	@Nonnull
	public Map<String, Duration> getDurations(){
		if(!this.durations.containsValue(this.duration)) {
			this.durations.put(TOTAL_DURATION_KEY, this.duration);
		}
		return this.durations;
	}

	/**
	 * Removes given duration from list (map) of durations
	 * @param name The name of the duration to remove from list
	 */
	public void removeDurationFromListOfDurations(@Nullable String name) {
		this.durations.remove(name);
	}
	
	/**
	 * Set recipes food label
	 * @param foodLabel The food label to set for the recipe
	 */
	public void setFoodLabel(@Nullable FoodLabel foodLabel) {
		if(foodLabel != null)
			this.foodLabel = foodLabel;
	}
	
	/**
	 * @return The set food label
	 */
	@Nonnull
	public FoodLabel getFoodLabel() {
		return this.foodLabel;
	}

	/**
	 * Set the recipes id. This is supposed just to be set by the database model
	 * @param id ID of recipe
	 */
	public void setId(long id){
		this.id = id;
	}
	
	/**
	 * @return The recipes ID. -1 in case it has not been uploaded to the database yet.
	 */
	public long getId() {
		return this.id;
	}
	
	/**
	 * Set a rendered image for the recipe. For better performance choose setImageFile() if possible
	 * @param image The image of the recipe
	 */
	public void setImage(@Nullable RenderedImage image) {
		this.image = image;
	}
	
	/**
	 * @return Image, if loaded; Null if there is no image yet. Tries to load it from imagePath
	 */
	@Nullable
	public RenderedImage getImage() {
		return this.image;
	}
	
	/**
	 * Set the image file for this recipe
	 * @param imageFile The recipes image file
	 */
	public void setImageFile (@Nullable File imageFile) {
		this.imageFile = imageFile;
	}
	
	/**
	 * @return Recipes image file, null if it has not been set yet. Tries to load it  from imagePath
	 */
	@Nullable
	public File getImageFile () {
		return this.imageFile;
	}
	
	/**
	 * Set path where to find the image to this recipe
	 * @param imagePath Path as string
	 */
	public void setImagePath(@Nullable String imagePath) {
		if(imagePath != null)
			this.imagePath = imagePath;
	}
	
	/**
	 * @return Returns image path if set, empty string otherwise
	 */
	@Nonnull
	public String getImagePath() {
		return this.imagePath;
	}
	
	/**
	 * Set the map of ingredients
	 * @param ingredients The recipe ingredients
	 */
	public void setIngredients(@Nullable List<IngredientWithAmount> ingredients){
		if(ingredients != null) {
			this.ingredients.clear();
			this.ingredients.addAll(ingredients);
		}
	}
	
	/**
	 * Add one specific ingredient with amount to the list of ingredients
	 * @param ingredient The ingredient to add
	 */
	public void addIngredientToListOfIngredients(@Nullable IngredientWithAmount ingredient){
		if(ingredient != null)
			this.ingredients.add(ingredient);
	}

	/**
	 * @return The map of ingredients with their amount (quantity and unit)
	 */
	@Nonnull
	public List<IngredientWithAmount> getIngredients(){
		return this.ingredients;
	}
	
	/**
	 * Remove target ingredient from the map of ingredients
	 * @param ingredient Ingredient to remove
	 */
	public void removeIngredient(@Nullable Ingredient ingredient) {
		this.ingredients.removeIf(i -> i.getIngredient().equals(ingredient));
	}
	
	/**
	 * Set the instructions of the recipe
	 * @param instructions Instruction as a list of strings
	 */
	public void setInstructions(@Nullable List<String> instructions){
		if(instructions != null)
			this.instructions = instructions;
	}
	
	/**
	 * Add an instruction to the list of instructions
	 * @param instruction Instruction to add as string
	 */
	public void addInstruction(@Nullable String instruction) {		
		//Kind of random number which shall ensure, that the string is not way too short
		if(instruction != null && instruction.length() > 5)
			this.instructions.add(instruction);
	}
	
	/**
	 * @return List of instructions
	 */
	@Nonnull
	public List<String> getInstructions (){
		return this.instructions;
	}
	
	/**
	 * @param originalServings The servings the recipe was made for originally. These must not be 0 for later calculation and neither negative
	 */
	public void setOriginalServings(int originalServings){
		if(originalServings > 0)
			this.originalServings = originalServings;
	}

	/**
	 * @return The servings the recipe was made for originally as int
	 */
	public int getOriginalServings(){
		return this.originalServings;
	}
	
	/**
	 * Set the URL for the recipe if it has been loaded from the Internet
	 * @param url URL where the recipe has been found
	 */
	public void setUrl(@Nullable String url){
		if(url != null)
			this.url = url;
	}
	
	/**
	 * @return The URL where the recipe has been found if it was downloaded from the Internet.
	 * It will be checked if it is a valid URL before passing it to the database.
	 */
	@Nonnull
	public String getUrl() {
		return this.url;
	}
	
    /**
     * Set the map of user ratings.
     * @param userRatings the map of ratings  
     */
	public void setUserRatings(@Nullable Map<Long, Integer> userRatings) {
    	if(userRatings != null)
    		this.userRatings.putAll(userRatings);
	}
	
    /**
     * Add a rating for a recipe
     * @param userId long which is the users ID
     * @param rating integer from -10 to 10 which displays the rating 
     */
	public void addUserRating(long userId, int rating){
		this.userRatings.put(userId, rating);
	}
	
	/**
     * @return Map of user ratings. Long = user ID; Integer = rating
     */
	@Nonnull
	public Map<Long, Integer> getUserRatings(){
		return this.userRatings;
	}
	
    /**
     * Removes a user rating
     * @param userId the ID of the user which rating shall be removed
     */
    public void removeUserRating(long userId) {
		this.userRatings.remove(userId);
    }
	
    /**
     * Sets the oven settings to the given one
     * @param ovenSettings	A map where the key is mapped to the instructions steps number. A code as String for the needed setting
     */
    public void setOvenSettings(@Nullable Map<Integer, String> ovenSettings) {
    	if(ovenSettings != null) {
    		this.ovenSettings.clear();
    		this.ovenSettings.putAll(ovenSettings);
    	}
    }
    
    /**
     * Adds an oven setting to the map of oven settings
     * @param instructionStepNumber	The number of the instruction step where this setting shall be mapped to
     * @param ovenSetting			The setting code as string
     */
    public void addOvenSetting(int instructionStepNumber, @Nullable String ovenSetting) {
    	if(ovenSetting == null) {
    		ovenSetting = "";
    	}
    	this.ovenSettings.put(instructionStepNumber, ovenSetting);
    }
    
    /**
     * Removes the setting for the given instruction step number
     * @param instructionStepNumber	The instruction steps number which's setting shall be deleted
     */
    public void removeOvenSetting(int instructionStepNumber) {
    	if(this.ovenSettings.containsKey(instructionStepNumber)) {
    		this.ovenSettings.replace(instructionStepNumber, "");
    	}
    }
    
    /**
     * @return The oven settings mapped to the instruction steps number
     */
    @Nonnull
    public Map<Integer, String> getOvenSettings() {
    	return this.ovenSettings;
    }
    
    /**
     * @return The instructions (String) mapped to the oven settings 
     */
    public Map<String, String> getInstructionsWithOvenSettings() {
    	Map<String, String> instructionsWithOvenSettings = new HashMap<>();
    	int instructionsSize = this.instructions.size();
    	for(Map.Entry<Integer, String> entry : this.ovenSettings.entrySet()) {
    		//Ensure that the key exists in the instructions list
    		if(instructionsSize > entry.getKey()) {
    			instructionsWithOvenSettings.put(this.instructions.get(entry.getKey()), entry.getValue());
    		}
    	}
    	return instructionsWithOvenSettings;
    }  
    
    public void updateImage(@Nullable File imageFile) {
    	this.imageFile = imageFile;
    	this.updateImage = true;
    }
    
    @JsonProperty("updateImage")
    public boolean updateImage() {
    	return this.updateImage;
    }
}