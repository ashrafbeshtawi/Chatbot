package de.dailab.oven.controller;

import de.dailab.oven.api_common.error.ErrorResponse;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.api_common.recipe.RecipeRequest;
import de.dailab.oven.api_common.recipe.RecipeResponse;
import de.dailab.oven.api_common.user.UserObj;
import de.dailab.oven.database.backup.listeners.BackupHandler;
import de.dailab.oven.database.backup.listeners.CriticalDatabaseStateListener;
import de.dailab.oven.database.backup.listeners.CriticalStateObserver;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.Category;
import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.Recipe;
import de.dailab.oven.model.data_model.User;
import de.dailab.oven.model.data_model.filters.RecipeFilter;
import de.dailab.oven.recipe_services.common.RecipeSorter;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nonnull;

public class DatabaseController {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseController.class);

    private static DatabaseController singleInstance = null;
    @Nonnull
    private static final BackupHandler BACKUP_HANDLER = BackupHandler.getInstance();
    @Nonnull
    private static final CriticalDatabaseStateListener CDSL = CriticalStateObserver.getInstance();
    private Query query;

    private DatabaseController() {
        try {
            //always stay connected to DB
			this.query = new Query();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
        BACKUP_HANDLER.setCriticalDatabaseStateListener(CDSL);
        BACKUP_HANDLER.startHandling();
        //If a message shall be shown when backup handling does not work since of invalid configuration ask for it with BACKUP_HANDLER.isWorking()
    }

    public static DatabaseController getInstance() {
        if (singleInstance == null)
            singleInstance = new DatabaseController();

        return singleInstance;
    }

    /*
     * Handles requests to get Recipes, Recommending, Data-acquisition...
     *
     * @param requestObject RecipeRequest object
     * @return returns a List of Recipes
     */
    public List<Recipe> getRecipes(final RecipeRequest requestObject) throws Exception {

        //check if empty
        if (requestObject.getRecipeFilter() == null) {
            throw new IllegalArgumentException("RecipeFilter can't be empty");
        }

        User user = null;

        // ADD USERS PREFERENCES (if possible)
        try {
            user = getUser(requestObject);
        } catch (final ResponseException e) {
            LOG.error(e.getMessage(), e);
        }

        final List<Recipe> recipeResult;
        final RecipeFilter filter = requestObject.getRecipeFilter();

        //add user preference
        addUserPreference(filter, user);

        //Recommendation
        if (user != null && (requestObject.isCollaborativeRecommendation() || requestObject.isContentBasedRecommendation())) {
            //rate
            LOG.info("Requesting 'recommendation' for user {} [id={}] ", user.getName(), user.getId());
            final List<Recipe> recipes = dbRequest(filter);
            recipeResult = new RecipeSorter().sortRecipesByRatings(recipes, user);

        } else {
            if (user == null)
                LOG.info("Unregistered user, requesting recipes directly from database...");
            //try to get Recipe, can throw error...
            final List<Recipe> recipes = dbRequest(filter);
            recipeResult = new RecipeSorter().sortRecipesByRatings(recipes, user);
        }
        LOG.info("Found {} recipes", recipeResult.size());

        //set as viewed...
        if (recipeResult.size() <= 4 && user != null) {
            for (final Recipe recipe : recipeResult) {
				this.query.addCookedRecipe(user, recipe.getId(), LocalDate.now());
            }
        }

        //recalculate and safe the recipes for learning user behaviour
        //arcelik does not want to change values for user behaviour...

        //return to Websocket if user available
        if (user != null){
            final RecipeResponse recipeResponse = new RecipeResponse(recipeResult);
            WebsocketController.getInstance().send(WebsocketController.OVEN_RECIPE,requestObject.getUserID(), recipeResponse);
        }

        //return to caller
        return recipeResult;
    }

    private void addUserPreference(final RecipeFilter recipeFilter, final User user) {

        if (user == null || user.getId() < 0l) {
            return;
        }

        // Language
        for (final Language language : user.getSpokenLanguages()) {
            recipeFilter.addRecipeLanguage(language);
        }

        // IncompatibleIngredients
        for (final Ingredient ingredient : user.getIncompatibleIngredients()) {
            recipeFilter.addExcludedIngredient(ingredient);
        }

        //liked Ingredient
        for (final Ingredient ing : user.getLikesIngredients()) {
            recipeFilter.addPossibleIngredient(ing);
        }

        //category
        for (final Category category : user.getPreferredCategories()) {
            recipeFilter.addRequiredCategory(category);
        }
    }

    private List<Recipe> dbRequest(final RecipeFilter recipeFilter) throws Exception {
        try {
            return this.query.getRecipe(recipeFilter);
        } catch (final ServiceUnavailableException | NullPointerException e) {
            //error with DB, but try again and renew session, at every request!
            try {
				this.query = new Query();
                return this.query.getRecipe(recipeFilter);
            } catch (final Exception e1) {
                throw new ServiceUnavailableException("Error connecting to Neo4J DB", e1);
            }
        }
    }

    private User getUser(final RecipeRequest requestObject) throws ResponseException {
        //get user
        try {
			this.query = new Query();
            final List<User> users = this.query.getUser("", requestObject.getUserID());
            if (users.isEmpty() || requestObject.getUserID() == -1) {
                return null;
            } else {
                return users.get(0);
            }
        } catch (final Exception e) {
            throw new ResponseException(new ErrorResponse(500, e.getMessage()));
        }
    }


    /*
     * @param userRequest
     * @return UserResponse, returns the user
     * @throws ResponseException
     */
    public UserObj.UserResponse rateRecipe(final UserObj.UserRequest userRequest) throws ResponseException {

        //Rating and Recipe must be set
        if (userRequest.getRating() == -69 || userRequest.getRecipeID() == -69) {
            throw new ResponseException(new ErrorResponse(422, "Rating and Recipe has to be set"));
        }

        //Rating must be between -10 and 10
        if (userRequest.getRating() < -10 || userRequest.getRating() > 10) {
            throw new ResponseException(new ErrorResponse(422, "Rating has to be between -10 and 10"));
        }

        try {

            //get the User
            final List<User> userList = this.query.getUser(userRequest.getUserName(), userRequest.getUserID());

            //only rate if the user found exactly ONE user
            if (userList.size() != 1) {
                throw new ResponseException(new ErrorResponse(422, "Specify a User. Found Users:" + userList.size()));
            }

            final User user = userList.get(0);
            
            user.setCurrentlySpokenLanguage(user.getSpokenLanguages().iterator().next());
            //rate the Recipe
            user.addRecipeRating(userRequest.getRecipeID(), userRequest.getRating());
            //save
			this.query.putUser(user);

            //return user
            return new UserObj.UserResponse(this.query.getUser("", user.getId()));

        } catch (final ResponseException e) {
            throw e;
        } catch (final Exception e) {
            throw new ResponseException(new ErrorResponse(500, e.getMessage()));
        }
    }

    /**
     * lowerst a given Recipe from a given User by one if already rated, if not rated yet we assume its 'rated' 0
     *
     * @param userID   ID of User
     * @param recipeID ID of Recipe
     */
    public void lowerRatingByOne(final long userID, final long recipeID) {
        int rating = 0;
        User user = null;
        try {
            user = this.query.getUser("", userID).get(0);
            rating = user.getRecipeRatings().get(recipeID);
        } catch (final Exception e) {
            LOG.info("lower Rating by one: Problems getting user or rating: " + e.getMessage() + " " + e.getCause());
        }

        //lower rating
        rating = rating - 1;

        //dont set it lower than -10
        if (rating < -10)
            return;

        //ony when user is assigned
        if (user == null)
            return;

        //rate the Recipe
        user.addRecipeRating(recipeID, rating);
        //save
        try {
			this.query.putUser(user);
        } catch (final Exception e) {
            LOG.info("lower Rating by one: Problems saving rating: " + e.getMessage() + " " + e.getCause());
        }

    }

    /**
     * create lowByte and highByte as Array
     *
     * @param number
     * @return Array [0] = lowByte [1] = highByte
     */
    private String[] createByteCode(final Integer number) {

        final String[] returnList = new String[2];

        //create lowByte
        returnList[0] = String.valueOf(Math.floorMod(number, 256));

        //create highByte
        if (number >= 256) {
            returnList[1] = "1";
        } else {
            returnList[1] = "0";
        }

        return returnList;
    }

    /**
     * return Integer calculated from LowByte and HighByte
     *
     * @param lowByteS
     * @param highByteS
     * @return Integer
     */
    private Integer createNumber(final String lowByteS, final String highByteS) {
        final int lowByte = Integer.parseInt(lowByteS);
        final int highByte = Integer.parseInt(highByteS);
        return lowByte + 256 * highByte;
    }

    /**
     * creates a new Factor to multiply the RecipeValues
     *
     * @param numberOfRatings
     * @param avgFactor
     * @return Factor (1 if the Recipe is already perfect)
     */
    private double getFactor(final int numberOfRatings, final Double avgFactor) {

        final double change = (0.3 / ((numberOfRatings / 5.0) + 1));
        final double factor;
        //50/50 chance of increase or decrease
        if (Math.random() < 0.5) {
            factor = avgFactor + change;
        } else {
            factor = avgFactor - change;
        }
        //factor is between 1.3 and 0.7
        if (factor > 1.3)
            return 1.3;
        if (factor < 0.7)
            return 0.7;
        return factor;

    }

    /**
     * @return The current query instance
     * @deprecated Preliminary used to fix recommender issue
     */
    @Deprecated
    public Query getQuery() {
        return this.query;
    }
}
