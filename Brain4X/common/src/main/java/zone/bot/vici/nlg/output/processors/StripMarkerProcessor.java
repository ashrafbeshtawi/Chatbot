package zone.bot.vici.nlg.output.processors;

import zone.bot.vici.nlg.output.MarkerData;
import zone.bot.vici.nlg.output.MarkerProcessor;
import zone.bot.vici.nlg.output.MarkerProcessorApi;

import javax.annotation.Nonnull;

public class StripMarkerProcessor implements MarkerProcessor {
    @Override
    public void process(@Nonnull final MarkerProcessorApi context, @Nonnull final MarkerData markerData) {
        context.renderContent(context.getOutputWriter());
    }
}
