package zone.bot.vici.pattern.matcher;

import zone.bot.vici.intent.MessageToken;
import zone.bot.vici.pattern.model.WordNode;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class WordNodeMatcher implements PatternNodeMatcher<WordNode> {

	@Nonnull
	private final WordNode node;
	@Nonnull
	private final String word;

	WordNodeMatcher(@Nonnull final WordNode node) {
		this.node = node;
		this.word = node.getWord().toLowerCase();
	}

	@Override
	public List<MatcherContext> matches(@Nonnull final MatcherContext context) {
		final Iterator<MessageToken> iter = context.tokenIterator();
		if(!iter.hasNext()) {
			return Collections.emptyList();
		}
		final MessageToken token = iter.next();
		if(this.word.equals(token.getLowerCaseValue())) {
			context.addMatch(this.node, token);
			return Collections.singletonList(context);
		}
		return Collections.emptyList();
	}

	@Override
	public WordNode getPatternNode() {
		return this.node;
	}

}
