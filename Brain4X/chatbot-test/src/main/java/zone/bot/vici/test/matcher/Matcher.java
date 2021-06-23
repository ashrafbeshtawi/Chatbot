package zone.bot.vici.test.matcher;

import javax.annotation.Nonnull;

public interface Matcher {

	default void init(@Nonnull final MatcherAPI matcherApi) { }

	void verify();

}
