package zone.bot.vici.pattern.matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.intent.Message;
import zone.bot.vici.intent.MessageToken;
import zone.bot.vici.intent.NamedEntity;
import zone.bot.vici.intent.StaticNamedEntity;
import zone.bot.vici.pattern.PatternMatcher;
import zone.bot.vici.pattern.PatternMatcherResult;
import zone.bot.vici.pattern.model.BotPattern;
import zone.bot.vici.pattern.model.BotPatternEntity;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class BotPatternMatcher implements PatternMatcher {

	public static class BotPatternMatcherResult implements PatternMatcherResult {

		private final boolean match;

		@Nonnull
		private final List<PatternNodeMatch> patternNodeMatches;

		@Nonnull
		private final Map<String, List<NamedEntity>> namedEntities;

		@Nonnull
		static final BotPatternMatcherResult FAIL = new BotPatternMatcherResult();

		private BotPatternMatcherResult() {
			this.match = false;
			this.patternNodeMatches = Collections.emptyList();
			this.namedEntities = Collections.emptyMap();
		}

		BotPatternMatcherResult(@Nonnull final List<PatternNodeMatch> patternNodeMatches, @Nonnull final Map<String, List<NamedEntity>> namedEntities) {
			this.match = true;
			this.patternNodeMatches = patternNodeMatches;
			this.namedEntities = namedEntities;
		}

		public boolean isMatch() {
			return this.match;
		}

		@Nonnull
		public Map<String, List<NamedEntity>> getNamedEntities() {
			return this.namedEntities;
		}

		@Nonnull
		public List<PatternNodeMatch> getPatternNodeMatches() {
			return this.patternNodeMatches;
		}
	}

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(BotPatternMatcher.class);

	@Nonnull
	private final BotPattern pattern;
	@Nonnull
	private final PatternNodeMatcher<?> rootMatcher;

	public BotPatternMatcher(@Nonnull final BotPattern pattern) {
		this.pattern = pattern;
		this.rootMatcher = new MatcherBuilder(pattern).build();
	}

	private List<MatcherContext> findMatches(@Nonnull final Message input, @Nonnull final List<MessageToken> messageTokens) {
		final MatcherContext matcherContext = new MatcherContext(input, messageTokens);
		final List<MatcherContext> contexts = this.rootMatcher.matches(matcherContext);
		return contexts.stream().filter((c) -> !c.tokenIterator().hasNext()).collect(Collectors.toList());
	}

	@Nonnull
	private static MatcherContext chooseBestMatch(@Nonnull final List<MatcherContext> contexts) {
		contexts.sort(new MatchNodeComplexityComparator());
		return contexts.get(0);
	}

	@Nonnull
	private static Map<String, List<NamedEntity>> extractNamedEntities(@Nonnull final MatcherContext context) {
		final Map<String, List<NamedEntity>> namedEntities = new HashMap<>();
		final String message = context.getMessage().getMessage();
		for(final PatternNodeMatch match : context.getMatches()) {
			for(final BotPatternEntity namedEntity : match.getNode().getNamedEntities()) {
				final String name = namedEntity.getName();
				final String rawValue = message.substring(match.getBegin(), match.getEnd());
				final String value = namedEntity.getFixedValue() == null ? rawValue : namedEntity.getFixedValue();
				final List<NamedEntity> entities = namedEntities.computeIfAbsent(name, (k) -> new LinkedList<>());
				entities.add(new NamedEntity() {
					@Nonnull
					@Override
					public String getName() {
						return name;
					}

					@Nonnull
					@Override
					public String getRawValue() {
						return rawValue;
					}

					@Nonnull
					@Override
					public String getValue() {
						return value;
					}

					@Nonnull
					@Override
					public List<MessageToken> getMessageToken() {
						// TODO
						return Collections.emptyList();
					}

					@Override
					public int begin() {
						return match.getBegin();
					}

					@Override
					public int end() {
						return match.getEnd();
					}
				});
			}
		}
		return namedEntities;
	}

	@Override
	public BotPatternMatcherResult match(@Nonnull final Message input, @Nonnull final List<MessageToken> messageTokens) {
		final List<MatcherContext> contexts = findMatches(input, messageTokens);
		contexts.forEach(MatcherContext::consolidate);
		if(contexts.isEmpty()) {
			return BotPatternMatcherResult.FAIL;
		}
		final MatcherContext resultContext;
		if(contexts.size()==1) {
			resultContext = contexts.get(0);
		} else {
			LOG.warn("Multiple matcher solutions found for input '{}' by pattern '{}'", input.getMessage(), this.rootMatcher.getPatternNode().toString());
			resultContext = chooseBestMatch(contexts);
		}
		final Map<String, List<NamedEntity>> namedEntities = extractNamedEntities(resultContext);
		for(final Map.Entry<String, String> entry : this.pattern.getStaticEntities().entrySet()) {
			namedEntities.computeIfAbsent(entry.getKey(), (key) -> new LinkedList<>()).add(new StaticNamedEntity(entry.getKey(), entry.getValue()));
		}
		return new BotPatternMatcherResult(resultContext.getMatches(), namedEntities);
	}

	@Nonnull
	public BotPattern getPattern() {
		return this.pattern;
	}

}
