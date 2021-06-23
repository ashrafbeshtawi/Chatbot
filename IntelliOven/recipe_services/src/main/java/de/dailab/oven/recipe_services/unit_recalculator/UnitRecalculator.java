package de.dailab.oven.recipe_services.unit_recalculator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.model.data_model.*;

/**
 * using units from https://en.wikibooks.org/wiki/Cookbook:Units_of_measurement
 * used rounded values of units when the definition isn't clear 
 * recalculate everything in gram, millilitre or metre
 */
public class UnitRecalculator {
	
	
	//one triplet holds : <amount to recalculate, which unit the ingredient has, in which unit we convert>
	
	/*Volume Units*/
	static final Triplet<Float, Unit, Unit> V1 = new Triplet<>(5.0f, Unit.TEASPOON,Unit.MILLILITERS);
	static final Triplet<Float, Unit, Unit> V2 = new Triplet<>(15.0f, Unit.TABLESPOON,Unit.MILLILITERS);
	static final Triplet<Float, Unit, Unit> V3 = new Triplet<>(30.0f, Unit.FLUIDOUNCE,Unit.MILLILITERS);
	static final Triplet<Float, Unit, Unit> V4 = new Triplet<>(240.0f, Unit.CUP,Unit.MILLILITERS);
	static final Triplet<Float, Unit, Unit> V5 = new Triplet<>(946.0f, Unit.QUART,Unit.MILLILITERS);
	static final Triplet<Float, Unit, Unit> V6 = new Triplet<>(473.0f, Unit.PINT,Unit.MILLILITERS);
	static final Triplet<Float, Unit, Unit> V7 = new Triplet<>(4000.0f, Unit.GALON,Unit.MILLILITERS);
	static final Triplet<Float, Unit, Unit> V8 = new Triplet<>(1000.0f, Unit.LITERS,Unit.MILLILITERS);
	static final Triplet<Float, Unit, Unit> V9 = new Triplet<>(1.0f, Unit.MILLILITERS,Unit.MILLILITERS);
	static final Triplet<Float, Unit, Unit> V0 = new Triplet<>(100.0f, Unit.DECILITER,Unit.MILLILITERS);
	
	/*Mass Units*/
	static final Triplet<Float, Unit, Unit> M1 = new Triplet<>(1.0f, Unit.GRAM,Unit.GRAM);
	static final Triplet<Float, Unit, Unit> M2 = new Triplet<>(1000.0f, Unit.KILOGRAM,Unit.GRAM);
	static final Triplet<Float, Unit, Unit> M3 = new Triplet<>(0.001f, Unit.MILLIGRAM,Unit.GRAM);
	static final Triplet<Float, Unit, Unit> M4 = new Triplet<>(500.0f, Unit.POUND,Unit.GRAM);
	static final Triplet<Float, Unit, Unit> M5 = new Triplet<>(30.0f, Unit.OUNCE,Unit.GRAM);
	
	/*Length Units*/
	static final Triplet<Float, Unit, Unit> L1 = new Triplet<>(2.54f, Unit.INCH,Unit.CENTIMETER);
	static final Triplet<Float, Unit, Unit> L2 = new Triplet<>(100.0f, Unit.METER,Unit.CENTIMETER);
	static final Triplet<Float, Unit, Unit> L3 = new Triplet<>(1.0f, Unit.CENTIMETER,Unit.CENTIMETER);
	static final Triplet<Float, Unit, Unit> L4 = new Triplet<>(10.0f, Unit.DECILITER,Unit.CENTIMETER);
	static final Triplet<Float, Unit, Unit> L5 = new Triplet<>(0.1f, Unit.MILLIMETER,Unit.CENTIMETER);



	

	
	@SuppressWarnings("rawtypes")
	static Triplet[] newUnit = {V1,V2,V3,V4,V5,V6,V7,V8,V9,V0,M1,M2,M3,M4,M5,L1,L2,L3,L4,L5};
	
	/*changes Unit to defined one*/
	/*if no Unit is set it will set it to UNDEF*/
	/*if the unit isn't defined  it will remain*/
	public void recalculate(final Recipe recipe ){
		
		
		//if no portion is set, assume that it is only for one person
		if(recipe.getOriginalServings() == 0){
			recipe.setOriginalServings(1);
		}

		final List<IngredientWithAmount> ings = recipe.getIngredients(); //list of all ingredients
		final List<IngredientWithAmount> recalculatedIngs = recipe.getIngredients(); //list of all ingredients
		
		for(final IngredientWithAmount tmp : ings){
			IngredientWithAmount newIng = tmp;
			if(tmp.getUnit() == Unit.UNDEF || tmp.getUnit() == Unit.PIECES){
				newIng = new IngredientWithAmount(tmp.getIngredient(), tmp.getQuantity() / (float)recipe.getOriginalServings(), tmp.getUnit());
				continue;
			}
			for(final Triplet triplet : newUnit) {
				if(tmp.getUnit() == triplet.getSecond()) {  //when unit of ingredient matches one in our array
					final float factor = (float) triplet.getFirst();
					final Unit newU = (Unit) triplet.getThird();
					newIng = new IngredientWithAmount(tmp.getIngredient(), tmp.getQuantity() * factor / (float) recipe.getOriginalServings(), newU);

				}
			}
			recalculatedIngs.add(newIng);
		}
		recipe.setIngredients(recalculatedIngs);
		
		changeUnit(recipe);
	}
	
