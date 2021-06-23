package zone.bot.vici.intent;

import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public interface Message {

    @Nonnull
    String getMessage();

    @Nonnull
    Language getLanguage();

    @Nonnull
    default Map<String, String> getMetadata() {
        return Collections.emptyMap();
    }

}
