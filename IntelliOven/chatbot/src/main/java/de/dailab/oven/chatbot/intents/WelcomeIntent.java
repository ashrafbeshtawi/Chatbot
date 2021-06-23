package de.dailab.oven.chatbot.intents;

import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import zone.bot.vici.intent.IntentRequest;
import zone.bot.vici.intent.IntentResponse;
import zone.bot.vici.intent.MessageOutputChannel;

import javax.annotation.Nonnull;

public class WelcomeIntent extends IntelliOvenIntent {

	public WelcomeIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState) {
		super(channel, appState, DialogState.WELCOME, IntelliOvenAppState.DialogState.GOODBYE, IntelliOvenAppState.DialogState.PROVIDE_RATING);
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		final String actionCode = request.getNamedEntities().getSingleOrDefault("action", "Hi").getValue();
		getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "Welcome."+actionCode);
		setState(DialogState.GOODBYE);
		return IntentResponse.HANDLED;
	}

}
