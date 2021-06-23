package de.dailab.oven.chatbot.log;

import zone.bot.vici.intent.IntentRequest.InputType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InputMessage extends Message {

	public enum HandlerType {
		NONE(-1), FALLBACK(0), POSTPONED(1), REGULAR(2), PRIORITY(3), RESPONSE(4);

		private final int value;

		HandlerType(final int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	@Nonnull
	private final InputType channelType;
	@Nonnull
	private final HandlerType type;
	@Nullable
	private final String handledBySkill;
	@Nullable
	private final String handledByIntent;

	InputMessage(@Nonnull final zone.bot.vici.intent.InputMessage msg, @Nonnull final String user, @Nonnull final InputType channelType, @Nonnull final HandlerType type, @Nullable final String handledBySkill, @Nullable final String handledByIntent) {
		super(msg, user);
		this.channelType = channelType;
		this.type = type;
		this.handledBySkill = handledBySkill;
		this.handledByIntent = handledByIntent;
	}

	@Override
	MessageType getType() {
		return MessageType.INPUT;
	}

	@Nonnull
	@Override
	public InputType getChannelType() {
		return this.channelType;
	}

	@Nonnull
	public HandlerType getHandlerType() {
		return this.type;
	}

	@Nullable
	public String getHandledBySkill() {
		return this.handledBySkill;
	}

	@Nullable
	public String getHandledByIntent() {
		return this.handledByIntent;
	}

}
