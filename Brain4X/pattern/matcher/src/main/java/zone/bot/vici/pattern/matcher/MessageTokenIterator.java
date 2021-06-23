package zone.bot.vici.pattern.matcher;

import zone.bot.vici.intent.MessageToken;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

class MessageTokenIterator implements Iterator<MessageToken> {

	@Nonnull
	private final List<MessageToken> messageTokens;
	private int pointer;

	MessageTokenIterator(@Nonnull final List<MessageToken> messageTokens, final int startIndex) {
		this.messageTokens = messageTokens;
		this.pointer = startIndex;
	}

	@Override
	public boolean hasNext() {
		return this.pointer < this.messageTokens.size();
	}

	@Override
	public MessageToken next() {
		if(!hasNext()) {
			throw new NoSuchElementException();
		}
		return this.messageTokens.get(this.pointer++);
	}

}
