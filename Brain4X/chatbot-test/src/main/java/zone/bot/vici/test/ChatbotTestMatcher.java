package zone.bot.vici.test;

import zone.bot.vici.test.matcher.Matcher;

import javax.annotation.Nonnull;
import java.util.List;

public class ChatbotTestMatcher {

	@Nonnull
	private final List<Matcher> matchers;

	ChatbotTestMatcher(@Nonnull final List<Matcher> matcher) {
		this.matchers = matcher;
	}

	public void verify() {
		for(final Matcher matcher : this.matchers) {
			matcher.verify();
		}
	}

}
