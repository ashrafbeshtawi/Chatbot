package de.dailab.oven.api.interfaces.recipe.api;


import de.dailab.oven.api.helper.serialization.ErrorHandler;
import de.dailab.oven.api.interfaces.recipe.RecipeController;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.api_common.recipe.RecipeRequest;
import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.database.query.ImageQuery;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.data_model.FoodLabel;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.filters.RecipeFilter;
import de.dailab.oven.model.database.NodeLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLConnection;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("oven/recipe")
public class RecipeHTTP {

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(RecipeHTTP.class);

    @Autowired
    private SimpMessagingTemplate template;

    /**
     * r00
     * Make requests to the DB to get Dishes
     *
     * @param requestObject JSON as class RecipeFilter
     * @return List of Recipes
     */
    @PostMapping(value = "/get")
    public ResponseEntity get(@RequestBody final RecipeRequest requestObject) {

        try {
            return ResponseEntity.status(HttpStatus.OK).body(RecipeController.getRecipes(requestObject));
        } catch (final ResponseException e) {
            return ErrorHandler.get(e.getStatus(), e.getResponse().getMessage());
        }
    }

    @PostMapping(value = "/add")
    public ResponseEntity add(@RequestBody final Recipe recipe) {
        try {
            new Query().putSingleRecipe(recipe);
            return ResponseEntity.status(HttpStatus.OK).body("");
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            return ErrorHandler.get(500, e.getMessage());
        }
    }

    @GetMapping(value = "/image/{recipeID}")
    public ResponseEntity getRecipeImage(@PathVariable final long recipeID) {
        try {
            final File imageFile = new ImageQuery(DatabaseController.getInstance().getQuery().getGraph()).loadImageFile(recipeID, NodeLabel.RECIPE);
            final String mimeType = URLConnection.guessContentTypeFromName(imageFile.getName());
            final HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType(mimeType));
            return new ResponseEntity(new InputStreamResource(new FileInputStream(imageFile)), responseHeaders, HttpStatus.OK);
        } catch (final FileNotFoundException e) {
            LOG.error("Recipe with id '" + recipeID + "' should have an image but the file is missing", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (final Exception e){
            LOG.error(e.getMessage());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * r99
     * remove, only used for debugging
     *
     * @return remove, only used for debugging
     */
    @PostMapping(value = "/getExample")
    public ResponseEntity getRecipeFilter(@RequestBody final RecipeRequest requestObject) {

        //Set filter if not set, only done to show the default...
        if (requestObject.getRecipeFilter() == null) {
            final RecipeFilter rf = new RecipeFilter();

            final Set<Language> recipeLanguages = new HashSet<>();
            recipeLanguages.add(Language.GERMAN);
            rf.setRecipeLanguages(recipeLanguages);
            rf.setRecipeName("chicken");
            rf.addRequiredCategory(new Category("VEGAN"));
            rf.addExcludedCategory(new Category("MEAT"));
            rf.addPossibleAuthor("John Doe");
            rf.addRequiredIngredient(new Ingredient("cheese", Language.ENGLISH));
            rf.addPossibleIngredient(new Ingredient("mozerella", Language.ENGLISH));
            rf.addExcludedIngredient(new Ingredient("ham", Language.ENGLISH));
            rf.addPossibleAuthor("Hendrik");
            rf.addExcludedAuthor("Sahin");
            rf.setCookedWithin(Duration.ofHours(2));
            rf.setIsFoodLabel(FoodLabel.GREEN);
            rf.setRecipeId(100l);
            rf.setMaxNumberOfRecipesToParsePerLanguage(100);
            rf.setOriginalServings(4);

            requestObject.setRecipeFilter(rf);
            requestObject.setPersons(5);
            requestObject.setCollaborativeRecommendation(true);
        }

        return ResponseEntity.status(HttpStatus.OK).body(requestObject);

    }

}
