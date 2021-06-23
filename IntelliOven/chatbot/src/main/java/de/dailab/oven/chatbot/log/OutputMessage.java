package de.dailab.oven.chatbot.log;

import zone.bot.vici.intent.IntentRequest.InputType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OutputMessage extends Message {

	@Nullable
	private final String sentBySkill;

	OutputMessage(@Nonnull final zone.bot.vici.intent.Message msg, @Nullable final String skill) {
		super(msg, "");
		this.sentBySkill = skill;
	}

	@Override
	MessageType getType() {
		return MessageType.OUTPUT;
	}

	@Nonnull
	@Override
	public InputType getChannelType() {
		return InputType.CHAT;
	}

	@Nullable
	public String getSentBySkill() {
		return this.sentBySkill;
	}

}
