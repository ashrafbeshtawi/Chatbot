package zone.bot.vici.intent.events;

import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResponseMessageCreated extends DialogEvent {

	@Nullable
	private final String messageTemplateId;
	@Nonnull
	private final String message;
	@Nonnull
	private final Language language;
	@Nullable
	private final Object datamodel;

	public ResponseMessageCreated(@Nullable final String messageTemplateId, @Nonnull final String message, @Nonnull final Language language, @Nullable final Object datamodel) {
		this.messageTemplateId = messageTemplateId;
		this.message = message;
		this.language = language;
		this.datamodel = datamodel;
	}

	@Nullable
	public String getMessageTemplateId() {
		return this.messageTemplateId;
	}

	@Nonnull
	public String getMessage() {
		return this.message;
	}

	@Nonnull
	public Language getLanguage() {
		return this.language;
	}

	@Nullable
	public Object getDatamodel() {
		return this.datamodel;
	}
}
