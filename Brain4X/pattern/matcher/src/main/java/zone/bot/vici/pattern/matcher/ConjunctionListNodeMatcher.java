package zone.bot.vici.pattern.matcher;

import zone.bot.vici.pattern.model.ConjunctionListNode;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class ConjunctionListNodeMatcher implements PatternNodeMatcher<ConjunctionListNode> {

	@Nonnull
	private final ConjunctionListNode node;
	@Nonnull
	private final PatternNodeMatcher<?> listItemMatcher;
	@Nonnull
	private final PatternNodeMatcher<?> conjunctionMatcher;

	ConjunctionListNodeMatcher(@Nonnull final ConjunctionListNode node, @Nonnull final PatternNodeMatcher<?> listItemMatcher, @Nonnull final PatternNodeMatcher<?> conjunctionMatcher) {
		this.node = node;
		this.listItemMatcher = listItemMatcher;
		this.conjunctionMatcher = conjunctionMatcher;
	}

	public List<MatcherContext> matchesMultiple(@Nonnull final MatcherContext context) {
		final List<MatcherContext> results = new LinkedList<>();
		final List<MatcherContext> conjunctionResults = new LinkedList<>(this.conjunctionMatcher.matches(context.branch()));
		if(!conjunctionResults.isEmpty()) {
			for(final MatcherContext conjunctionResult : conjunctionResults) {
				results.addAll(this.listItemMatcher.matches(conjunctionResult));
			}
			return results;
		}
		final List<MatcherContext> nextElementResults = this.listItemMatcher.matches(context.branch());
		for(final MatcherContext nextElementResult : nextElementResults) {
			results.addAll(matchesMultiple(nextElementResult));
		}
		return results;
	}

	@Override
	public List<MatcherContext> matches(@Nonnull final MatcherContext context) {
		final int begin = context.getMessagePointer();
		final List<MatcherContext> singleElementResults = this.listItemMatcher.matches(context);
		final LinkedList<MatcherContext> results = new LinkedList<>(singleElementResults);
		for(final MatcherContext singleElementResult : singleElementResults) {
			results.addAll(matchesMultiple(singleElementResult));
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
	public ConjunctionListNode getPatternNode() {
		return this.node;
	}
}
