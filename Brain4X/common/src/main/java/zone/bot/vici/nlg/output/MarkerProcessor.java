package zone.bot.vici.nlg.output;

import javax.annotation.Nonnull;

public interface MarkerProcessor {

    void process(@Nonnull final MarkerProcessorApi context, @Nonnull final MarkerData markerData);

}
