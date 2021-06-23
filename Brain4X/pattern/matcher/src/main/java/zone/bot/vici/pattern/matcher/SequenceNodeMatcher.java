package zone.bot.vici.pattern.matcher;

import zone.bot.vici.pattern.model.SequenceNode;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

class SequenceNodeMatcher implements PatternNodeMatcher<SequenceNode> {

	@Nonnull
	private final SequenceNode node;
	@Nonnull
	private final List<PatternNodeMatcher> children;

	SequenceNodeMatcher(@Nonnull final SequenceNode node, @Nonnull final List<PatternNodeMatcher> children) {
		this.node = node;
		this.children = children;
	}

	private static List<MatcherContext> matches(@Nonnull final List<MatcherContext> contexts, @Nonnull final PatternNodeMatcher child) {
		final List<MatcherContext> results = new LinkedList<>();
		for(final MatcherContext context : contexts) {
			results.addAll(child.matches(context));
		}
		return results;
	}

	@Override
	public List<MatcherContext> matches(@Nonnull final MatcherContext context) {
		final int begin = context.getMessagePointer();
		List<MatcherContext> currentResults = Collections.singletonList(context);
		for(final PatternNodeMatcher<?> child : this.children) {
			currentResults = matches(currentResults, child);
		}
		for(final MatcherContext result : currentResults) {
			final Optional<PatternNodeMatch> match = result.getLastMatch();
			match.ifPresent(value -> result.addMatch(this.node, begin, value.getEnd()));
		}
		return currentResults;
	}

	@Override
	public SequenceNode getPatternNode() {
		return this.node;
	}
}
