package de.dailab.oven.model.data_model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import zone.bot.vici.Language;

public class Ingredient
{
	@Nonnull
    private String name = "";
	@Nonnull
    private Map<Nutrition, Amount> nutrition = new EnumMap<>(Nutrition.class);
    @Nonnull
	private Language language = Language.UNDEF;
    @Nonnull
    private List<String> attributes = new ArrayList<>();
    private long id = -1;
    private boolean checkedForNutritions = false;
	/**
	 * Initialize with all parameters at once
	 * @param name Name of ingredient
	 * @param nutrition Nutrition of ingredient
	 * @param language Language of Ingredient. Use language code 2 or language code 3
	 */
	public Ingredient(@Nullable String name, @Nullable Map<Nutrition, Amount> nutrition, @Nullable Language language){
		setName(name);
		setNutrition(nutrition);
		setLanguage(language);
	}

	/**
	 * Initialize just with name and language to call for nutrition later
	 * @param name Name of ingredient
	 * @param language Language of Ingredient
	 * @deprecated Soon, use correct Language instead
	 */
	@Deprecated
	public Ingredient(String name, String language){
		setName(name);
		setLanguage(language);
	}

	/**
	 * Initialize just with name and language to call for nutrition later
	 * @param name Name of ingredient
	 * @param language Language of Ingredient
	 */
	public Ingredient(String name, Language language){
		setName(name);
		setLanguage(language);
	}
	
	/**
	 * @return String with name of ingredient
	 */
	@Nonnull
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set ingredients name
	 * @param name Name of Ingredient
	 */
	public void setName(@Nullable String name){
		if(name != null && !name.contentEquals(this.name))
			this.name = name.toLowerCase();	
	}

	/**
	 * @return Long which is the current ID
	 */
	@Nonnull
	public long getID() {
		return this.id;
	}
	
	/**
	 * Set ingredients name
	 * @param name Name of Ingredient
	 */
	public void setID(long id){
		if(id != this.id)
			this.id = id;			
	}
	
	/**
	 * @return EnumMap with nutrition and amount of ingredient
	 */
	@Nonnull
	public Map<Nutrition, Amount> getNutrition() {
		return this.nutrition;
	}
	
	/**
	 * @param nutrition Map with nutrition
	 */
	public void setNutrition(@Nullable Map<Nutrition, Amount> nutrition) {
		if(nutrition != null && !nutrition.equals(this.nutrition))
			this.nutrition = nutrition;
	}
	
	/**
	 * @param nutrition Map with nutrition
	 */
	public void addNutrition(@Nullable Nutrition nutrition, @Nullable Amount amount) {
		if(nutrition != null && amount != null) 
			this.nutrition.put(nutrition, amount);	
	}
	
	/**
	 * Set the list of attributes (sliced, chopped, ...)
	 * @param attributes	The attributes to set
	 */
	public void setAttributes(@Nullable List<String> attributes) {		
		if(attributes != null) {
			this.attributes.clear();
			this.attributes.addAll(attributes);
		}
	}
	
	/**
	 * Add an attribute to the list of attributes
	 * @param attribute	The attribute to add
	 */
	public void addAttribute(@Nullable String attribute) {
		if(attribute != null && !attribute.isEmpty())
			this.attributes.add(attribute);
	}
	
	/**
	 * Removes the given attribute from the list of attributes
	 * @param attribute	The attribute to remove	
	 * @return			{@code True} if attribute has been removed<br>
	 * 					{@code False} otherwise
	 */
	public boolean removeAttribute(@Nullable String attribute) {
		return this.attributes.remove(attribute);
	}
	
	/**
	 * @return The list of attributes (within there are no {@code null}-values or empty strings
	 */
	@Nonnull
	public List<String> getAttributes() {
		return this.attributes;
	}
	
	/**
	 * Set if there was already a look up for nutrition
	 */
	public void setCheckedForNutrition(boolean isChecked) {
		if(this.checkedForNutritions != isChecked)
			this.checkedForNutritions = isChecked;
	}
	
	/**
	 * @return TRUE if it was checked for nutrition already; FALSE otherwise
	 */
	public boolean isCheckedForNutrition() {
		return this.checkedForNutritions;
	}
	
	/**
	 * @param language Language of ingredient as String. Use language code 2 or language code 3.
	 * @deprecated Soon, use correct Language instead
	 */
	@Deprecated
	public void setLanguage (@Nullable String language) {
		if(language != null) {
			language = language.toLowerCase();
			Language newLanguage = Language.getLanguage(language);
			if(newLanguage != this.language)
				this.language = Language.getLanguage(language);
		}
	}
	
	/**
	 * @param language Language of ingredient as String. Use language code 2 or language code 3.
	 */
	public void setLanguage (@Nullable Language language) {
		if(language != null && !language.equals(this.language))
			this.language = language;
	}
	
	/**
	 * @return Language of ingredient
	 */
	@Nonnull
	public Language getLanguage() {
		return this.language;
	}
	
	/**
	 * @return String, the name of the ingredient
	 */
	@Override
	@Nonnull
	public String toString(){
		return this.getName();
	}

	@Override
	public boolean equals(Object object) {
		if(object == null)
			return false;
		
		if(this.getClass() != object.getClass())
			return false;
		
		final Ingredient otherIngredient = (Ingredient) object;
		
		//Check name
		if((this.name == null) ? (otherIngredient.name != null) 
				: !this.name.equals(otherIngredient.name))
			return false;
		
		//Check language
		return this.language.equals(otherIngredient.language); 	
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.language);
	}
}