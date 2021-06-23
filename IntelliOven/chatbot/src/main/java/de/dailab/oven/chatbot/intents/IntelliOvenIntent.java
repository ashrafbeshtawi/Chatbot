package de.dailab.oven.chatbot.intents;

import de.dailab.oven.model.IntelliOvenAppState;
import zone.bot.vici.intent.Intent;
import zone.bot.vici.intent.MessageOutputChannel;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class IntelliOvenIntent implements Intent {

	@Nonnull
	private final MessageOutputChannel outputChannel;
	@Nonnull
	private final IntelliOvenAppState appState;

	@Nonnull
	private final Set<IntelliOvenAppState.DialogState> enableStates = new HashSet<>();

	public IntelliOvenIntent(@Nonnull final MessageOutputChannel channel, @Nonnull final IntelliOvenAppState appState, final IntelliOvenAppState.DialogState... enableStates) {
		this.outputChannel = channel;
		this.appState = appState;
		Collections.addAll(this.enableStates, enableStates);
	}

	@Override
	public boolean isEnabled() {
		return this.enableStates.contains(this.appState.getDialogState());
	}

	@Nonnull
	public MessageOutputChannel getOutputChannel() {
		return this.outputChannel;
	}

	public void setState(@Nonnull final IntelliOvenAppState.DialogState state) {
		this.appState.setDialogState(state);
	}

	@Nonnull
	public IntelliOvenAppState getAppState() {
		return this.appState;
	}
}
