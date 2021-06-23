package de.dailab.oven.chatbot.log;

import zone.bot.vici.Language;
import zone.bot.vici.intent.IntentRequest.InputType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public abstract class Message {

	public enum MessageType {
		INPUT, OUTPUT;
	}

	@Nonnull
	private final String device = "local";
	@Nonnull
	private final String user;
	private final long timestamp = System.currentTimeMillis();
	private final Map<String, String> metadata = new HashMap<>();
	@Nonnull
	private final String message;
	@Nonnull
	private final Language language;
	@Nonnull
	private final String channel = "";

	Message(@Nonnull final zone.bot.vici.intent.Message msg, @Nonnull final String user) {
		this.message = msg.getMessage();
		this.language = msg.getLanguage();
		this.user = user;
	}

	abstract MessageType getType();

	@Nonnull
	public abstract InputType getChannelType();

	@Nonnull
	public String getDevice() {
		return this.device;
	}

	@Nonnull
	public String getUser() {
		return this.user;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	@Nonnull
	public String getMessage() {
		return this.message;
	}

	@Nonnull
	public Language getLanguage() {
		return this.language;
	}

	@Nonnull
	public String getChannel() {
		return this.channel;
	}

	/*
	Message:
direction (input/output)
device
userid
timestamp
metadata (key-value map)
message
channel
channelType


InputMessage:
handlerType (-1: NONE, 0: FALLBACK, 1: POSTPONED, 2: REGULAR, 3: PRIORITY, 4: RESPONSE)
handledBySkill
handledByIntent


OutputMessage:
sentBySkill
	 */

}
