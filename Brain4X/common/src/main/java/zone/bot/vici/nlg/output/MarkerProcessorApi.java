package zone.bot.vici.nlg.output;

import javax.annotation.Nonnull;
import java.io.StringWriter;

public interface MarkerProcessorApi {

    @Nonnull
    StringWriter getOutputWriter();

    void renderContent(@Nonnull final StringWriter writer);
}
