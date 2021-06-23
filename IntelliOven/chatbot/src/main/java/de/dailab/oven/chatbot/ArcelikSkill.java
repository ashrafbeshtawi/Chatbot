package de.dailab.oven.chatbot;

import de.dailab.oven.DummyOven;
import de.dailab.oven.chatbot.intents.*;
import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.database.UserController;
import de.dailab.oven.model.IntelliOvenAppState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;
import zone.bot.vici.NLU;
import zone.bot.vici.Skill;
import zone.bot.vici.exceptions.LifecycleException;
import zone.bot.vici.intent.Intent;
import zone.bot.vici.intent.SkillAPI;
import zone.bot.vici.pattern.matcher.BotPatternMatcher;
import zone.bot.vici.pattern.matcher.BotPatternMatcherNLU;
import zone.bot.vici.pattern.model.BotPattern;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class ArcelikSkill implements Skill {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(ArcelikSkill.class);
	@Nonnull
	private static final String RES_PATH_RESPONSES = "de/dailab/oven/chatbot/%s/responses.properties";

	@Nonnull
	private final Map<String, Intent> intents = new HashMap<>();
	@Nonnull
	private final BotPatternMatcherNLU nlu = new BotPatternMatcherNLU();
	@Nonnull
	private final PatternLoader patternLoader = new PatternLoader();

	private static Set<BotPatternMatcher> toMatcherSet(@Nonnull final Set<BotPattern> pattern) {
		return pattern.parallelStream().map(BotPatternMatcher::new).collect(Collectors.toSet());
	}

	@Nonnull
	@Override
	public String getName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void init(@Nonnull final SkillAPI skillApi) throws LifecycleException {
		final IntelliOvenAppState appState = skillApi.getApi(IntelliOvenAppState.class).orElseThrow(() -> new LifecycleException("Required API 'IntelliOvenAppState' not available"));
		final UserController userController = skillApi.getApi(UserController.class).orElseThrow(() -> new LifecycleException("Required API 'UserController' not available"));
		final DatabaseController databaseController = skillApi.getApi(DatabaseController.class).orElseThrow(() -> new LifecycleException("Required API 'DatabaseController' not available"));
		final DummyOven oven = skillApi.getApi(DummyOven.class).orElseThrow(() -> new LifecycleException("Required API 'Oven' not available"));
		this.intents.put("Welcome", new WelcomeIntent(skillApi, appState));
		this.intents.put("Goodbye", new GoodbyeIntent(skillApi, appState));
		this.intents.put("Smalltalk", new SmalltalkIntent(skillApi, appState));
		this.intents.put("Weather", new WeatherIntent(skillApi, appState));
		this.intents.put("UserAuthentification", new UserAuthentificationIntent(skillApi, appState, userController));
		this.intents.put("UserPreferences", new UserPreferencesIntent(skillApi, appState, userController));
		this.intents.put("RecipeSearch", new RecipeSearchIntent(skillApi, appState, userController, databaseController));
		this.intents.put("PickNextRecipe", new PickNextRecipeIntent(skillApi, appState, databaseController));
		final RecipeConfirmationIntent recipeConfirmationIntent = new RecipeConfirmationIntent(skillApi, appState);
		appState.addListener(recipeConfirmationIntent);
		this.intents.put("RecipeConfirmation", recipeConfirmationIntent);
		this.intents.put("RecipeStep", new RecipeStepIntent(skillApi, appState));
		this.intents.put("GetRecipeName", new GetRecipeNameIntent(skillApi, appState));
		this.intents.put("GetRecipeIngredients", new GetRecipeIngredientsIntent(skillApi, appState));
		this.intents.put("GetRecipePreparationTime", new GetRecipePreparationTimeIntent(skillApi, appState));
		this.intents.put("EndCookingProcess", new EndCookingProcessIntent(skillApi, appState));
		this.intents.put("ProvideRating", new ProvideRatingIntent(skillApi, appState, userController, databaseController));
		this.intents.put("OvenControl", new OvenControlIntent(skillApi, appState, oven));
		this.intents.put("Help", new HelpIntent(skillApi, appState));
		try {
			this.patternLoader.registerTemplates();
		} catch(final IOException e) {
			LOG.error("Failed to load pattern templates", e);
		}
		for(final Map.Entry<String, Intent> intentEntry : this.intents.entrySet()) {
			final Map<Language, Set<BotPattern>> map;
			try {
				map = this.patternLoader.getPatterns(intentEntry.getKey());
			} catch(final IOException e) {
				throw new LifecycleException(e);
			}
			for(final Map.Entry<Language, Set<BotPattern>> patternEntry : map.entrySet()) {
				this.nlu.addPatternMatcher(patternEntry.getKey(), intentEntry.getValue(), toMatcherSet(patternEntry.getValue()));
			}
		}
		loadResponseTemplates(skillApi);
	}

	private static void loadResponseTemplates(@Nonnull final SkillAPI skillApi) {
		for(final Language language : Language.getLanguages()) {
			final String resourcePath = String.format(RES_PATH_RESPONSES, language.getLangCode2());
			final InputStream resourceAsStream = ArcelikSkill.class.getClassLoader().getResourceAsStream(resourcePath);
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

	@Nonnull
	@Override
	public Map<String, Intent> getNamedIntents() {
		return this.intents;
	}

	@Nonnull
	@Override
	public NLU getNLU() {
		return this.nlu;
	}

}
