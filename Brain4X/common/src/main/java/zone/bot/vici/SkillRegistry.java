package zone.bot.vici;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

public class SkillRegistry implements SkillResolverService {

	@Nonnull
	private final Set<Skill> skills = new HashSet<>();

	public SkillRegistry() {
		for(final SkillResolverService service : ServiceLoader.load(SkillResolverService.class)) {
			this.skills.addAll(service.getSkills());
		}
	}

	@Nonnull
	@Override
	public Set<Skill> getSkills() {
		return this.skills;
	}


}
