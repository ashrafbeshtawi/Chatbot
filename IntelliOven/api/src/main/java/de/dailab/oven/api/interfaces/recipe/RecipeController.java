package de.dailab.oven.api.interfaces.recipe;

import de.dailab.oven.api.interfaces.view.ViewController;
import de.dailab.oven.api_common.error.ErrorResponse;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.api_common.recipe.RecipeRequest;
import de.dailab.oven.api_common.recipe.RecipeResponse;
import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.controller.WebsocketController;
import de.dailab.oven.database.exceptions.InputException;
import de.dailab.oven.model.data_model.Recipe;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecipeController {

    //Singleton
    private static RecipeController singleInstance = null;
    //Map with userID and MessageList
    private final Map<Long, RecipeResponse> recipeResponseMap = new HashMap<>();


    private RecipeController() {
        //needed
    }

    public static RecipeController getInstance() {
        if (singleInstance == null)
            singleInstance = new RecipeController();
        return singleInstance;
    }

    public static RecipeResponse getRecipes(final RecipeRequest request) throws ResponseException {
        ErrorResponse response;
        try {
            final RecipeResponse recipeResponse = new RecipeResponse(DatabaseController.getInstance().getRecipes(request));

            //set the request to the local storage and update the view
            RecipeController.getInstance().setRecipes(recipeResponse, request.getUserID());
            ViewController.getInstance().update(request.getUserID());

            return recipeResponse;
        } catch (final IllegalArgumentException e) {
            //wrong Body
            response = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage());
            //return ALWAYS to Websocket
            WebsocketController.getInstance().send(WebsocketController.OVEN_RECIPE, request.getUserID(), response);
        } catch (final InputException e) {
            //not found
            response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
            //return ALWAYS to Websocket
            WebsocketController.getInstance().send(WebsocketController.OVEN_RECIPE, request.getUserID(), response);
        } catch (final ServiceUnavailableException e) {
            //DB down
            response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            //return ALWAYS to Websocket
            WebsocketController.getInstance().send(WebsocketController.OVEN_RECIPE, request.getUserID(), response);
        } catch (final Exception e) {
            //other Errors
            Logger.getLogger(RecipeController.class.toString()).log(Level.WARNING, e.getMessage());
            //other exception
            response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            //return ALWAYS to Websocket
            WebsocketController.getInstance().send(WebsocketController.OVEN_RECIPE, request.getUserID(), response);
        }
        throw new ResponseException(response);
    }

    private void setRecipes(final RecipeResponse recipeResponse, final long userID) {
        this.recipeResponseMap.put(userID, recipeResponse);
    }

    public RecipeResponse getRecipeResponseMap(final long userID) {
        //get the current user, check if its empty...
        this.recipeResponseMap.computeIfAbsent(userID, k -> new RecipeResponse(new ArrayList<Recipe>()));
        return this.recipeResponseMap.get(userID);
    }
    
    public void updateRecipePicture(final RecipeRequest request, final File newImage) {
    	/*TODO: Fill with content
    	 * recipe.updateImage
    	 * recipeQuery.putSingleRecipe
    	 */
    }
}
