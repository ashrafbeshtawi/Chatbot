package de.dailab.oven.recipe_services.common;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.dailab.oven.model.data_model.*;
import zone.bot.vici.Language;

public class RecipePrinter {

	private static final String UNKOWN = "Unknown";
	private static final String NEW_LINE = System.lineSeparator();
	private static final String SEPERATOR = "---------";
	
	private RecipePrinter() { }
	
	public static String recipeToString(@Nonnull final Recipe recipe) {
		final StringBuilder sb = new StringBuilder();
		printName(sb, recipe.getName());
		printID(sb, recipe.getId());
		printAuthor(sb, recipe.getAuthor());
		printLanguage(sb, recipe.getLanguage());
		printFoodLabel(sb, recipe.getFoodLabel().toString());
		printOriginalServings(sb, recipe.getOriginalServings());
		printUrl(sb, recipe.getUrl());
		printCategories(sb, recipe.getCategories());
		printDuration(sb, recipe.totalDuration());
		printDurations(sb, recipe.getDurations());
		printIngredients(sb, recipe.getIngredients());
		printInstructions(sb, recipe.getInstructions());
		printUserRatings(sb, recipe.getUserRatings());
		return sb.toString();
	}
	
	/**
	 * Prints "Author: author"
	 * @param author Author string
	 */
	private static void printAuthor(@Nonnull final StringBuilder sb, @Nullable String author) {
		if(author == null || author.contentEquals("")) {
			author = UNKOWN;
		}
		sb.append("Author: ").append(author);
		sb.append(NEW_LINE);
	}
	
	/**
	 * Prints "Recipe name: recipeName"
	 * @param recipeName recipe name string
	 */
	private static void printName(@Nonnull final StringBuilder sb, @Nullable String recipeName) {
		if(recipeName == null || recipeName.contentEquals("")) {
			recipeName = UNKOWN;
		}
		sb.append("Recipe name: ").append(recipeName);
		sb.append(NEW_LINE);
	}
	
	/**
	 * Prints "Food label: foodLabel"
	 * @param foodLabel food label String
	 */
	private static void printFoodLabel(@Nonnull final StringBuilder sb, @Nullable String foodLabel) {
		if(foodLabel == null || foodLabel.contentEquals("")) {
			foodLabel = FoodLabel.UNDEF.toString();
		}
		sb.append("Food label: ").append(foodLabel);
		sb.append(NEW_LINE);
	}
	
	/**
	 * Prints "ID: id"
	 * @param id type long
	 */
	private static void printID(@Nonnull final StringBuilder sb, final long id) {
		sb.append("ID: ").append(id);
		sb.append(NEW_LINE);
	}
	
	/**
	 * Prints "Language: language"
	 * @param language language string
	 */
	private static void printLanguage(@Nonnull final StringBuilder sb, @Nullable Language language) {
		if(language == null) {
			language = Language.UNDEF;
		}
		sb.append("Language: ").append(language.getName());
		sb.append(NEW_LINE);
	}
	
	/**
	 * Prints "Original Servings: originalServings"
	 * @param originalServings integer of original servings
	 */
	private static void printOriginalServings(@Nonnull final StringBuilder sb, final int originalServings) {
		sb.append("Original servings: ").append(originalServings);
		sb.append(NEW_LINE);
	}
	
	/**
	 * Prints "URL: url"
	 * @param url URL as string
	 */
	private static void printUrl(@Nonnull final StringBuilder sb, @Nullable String url) {
		if(url == null) {
			url = "";
		}
		sb.append("URL: ").append(url);
		sb.append(NEW_LINE);
	}
	
	/**
	 * Prints:
	 * "---------
	 * Categories:
	 * category1
	 * category2
	 * ---------"
	 * @param categories List of categories
	 */
	private static void printCategories(@Nonnull final StringBuilder sb, @Nullable Set<Category> categories) {
		sb.append(SEPERATOR);
		sb.append("Categories:");
		if(categories == null) {
			categories = new HashSet<>();
		}
		for(final Category category : categories) {
			sb.append(category);
		}
		sb.append(SEPERATOR);
		sb.append(NEW_LINE);
	}
	
