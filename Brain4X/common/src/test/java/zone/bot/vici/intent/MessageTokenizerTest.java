package zone.bot.vici.intent;

import org.junit.Assert;
import org.junit.Test;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import java.util.List;

public class MessageTokenizerTest {

	@Test
	public void test() {
		final String input = "My message: \"Hello, world.\" Okay? Okay! Fine";
		final Message message = new Message() {

			@Nonnull
			@Override
			public String getMessage() {
				return input;
			}

			@Nonnull
			@Override
			public Language getLanguage() {
				return Language.ENGLISH;
			}
		};
		final MessageTokenizer tokenizer = new MessageTokenizer(message);
		final List<MessageToken> tokens = tokenizer.getAllToken();

		Assert.assertEquals("My", tokens.get(0).getValue());
		Assert.assertEquals("message", tokens.get(1).getLowerCaseValue());
		Assert.assertEquals("message", input.substring(tokens.get(1).begin(), tokens.get(1).end()));
		Assert.assertEquals("hello", tokens.get(2).getLowerCaseValue());
		Assert.assertEquals("world", tokens.get(3).getLowerCaseValue());
		Assert.assertEquals("okay", tokens.get(4).getLowerCaseValue());
		Assert.assertEquals("okay", tokens.get(5).getLowerCaseValue());
		Assert.assertEquals("fine", tokens.get(6).getLowerCaseValue());
	}

}
