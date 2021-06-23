package zone.bot.vici.pattern.matcher;

import zone.bot.vici.intent.MessageToken;
import zone.bot.vici.pattern.model.LiteralNode;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class LiteralNodeMatcher implements PatternNodeMatcher<LiteralNode> {

	@Nonnull
	private final LiteralNode node;
	@Nonnull
	private final String literal;

	LiteralNodeMatcher(@Nonnull final LiteralNode node) {
		this.node = node;
		this.literal = node.getLiteral();
	}

	@Override
	public List<MatcherContext> matches(@Nonnull final MatcherContext context) {
		final Iterator<MessageToken> iter = context.tokenIterator();
		if(!iter.hasNext()) {
			return Collections.emptyList();
		}
		final int begin = iter.next().begin();
		if(context.getMessage().getMessage().startsWith(this.literal, begin)) {
			context.addMatch(this.node, begin, begin+literal.length());
			return Collections.singletonList(context);
		}
		return Collections.emptyList();
	}

	@Override
	public LiteralNode getPatternNode() {
		return this.node;
	}

}
