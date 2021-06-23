package de.dailab.oven.chatbot.intents;

import de.dailab.oven.DummyOven;
import de.dailab.oven.chatbot.ArcelikSkillResolver;
import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.database.UserController;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.IntelliOvenAppState;
import org.junit.Test;
import zone.bot.vici.Language;
import zone.bot.vici.test.ChatbotTest;
import zone.bot.vici.test.ChatbotTestMatcher;
import zone.bot.vici.test.matcher.ResponseMessageMatcher;

public class WelcomeTest {

	@Test
	public void test() throws DatabaseException, ConfigurationException {
		final ChatbotTest chatbotTest = ChatbotTest.configure(new ArcelikSkillResolver())
		.registerApi(new IntelliOvenAppState(), IntelliOvenAppState.class)
		.registerApi(new UserController(new Query().getGraph()), UserController.class)
				.registerApi(DatabaseController.getInstance(), DatabaseController.class)
				.registerApi(new DummyOven(), DummyOven.class).build();

		final ChatbotTestMatcher test = chatbotTest.createTest().addMatcher(new ResponseMessageMatcher().withLanguage(Language.GERMAN).withTemplateKey("Welcome.Hi")).build();

		chatbotTest.sendMessage(Language.GERMAN, 1, "Hallo");
		test.verify();
	}

}
