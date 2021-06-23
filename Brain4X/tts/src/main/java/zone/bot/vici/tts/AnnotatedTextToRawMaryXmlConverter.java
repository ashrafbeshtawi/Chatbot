package zone.bot.vici.tts;

import zone.bot.vici.Language;
import zone.bot.vici.nlg.output.BBCodeSyntaxConfiguration;
import zone.bot.vici.nlg.output.MarkerParser;
import zone.bot.vici.nlg.output.processors.StripMarkerProcessor;

import javax.annotation.Nonnull;
import java.io.StringWriter;
import java.util.function.BiFunction;

/**
 * Convert text (with tts annotations) into the RawMaryXML format.
 *
 * @author Hendrik Motza
 * @since 2019.12
 */
public class AnnotatedTextToRawMaryXmlConverter implements BiFunction<String, Language, String> {

    @Nonnull
    private static final String PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<maryxml version=\"0.4\"\n" +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "xmlns=\"http://mary.dfki.de/2002/MaryXML\"\n" +
            "xml:lang=\"%s\">";
    @Nonnull
    private static final String SUFFIX = "</maryxml>";

    @Nonnull
    private final TTS2RawMaryXMLProcessor annotationProcessor;
    @Nonnull
    private final MarkerParser parser;

    /**
     * Constructor.
     *
     * @param maryTTS tts instance used for synthesis
     */
    public AnnotatedTextToRawMaryXmlConverter(@Nonnull final MaryTTS maryTTS){
        this.annotationProcessor = new TTS2RawMaryXMLProcessor(maryTTS);
        this.parser = new MarkerParser(new BBCodeSyntaxConfiguration(), new StripMarkerProcessor());
		this.parser.addProcessor("tts", this.annotationProcessor);
    }

    @Override
    public String apply(final String s, final Language language) {
        final String input = s.replace("<", "&lt;").replace("&", "&amp;"); //StringEscapeUtils.escapeHtml(s);
        final StringWriter out = new StringWriter();
        out.write(String.format(PREFIX, this.annotationProcessor.languageToMaryLocaleRepresentation(language)));
        out.write(this.parser.process(input));
        out.write(SUFFIX);
        return out.toString();
    }
}
