package zone.bot.vici.pattern.matcher;

import zone.bot.vici.pattern.model.BotPatternNode;

import javax.annotation.Nonnull;
import java.util.List;

public interface PatternNodeMatcher<T extends BotPatternNode> {

	List<MatcherContext> matches(@Nonnull final MatcherContext context);

	T getPatternNode();

}
