package zone.bot.vici.pattern.matcher;

import zone.bot.vici.pattern.model.AnyNode;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class AnyNodeMatcher implements PatternNodeMatcher<AnyNode> {

	@Nonnull
	private final AnyNode node;
	@Nonnull
	private final PatternNodeMatcher<?> child;

	AnyNodeMatcher(@Nonnull final AnyNode node, @Nonnull final PatternNodeMatcher<?> child) {
		this.node = node;
		this.child = child;
	}

	private List<MatcherContext> matches(@Nonnull final List<MatcherContext> contexts) {
		final List<MatcherContext> results = new LinkedList<>();
		for(final MatcherContext context : contexts) {
			results.addAll(this.child.matches(context.branch()));
		}
		return results;
	}

	@Override
	public List<MatcherContext> matches(@Nonnull final MatcherContext context) {
		final int begin = context.getMessagePointer();
		final LinkedList<MatcherContext> results = new LinkedList<>();
		results.add(context);
		List<MatcherContext> lastResults = new LinkedList<>();
		lastResults.add(context);
		while(!(lastResults = matches(lastResults)).isEmpty()) {
			results.addAll(lastResults);
		}
		/* Some of the results contexts might have some of the previous list entries as parent.
		Adding a match entry directly would lead to propagation of partial matches.
		To avoid this, we create another branch from each result.
		*/
		final List<MatcherContext> clonedResults = results.stream().map(MatcherContext::branch).collect(Collectors.toList());
		for(final MatcherContext result : clonedResults) {
			final Optional<PatternNodeMatch> match = result.getLastMatch();
			match.ifPresent(value -> result.addMatch(this.node, begin, value.getEnd()));
		}
		return clonedResults;
	}

	@Override
	public AnyNode getPatternNode() {
		return this.node;
	}
}
