package de.dailab.oven.chatbot.intents;

import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import de.dailab.oven.model.data_model.Recipe;
import zone.bot.vici.intent.IntentRequest;
import zone.bot.vici.intent.IntentResponse;
import zone.bot.vici.intent.MessageOutputChannel;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class GetRecipePreparationTimeIntent extends IntelliOvenIntent {

	public GetRecipePreparationTimeIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState) {
		super(channel, appState, DialogState.RECIPE_STEP, DialogState.RECIPE_CONFIRMATION);
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		final Recipe recipe = getAppState().getSelectedRecipe();
		final HashMap<String, Object> dataModel = new HashMap<>();
		dataModel.put("recipe", recipe);
		assert recipe != null;
		dataModel.put("preparationTime", recipe.totalDuration().toMinutes());
		getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "RecipeStep.GetPreparationTime", dataModel);
		setState(DialogState.RECIPE_STEP);
		return IntentResponse.HANDLED;
	}
}
