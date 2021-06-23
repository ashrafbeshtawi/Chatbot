package zone.bot.vici.test.matcher;

import zone.bot.vici.intent.events.DialogEvent;
import zone.bot.vici.intent.events.DialogEventListener;

import javax.annotation.Nonnull;

public interface MatcherAPI {

	<T extends DialogEvent> void addEventListener(@Nonnull final Class<T> eventType, @Nonnull final DialogEventListener<T> listener);

}
