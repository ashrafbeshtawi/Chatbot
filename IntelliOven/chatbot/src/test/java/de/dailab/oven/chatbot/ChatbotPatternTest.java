package de.dailab.oven.chatbot;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;
import zone.bot.vici.intent.Message;
import zone.bot.vici.intent.MessageTokenizer;
import zone.bot.vici.pattern.InputSample;
import zone.bot.vici.pattern.NamedEntitiesValidator;
import zone.bot.vici.pattern.matcher.BotPatternMatcher;
import zone.bot.vici.pattern.model.BotPattern;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

public class ChatbotPatternTest {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(ChatbotPatternTest.class);

	@TestFactory
	public Collection<DynamicContainer> test() throws IOException {
		final PatternLoader patternLoader = new PatternLoader();
		patternLoader.registerTemplates();
		final String[] intents = new String[] {"Welcome", "UserAuthentification", "Smalltalk", "GoodBye", "RecipeConfirmation", "Help",
				"Cooking", "UserPreferences", "OvenControl", "ProvideRating", "Weather", "RecipeStep", "GetRecipeName", "GetRecipePreparationTime", "EndCookingProcess", "PickNextRecipe", "GetRecipeIngredients"};
		final List<DynamicContainer> intentTests = new LinkedList<>();
		for(final String intent : intents) {
			final Map<Language, Set<BotPattern>> patterns = patternLoader.getPatterns(intent);
			for(final Map.Entry<Language, Set<BotPattern>> entry : patterns.entrySet()) {
				final String displayName = String.format("Intent: %s (%s)", intent, entry.getKey().getName());
				intentTests.add(DynamicContainer.dynamicContainer(displayName, getIntentTests(entry.getValue())));
			}
		}
		return intentTests;
	}

	private static Collection<? extends DynamicNode> getIntentTests(@Nonnull final Set<BotPattern> patterns) {
		final List<DynamicContainer> tests = new LinkedList<>();
		int i=1;
		for(final BotPattern pattern : patterns) {
			tests.add(DynamicContainer.dynamicContainer("Pattern "+i, getPatternTests(pattern)));
			i++;
		}
		return tests;
	}

	private static Collection<? extends DynamicNode> getPatternTests(@Nonnull final BotPattern pattern) {
		final List<DynamicNode> tests = new LinkedList<>();
		final BotPatternMatcher matcher = new BotPatternMatcher(pattern);
		int i=1;
		for(final InputSample sample : pattern.getSamples()) {
			tests.add(DynamicContainer.dynamicContainer("Sample "+i, getSampleTests(pattern, matcher, sample)));
			i++;
		}
		return tests;
	}

	private static Collection<? extends DynamicNode> getSampleTests(@Nonnull final BotPattern pattern, final BotPatternMatcher matcher, @Nonnull final InputSample sample) {
		final List<DynamicNode> tests = new LinkedList<>();
		tests.add(DynamicTest.dynamicTest("Sample Matches", () -> testSampleMatching(pattern, matcher, sample)));
		return tests;
	}

	private static void testSampleMatching(@Nonnull final BotPattern pattern, final BotPatternMatcher matcher, @Nonnull final InputSample sample) {
		final Message message = sample.getMessage();
		final BotPatternMatcher.BotPatternMatcherResult result = matcher.match(message, new MessageTokenizer(message).getAllToken());
		Assertions.assertTrue(result.isMatch(), () -> "Pattern does not match sample!\nPattern: "+pattern.getPatternRootNode().toString()+"\nSample: "+message.getMessage());
		for(final NamedEntitiesValidator namedEntitiesPredicate : sample.getNamedEntitiesPredicates()) {
			final boolean success = namedEntitiesPredicate.test(result.getNamedEntities());
			Assertions.assertTrue(success, () -> "Named entity verification failed!\nPattern: "+pattern.getPatternRootNode().toString()+"\nSample: "+message.getMessage());
		}
	}

}
