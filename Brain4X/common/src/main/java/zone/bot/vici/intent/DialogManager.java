package zone.bot.vici.intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.*;
import zone.bot.vici.exceptions.LifecycleException;
import zone.bot.vici.intent.SkillIntentRegistry.IntentContextData;
import zone.bot.vici.intent.events.DialogEvent;
import zone.bot.vici.intent.events.DialogEventListener;
import zone.bot.vici.intent.events.FilteredListener;
import zone.bot.vici.intent.events.ResponseMessageCreated;
import zone.bot.vici.nlg.output.ResponseBuilder;
import zone.bot.vici.nlg.output.ResponseTemplateNotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class DialogManager {

    private static class SkillEntry {

        @Nonnull
        private final Skill skill;
        @Nonnull
        private final Map<String, Intent> intents = new HashMap<>();

        public SkillEntry(@Nonnull final Skill skill) {
            this.skill = skill;
        }

    }

    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(DialogManager.class);

    @Nonnull
    private final BiFunction<SkillAPI, IntentRequest, IntentResponse> fallbackIntent;
    @Nonnull
    private final List<SkillEntry> skills = new LinkedList<>();
    @Nonnull
    private final Map<String, Object> customApis = new HashMap<>();
    @Nonnull
    private final BiConsumer<Language, String> messageSender;
    @Nonnull
    private final ResponseBuilder responseBuilder = new ResponseBuilder();
    @Nonnull
    private final Map<Class<? extends DialogEvent>, Set<DialogEventListener<? extends DialogEvent>>> listeners = new HashMap<>();
    @Nonnull
    private final SkillResolverService skillRegistry;
    @Nonnull
    private final SkillIntentRegistry skillIntentMap = new SkillIntentRegistry();
    @Nonnull
    private final DefaultDialogFlowControl flowControl = new DefaultDialogFlowControl();
    @Nonnull
    private final MessageOutputChannel outputChannel = new MessageOutputChannel() {
        @Override
        public void sendRawMessageToUser(@Nonnull final Language language, @Nonnull final String message) {
            sendEvent(new ResponseMessageCreated(null, message, language, null));
            DialogManager.this.messageSender.accept(language, message);
        }

        @Override
        public void sendMessageToUser(@Nonnull final Language language, @Nonnull final String responseTemplateKey, @Nullable final Object dataModel) {
            try {
                final String message = DialogManager.this.responseBuilder.buildResponse(language, responseTemplateKey, dataModel);
                sendEvent(new ResponseMessageCreated(responseTemplateKey, message, language, dataModel));
                DialogManager.this.messageSender.accept(language, message);
            } catch (final ResponseTemplateNotFoundException e) {
                throw new IllegalArgumentException("Response key ['"+responseTemplateKey+"'] is not registered for language ['"+language+"']", e);
            }
        }
    };

    public DialogManager(@Nonnull final BiFunction<SkillAPI, IntentRequest, IntentResponse> fallbackHandler, @Nonnull final BiConsumer<Language, String> outputMessageTransmitter, @Nonnull final SkillResolverService skillRegistry) {
        this.fallbackIntent = Objects.requireNonNull(fallbackHandler, "Parameter 'fallbackHandler' must not be null");
        this.messageSender = Objects.requireNonNull(outputMessageTransmitter, "Parameter 'outputMessageTransmitter' must not be null");
        this.skillRegistry = Objects.requireNonNull(skillRegistry, "Parameter 'skillRegistry' must not be null");
    }

    @SafeVarargs
    public final <T extends DialogEvent> void addEventListener(@Nonnull final Class<T> eventType, @Nonnull final DialogEventListener<T> listener, final Predicate<T>... filters) {
        synchronized(this.listeners) {
            final Set<DialogEventListener<? extends DialogEvent>> eventListeners = this.listeners.computeIfAbsent(eventType, (k) -> new HashSet<>());
            if(filters.length == 0) {
                eventListeners.add(listener);
            } else {
                eventListeners.add(new FilteredListener<>(listener, filters));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public final <T extends DialogEvent> void sendEvent(@Nonnull final T event) {
        synchronized(this.listeners) {
            final Set<DialogEventListener<? extends DialogEvent>> eventListeners = this.listeners.get(event.getClass());
            if(eventListeners == null) {
                return;
            }
            for(final DialogEventListener<? extends DialogEvent> l : eventListeners) {
                ((DialogEventListener<T>)l).handle(event);
            }
        }
    }

    public void init() {
        this.skillRegistry.getSkills().forEach(this::registerSkill);
        LOG.info("{} skills registered", this.skills.size());
        this.skills.parallelStream().forEach(se -> {
            try {
                se.skill.init(getSkillApi());
                se.intents.putAll(se.skill.getNamedIntents());
            } catch(final LifecycleException e) {
                LOG.error("Failed to init skill {}", se.skill.getName(), e);
            }
        });
        this.skillRegistry.getSkills().forEach(this.skillIntentMap::registerSkill);
    }

    public void registerSkill(@Nonnull final Skill skill) {
        this.skills.add(new SkillEntry(Objects.requireNonNull(skill, "Parameter 'skill' must not be null")));
    }

    public <T> void registerApi(@Nonnull final T instance, @Nonnull final Class<T> apiClass) {
        Objects.requireNonNull(instance, "Parameter 'instance' must not be null");
        Objects.requireNonNull(apiClass, "Parameter 'apiClass' must not be null");
        this.customApis.put(apiClass.getCanonicalName(), instance);
    }

    private SkillAPI getSkillApi() {
        return new AbstractSkillApi(this.outputChannel, this.flowControl) {

            @Nonnull
            @SuppressWarnings("unchecked")
            @Override
            public <T> Optional<T> getApi(@Nonnull final Class<T> apiClass) {
                Objects.requireNonNull(apiClass, "Parameter 'apiClass' must not be null");
                return Optional.ofNullable((T) DialogManager.this.customApis.get(apiClass.getCanonicalName()));
            }

            @Nonnull
            @Override
            public DialogFlowControl getDialogFlowControl() {
                return DialogManager.this.flowControl;
            }

            @Override
            public void registerResponseTemplate(@Nonnull final Language language, @Nonnull final String templateIdentifier, @Nonnull final String template) {
                DialogManager.this.responseBuilder.registerTemplate(language, templateIdentifier, template);
            }
        };
    }

    private static void validateInputs(@Nonnull final InputMessage[] inputMessages, @Nonnull final UserMatch[] users) {
        Objects.requireNonNull(inputMessages, "Parameter 'inputMessages' must not be null");
        Objects.requireNonNull(users, "Parameter 'users' must not be null");
        if(inputMessages.length==0) {
            throw new IllegalArgumentException("Parameter 'inputMessages' must contain at least one entry");
        }
    }

    public void handleInputMessage(@Nonnull final InputMessage[] inputMessages, @Nonnull final UserMatch[] users, @Nonnull final IntentRequest.InputType inputType) {
        validateInputs(inputMessages, users);
        Runnable alternativeHandler = null;
        final List<Intent> responseIntents = this.flowControl.getResponseHandlerIntents();
        final List<Intent> prioritizedIntents = this.flowControl.getPrioritizedIntents();
        for(final InputMessage inputMessage : inputMessages) {
            for(final Intent intent : responseIntents) {
                if(!intent.isEnabled()) {
                    continue;
                }
                final IntentContextData contextData = this.skillIntentMap.getContext(intent);
                final Optional<NLUResult> nluResult = contextData == null ? Optional.empty() : contextData.getSkill().getNLU().analyze(intent, inputMessage);
                final NamedEntities entities = new NamedEntities(nluResult.map(NLUResult::getNamedEntities).orElse(Collections.emptyMap()));
                final IntentResponse response = intent.handle(buildIntentRequest(inputMessage, users, inputType, entities));
                if(response.handled()) {
                    return;
                }
                if(alternativeHandler == null) {
                    alternativeHandler = response.getAlternativeHandler();
                }
            }
            for(final Intent intent : prioritizedIntents) {
                if(!intent.isEnabled()) {
                    continue;
                }
                final IntentContextData contextData = this.skillIntentMap.getContext(intent);
                if(contextData == null) {
                    LOG.error("Skipped unregistered intent of class '{}'", intent.getClass().getCanonicalName());
                    continue;
                }
                final Optional<NLUResult> nluResult = contextData.getSkill().getNLU().analyze(intent, inputMessage);
                if(!nluResult.isPresent()) {
                    continue;
                }
                final NamedEntities entities = new NamedEntities(nluResult.map(NLUResult::getNamedEntities).get());
                final IntentResponse response = intent.handle(buildIntentRequest(inputMessage, users, inputType, entities));
                if(response.handled()) {
                    return;
                }
                if(alternativeHandler == null) {
                    alternativeHandler = response.getAlternativeHandler();
                }
            }
            for(final SkillEntry skill : this.skills) {
                final NLU nlu = skill.skill.getNLU();
                for(final NLUResult nluResult : nlu.analyzeAll(inputMessage)) {
                    if(!nluResult.getIntent().isEnabled()) {
                        continue;
                    }
                    final IntentResponse response = nluResult.getIntent().handle(buildIntentRequest(inputMessage, users, inputType, new NamedEntities(nluResult.getNamedEntities())));
                    if(response.handled()) {
                        return;
                    }
                    if(alternativeHandler == null) {
                        alternativeHandler = response.getAlternativeHandler();
                    }
                }
            }
        }
        handleFallback(inputMessages, users, inputType, alternativeHandler);
    }

    private void handleFallback(@Nonnull final InputMessage[] inputMessages, @Nonnull final UserMatch[] users, @Nonnull final IntentRequest.InputType inputType, @Nullable final Runnable alternativeHandler) {
        Runnable altHandler = alternativeHandler;
        for(final InputMessage inputMessage : inputMessages) {
            final IntentResponse response = this.fallbackIntent.apply(getSkillApi(), buildIntentRequest(inputMessage, users, inputType, new NamedEntities(Collections.emptyMap())));
            if (response.handled()) {
                return;
            }
            if(altHandler == null) {
                altHandler = response.getAlternativeHandler();
            }
        }
        if(altHandler != null) {
            altHandler.run();
        }
    }

    @Nonnull
    private static IntentRequest buildIntentRequest(@Nonnull final InputMessage inputMessage, @Nonnull final UserMatch[] users, @Nonnull final IntentRequest.InputType inputType, @Nonnull final NamedEntities entities) {
        if(users.length == 0) {
            throw new IllegalArgumentException("Parameter 'users' must contain at least one entry");
        }
        return new IntentRequest() {
            @Nonnull
            @Override
            public InputMessage getMessage() {
                return inputMessage;
            }

            @Nonnull
            @Override
            public List<MessageToken> getMessageTokens() {
                return Collections.emptyList();
            }

            @Nonnull
            @Override
            public UserMatch getUser() {
                return users[0];
            }

            @Nonnull
            @Override
            public UserMatch[] getAlternativeUsers() {
                return users;
            }

            @Nonnull
            @Override
            public NamedEntities getNamedEntities() {
                return entities;
            }

            @Nonnull
            @Override
            public InputType getInputType() {
                return inputType;
            }

        };
    }

}
