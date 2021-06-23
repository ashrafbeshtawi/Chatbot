package de.dailab.oven.chatbot.intents;

import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import zone.bot.vici.intent.IntentRequest;
import zone.bot.vici.intent.IntentResponse;
import zone.bot.vici.intent.MessageOutputChannel;

import javax.annotation.Nonnull;

public class HelpIntent extends IntelliOvenIntent {

	public HelpIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState) {
		super(channel, appState, DialogState.WELCOME, DialogState.GOODBYE, DialogState.PROVIDE_RATING);
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "Help");
		setState(DialogState.GOODBYE);
		return IntentResponse.HANDLED;
	}

}
