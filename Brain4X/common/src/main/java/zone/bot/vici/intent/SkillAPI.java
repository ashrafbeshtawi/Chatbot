package zone.bot.vici.intent;

import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Interface for Skills/Intents made available during initialization.
 *
 * @author Hendrik Motza
 * @since 2019.11
 */
public interface SkillAPI extends MessageOutputChannel {

    /**
     * Get instance of a custom api that was made available by the application for all intents.
     *
     * @param apiClass class instance of the requested api
     * @param <T> type of the api
     * @return optional instance of the api
     */
    @Nonnull
    <T> Optional<T> getApi(@Nonnull final Class<T> apiClass);

    @Nonnull
    DialogFlowControl getDialogFlowControl();

    void registerResponseTemplate(@Nonnull final Language language, @Nonnull final String templateIdentifier, @Nonnull final String template);

}