	/**
	 * Prints "Total duration: 1h 30min"
	 * @param duration recipe duration (total)
	 */
	private static void printDuration(@Nonnull final StringBuilder sb, @Nullable Duration duration) {
		if(duration == null) {
			duration = Duration.ZERO;
		}
		sb.append(generateDurationString("Total duration", duration));
		sb.append(NEW_LINE);
	}
	
	/**
	 * Prints:
	 * "---------
	 * Durations:
	 * duration1
	 * duration2
	 * ---------"
	 * @param durations Map of durations
	 */
	private static void printDurations(@Nonnull final StringBuilder sb, @Nullable Map<String, Duration> durations) {
		if(durations == null) {
			durations = new HashMap<>();
		}
		sb.append(SEPERATOR);
		sb.append("Durations:");
		String key = "";
		Duration value;
		for(final Map.Entry<String, Duration> entry : durations.entrySet()) {
			key = entry.getKey();
			if (key == null || key.contentEquals("")) {
				key = UNKOWN;
			}
			value = entry.getValue();
			if(value == null) {
				value = Duration.ZERO;
			}
			sb.append(generateDurationString(key, value));
		}
		sb.append(SEPERATOR);
		sb.append(NEW_LINE);
	}
	
	/**
	 * @param durationName name of duration
	 * @param duration duration itself
	 * @return String "durationName: xh ymin"
	 */
	private static String generateDurationString (@Nonnull final String durationName, @Nonnull Duration duration) {
		String printString = durationName + ": ";
		if(duration.toHours() != 0) {
			printString = printString + Long.toString(duration.toHours()) + "h ";
			duration = duration.minusHours(duration.toHours());
		}
		printString = printString + Long.toString(duration.toMinutes()) + "min";
		return printString;
	}
	
	/**
	 * Prints:
	 * "---------
	 * Ingredients:
	 * ingredient1 0 mg [Nutrition]
	 * intregient2 2 ml [Nutrition]
	 * ---------"
	 * @param ingredients map of ingredients with their amounts
	 */
	private static void printIngredients(@Nonnull final StringBuilder sb, @Nullable final List<IngredientWithAmount> ingredients) {
		sb.append(SEPERATOR);
		sb.append("Ingredients:");
		if(ingredients != null) {
			for(final IngredientWithAmount entry : ingredients) {
				final Ingredient ingredient = entry.getIngredient();
				if(ingredient.getName().contentEquals("")) {
					continue;
				}
				sb.append(ingredient.getName()).append(" ").append(entry.getQuantity()).append(" ").append(entry.getUnit().name().toLowerCase()).append(" Nutrition: ").append(ingredient.getNutrition());
			}			
		}
		sb.append(SEPERATOR);
		sb.append(NEW_LINE);
	}
	
	/**
	 * Prints:
	 * "---------
	 * Instructions:
	 * instructionStep1
	 * instructionStep1
	 * ---------"
	 * @param instructions list of instructions
	 */
	private static void printInstructions(@Nonnull final StringBuilder sb, @Nullable List<String> instructions) {
		if(instructions == null) {
			instructions = new ArrayList<>();
		}
		sb.append(SEPERATOR);
		sb.append("Instructions:");
		for(final String instructionStep : instructions) {
			sb.append(instructionStep);
		}
		sb.append(SEPERATOR);
		sb.append(NEW_LINE);
	}
	
	/**
	 * Prints:
	 * "---------
	 * User ratings:
	 * User-ID: x Rating: y
	 * User-ID: z Rating: a
	 * ---------"
	 * @param userRatings list of user ratings
	 */
	private static void printUserRatings(@Nonnull final StringBuilder sb, @Nullable Map<Long, Integer> userRatings) {
		if (userRatings == null) {
			userRatings = new HashMap<>();
		}
		sb.append(SEPERATOR);
		sb.append("User ratings:");
		long key;
		int value;
		for(final Map.Entry<Long, Integer> entry : userRatings.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			sb.append("User-ID: ").append(key).append(" Rating: ").append(value);
		}
		sb.append(SEPERATOR);
		sb.append(NEW_LINE);
	}
}
