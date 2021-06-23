package zone.bot.vici.pattern.matcher;

import zone.bot.vici.intent.MessageToken;
import zone.bot.vici.pattern.model.RegexNode;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexNodeMatcher implements PatternNodeMatcher<RegexNode> {

	@Nonnull
	private final RegexNode node;
	@Nonnull
	private final Pattern pattern;

	RegexNodeMatcher(@Nonnull final RegexNode node) {
		this.node = node;
		this.pattern = node.getPattern();
	}

	@Override
	public List<MatcherContext> matches(@Nonnull final MatcherContext context) {
		final Iterator<MessageToken> iter = context.tokenIterator();
		if(!iter.hasNext()) {
			return Collections.emptyList();
		}
		final int begin = iter.next().begin();
		final Matcher matcher = this.pattern.matcher(context.getMessage().getMessage());
		if(matcher.find(begin) && matcher.start()==begin) {
			context.addMatch(this.node, begin, matcher.end());
			return Collections.singletonList(context);
		}
		return Collections.emptyList();
	}

	@Override
	public RegexNode getPatternNode() {
		return this.node;
	}

}
