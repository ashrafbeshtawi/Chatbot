package zone.bot.vici.pattern;

import zone.bot.vici.intent.NamedEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface NamedEntitiesValidator extends Predicate<Map<String, List<NamedEntity>>> {
}
