package zone.bot.vici;

import zone.bot.vici.intent.Intent;
import zone.bot.vici.intent.Message;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface NLU {

	@Nonnull
	List<NLUResult> analyzeAll(@Nonnull final Message message);

	@Nonnull
	Optional<NLUResult> analyze(@Nonnull final Message message);

	@Nonnull
	Optional<NLUResult> analyze(@Nonnull final Intent intent, @Nonnull final Message message);

}
