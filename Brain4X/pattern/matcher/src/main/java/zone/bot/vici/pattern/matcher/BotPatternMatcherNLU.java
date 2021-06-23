package zone.bot.vici.pattern.matcher;

import zone.bot.vici.Language;
import zone.bot.vici.NLU;
import zone.bot.vici.NLUResult;
import zone.bot.vici.intent.*;
import zone.bot.vici.pattern.matcher.BotPatternMatcher.BotPatternMatcherResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class BotPatternMatcherNLU implements NLU {

	@Nonnull
	private final Map<Language, Map<Intent, Set<BotPatternMatcher>>> intentMatcherMap = new HashMap<>();

	public void addPatternMatcher(@Nonnull final Language language, @Nonnull final Intent intent, @Nonnull final Set<BotPatternMatcher> matchers) {
		this.intentMatcherMap.computeIfAbsent(language, k -> new HashMap<>()).put(intent, matchers);
	}

	@Nonnull
	@Override
	public List<NLUResult> analyzeAll(@Nonnull final Message message) {
		final Map<Intent, Set<BotPatternMatcher>> map = this.intentMatcherMap.get(message.getLanguage());
		if(map == null) {
			return Collections.emptyList();
		}
		final List<MessageToken> messageTokens = new MessageTokenizer(message).getAllToken();
		return map.entrySet().parallelStream().map(e -> analyze(e.getKey(), e.getValue(), message, messageTokens)).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Nonnull
	@Override
	public Optional<NLUResult> analyze(@Nonnull final Message message) {
		final Map<Intent, Set<BotPatternMatcher>> map = this.intentMatcherMap.get(message.getLanguage());
		if(map == null) {
			return Optional.empty();
		}
		final List<MessageToken> messageTokens = new MessageTokenizer(message).getAllToken();
		return map.entrySet().parallelStream().map(e -> analyze(e.getKey(), e.getValue(), message, messageTokens)).findAny();
	}

	@Nonnull
	@Override
	public Optional<NLUResult> analyze(@Nonnull final Intent intent, @Nonnull final Message message) {
		final Map<Intent, Set<BotPatternMatcher>> map = this.intentMatcherMap.get(message.getLanguage());
		if(map == null) {
			return Optional.empty();
		}
		final List<MessageToken> messageTokens = new MessageTokenizer(message).getAllToken();
		final Set<BotPatternMatcher> matchers = map.get(intent);
		if(matchers == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(analyze(intent, matchers, message, messageTokens));
	}

	@Nullable
	private static NLUResult analyze(@Nonnull final Intent intent, @Nonnull final Set<BotPatternMatcher> matchers, @Nonnull final Message message, @Nonnull final List<MessageToken> messageTokens) {
		if(!intent.isEnabled()) {
			return null;
		}
		for(final BotPatternMatcher matcher : matchers) {
			final BotPatternMatcherResult result = matcher.match(message, messageTokens);
			if(result.isMatch()) {
				return new NLUResult(intent, result.getNamedEntities());
			}
		}
		return null;
	}

}
