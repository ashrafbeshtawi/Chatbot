package zone.bot.vici;

import zone.bot.vici.exceptions.LifecycleException;
import zone.bot.vici.intent.Intent;
import zone.bot.vici.intent.SkillAPI;

import javax.annotation.Nonnull;
import java.util.Map;

public interface Skill {

	@Nonnull
	String getName();

	void init(@Nonnull final SkillAPI skillAPI) throws LifecycleException;

	@Nonnull
	Map<String, Intent> getNamedIntents();

	@Nonnull
	NLU getNLU();

}
