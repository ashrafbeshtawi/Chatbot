package de.dailab.oven.model.util;

import de.dailab.oven.model.data_model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UnitTranslationsUtil {

	public static class UnitTranslationData {

		@Nonnull
		private final String singular;
		@Nonnull
		private final String plural;
		@Nonnull
		private final String abbreviation;
		@Nonnull
		private final String[] abbreviations;

		public UnitTranslationData(@Nonnull final String singular, @Nonnull final String plural, @Nonnull final String abbreviation, @Nonnull final String[] abbreviations) {
			this.singular = singular;
			this.plural = plural;
			this.abbreviation = abbreviation;
			this.abbreviations = abbreviations;
		}

		@Nonnull
		public String getSingular() {
			return this.singular;
		}

		@Nonnull
		public String getPlural() {
			return this.plural;
		}

		@Nonnull
		public String getAbbreviation() {
			return this.abbreviation;
		}

		@Nonnull
		public String[] getAbbreviations() {
			return this.abbreviations;
		}

	}

	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(UnitTranslationsUtil.class);
	@Nonnull
	private static final String RES_PATH = "de/dailab/oven/model/%s/Units.csv";
	@Nonnull
	private static final Map<Language, Map<String, UnitTranslationData>> dataMap = new HashMap<>();

	static {
		for(final Language language : Language.getLanguages()) {
			final Map<String, UnitTranslationData> unitMap = new HashMap<>();
			dataMap.put(language, unitMap);
			final String resourcePath = String.format(RES_PATH, language.getLangCode2());
			final InputStream is = Unit.class.getClassLoader().getResourceAsStream(resourcePath);
			if(is == null) {
				LOG.warn("No unit translations found for language: {}", language.getName());
				continue;
			}
			try (final BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
				String line;
				while ((line = br.readLine()) != null) {
					final String[] values = line.split(";");
					if(values.length<=1) {
						continue;
					}
					final String id = values[0];
					final String singular = values.length<=2 ? id : values[1];
					final String plural = values.length<=3 ? id : values[2];
					final String abbreviation = values.length<=4 ? id : values[3];
					final String[] abbreviations = values.length<=5 ? new String[0] : Arrays.copyOfRange(values, 4, values.length);
					unitMap.put(id, new UnitTranslationData(singular, plural, abbreviation, abbreviations));
				}
			}
			catch (final IOException e) {
				LOG.error("Could not load properties file with response templates from resource path: {}", resourcePath, e);
			}
		}
	}

	private UnitTranslationsUtil() { }

	public static UnitTranslationData getUnitLabels(@Nonnull final Language language, @Nonnull final String unitName) {
		return dataMap.computeIfAbsent(language, (k) -> new HashMap<>()).computeIfAbsent(unitName, (id) -> new UnitTranslationData(id, id, id, new String[0]));
	}

}
