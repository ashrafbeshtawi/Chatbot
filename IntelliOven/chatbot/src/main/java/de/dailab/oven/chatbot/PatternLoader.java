package de.dailab.oven.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;
import zone.bot.vici.intent.Message;
import zone.bot.vici.intent.MessageTokenizer;
import zone.bot.vici.pattern.EqualValuesEntityValidator;
import zone.bot.vici.pattern.InputSample;
import zone.bot.vici.pattern.NamedEntitiesValidator;
import zone.bot.vici.pattern.matcher.BotPatternMatcher;
import zone.bot.vici.pattern.matcher.BotPatternMatcher.BotPatternMatcherResult;
import zone.bot.vici.pattern.model.BotPattern;
import zone.bot.vici.pattern.model.BotPatternContext;
import zone.bot.vici.pattern.parser.BotPatternParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

public class PatternLoader {

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(PatternLoader.class);
	@Nonnull
	private static final String DEFAULT_BASE_RESOURCE_PATH = "de/dailab/oven/chatbot";
	@Nonnull
	private static final String RES_PATH_RESPONSES = "%s/%s/%s.json";
	@Nonnull
	private static final ObjectMapper MAPPER = new ObjectMapper();
	@Nonnull
	private final BotPatternParser parser;
	@Nonnull
	private final String baseResourcePath;

	public PatternLoader() {
		this(DEFAULT_BASE_RESOURCE_PATH, new BotPatternContext());
	}

	public PatternLoader(@Nonnull final String baseResourcePath) {
		this(baseResourcePath, new BotPatternContext());
	}

	public PatternLoader(@Nonnull final String baseResourcePath, @Nonnull final BotPatternContext botPatternContext) {
		this.baseResourcePath = baseResourcePath;
		this.parser = new BotPatternParser(botPatternContext);
	}

	public void registerTemplates() throws IOException {
		for(final Language language : Language.getLanguages()) {
			final String resourcePath = String.format(RES_PATH_RESPONSES, this.baseResourcePath, language.getLangCode2(), "templates");
			final URL url = PatternLoader.class.getClassLoader().getResource(resourcePath);
			if(url == null) {
				continue;
			}
			LOG.info("Loading {} pattern template file", language.getName());
			try(final InputStream is = url.openStream()) {
				processTemplateResource(is);
			} catch(final IOException e) {
				throw new IOException("Could not parse pattern resource file: "+resourcePath, e);
			}
		}
	}

	public Map<Language, Set<BotPattern>> getPatterns(@Nonnull final String intentName) throws IOException {
		final Map<Language, Set<BotPattern>> intentPatternMap = new HashMap<>();
		for(final Language language : Language.getLanguages()) {
			final String resourcePath = String.format(RES_PATH_RESPONSES, this.baseResourcePath, language.getLangCode2(), intentName);
			final URL url = PatternLoader.class.getClassLoader().getResource(resourcePath);
			if(url == null) {
				intentPatternMap.put(language, Collections.emptySet());
				continue;
			}
			LOG.info("Loading {} pattern file for intent {}", language.getName(), intentName);
			try(final InputStream is = url.openStream()) {
				intentPatternMap.put(language, readResource(is, language));
			} catch(final IOException e) {
				throw new IOException("Could not parse pattern resource file: "+resourcePath, e);
			}
		}
		return intentPatternMap;
	}

	private void processTemplateResource(@Nonnull final InputStream is) throws IOException {
		final JsonNode root = MAPPER.readTree(new InputStreamReader(is, StandardCharsets.UTF_8));
		final Iterator<Entry<String, JsonNode>> iter = root.fields();
		while(iter.hasNext()) {
			final Entry<String, JsonNode> entry = iter.next();
			final String patternValue = entry.getValue().textValue();
			this.parser.parseAndRegisterTemplate(entry.getKey(), patternValue);
		}
	}

	private Set<BotPattern> readResource(@Nonnull final InputStream is, @Nonnull final Language language) throws IOException {
		final Set<BotPattern> patterns = new HashSet<>();
		final JsonNode root = MAPPER.readTree(new InputStreamReader(is, StandardCharsets.UTF_8));
		for(final JsonNode entry : root) {
			final String patternValue = entry.get("pattern").textValue();
			final BotPattern pattern = this.parser.parse(patternValue);
			final JsonNode samplesArrayNode = entry.get("samples");
			extractSamples(samplesArrayNode, language).forEach(pattern::addSample);
			patterns.add(pattern);
		}
		return patterns;
	}

	private static Set<InputSample> extractSamples(@Nullable final JsonNode samplesArrayNode, @Nonnull final Language language) {
		if(samplesArrayNode == null) {
			return Collections.emptySet();
		}
		final Set<InputSample> samples = new HashSet<>();
		for(final JsonNode sample : samplesArrayNode) {
			final String sampleValue = sample.get("sample").textValue();
			final List<NamedEntitiesValidator> validators = new LinkedList<>();
			final JsonNode entitiesObjectNode = sample.get("entities");
			if(entitiesObjectNode != null) {
				final Iterator<String> fieldNameIterator = entitiesObjectNode.fieldNames();
				while(fieldNameIterator.hasNext()) {
					final String entityName = fieldNameIterator.next();
					final JsonNode entityNode = entitiesObjectNode.get(entityName);
					if(entityNode.isArray()) {
						final List<String> expectedValues = new LinkedList<>();
						for(final JsonNode valueNode : entityNode) {
							expectedValues.add(valueNode.textValue());
						}
						validators.add(new EqualValuesEntityValidator(entityName, expectedValues));
					} else {
						validators.add(new EqualValuesEntityValidator(entityName, entityNode.textValue()));
					}
				}
			}
			samples.add(new InputSample(language, sampleValue, validators));
		}
		return samples;
	}

	public static void main(final String[] args) {
		final BotPatternParser parser = new BotPatternParser();
		final BotPattern pattern = parser.parse("_{allergy}+(und)");
		final BotPatternMatcher matcher = new BotPatternMatcher(pattern);
		final Message message = new Message() {
			@Nonnull
			@Override
			public String getMessage() {
				return "Nüsse, Gelatine und Lactose";
			}

			@Nonnull
			@Override
			public Language getLanguage() {
				return Language.GERMAN;
			}
		};
		final BotPatternMatcherResult matcherResult = matcher.match(message, new MessageTokenizer(message).getAllToken());
		//LOG.warn("Match {}", (matcherResult.isMatch() ? "Success" : "Fail"));
		System.out.println((matcherResult.isMatch() ? "Success" : "Fail"));
		System.out.println(matcherResult.getNamedEntities().get("allergy").get(0).getValue());
	}

}
