package zone.bot.vici.test;

import zone.bot.vici.test.matcher.Matcher;
import zone.bot.vici.test.matcher.MatcherAPI;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class ChatbotTestStepBuilder {

	@Nonnull
	private final List<Matcher> matcherList = new LinkedList<>();
	@Nonnull
	private final MatcherAPI matcherApi;

	ChatbotTestStepBuilder(@Nonnull final MatcherAPI matcherApi) {
		this.matcherApi = matcherApi;
	}


	public ChatbotTestStepBuilder addMatcher(@Nonnull final Matcher matcher) {
		this.matcherList.add(matcher);
		return this;
	}

	public ChatbotTestMatcher build() {
		for(final Matcher matcher : this.matcherList) {
			matcher.init(this.matcherApi);
		}
		return new ChatbotTestMatcher(this.matcherList);
	}

}
