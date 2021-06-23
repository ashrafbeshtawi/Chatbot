package zone.bot.vici;

import javax.annotation.Nonnull;

public interface SkillResolverListener {

	void onSkillAdded(@Nonnull final Skill skill);

	void onSkillRemoved(@Nonnull final Skill skill);

}
