package zone.bot.vici;

import javax.annotation.Nonnull;
import java.util.Set;

public interface SkillResolverService {

	@Nonnull
	Set<Skill> getSkills();

}
