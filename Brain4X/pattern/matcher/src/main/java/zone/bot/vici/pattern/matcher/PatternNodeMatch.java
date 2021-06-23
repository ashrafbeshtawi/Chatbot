package zone.bot.vici.pattern.matcher;

import zone.bot.vici.pattern.model.BotPatternNode;

import javax.annotation.Nonnull;

public class PatternNodeMatch {

	@Nonnull
	private final BotPatternNode node;
	private final int begin;
	private final int end;

	PatternNodeMatch(@Nonnull final BotPatternNode node, final int begin, final int end) {
		this.node = node;
		this.begin = begin;
		this.end = end;
	}

	@Nonnull
	public BotPatternNode getNode() {
		return this.node;
	}

	public int getBegin() {
		return this.begin;
	}

	public int getEnd() {
		return this.end;
	}

}
