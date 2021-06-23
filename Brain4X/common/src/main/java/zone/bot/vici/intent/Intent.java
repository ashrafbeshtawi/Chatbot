package zone.bot.vici.intent;

import javax.annotation.Nonnull;

public interface Intent {

    default boolean isEnabled() {
        return true;
    }

    @Nonnull
    IntentResponse handle(@Nonnull final IntentRequest request);

}
