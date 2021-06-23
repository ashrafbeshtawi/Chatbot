package zone.bot.vici.pattern;

import zone.bot.vici.Language;
import zone.bot.vici.intent.Message;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class InputSample {

	@Nonnull
	private final Message message;
	@Nonnull
	private final List<NamedEntitiesValidator> namedEntitiesPredicates;

	public InputSample(@Nonnull final Language language, @Nonnull final String message) {
		this(language, message, Collections.emptyList());
	}

	public InputSample(@Nonnull final Language language, @Nonnull final String message, @Nonnull final List<NamedEntitiesValidator> namedEntitiesPredicates) {
		this.message = new Message() {
			@Nonnull
			@Override
			public String getMessage() {
				return message;
			}

			@Nonnull
			@Override
			public Language getLanguage() {
				return language;
			}
		};
		this.namedEntitiesPredicates = namedEntitiesPredicates;
	}

	@Nonnull
	public Message getMessage() {
		return this.message;
	}

	@Nonnull
	public List<NamedEntitiesValidator> getNamedEntitiesPredicates() {
		return this.namedEntitiesPredicates;
	}
}
