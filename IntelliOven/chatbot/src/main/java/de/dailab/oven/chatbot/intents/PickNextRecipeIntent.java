package de.dailab.oven.chatbot.intents;

import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import de.dailab.oven.model.data_model.Recipe;
import zone.bot.vici.intent.IntentRequest;
import zone.bot.vici.intent.IntentResponse;
import zone.bot.vici.intent.MessageOutputChannel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PickNextRecipeIntent extends IntelliOvenIntent {

	@Nonnull
	private final DatabaseController databaseController;

	public PickNextRecipeIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState, @Nonnull final DatabaseController databaseController) {
		super(channel, appState, DialogState.RECIPE_STEP, DialogState.RECIPE_CONFIRMATION);
		this.databaseController = databaseController;
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		final List<Recipe> searchResults = getAppState().getRecipeSearchResult();
		final Recipe selectedRecipe = getAppState().getSelectedRecipe();
		assert searchResults != null;
		assert selectedRecipe != null;
		this.databaseController.lowerRatingByOne(request.getUser().getUserID(), selectedRecipe.getId());
		if(searchResults.isEmpty()) {
			setState(DialogState.GOODBYE);
			getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "NoMoreSearchResultsAvailable");
			return IntentResponse.HANDLED;
		}
		final int randomIndex = new Random().nextInt(searchResults.size());
		// removing alternatives was missing (so that not the same recipe can come up twice)
		final List<Recipe> recipeAlternatives = new ArrayList<>(searchResults);
		final Recipe nextRecipe = recipeAlternatives.get(randomIndex);
		recipeAlternatives.remove(nextRecipe);
		// Reset recipeSearchResult
		getAppState().setRecipeSearchResult(recipeAlternatives);
		getAppState().setSelectedRecipe(nextRecipe);
		final HashMap<String, Object> dataModel = new HashMap<>();
		dataModel.put("recipe", selectedRecipe);
		setState(DialogState.RECIPE_CONFIRMATION);
		getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "RecipeStep.GetRecipeAlternative", dataModel);
		return IntentResponse.HANDLED;
	}
}
