package zone.bot.vici.pattern.matcher;

import zone.bot.vici.intent.MessageToken;
import zone.bot.vici.pattern.model.WildcardNode;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class WildcardNodeMatcher implements PatternNodeMatcher<WildcardNode> {

	@Nonnull
	private final WildcardNode node;

	WildcardNodeMatcher(@Nonnull final WildcardNode node) {
		this.node = node;
	}

	@Override
	public List<MatcherContext> matches(@Nonnull final MatcherContext context) {
		final Iterator<MessageToken> iter = context.tokenIterator();
		if(!iter.hasNext()) {
			return Collections.emptyList();
		}
		final MessageToken token = iter.next();
		context.addMatch(this.node, token);
		return Collections.singletonList(context);
	}

	@Override
	public WildcardNode getPatternNode() {
		return this.node;
	}

}
