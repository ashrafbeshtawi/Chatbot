package de.dailab.oven.data_acquisition.parser;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dailab.oven.model.data_model.*;
import de.dailab.oven.recipe_services.nutrition_evaluator.NutritionsOnCall;
import zone.bot.vici.Language;

public class ChefkochParser{
	
	private static final Language LANGUAGE = Language.GERMAN;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ChefkochParser.class);
	
	/*
	 * Get HTML DOM with ingredients (chefkoch)
	 * @param url - String
	 * @return Document with downloaded HTML of input url
	 * @throws IOException 
	 */
	private static Document getHtml(final String url) {
		try {
			return Jsoup.connect(url).get();
		}
		catch (final IOException e) {
			LOGGER.info("Couldn't download HTML-Document from chefkoch");
			return null;
		}
	}

	
	/**
	 * Get recipe name
	 * @param doc downloaded HTML
	 * @return title of the recipe (including " von ... | Chefkoch")
	 */
	private static String getFullRecipeName(final Document doc){
		return doc.title();
	}
	
	
	/**
	 * Get number of servings
	 * @param doc - downloaded HTML
	 * @return number of servings
	 */
	private static int getNumberOfServings(final Document doc) {
//		works not every time
		return Integer.parseInt(doc.select("body>main>article>div>form>input").attr("value"));
	}
	
	/**
	 * parses instruction from downloaded HTML document
	 * @param doc downloaded HTML document
	 * @return list with steps
	 */
	private static List<String> getInstructionSteps(final Document doc) {
//		 	works not every time:	
		 final List<String> instructions = new ArrayList<>();
		 String instructionsAll =  doc.select("article>div.ds-box").get(1).toString().split("<div class=\"ds-box\">")[1].split("<\\/div>")[0];
		 
		 // set marker for Zeilenumbruch
		 instructionsAll = instructionsAll.replace("<br>", "-3-").replace("\n", "");		
		 final String[] insSplitted = instructionsAll.split("-3-");
		  
		 // discard empty lines
		 for (final String instruction: insSplitted){
		 		if(!(instruction.equals("") || instruction.equals(" ") || instruction.equals("  "))){
		 				instructions.add(instruction);
		 		}
		 }
		 return instructions;
		 
	}
	

	/**
	 *  parses duration from downloaded HTML document
	 * @param doc
	 * @param d
	 * @return Duration object d
	 */
	private static Duration getDuration(final Document doc, Duration d) {
		try{
			final String time = doc.select("span:contains(Gesamtzeit)").text().split(" ([a-zA-Z]+) ca. | Min.")[1];
			d = d.plusMinutes(Integer.parseInt(time));
			return d;
		} catch (final Exception e){
			return Duration.ZERO;
		}
	}
	
	
	/*
	 * parses amount from the given String
	 * @param amountString
	 * @return amount from String as double
	 */
	private static double getAmountFromString(final String amountString) {
		double a = 0.0;
		final Pattern slashPattern = Pattern.compile("(.+)/(.+)"); //detects a "/"
		/*
		 * String[] test  = "1 1/2 g".split(" [A-Za-z]+"); // "1 1/2" --> Einheit nicht zwangslaeufig noetig
		 * String[] test2 = "1 1/2 g".split("([0-9]| |/)+ "); // {" ", "g"} --> Einheit noetig!
		 */
		
		
		if(amountString.contains("n. B.")){
			return 0.0;
		}
		
		final String[] splitted = amountString.split(" ");
		for (String part: splitted) {
			
			if (part.equals("½")){
				part = "1/2";
			} else if (part.equals("¼")){
				part = "1/4";
			} else if (part.equals("¾")){
				part = "3/4";
			}
		
			// divide if we find a "/"
			if (slashPattern.matcher(part).matches()){
				final String[] fraction = part.split("/");
				final char firstCharacter = fraction[1].charAt(0);
				//Check if it's meant to be a fraction or a singular / plural thing (Zehe/n)
				if(Character.isDigit(firstCharacter)) {
					a += Math.round((Integer.parseInt(fraction[0])/(double)Integer.parseInt(fraction[1])) * 100d) / 100d;
				}
 			}
			
			else {
				final String decFraction = part.replace(",", ".");
				try {
					a += Math.round(Double.parseDouble(decFraction) * 100d) / 100d;
				} catch (final Exception e) {
					a += 0.0;
				}
			}
		}

		return a;
	}
	
		
	/**
	 * parses ingredients and their amount and unit from the given HTML doc
	 * @param doc downloaded HTML doc
	 * @return List of Ingredient/Amount pairs
	 */
	@Nullable
	private static  List<IngredientWithAmount> getIngredients(final Document doc) {
		final List<IngredientWithAmount> ingredients = new LinkedList<>();
		
		try{
			final Elements everything = doc.select("table.ingredients>tbody>tr>td");
			
			final Unit[] units = Unit.values();
			final List<String> unitsAllAbbreviations = new ArrayList<>();
			for(int i = 0; i < units.length; i++) {
				Collections.addAll(unitsAllAbbreviations, units[i].getUnitAbbrevations());
			}
			
			//initializing stuff for for-looop
			int counter = 1;
			String unit = "Stk";
			Amount amount = null;
			NutritionsOnCall noc = new NutritionsOnCall();
			Ingredient ing = null;
			
			for (final Element elem: everything){
				final String tmp = elem.text();
				
				//only reset everything if we added everything to the ingredients-list  in the iteration before
				if(counter%2 == 1){
					//reinitializing stuff
					unit = "Stk";
					amount = null;
					noc = new NutritionsOnCall();
					ing = null;
				}
				
				if (elem.hasClass("td-left")){ //Amount and Unit
					
					//DIVIDE STRING INTO SEPARATED AMOUNT AND UNIT
		    		//Check by chartAt(i) for number (inkl. / and -)
		    		int endAmount = 0;
		    		StringBuilder amountString = new StringBuilder();
		    		for(int i = 0; i < tmp.length(); i++) {
		    			final char character = tmp.charAt(i);
		    			if(!Character.isLetter(character)) {
		    				amountString.append(character);
		    				endAmount += 1;
		    			}
		    			else {
		    				break;
		    			}
		    		}
		    		//in case String unit2 is empty
		    		if(endAmount != tmp.length()) {
		    			unit = "";
		    			for(int i = endAmount; i < tmp.length(); i++) {
			    			final char character = tmp.charAt(i);
			    			unit += character;
			    		}
		    		}
		    		
					final double a = getAmountFromString(amountString.toString());
					
		    		if(a == 0.0) {
		    			unit = "undef";
		    		}
		    		else {
		    			if(!unitsAllAbbreviations.contains(unit)) {
		    				unit = "Stk";
		    			}	
		    		}
					amount = new Amount((float) a, unit);
					
					
				} else { //ingredient name
					ing = new Ingredient(tmp, noc.getNutritionsEntrys(tmp), LANGUAGE);
				}
				
				
				
				//only making a new Entry if we parsed ingredient name AND amount and unit
				if (ing != null && amount != null){
					ingredients.add(new IngredientWithAmount(ing, amount.getQuantity(), amount.getUnit()));
				}
				counter +=1;
			}
			
			return ingredients;
		} catch (final Exception e){
			return null;
		}
	}



	
	
	/*
	 * parses categories form downloaded HTML document
	 * @param tmp
	 * @return list with categories
	 */
	private static Set<String> getCategories(final Document doc) {
		final List<String> cats = doc.select("body>main>article>amp-carousel>div").eachText();
		final Set<String> categories = new HashSet<>();
		categories.addAll(cats);
		return categories;
	}
	
	
	/*
	 * parses the input url into a JSONObject
	 * @param url String
	 * @return Recipe Object
	 */
	public Recipe getRecipeFromUrl(final String url, final Recipe r) {
		final Document doc = getHtml(url);
		if(doc != null) {
			final Duration d = Duration.ZERO;

			try {
				return convertToRecipeObj(
						r, url, getFullRecipeName(doc),
						getNumberOfServings(doc),
						getIngredients(doc),
						getInstructionSteps(doc),
						getDuration(doc, d),
						getCategories(doc)
				);
			}
			catch (final Exception e) {
				LOGGER.info("Couldn't download HTML-Document from chefkoch");
				return null;
			}
		} 
		return null;
	}
	
	
	
	/*
	 * saves given parameters into Recipe r
	 * @param r Recipe-Object where data is saved into
	 * @param url url of recipe
	 * @param name name of recipe
	 * @param portions number of servings
	 * @param list 
	 * @param instructions
	 * @param duration
	 * @param categories
	 * @return filled in Recipe Object r
	 */
	private static Recipe convertToRecipeObj(final Recipe r, final String url, final String name, final int portions, final List<IngredientWithAmount> ingredients, final List<String> instructions, final Duration duration, final Set<String> categories) {
	
		r.setUrl(url);
		r.setName(name.split(" von ")[0]);
		try {
			r.setAuthor(name.split(" von ")[1]);
		}
		catch(final Exception e) {
			r.setName(name.split(" \\|")[0]);
			r.setAuthor("Chefkoch");
		}
		r.addDurationToListOfDurations("total", duration);
		r.setInstructions(instructions);
		
        Set<Category> catSet = new HashSet<>();
        categories.forEach(c -> catSet.add(new Category(c)));
        r.setCategories(catSet);
        
		r.setOriginalServings(portions);		
		r.setIngredients(ingredients);
				
		return r;
	}
	
	
	
	
	
	/**
	 * puts given data into JSON
	 * @param url - String
	 * @param name - String
	 * @param ingredients - standardized list of lists
	 * @param instructions - list of instruction steps
	 * @return JSONObject
	 */
	private static JSONObject convertToJson(final String url, final String name, final List<IngredientWithAmount> ingredients, final List<String> instructions, final Duration duration, final Set<String> set) {
//		Duration is missing here
		final JSONObject recipe = new JSONObject(); //whole JSON in the end
		final JSONArray allIngs = new JSONArray();
		final JSONArray allInst = new JSONArray();
		final JSONArray allCats = new JSONArray();
		JSONObject singleIng;
		
		// add each ingredient (with amount and unit) to JSONArray
		for (final IngredientWithAmount ing: ingredients) {
			singleIng = new JSONObject();

			singleIng.put("name",ing.getIngredient().getName());
			singleIng.put("amount", ing.getQuantity());
			singleIng.put("unit", ing.getUnit());
			
			allIngs.put(singleIng);
		}
		
		// add all baking steps to the instruction JSONArray
		for (final String step: instructions) {
			allInst.put(step);
		}
		
		// add all baking steps to the instruction JSONArray
		for (final String cat: set) {
			allCats.put(cat);
		}

		recipe.put("url", url);
		recipe.put("author", (name.split("(.)+ von ")[1]).split(" \\|(.)+")[0] + ", Chefkoch");
		recipe.put("name", name.split(" von(.)+")[0]);
		recipe.put("ingredients", allIngs);
		recipe.put("instructions", allInst);
		recipe.put("categories", allCats);
		recipe.put("duration", duration);
		
		return recipe;
	}
	
	/*
	 * parses the input url into a JSONObject
	 * @param url - String
	 * @return JSONObject with recipe structure  
	 */
	public static JSONObject getJsonFromUrl(final String url) {
		final Document doc = getHtml(url);
		if (doc != null){
			final Duration d = Duration.ZERO;
			try{
				return convertToJson(url, getFullRecipeName(doc), getIngredients(doc), getInstructionSteps(doc), getDuration(doc, d), getCategories(doc));
			} catch (final Exception e){
				LOGGER.info("Couldn't parse HTML-Document from chefkoch");
				return null;
			}
			
		} else {
			return null;
		}
	}
		
}