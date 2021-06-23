package zone.bot.vici.pattern;

import zone.bot.vici.intent.NamedEntity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public interface PatternMatcherResult {

	boolean isMatch();

	@Nonnull
	Map<String, List<NamedEntity>> getNamedEntities();

}
