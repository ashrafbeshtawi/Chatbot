package zone.bot.vici.nlg.output;

import freemarker.core.Environment;
import freemarker.template.*;
import zone.bot.vici.Language;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

class TTSDirective implements TemplateDirectiveModel {

    private static final String PARAM_NAME_REPLACEMENT = "replace";
    private static final String PARAM_NAME_LANGUAGE = "lang";
    private static final String PARAM_NAME_PHONEMES = "phone";

    public void execute(final Environment env,
						final Map params, final TemplateModel[] loopVars,
						final TemplateDirectiveBody body)
            throws TemplateException, IOException {

        String replacement = null;
        String language = null;
        String phonemes = null;

        final Iterator paramIter = params.entrySet().iterator();
        while (paramIter.hasNext()) {
            final Map.Entry ent = (Map.Entry) paramIter.next();

            final String paramName = (String) ent.getKey();
            final TemplateModel paramValue = (TemplateModel) ent.getValue();

            if (paramName.equals(PARAM_NAME_LANGUAGE)) {
                language = ((TemplateScalarModel) paramValue).getAsString();
            } else if (paramName.equals(PARAM_NAME_REPLACEMENT)) {
                replacement = ((TemplateScalarModel) paramValue).getAsString();
            } else if (paramName.equals(PARAM_NAME_PHONEMES)) {
                phonemes = ((TemplateScalarModel) paramValue).getAsString();
            } else {
                throw new TemplateModelException("Unsupported parameter: " + paramName);
            }
        }
        if (replacement == null && language == null && phonemes == null) {
            throw new TemplateModelException("Neither parameter '"+PARAM_NAME_REPLACEMENT+"', '"+PARAM_NAME_PHONEMES+"' nor '"+PARAM_NAME_LANGUAGE+"' is specified.");
        }

        final Writer out = env.getOut();
        out.write("[tts");
        if(replacement != null) {
            out.write(" replace=\"");
            out.write(replacement.replace("\"", "\\\""));
            out.write("\"");
        }
        if(language != null) {
            final Language lang = Language.getLanguage(language);
            if(Language.UNDEF.equals(lang)) {
                throw new TemplateModelException("Unknown language identifier '" + language+ "'");
            }
            out.write(" lang=\"");
            out.write(lang.getLangCode2());
            out.write("\"");
        }
        if(phonemes != null) {
            out.write(" phone=\"");
            out.write(phonemes.replace("\"", "\\\""));
            out.write("\"");
        }
        out.write("]");
        body.render(out);
        out.write("[/tts]");
    }

}
