package zone.bot.vici.intent;

import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MessageOutputChannel {

	/**
	 * Sends the provided message to the user.
	 *
	 * @param message to be sent to user
	 * @param language in which the message is written
	 */
	void sendRawMessageToUser(@Nonnull final Language language, @Nonnull final String message);

	/**
	 * Sends the registered response for provided key to the user.
	 *
	 * @param language in which the message is written
	 * @param responseTemplateKey that was used to register the response template
	 */
	default void sendMessageToUser(@Nonnull final Language language, @Nonnull final String responseTemplateKey) {
		sendMessageToUser(language, responseTemplateKey, null);
	}

	/**
	 * Sends the registered response for provided key to the user after being formatted using the given dataModel.
	 *
	 * @param language in which the message is written
	 * @param responseTemplateKey that was used to register the response template
	 * @param dataModel to be used to format the response template
	 */
	void sendMessageToUser(@Nonnull final Language language, @Nonnull final String responseTemplateKey, @Nullable final Object dataModel);

}
