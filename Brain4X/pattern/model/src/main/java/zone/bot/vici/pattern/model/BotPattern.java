package zone.bot.vici.pattern.model;

import zone.bot.vici.pattern.InputSample;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BotPattern {

	@Nonnull
	private final BotPatternNode patternRootNode;
	@Nonnull
	private final BotPatternContext context;
	@Nonnull
	private final Map<String, String> staticEntities = new HashMap<>();
	@Nonnull
	private final List<InputSample> samples = new LinkedList<>();

	public BotPattern(@Nonnull final BotPatternNode pattern, @Nonnull final BotPatternContext context) {
		this.patternRootNode = pattern;
		this.context = context;
	}

	@Nonnull
	public BotPatternNode getPatternRootNode() {
		return this.patternRootNode;
	}

	@Nonnull
	public BotPatternContext getContext() {
		return this.context;
	}

	public void addStaticEntity(@Nonnull final String name, @Nonnull final String value) {
		this.staticEntities.put(name, value);
	}

	@Nonnull
	public Map<String, String> getStaticEntities() {
		return this.staticEntities;
	}

	public void addSample(@Nonnull final InputSample sample) {
		this.samples.add(sample);
	}

	@Nonnull
	public List<InputSample> getSamples() {
		return this.samples;
	}

}
