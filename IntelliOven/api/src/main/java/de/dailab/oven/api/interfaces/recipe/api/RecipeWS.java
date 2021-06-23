package de.dailab.oven.api.interfaces.recipe.api;

import de.dailab.oven.api.interfaces.recipe.RecipeController;
import de.dailab.oven.api_common.Sendable;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.api_common.recipe.RecipeRequest;
import de.dailab.oven.controller.WebsocketController;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
public class RecipeWS {

    /**
     * r00
     * Make requests to the DB to get Dishes
     *
     * @param requestObject JSON as class RecipeFilter
     * @return List of Recipes
     */
    @MessageMapping("/oven/recipe/get")
    //@SendTo(WebsocketController.OVEN_RECIPE) we already send the result in RecipeController
    public Sendable get(final RecipeRequest requestObject) {
        try {
            return RecipeController.getRecipes(requestObject);
        } catch (final ResponseException e) {
            return e.getResponse();
        }
    }

}
