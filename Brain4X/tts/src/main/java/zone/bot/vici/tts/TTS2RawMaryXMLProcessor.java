package zone.bot.vici.tts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;
import zone.bot.vici.nlg.output.MarkerData;
import zone.bot.vici.nlg.output.MarkerProcessor;
import zone.bot.vici.nlg.output.MarkerProcessorApi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Processes tts markers and converts them into the corresponding RawMaryXML format. Be aware that this converts the the tts tags to xml but does not transform the whole content into an xml document.
 *
 * @author Hendrik Motza
 * @since 2019.12
 */
class TTS2RawMaryXMLProcessor implements MarkerProcessor {

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(TTS2RawMaryXMLProcessor.class);

    @Nonnull
    private final MaryTTS maryTTS;

    /**
     * Constructor.
     *
     * @param maryTTS instance of marytts to request information about the installed languages, {@code null disallowed}
     */
    TTS2RawMaryXMLProcessor(@Nonnull final MaryTTS maryTTS) {
        this.maryTTS = Objects.requireNonNull(maryTTS, "Parameter 'maryTTS' must not be null");
    }

    @Override
    public void process(@Nonnull final MarkerProcessorApi context, @Nonnull final MarkerData markerData) {
        final StringWriter out = context.getOutputWriter();
        final Optional<String> optUnnamedParameter = markerData.getOptUnnamedParameter();
        final Map<String, String> params = markerData.getNamedParameters();
        final String replacement;
        final String locale;
        final String phone;
        if(optUnnamedParameter.isPresent()) {
            replacement = optUnnamedParameter.get();
            locale = null;
            phone = null;
        } else {
            replacement = params.get("replace");
            final String l = params.get("lang");
            final Language lang = l == null ? null : Language.getLanguage(l);
            locale = languageToMaryLocaleRepresentation(lang);
            phone = params.get("phone");
        }
        if(locale != null || phone != null) {
            out.write("<t ");
            if(phone != null) {
                out.write("ph=\"");
                out.write(phone.replace("\"", "\\\""));
                out.write("\">");
            } else {
                out.write("xml:lang=\"");
                out.write(locale);
                out.write("\">");
            }
        }
        if(replacement != null) {
            out.write(replacement.replace("\"", "\\\""));
        } else {
            context.renderContent(out);
        }
        if(locale != null || phone != null) {
            out.write("</t>");
        }
    }

    /**
     * MaryTTS might use another string representation for a language or might not support a specific language.
     * This method will return the corresponding language string for MaryTTS or {@code null} if none is available.
     *
     * @param language Language to be mapped, {@code null} allowed
     * @return Locale representation for MaryTTS or {@code null} if specified language is {@code null}, {@link Language#UNDEF undefined} or not available in MaryTTS.
     */
    @Nullable
    String languageToMaryLocaleRepresentation(@Nullable final Language language) {
        if(Language.UNDEF.equals(language)) {
            LOG.error("TTS language marker was used for language {} but this language is not registered. Marker will be ignored!", language.getLangCode3());
            return null;
        }
        if(language != null) {
            final String locale = this.maryTTS.getLocaleIdentifierByLangCode3(language.getLangCode3());
            if(locale == null) {
                LOG.error("TTS language marker was used for language {} but this language is not installed in MaryTTS. Marker will be ignored!", language.getLangCode3());
            }
            return locale;
        }
        return null;
    }

}
