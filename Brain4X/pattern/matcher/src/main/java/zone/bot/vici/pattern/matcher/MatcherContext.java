package zone.bot.vici.pattern.matcher;

import zone.bot.vici.intent.Message;
import zone.bot.vici.intent.MessageToken;
import zone.bot.vici.pattern.model.BotPatternNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

class MatcherContext {

	@Nullable
	private MatcherContext parentBranch;
	@Nonnull
	private LinkedList<PatternNodeMatch> matches = new LinkedList<>();
	@Nonnull
	private final Message message;
	@Nonnull
	private final List<MessageToken> messageTokens;
	private int messagePointer = 0;
	private int messageTokenIndex = 0;

	private MessageToken nextToken;

	MatcherContext(@Nonnull final Message message, @Nonnull final List<MessageToken> messageTokens) {
		this.parentBranch = null;
		this.message = message;
		this.messageTokens = Collections.unmodifiableList(new ArrayList<>(messageTokens));
		this.nextToken = messageTokens.isEmpty() ? null : messageTokens.get(0);
	}

	private MatcherContext(@Nonnull final MatcherContext matcherContext) {
		this.parentBranch = matcherContext;
		this.message = matcherContext.message;
		this.messageTokens = matcherContext.messageTokens;
		this.messagePointer = matcherContext.messagePointer;
		this.messageTokenIndex = matcherContext.messageTokenIndex;
		this.nextToken = matcherContext.nextToken;
	}

	@Nonnull
	public MatcherContext branch() {
		return new MatcherContext(this);
	}

	public void addMatch(@Nonnull final BotPatternNode patternNode, @Nonnull final MessageToken matchedToken) {
		addMatch(patternNode, matchedToken.begin(), matchedToken.end());
	}

	public void addMatch(@Nonnull final BotPatternNode patternNode, final int begin, final int end) {
		this.matches.add(new PatternNodeMatch(patternNode, begin, end));
		if(end > this.messagePointer) {
			setPointer(end+1);
		}
	}

	private void setPointer(final int index) {
		this.messagePointer = index;
		for(int i = this.messageTokenIndex; i<this.messageTokens.size(); i++) {
			final MessageToken current = this.messageTokens.get(i);
			if(this.messagePointer >= current.end()) {
				this.messageTokenIndex = i+1;
				this.nextToken = null;
			}
			else if(this.messagePointer < current.end()) {
				if(this.messagePointer < current.begin()) {
					this.nextToken = current;
					this.messagePointer = current.begin();
				} else {
					final String newTokenValue = this.message.getMessage().substring(index, current.end());
					this.nextToken = new MessageToken() {
						@Override
						public int begin() {
							return index;
						}

						@Override
						public int end() {
							return current.end();
						}

						@Nonnull
						@Override
						public String getValue() {
							return newTokenValue;
						}

						@Nonnull
						@Override
						public String getLowerCaseValue() {
							return newTokenValue.toLowerCase();
						}
					};
				}
				this.messageTokenIndex = i;
				return;
			}
		}
	}

	@Nonnull
	public Message getMessage() {
		return this.message;
	}

	@Nonnull
	Iterator<MessageToken> tokenIterator() {
		return new MessageTokenIterator(this.messageTokens, this.messageTokenIndex);
	}

	public int getMessagePointer() {
		return this.messagePointer;
	}

	@Nonnull
	public List<PatternNodeMatch> getMatches() {
		return this.matches;
	}

	@Nonnull
	public Optional<PatternNodeMatch> getLastMatch() {
		if(this.matches.isEmpty()) {
			if(this.parentBranch != null) {
				return this.parentBranch.getLastMatch();
			}
			return Optional.empty();
		}
		return Optional.of(this.matches.getLast());
	}

	public void consolidate() {
		if(this.parentBranch != null) {
			this.parentBranch.consolidate();
			final LinkedList<PatternNodeMatch> mergedMatches = new LinkedList<>(this.parentBranch.matches);
			mergedMatches.addAll(this.matches);
			this.matches = mergedMatches;
			this.parentBranch = null;
		}
	}

}
