package de.dailab.oven.chatbot.intents;

import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import zone.bot.vici.intent.IntentRequest;
import zone.bot.vici.intent.IntentResponse;
import zone.bot.vici.intent.MessageOutputChannel;

import javax.annotation.Nonnull;

public class EndCookingProcessIntent extends IntelliOvenIntent {

	public EndCookingProcessIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState) {
		super(channel, appState, DialogState.RECIPE_STEP);
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		getAppState().setRecipeSearchResult(null);
		getAppState().setRecipeFilter(null);
		getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "RateRecipe");
		setState(DialogState.PROVIDE_RATING);
		return IntentResponse.HANDLED;
	}

}
