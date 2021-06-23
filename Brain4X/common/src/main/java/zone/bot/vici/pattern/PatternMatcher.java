package zone.bot.vici.pattern;

import zone.bot.vici.intent.Message;
import zone.bot.vici.intent.MessageToken;

import javax.annotation.Nonnull;
import java.util.List;

public interface PatternMatcher {

	PatternMatcherResult match(@Nonnull final Message input, @Nonnull final List<MessageToken> messageTokens);

}
