package zone.bot.vici.pattern.matcher;

import zone.bot.vici.pattern.model.GroupNode;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

class GroupNodeMatcher implements PatternNodeMatcher<GroupNode> {

	@Nonnull
	private final GroupNode node;
	@Nonnull
	private final PatternNodeMatcher<?> child;

	GroupNodeMatcher(@Nonnull final GroupNode node, @Nonnull final PatternNodeMatcher<?> child) {
		this.node = node;
		this.child = child;
	}

	@Override
	public List<MatcherContext> matches(@Nonnull final MatcherContext context) {
		final int begin = context.getMessagePointer();
		final List<MatcherContext> results = this.child.matches(context);
		for(final MatcherContext result : results) {
			final Optional<PatternNodeMatch> match = result.getLastMatch();
			match.ifPresent(value -> result.addMatch(this.node, begin, value.getEnd()));
		}
		return results;
	}

	@Override
	public GroupNode getPatternNode() {
		return this.node;
	}

}
