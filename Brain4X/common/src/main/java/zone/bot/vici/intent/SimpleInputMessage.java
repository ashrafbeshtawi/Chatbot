package zone.bot.vici.intent;

import zone.bot.vici.Language;

import javax.annotation.Nonnull;

public class SimpleInputMessage implements InputMessage {

    @Nonnull
    private final Language language;
    @Nonnull
    private final String message;
    private final float probability;

    public SimpleInputMessage(@Nonnull Language language, @Nonnull String message, float probability) {
        this.language = language;
        this.message = message;
        this.probability = probability;
    }

    public SimpleInputMessage(@Nonnull Language language, @Nonnull String message) {
        this(language, message, 1);
    }

    @Override
    public float getProbability() {
        return this.probability;
    }

    @Nonnull
    @Override
    public String getMessage() {
        return this.message;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return this.language;
    }
}
