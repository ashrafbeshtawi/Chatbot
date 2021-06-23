package zone.bot.vici.nlg.output;

import zone.bot.vici.nlg.output.MarkerFinder.MarkerNode;

import javax.annotation.Nonnull;
import java.io.StringWriter;
import java.util.*;

public class MarkerParser {

    @Nonnull
    private final MarkerFinder markerFinder;
    @Nonnull
    private final MarkerProcessor defaultProcessor;
    @Nonnull
    private final Map<String, MarkerProcessor> processors = new HashMap<>();

    public MarkerParser(@Nonnull final MarkerSyntaxConfiguration cfg, @Nonnull final MarkerProcessor defaultProcessor) {
        this.markerFinder = new MarkerFinder(Objects.requireNonNull(cfg, "Parameter 'cfg' must not be null"));
        this.defaultProcessor = Objects.requireNonNull(defaultProcessor, "Parameter 'defaultProcessor' must not be null");
    }

    public void addProcessor(@Nonnull final String tagName, @Nonnull final MarkerProcessor processor) {
        Objects.requireNonNull(tagName, "Parameter 'tagName' must not be null");
        Objects.requireNonNull(processor, "Parameter 'processor' must not be null");
        this.processors.put(tagName, processor);
    }

    public String process(@Nonnull final String input) {
        final List<MarkerNode> markerNodes = this.markerFinder.findMarker(input);
        // create a flat list of all markers (siblings and childrens) ordered from first to last appearance
        final List<MarkerNode> flatMarkerList = new LinkedList<>();
        addChildrenMarkerNodes(markerNodes, flatMarkerList);
        final StringWriter out = new StringWriter();
        process(input, markerNodes, out, 0, input.length());
        return out.toString();
    }

    private void process(@Nonnull final String input, @Nonnull final List<MarkerNode> markerNodes, @Nonnull final StringWriter out, final int start, final int end) {
        int pointer = start;
        for(final MarkerNode markerNode : markerNodes) {
            out.append(input.substring(pointer, markerNode.getBegin()));
            final MarkerProcessor processor = this.processors.getOrDefault(markerNode.getMarkerData().getTagName(), this.defaultProcessor);
            processor.process(new MarkerProcessorApi() {
                @Nonnull
                @Override
                public StringWriter getOutputWriter() {
                    return out;
                }

                @Override
                public void renderContent(@Nonnull final StringWriter writer) {
                    process(input, markerNode.getChildMarker(), writer, markerNode.getContentBegin(), markerNode.getContentEnd());
                }
            }, markerNode.getMarkerData());
            pointer = markerNode.getEnd();
        }
        out.append(input.substring(pointer, end));
    }

    private void addChildrenMarkerNodes(@Nonnull final List<MarkerNode> children, @Nonnull final List<MarkerNode> resultList) {
        for(final MarkerNode node : children) {
            resultList.add(node);
            addChildrenMarkerNodes(node.getChildMarker(), resultList);
        }
    }


}
