package zone.bot.vici.intent;

import javax.annotation.Nonnull;
import java.util.*;

public class MessageTokenizer {

	@Nonnull
	private final List<MessageToken> messageTokens;

	@Nonnull
	private static final Set<Character> CHARACTERS_TO_SKIP;

	static {
		CHARACTERS_TO_SKIP = new HashSet<>();
		CHARACTERS_TO_SKIP.add('.');
		CHARACTERS_TO_SKIP.add('?');
		CHARACTERS_TO_SKIP.add('!');
		CHARACTERS_TO_SKIP.add(',');
		CHARACTERS_TO_SKIP.add(';');
		CHARACTERS_TO_SKIP.add(':');
		CHARACTERS_TO_SKIP.add('"');
		CHARACTERS_TO_SKIP.add('\'');
	}

	public MessageTokenizer(@Nonnull final Message message) {
		final String input = message.getMessage();
		final List<MessageToken> tokens = new LinkedList<>();
		int pointer = 0;
		for(int i=0; i<input.length(); i++) {
			final char currentChar = input.charAt(i);
			if(Character.isWhitespace(currentChar) || CHARACTERS_TO_SKIP.contains(currentChar)) {
				addToken(pointer, i, input, tokens);
				pointer = i+1;
			}
		}
		addToken(pointer, input.length(), input, tokens);
		this.messageTokens = Collections.unmodifiableList(new ArrayList<>(tokens));
	}

	private static void addToken(final int begin, final int end, @Nonnull final String input, @Nonnull final List<MessageToken> targetList) {
		if(begin<end) {
			final String value = input.substring(begin, end);
			final String valueLower = value.toLowerCase();
			final MessageToken token = new MessageToken() {
				@Override
				public int begin() {
					return begin;
				}

				@Override
				public int end() {
					return end;
				}

				@Nonnull
				@Override
				public String getValue() {
					return value;
				}

				@Nonnull
				@Override
				public String getLowerCaseValue() {
					return valueLower;
				}
			};
			targetList.add(token);
		}
	}

	public List<MessageToken> getAllToken() {
		return this.messageTokens;
	}



}
