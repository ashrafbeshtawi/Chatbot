package zone.bot.vici.tts;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Signalize problems that appear while attempting to generate speech from text.
 *
 * @author Hendrik Motza
 * @since 12.19
 */
public class SpeechSynthesisException extends Exception {

    public SpeechSynthesisException(@Nonnull final String message) {
        super(Objects.requireNonNull(message, "Parameter 'message' must not be null!"));
    }

    public SpeechSynthesisException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(Objects.requireNonNull(message, "Parameter 'message' must not be null!"),
                Objects.requireNonNull(cause, "Parameter 'cause' must not be null!"));
    }
}
