package de.dailab.oven.chatbot.intents;

import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import zone.bot.vici.intent.IntentRequest;
import zone.bot.vici.intent.IntentResponse;
import zone.bot.vici.intent.MessageOutputChannel;

import javax.annotation.Nonnull;

public class SmalltalkIntent extends IntelliOvenIntent {

	public SmalltalkIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState) {
		super(channel, appState, DialogState.WELCOME, DialogState.GOODBYE, DialogState.PROVIDE_RATING);
	}

	@Nonnull
	@Override
	public IntentResponse handle(@Nonnull final IntentRequest request) {
		final String actionCode = request.getNamedEntities().getSingle("action").orElseThrow(() -> new IllegalStateException("Action code is missing")).getValue();
		getOutputChannel().sendMessageToUser(request.getMessage().getLanguage(), "Smalltalk."+actionCode);
		setState(DialogState.GOODBYE);
		return IntentResponse.HANDLED;
	}

}
