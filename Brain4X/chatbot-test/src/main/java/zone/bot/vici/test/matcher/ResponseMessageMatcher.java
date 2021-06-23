package zone.bot.vici.test.matcher;

import org.junit.Assert;
import zone.bot.vici.Language;
import zone.bot.vici.intent.events.DialogEventListener;
import zone.bot.vici.intent.events.ResponseMessageCreated;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class ResponseMessageMatcher implements Matcher, DialogEventListener<ResponseMessageCreated> {

	public interface ResponseMessageChildMatcher {
		void verify(@Nonnull final ResponseMessageCreated event);
	}

	private class TemplateKeyMatcher implements ResponseMessageChildMatcher{

		@Nonnull
		private final String expected;

		TemplateKeyMatcher(@Nonnull final String expected) {
			this.expected = expected;
		}

		@Override
		public void verify(@Nonnull final ResponseMessageCreated event) {
			Assert.assertTrue("Expected response template key '"+this.expected+"' but was '"+event.getMessageTemplateId()+"'", this.expected.equals(event.getMessageTemplateId()));
		}
	}

	private class MessageContentMatcher implements ResponseMessageChildMatcher{

		@Nonnull
		private final String expected;

		MessageContentMatcher(@Nonnull final String expected) {
			this.expected = expected;
		}

		@Override
		public void verify(@Nonnull final ResponseMessageCreated event) {
			Assert.assertTrue("Expected response message '"+this.expected+"' but was '"+event.getMessageTemplateId()+"'", this.expected.equals(event.getMessage()));
		}
	}

	private class MessageLanguageMatcher implements ResponseMessageChildMatcher{

		@Nonnull
		private final Language expected;

		MessageLanguageMatcher(@Nonnull final Language expected) {
			this.expected = expected;
		}

		@Override
		public void verify(@Nonnull final ResponseMessageCreated event) {
			Assert.assertTrue("Expected response message with language '"+this.expected+"' but was '"+event.getLanguage()+"'", this.expected.equals(event.getLanguage()));
		}
	}

	@Nonnull
	private final List<ResponseMessageCreated> events = new LinkedList<>();
	@Nonnull
	private final List<ResponseMessageChildMatcher> childMatchers = new LinkedList<>();

	@Override
	public void init(@Nonnull final MatcherAPI matcherApi) {
		matcherApi.addEventListener(ResponseMessageCreated.class, this);
	}

	@Override
	public void handle(final ResponseMessageCreated event) {
		this.events.add(event);
	}

	@Override
	public void verify() {
		Assert.assertTrue("Expected 1 response message but detected "+ this.events.size()+" response messages", this.events.size()==1);
		final ResponseMessageCreated event = this.events.get(0);
		for(final ResponseMessageChildMatcher matcher : this.childMatchers) {
			matcher.verify(event);
		}
	}

	public ResponseMessageMatcher withTemplateKey(@Nonnull final String templateKey) {
		this.childMatchers.add(new TemplateKeyMatcher(templateKey));
		return this;
	}

	public ResponseMessageMatcher withMessage(@Nonnull final String content) {
		this.childMatchers.add(new MessageContentMatcher(content));
		return this;
	}

	public ResponseMessageMatcher withLanguage(@Nonnull final Language language) {
		this.childMatchers.add(new MessageLanguageMatcher(language));
		return this;
	}

}