	/*adjusts the amount of the ingredients and and changes to higher/lower unit if necessary*/
	public  void adjustPortion(final Recipe recipe, final int persons){
		
		final List<IngredientWithAmount> ings = recipe.getIngredients(); //list of all ingredients

		recipe.setIngredients(ings.stream().map(i -> new IngredientWithAmount(i.getIngredient(), i.getQuantity()*(float)persons, i.getUnit())).collect(Collectors.toList()));
		changeUnit(recipe);
	}
	
	/*changes Unit to next higher/lower one*/
	public  void changeUnit(final Recipe recipe){
		final List<IngredientWithAmount> ings = recipe.getIngredients(); //list of all ingredients
		final List<IngredientWithAmount> newIngs = new LinkedList<>();
		
		for(final IngredientWithAmount tmp : ings){
			Unit unit = tmp.getUnit();
			float quantity = tmp.getQuantity();
			if(tmp.getUnit() == Unit.GRAM){
				if(tmp.getQuantity() > 1000.0f){
					quantity = tmp.getQuantity() / 1000.0f;
					unit = Unit.KILOGRAM;
				}
				if(tmp.getQuantity() < 1.0f){
					quantity = tmp.getQuantity() * 1000.0f;
					unit = Unit.MILLIGRAM;
				}
			}else if(tmp.getUnit() == Unit.MILLILITERS){
				
				if(tmp.getQuantity() > 1000.0f){
					quantity = tmp.getQuantity() / 1000.0f;
					unit = Unit.LITERS;
				}
				
			}else{
				
				if(tmp.getQuantity() > 100.0f){
					quantity = tmp.getQuantity() / 100.0f;
					unit = Unit.METER;
				}
				if(tmp.getQuantity() < 1.0f){
					quantity = tmp.getQuantity() * 10.0f;
					unit = Unit.MILLIGRAM;
				}
				
			}
			newIngs.add(new IngredientWithAmount(tmp.getIngredient(), quantity, unit));
		}
					
	}
	
	/**
	 * Normalizes the given amount
	 * @param amount	The amount to normalize
	 * @return			Normalized amount
	 */
	@Nullable
	public Amount getNormalizedAmount(@Nullable Amount amount) {
		
		if(amount == null) return null;
		
		for(int j = 0; j< newUnit.length;j++ ){
			
			if(amount.getUnit() == newUnit[j].getSecond()){  //when unit of ingredient matches one in our array
				
				float factor = (float) newUnit[j].getFirst();
				amount.setQuantity(amount.getQuantity()*factor);
				
				Unit newU = (Unit) newUnit[j].getThird();
				amount.setUnit(newU);

			}	
		}
		
		return amount;
	}
	
	/**
	 * Normalizes the given string (lb->gramm,...) and adjusts the quantity
	 * @param str In format "Quantity Unit" 
	 * @return	Returns string with normalized quantity and unit
	 */
	@Nullable
	public String recalculateInString(@Nullable String str) {
		
		if(str == null) return str;
		
		str = str.toLowerCase();
		
		try {
			String[] terms = str.split(" ");
			
			float quantitiy = Float.parseFloat(terms[0]);
			
			Pattern unitPattern = Pattern.compile("(\\b[a-zA-Z]+)|(\\b([a-zA-Z]+)\\b)|([a-zA-Z]+)");
				
			Matcher m = unitPattern.matcher(terms[1]);
			
			m.find();
			String unitString = m.group();
			
			Unit unit = Unit.UNDEF.getUnitFromAbbreviation(unitString);
			
			if(unit == Unit.UNDEF || unit == Unit.PIECES) return str;
			
			Amount amount = new Amount(quantitiy, unit);
			
			str = getRecalculatedString(amount);
			
		} catch (Exception e) {return str;}
		
		return str;
	}
	
	/**
	 * Normalizes the given amount and passes it back as string
	 * @param amount	The amount to normalize
	 * @return			The amount as string
	 */
	private String getRecalculatedString(@Nonnull Amount amount) {
		return getNormalizedAmount(amount).toString().toLowerCase();
	}

}
