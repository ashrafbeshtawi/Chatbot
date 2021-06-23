package zone.bot.vici.nlg.output;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.*;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ResponseBuilder {

    @Nonnull
    private final Map<Language, Configuration> configs = new HashMap<>();
    @Nonnull
    private final Map<Language, StringTemplateLoader> templateLoaders = new HashMap<>();

    private static Configuration buildConfiguration() {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        // Do not fall back to higher scopes when reading a null loop variable:
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        cfg.setSharedVariable("rand", new RandomChoiceDirective());
        cfg.setSharedVariable("tts", new TTSDirective());
        return cfg;
    }

    public void registerTemplate(@Nonnull final Language language, @Nonnull final String templateIdentifier, @Nonnull final String template) {
        StringTemplateLoader templateLoader = this.templateLoaders.get(language);
        if(templateLoader == null) {
            final Configuration cfg = buildConfiguration();
            templateLoader = new StringTemplateLoader();
            cfg.setTemplateLoader(templateLoader);
            this.configs.put(language, cfg);
            this.templateLoaders.put(language, templateLoader);
        }
        templateLoader.putTemplate(templateIdentifier, template);
    }

    public String buildResponse(@Nonnull final Language language, @Nonnull final String templateIdentifier, @Nullable final Object dataModel) throws ResponseTemplateNotFoundException {
        final Configuration cfg = this.configs.get(language);
        if(cfg == null) {
            throw new ResponseTemplateNotFoundException("No templates got registered for language '"+language.getName()+"'");
        }
        try {
            final Template template = cfg.getTemplate(templateIdentifier);
            final StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (final ParseException | TemplateException e) {
            throw new ResponseProcessingException("Could not parse response template", e);
        } catch (final IOException e) {
            throw new ResponseTemplateNotFoundException("");
        }
    }


}
