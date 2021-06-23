package de.dailab.oven.chatbot;

import de.dailab.oven.model.IntelliOvenAppState;
import de.dailab.oven.model.IntelliOvenAppState.DialogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;
import zone.bot.vici.exceptions.LifecycleException;
import zone.bot.vici.intent.IntentRequest;
import zone.bot.vici.intent.IntentResponse;
import zone.bot.vici.intent.SkillAPI;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.BiFunction;

public class FallbackHandler implements BiFunction<SkillAPI, IntentRequest, IntentResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(FallbackHandler.class);

    private static final String RES_PATH_RESPONSES = "de/dailab/oven/chatbot/%s/responses.properties";

    private boolean initialized = false;
    @Nonnull
    private IntelliOvenAppState appState = new IntelliOvenAppState();

    public void initialize(@Nonnull final SkillAPI skillApi) throws LifecycleException {
        this.initialized = true;
        loadResponseTemplates(skillApi);
        this.appState = skillApi.getApi(IntelliOvenAppState.class).orElseThrow(() -> new LifecycleException("Required API 'IntelliOvenAppState' not available"));
    }

    private static void loadResponseTemplates(@Nonnull final SkillAPI skillApi) {
        for(final Language language : Language.getLanguages()) {
            final String resourcePath = String.format(RES_PATH_RESPONSES, language.getLangCode2());
            final InputStream resourceAsStream = FallbackHandler.class.getClassLoader().getResourceAsStream(resourcePath);
            if(resourceAsStream == null) {
                LOG.warn("No language pack found for language: {}", language.getName());
                continue;
            }
            final Properties properties = new Properties();
            try {
                properties.load(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
                for(final String key : properties.stringPropertyNames()) {
                    skillApi.registerResponseTemplate(language, key, properties.getProperty(key));
                }
            }
            catch (final IOException e) {
                LOG.error("Could not load properties file with response templates from resource path: {}", resourcePath, e);
            }
        }
    }

    @Override
    public IntentResponse apply(final SkillAPI skillAPI, final IntentRequest intentRequest) {
        if(!this.initialized) {
            try {
                initialize(skillAPI);
            } catch(final LifecycleException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        final String responseKey = DialogState.RECIPE_STEP.equals(this.appState.getDialogState()) ? "DefaultAnswers.NO_RECIPE_SELECTED_ANSWER" : "DefaultAnswers.I_DID_NOT_UNDERSTAND_ANSWER";
        return new IntentResponse(() -> skillAPI.sendMessageToUser(intentRequest.getMessage().getLanguage(), responseKey));
    }

}
