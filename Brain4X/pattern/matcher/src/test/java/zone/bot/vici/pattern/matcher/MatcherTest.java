package zone.bot.vici.pattern.matcher;

import org.junit.Assert;
import org.junit.Test;
import zone.bot.vici.Language;
import zone.bot.vici.intent.InputMessage;
import zone.bot.vici.intent.MessageTokenizer;
import zone.bot.vici.intent.SimpleInputMessage;
import zone.bot.vici.pattern.matcher.BotPatternMatcher.BotPatternMatcherResult;
import zone.bot.vici.pattern.model.BotPattern;
import zone.bot.vici.pattern.parser.BotPatternParser;

public class MatcherTest {

	@Test
	public void testReferenceMatchWithoutParams() {
		final String whoTemplate = "world";
		final String rawPattern = "hello ~who";
		final BotPatternParser parser = new BotPatternParser();
		parser.parseAndRegisterTemplate("who", whoTemplate);
		final BotPattern pattern = parser.parse(rawPattern);
		final BotPatternMatcher matcher = new BotPatternMatcher(pattern);
		final InputMessage message = new SimpleInputMessage(Language.ENGLISH, "hello world");
		final BotPatternMatcherResult result = matcher.match(message, new MessageTokenizer(message).getAllToken());
		Assert.assertTrue(result.isMatch());
	}

	@Test
	public void testReferenceMatchWithParams() {
		final String tupleTemplate = "$0 $1";
		final String rawPattern = "tuple ~tuple(first, second)";
		final BotPatternParser parser = new BotPatternParser();
		parser.parseAndRegisterTemplate("tuple", tupleTemplate);
		final BotPattern pattern = parser.parse(rawPattern);
		final BotPatternMatcher matcher = new BotPatternMatcher(pattern);
		final InputMessage message = new SimpleInputMessage(Language.ENGLISH, "tuple first second");
		final BotPatternMatcherResult result = matcher.match(message, new MessageTokenizer(message).getAllToken());
		Assert.assertTrue(result.isMatch());
	}

	@Test
	public void testNestedReferencesMatchWithParams() {
		final String tupleTemplate = "$0 $1";
		final String rawPattern = "zero ~tuple(first, ~tuple(second, third))";
		final BotPatternParser parser = new BotPatternParser();
		parser.parseAndRegisterTemplate("tuple", tupleTemplate);
		final BotPattern pattern = parser.parse(rawPattern);
		final BotPatternMatcher matcher = new BotPatternMatcher(pattern);
		final InputMessage message = new SimpleInputMessage(Language.ENGLISH, "zero first second third");
		final BotPatternMatcherResult result = matcher.match(message, new MessageTokenizer(message).getAllToken());
		Assert.assertTrue(result.isMatch());
	}

}
