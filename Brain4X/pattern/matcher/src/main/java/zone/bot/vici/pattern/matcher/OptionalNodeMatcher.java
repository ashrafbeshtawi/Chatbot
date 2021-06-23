package zone.bot.vici.pattern.matcher;

import zone.bot.vici.pattern.model.OptionalNode;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

class OptionalNodeMatcher implements PatternNodeMatcher<OptionalNode> {

	@Nonnull
	private final OptionalNode node;
	@Nonnull
	private final PatternNodeMatcher<?> child;

	OptionalNodeMatcher(@Nonnull final OptionalNode node, @Nonnull final PatternNodeMatcher<?> child) {
		this.node = node;
		this.child = child;
	}

	@Override
	public List<MatcherContext> matches(@Nonnull final MatcherContext context) {
		final List<MatcherContext> results = new LinkedList<>(this.child.matches(context.branch()));
		for(final MatcherContext result : results) {
			final Optional<PatternNodeMatch> match = result.getLastMatch();
			match.ifPresent(value -> result.addMatch(this.node, context.getMessagePointer(), value.getEnd()));
		}
		results.add(context.branch());
		return results;
	}

	@Override
	public OptionalNode getPatternNode() {
		return this.node;
	}

}
