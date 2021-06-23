package de.dailab.oven.chatbot.intents;

import de.dailab.chatbot.aal.utils.ChatbotUtils;
import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import de.dailab.oven.model.data_model.IngredientWithAmount;
import de.dailab.oven.model.data_model.Recipe;
import zone.bot.vici.intent.IntentRequest;
import zone.bot.vici.intent.IntentResponse;
import zone.bot.vici.intent.MessageOutputChannel;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

public class GetRecipeIngredientsIntent extends IntelliOvenIntent {

	public GetRecipeIngredientsIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState) {
		super(channel, appState, DialogState.RECIPE_STEP, DialogState.RECIPE_CONFIRMATION);
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		final Recipe recipe = getAppState().getSelectedRecipe();
		assert recipe != null;
		final List<IngredientWithAmount> ingredients = recipe.getIngredients();
		final HashMap<String, Object> dataModel = new HashMap<>();
		dataModel.put("recipe", recipe);
		dataModel.put("ingredients", ChatbotUtils.ingredientListAsNiceString(ingredients, request.getMessage().getLanguage()));
		getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "RecipeStep.GetIngredients", dataModel);
		setState(DialogState.RECIPE_STEP);
		return IntentResponse.HANDLED;
	}
}
