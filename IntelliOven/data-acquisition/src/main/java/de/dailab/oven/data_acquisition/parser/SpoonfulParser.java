package de.dailab.oven.data_acquisition.parser;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.dailab.oven.model.data_model.*;
import de.dailab.oven.recipe_services.nutrition_evaluator.NutritionsOnCall;
import zone.bot.vici.Language;

public class SpoonfulParser {
	
	private static final Language language = Language.ENGLISH;
	private static final Logger LOGGER = Logger.getLogger(SpoonfulParser.class.getName());

	/*
	 * Get HTML DOM with ingredients 
	 * @param url - String
	 * @return Document with downloaded HTML of input url
	 * @throws IOException 
	 */
	private static Document getHtml(final String url) throws IOException{
		return Jsoup.connect(url).get();
	}
	
	
	/**
	 * Get recipe name
	 * @param doc - downloaded HTML
	 * @return title of the recipe 
	 */
	private static String getFullRecipeName(final Document doc){
		return doc.title();
	}
	
	
	/**
	 * Get number of servings
	 * @param doc - downloaded HTML
	 * @return number of servings
	 */
	private static int getNumberOfServings(final Document doc){
		return Integer.parseInt(doc.select("span.wprm-recipe-servings-adjustable-tooltip").text()); 
	}
	
	
	/**
	 * parses instruction from downloaded HTML document
	 * @param doc - downloaded HTML document
	 * @return list with steps
	 */
	private static List<String> getInstructionSteps(final Document doc) {
		return doc.select("li.wprm-recipe-instruction").eachText();
	}
	
	/**
	 * saves preparation time and portions in given Duration object
	 * @param doc downloaded HTML document
	 * @param d Duration object
	 * @return filled Duration object
	 */
	private static Duration getDuration(final Document doc, Duration d){
		final String time = doc.selectFirst("span.wprm-recipe-total_time-minutes").text();
		d = d.plusMinutes(Integer.parseInt(time));
		return d;
	}
	
	/*
	 * parses amount from the given String
	 * @param amountString
	 * @return amount from String as double
	 */
	private static double getAmountFromString (final String amountString)
	{
		final Pattern slashPattern = Pattern.compile("(.+)/(.+)"); //detects a "/"
	
		double regAmount = 0.0;


		// set amount, convert into float if "/" is present
		final String[] splitted = amountString.split(" ");
		for (String part: splitted) {
			
			try{
				part = part.split("-")[0];
			
				if(part.length() == 1){
					if (part.equals("½")){
						part = "1/2";
					} else if (part.equals("¼")){
						part = "1/4";
					} else if (part.equals("¾")){
						part = "3/4";
					}
				}
	
				// divide if we find a "/"
				if (slashPattern.matcher(part).matches()){
					final String[] fraction = part.split("/");
					regAmount += Math.round((Integer.parseInt(fraction[0])/(double)Integer.parseInt(fraction[1])) * 100d) / 100d;
				}
				
				else {
					regAmount += parseDecFraction(part);

				}
			} catch (final ArrayIndexOutOfBoundsException e){
				LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
			}
		}
		return regAmount;
	}
	
	private static double parseDecFraction(String str) {
		final String decFraction = str.replace(",", ".");
		try {
			return Math.round(Double.parseDouble(decFraction) * 100d) / 100d;
		} catch (final Exception e) {
			return 0.0;
		}
	}
	
	/**
	 * parses ingredients and their amount and unit from the given HTML doc
	 * @param doc downloaded HTML doc
	 * @return List of Ingredient/Amount pairs
	 */
	private static List<IngredientWithAmount> getIngredients(final Document doc){
		final List<IngredientWithAmount> ingredients = new LinkedList<>();
		final Elements everything = doc.select("li.wprm-recipe-ingredient");
		
		final Unit[] units = Unit.values();
		final List<String> unitsAllAbbreviations = new ArrayList<>();
		for(int i = 0; i < units.length; i++) {
			Collections.addAll(unitsAllAbbreviations, units[i].getUnitAbbrevations());
		}
		
		for (int i = 1; i < everything.size(); i++){
			final Element listelem = everything.get(i);
			
			// parse Amount
			final float amount = (float) getAmountFromString(listelem.select("span.wprm-recipe-ingredient-amount").text());
			String unit = listelem.select("span.wprm-recipe-ingredient-unit").text();
    		if(amount == 0.0) {
    			unit = "undef";
    		}
    		else {
    			if(!unitsAllAbbreviations.contains(unit)) {
    				unit = "Pcs";
    			}	
    		}

			//parse Ingredient
			final NutritionsOnCall noc = new NutritionsOnCall();
			final String ingred = listelem.select("span.wprm-recipe-ingredient-name").text();
			final Ingredient ing = new Ingredient(ingred, noc.getNutritionsEntrys(ingred), language);
			
			ingredients.add(new IngredientWithAmount(ing, amount, Unit.valueOf(unit)));
			
		}
		
		return ingredients;
	}
	
	/*
	 * parses categories form downloaded HTML document
	 * @param tmp
	 * @return list with categories
	 */
	private static Set<String> getCategories(final Document doc) {
		final Set<String> cats = new HashSet<>();
		try {
			final List<Element> listFromDocument = doc.select("footer").select(".left").select("a");
			for(final Element elem : listFromDocument) {
				cats.add(elem.text());
			}			
		} catch (Exception e) {
			LOGGER.log(Level.INFO, e.getLocalizedMessage(), e.getCause());
		}
		return cats;
	}
	
	
	/*
	 * parses the input url into a JSONObject
	 * @param url - String
	 * @return Recipe Object
	 */
	public Recipe getRecipeFromUrl(final String url, final Recipe r) throws IOException {
		final Document doc = getHtml(url);
		final Duration d = Duration.ZERO;
		
		try{
			final String name 								= getFullRecipeName(doc);
			final int servings 								= getNumberOfServings(doc);
			final List<IngredientWithAmount> ingredients 		= getIngredients(doc);
			final List<String> instructions					= getInstructionSteps(doc);
			final Duration duration 							= getDuration(doc, d);
			final Set<String> categories 		        		= getCategories(doc);
			
			return convertToRecipeObj(r, url, name, servings, ingredients, instructions, duration, categories);
			
		} catch (final Exception e){
			return null;
		}
				
	}
	
	/*
	 * saves given parameters into Recipe r
	 * @param r Recipe-Object where data is saved into
	 * @param url url of recipe
	 * @param name name of recipe
	 * @param portions number of servings
	 * @param ingredients 
	 * @param instructions
	 * @param duration
	 * @param categories
	 * @return
	 */
	private static Recipe convertToRecipeObj(final Recipe r, final String url, final String name, final int portions, final List<IngredientWithAmount> ingredients, final List<String> instructions, final Duration duration, final Set<String> categories) {
		
		r.setUrl(url);
		r.setName(name.split(" - ")[0]);
		r.setAuthor(name.split(" - ")[1]);
		r.addDurationToListOfDurations("total", duration);
		r.setInstructions(instructions);
        Set<Category> catSet = new HashSet<>();
        categories.forEach(c -> catSet.add(new Category(c)));
        r.setCategories(catSet);
		r.setIngredients(ingredients);
		r.setOriginalServings(portions);
		
		return r;
	}
	
}
