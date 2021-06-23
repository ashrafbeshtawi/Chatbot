package de.dailab.oven.data_acquisition.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.model.data_model.*;
import zone.bot.vici.Language;

/**
 * Parses recipes from the recipe dataset provided by Arçelik.
 *
 * @author Hendrik Motza
 * @since 18.09
 */
public class ArcelikParser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ArcelikParser.class);
	
    private static final String[] supportedLocales = {"de-DE", "en-GB", "tr-TR"};
    private static final String[] ingredientHeadlines = {"Zutaten", "Ingredients", "Malzemeler"};
    private static final String[] instructionsHeadlines = {"Zubereitung", "Directions", "Direction", "Yapılışı"};

    private static final List<String> notVegetarianCategories = Arrays.asList("meat", "seafood", "poultry");
    private static final List<String> notVegetarianIngredients = Arrays.asList("sucuk", "salam", "sosis", "köfte", "kýyma", "kıyma", "eti", "levrek", "hamsi", "somon");
    
    private static final List<String> notVeganIngredients = Arrays.asList("süt", "dondurma", "yoðurt", "yoðurdu", "yoğurt", "yoğurdu", "peynir", "peyniri", "tereyaðý", "tereyağı", "bal", "balý", "balı");
    
    private static final Pattern servingsPattern = Pattern.compile("^(\\d*)(-\\d*)? ");
    private static final Pattern durationPattern = Pattern.compile("^((\\d*) (h|hour|hours|stunde|stunden|saat))? ?((\\d*) (min|m|minute|minutes|minuten|dk))?");

    protected static final List<String> LIST_NAMES = new ArrayList<>();

   
    
    private ArcelikParser() {}
    
    
    static Recipe parseRecipeFromJson(final JSONObject recipeNode, final String url, final String filename, final File image)
            throws RecipeParserException {
        Recipe recipe = new Recipe();
        try {
            boolean vegetarian = true;
        	boolean vegan = true;
            recipe.setAuthor("Arçelik");
            final String language = filename.substring(filename.length()-10, filename.length()-8);
            recipe.setLanguage(Language.getLanguage(language.toLowerCase()));
        	recipe.setImageFile(image);
            if (url != null) {
                recipe.setUrl(url);
            }
            final JSONObject infoNode = recipeNode.getJSONObject("info");
            recipe.setName(infoNode.getString("headerText"));
            if(recipe.getName().toLowerCase().contentEquals("hamur")) {
            	LOGGER.debug(filename);
            }
            recipe = setOriginalServings(recipe, infoNode);
            String wfa = infoNode.getString("wfa");
            StringBuilder ovenSetting;
            int counter = 0;
            while(!wfa.isEmpty()) {
            	ovenSetting = new StringBuilder();
            	boolean noTemp = true;
            	for(int i = 0; i < 8; i++) {
            		if(i > 0) {
            			ovenSetting.append(",");
            		}
            		ovenSetting.append(wfa.split(",", 2)[0]);
            		final String[] all = ovenSetting.toString().split(",");
            		final String current = all[all.length - 1];
            		if(i == 5 && !current.contentEquals("0")){
            				noTemp = false;
            		}
            		if(i == 6 && noTemp && !current.contentEquals("0")) {
            				noTemp = false;
            		}
            		wfa = cutWfa(wfa);
            	}
            	if(ovenSetting.toString().startsWith("0") || noTemp) {
            		ovenSetting = new StringBuilder();
            	}
            	recipe.addOvenSetting(counter, ovenSetting.toString());
            	counter += 1;
            }
            final int duration = parseDuration(infoNode.getString("duration"));
            final java.time.Duration dur = java.time.Duration.ZERO.plusMinutes(duration);
            recipe.addDurationToListOfDurations("total", dur);
            final JSONArray categoriesNode = infoNode.getJSONArray("categories");
            final Set<String> categories = new HashSet<>(categoriesNode.length());
            for(int i=0; i<categoriesNode.length(); i++) {
                categories.add(categoriesNode.getString(i).replace("RECIPE_CATEGORY_", ""));
            }
            
            Set<Category> catSet = new HashSet<>();
            categories.forEach(c -> catSet.add(new Category(c)));
            recipe.setCategories(catSet);
            final JSONArray contentNode = recipeNode.getJSONArray("content");
            final List<JSONObject> ingredientNodes = getObjectsAfterHeaderObjectWithTitle(ingredientHeadlines, contentNode);
            final List<IngredientWithAmount> ingredients = new LinkedList<>();
            for(final JSONObject ingredientBaseNode : ingredientNodes) {
                final JSONArray ingredientsNode = ingredientBaseNode.optJSONArray("ul");
                if(ingredientsNode == null) {
                    continue;
                }
                for(int i=0; i<ingredientsNode.length(); i++) {
                    ingredients.addAll(ParserUtils.parseIngredient(ingredientsNode.getString(i), recipe.getLanguage().getLangCode2()));
                }
            }
            recipe.setIngredients(ingredients);
            final List<IngredientWithAmount> ingreds = recipe.getIngredients();
            if(recipe.getLanguage() == Language.TURKISH) {
            	if(categories.isEmpty()) {
            		for(final IngredientWithAmount ingred : ingreds) {
            			final String ing = ingred.getIngredient().getName().toLowerCase();
            			final String[] i = ing.split(" ");
            			for(String in : i) {
            				in = in.toLowerCase();
            				if(notVeganIngredients.contains(in)) {
            					vegan = false;
            				}
            				if(notVegetarianIngredients.contains(in)) {
            					vegetarian = false;
            					vegan = false;
            				}
            			}
            		}
            	}
            	else{
            		
            		vegLoop:
            			for(String category : categories) {
            				category = category.toLowerCase();
            				for(final IngredientWithAmount ingred : ingreds) {
            					final String ing = ingred.getIngredient().getName().toLowerCase();
            					final String[] i = ing.split(" ");
            					for(String in : i) {
            						in = in.toLowerCase();
            						if(notVeganIngredients.contains(in)) {
            							vegan = false;
            						}
            						if(notVegetarianIngredients.contains(in)  || notVegetarianCategories.contains(category)) {
            							vegetarian = false;
            							vegan = false;
            						}
            						if(!vegan) {
            							break vegLoop;
            						}
            					}
            				}
            			}
            	}
            }

            if(vegetarian) {
            	recipe.addCategory(new Category("vegetarian"));
            	if(vegan) {
            		recipe.addCategory(new Category("vegan"));
            	}
            }
            
            final List<JSONObject> instructionsNodes = getObjectsAfterHeaderObjectWithTitle(instructionsHeadlines, contentNode);
            final JSONArray stepsNode = instructionsNodes.get(0).getJSONArray("ol");
            final List<String> steps = new ArrayList<>(stepsNode.length());
            for(int i=0; i<stepsNode.length(); i++) {
                steps.add(stepsNode.getString(i));
            }
            recipe.setInstructions(steps);
        } catch (final JSONException jsonException) {
            throw new RecipeParserException("Could not parse recipe", jsonException);
        }
        return recipe;
    }


	private static String cutWfa(String wfa) {
		try {
			wfa = wfa.split(",", 2)[1];                			
		} catch (final Exception e) {
			wfa = "";
		}
		return wfa;
	}


	private static Recipe setOriginalServings(final Recipe recipe, final JSONObject infoNode) {
		try {
		    final int servings = parseServings(infoNode.getString("servings"));
		    recipe.setOriginalServings(servings);
		} catch (final RecipeParserException e) {
			recipe.setOriginalServings(1);
		}
		return recipe;
	}

    private static List<JSONObject> getObjectsAfterHeaderObjectWithTitle(final String[] titles, final JSONArray parentNode)
            throws RecipeParserException {
        for(final String title : titles) {
            try {
                return getObjectsAfterHeaderObjectWithTitle(title, parentNode);
            }
            catch (final RecipeParserException e) {
                // nothing to do
            }
        }
        throw new RecipeParserException("Could not find headline entry");
    }

    private static List<JSONObject> getObjectsAfterHeaderObjectWithTitle(final String title, final JSONArray parentNode)
            throws RecipeParserException {
        boolean headLineFound = false;
        final List<JSONObject> results = new LinkedList<>();
        for(int i=0; i<parentNode.length(); i++) {
            final JSONObject jsonObject = parentNode.optJSONObject(i);
            if(jsonObject == null) {
                continue;
            }
            if(jsonObject.has("hd")) {
                if(headLineFound) {
                    return results;
                }
                else if(jsonObject.getString("hd").startsWith(title)) {
                    headLineFound = true;
                }
            }
            else if(headLineFound) {
                results.add(jsonObject);
            }
        }
        if(!headLineFound) {
            throw new RecipeParserException("Could not find headline entry for title: "+title);
        }
        return results;
    }

    private static int parseServings(final String input)
            throws RecipeParserException {
        final Matcher servingsMatcher = servingsPattern.matcher(input);
        if (!servingsMatcher.find()) {
            throw new RecipeParserException("Could not parse the amount of servings");
        }
        final String servingsGroup;
        try {
            servingsGroup = servingsMatcher.group(1);
        } catch (final IndexOutOfBoundsException e) {
            throw new RecipeParserException("Could not parse the amount of servings", e);
        }
        int servings = Integer.parseInt(servingsGroup);
        if(servings == 0) {
        	servings = 1;
        }
        return servings;
    }

    private static int parseDuration(final String input)
            throws RecipeParserException {
        final Matcher durationMatcher = durationPattern.matcher(input.toLowerCase());
        if (!durationMatcher.find()) {
            throw new RecipeParserException("Could not parse the cooking duration");
        }
        int duration = 0;
        final String hourGroup = durationMatcher.group(2);
        final String minuteGroup = durationMatcher.group(5);
        if(hourGroup!=null) {
            duration += Integer.parseInt(hourGroup)*60;
        }
        if(minuteGroup!=null) {
            duration += Integer.parseInt(minuteGroup);
        }
        return duration;
    }

    private static Recipe parseRecipeFile(final File file, final File image)
            throws RecipeParserException {
        final StringBuilder sb = new StringBuilder();
        try(final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            sb.append(br.readLine());
            return parseRecipeFromJson(new JSONObject(sb.toString()), null, file.getName(), image);
        }
        catch (final RecipeParserException | JSONException | IOException e) {
            throw new RecipeParserException("Could not parse recipe from file: "+file.getAbsolutePath(), e);
        }
    }

    public static List<Recipe> parseRecipesInDirectory(final File directory)
            throws InputException {
        final List<Recipe> recipes = new LinkedList<>();
        parseRecipesInDirectory(directory, recipes);
        return recipes;
    }

    private static List<RecipeParserException> parseRecipesInDirectory(final File directory, final List<Recipe> recipes) throws InputException {
        final List<RecipeParserException> exceptions = new LinkedList<>();
        for(final File file : directory.listFiles()) {
            if(file.isDirectory()) {
                parseRecipesInDirectory(file, recipes);
            }
            else if(file.isFile()) {
                String name = file.getName();
                if(!name.endsWith(".json") || name.startsWith(".")) {
                	continue;
                }
                name = name.substring(0,name.length()-5);
                for(final String supportedLocale : supportedLocales) {
                    if(name.endsWith(supportedLocale)) {
                        try {
                        	final Pattern pattern = Pattern.compile("\\d+");
                        	final Matcher matcher = pattern.matcher(name);
                        	int number = -1;
                        	while(matcher.find()) {
                        		number = Integer.parseInt(matcher.group());
                        	}
                        	if(number == -1) {
                        		throw new InputException("Didn't work");
                        	}
                        	final File image = new File(file.getParent() + File.separator + "recipe_full-recipe_" + number + "-tft.png");
                            recipes.add(parseRecipeFile(file, image));
                        } catch (final RecipeParserException e) {
                            exceptions.add(e);
                        }
                    }
                }
            }
        }
        return exceptions;
    }
}