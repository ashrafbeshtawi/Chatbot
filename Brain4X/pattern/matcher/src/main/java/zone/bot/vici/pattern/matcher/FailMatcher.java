package zone.bot.vici.pattern.matcher;

import zone.bot.vici.pattern.model.WildcardNode;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

class FailMatcher implements PatternNodeMatcher<WildcardNode> {

	@Override
	public List<MatcherContext> matches(@Nonnull final MatcherContext context) {
		return Collections.emptyList();
	}


	@Override
	public WildcardNode getPatternNode() {
		return new WildcardNode(null);
	}
}
