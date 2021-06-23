package zone.bot.vici.pattern.matcher;

import zone.bot.vici.pattern.model.AlternativesNode;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

class AlternativesNodeMatcher implements PatternNodeMatcher<AlternativesNode> {

	@Nonnull
	private final AlternativesNode node;
	@Nonnull
	private final List<PatternNodeMatcher> children;

	AlternativesNodeMatcher(@Nonnull final AlternativesNode node, @Nonnull final List<PatternNodeMatcher> children) {
		this.node = node;
		this.children = children;
	}

	@Override
	public List<MatcherContext> matches(@Nonnull final MatcherContext context) {
		final List<MatcherContext> results = new LinkedList<>();
		for(final PatternNodeMatcher<?> child : this.children) {
			final MatcherContext childContext = context.branch();
			results.addAll(child.matches(childContext));
		}
		for(final MatcherContext result : results) {
			final Optional<PatternNodeMatch> match = result.getLastMatch();
			match.ifPresent(value -> result.addMatch(this.node, context.getMessagePointer(), value.getEnd()));
		}
		return results;
	}

	@Override
	public AlternativesNode getPatternNode() {
		return this.node;
	}

}
