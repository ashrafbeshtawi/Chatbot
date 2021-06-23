package zone.bot.vici;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents a language specified by language codes defined in ISO-639-*
 *
 * @author Hendrik Motza
 * @since 2018.03
 */
public final class Language {

    @Nonnull
    private static final Pattern PATTERN_CODE2 = Pattern.compile("^[a-z]{2}$");
    @Nonnull
    private static final Pattern PATTERN_CODE3 = Pattern.compile("^[a-z]{3}$");

    @Nonnull
    private static final Map<String, Language> LANGUAGE_MAP = new HashMap<>();
    @Nonnull
    private static final Set<Language> LANGUAGES = new HashSet<>();

    @Nonnull
    public static final Language ENGLISH = Language.registerLanguage("English", "en", "eng");
    @Nonnull
    public static final Language GERMAN = Language.registerLanguage("German", "de", "deu");
    @Nonnull
    public static final Language TURKISH = Language.registerLanguage("Turkish", "tr", "tur");

    @Nonnull
    public static final Language UNDEF = new Language("UNDEF", "", "");

    @Nonnull
    private final String name;
    @Nonnull
    private final String langCode2;
    @Nonnull
    private final String langCode3;

    private Language(@Nonnull final String name, @Nonnull final String langCode2, @Nonnull final String langCode3) {
        this.name = name;
        this.langCode2 = langCode2;
        this.langCode3 = langCode3;
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nonnull
    public String getLangCode2() {
        return this.langCode2;
    }

    @Nonnull
    public String getLangCode3() {
        return this.langCode3;
    }

    /**
     * Get a language by its language code.
     *
     * @param langCode 2- or 3-digit language code of the requested language, {@code null} disallowed
     * @return language or instance of {@link #UNDEF} if no language got registered with the specified langCode
     */
    @Nonnull
    public static synchronized Language getLanguage(@Nonnull final String langCode) {
        final Language language = LANGUAGE_MAP.get(Objects.requireNonNull(langCode, "Parameter 'langCode' must not be null"));
        return language == null ? UNDEF : language;
    }

    /**
     * Get all registered languages.
     *
     * @return Set of all registered languages
     */
    @Nonnull
    public static synchronized Set<Language> getLanguages() {
        return Collections.unmodifiableSet(LANGUAGES);
    }

    /**
     * Register a new language.
     *
     * @param name Label for the specified language, {@code null} and empty string disallowed
     * @param langCode2 language code for the specified language as defined in ISO-639-1 (2 lowercase latin letters)
     * @param langCode3 language code for the specified language as defined in ISO-639-2/T (3 lowercase latin letters)
     * @return instance of the new registered language
     * @throws IllegalArgumentException Thrown if parameters do not match the required formats or if one of the language codes is already registered
     */
    @Nonnull
    public static synchronized Language registerLanguage(@Nonnull final String name, @Nonnull final String langCode2, @Nonnull final String langCode3) {
        Objects.requireNonNull(name, "Parameter 'name' must not be null");
        Objects.requireNonNull(langCode2, "Parameter 'langCode2' must not be null");
        Objects.requireNonNull(langCode3, "Parameter 'langCode3' must not be null");
        if(name.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'name' must not be empty");
        }
        if(!PATTERN_CODE2.matcher(langCode2).matches()) {
            throw new IllegalArgumentException("Parameter 'langCode2' does not match the required format");
        }
        if(!PATTERN_CODE3.matcher(langCode3).matches()) {
            throw new IllegalArgumentException("Parameter 'langCode3' does not match the required format");
        }
        if(LANGUAGE_MAP.containsKey(langCode2)) {
            throw new IllegalArgumentException("Language for language code '"+langCode2+"' already registered");
        }
        if(LANGUAGE_MAP.containsKey(langCode3)) {
            throw new IllegalArgumentException("Language for language code '"+langCode3+"' already registered");
        }
        final Language language = new Language(name, langCode2, langCode3);
        LANGUAGE_MAP.put(langCode2, language);
        LANGUAGE_MAP.put(langCode3, language);
        LANGUAGES.add(language);
        return language;
    }

    @Override
    public String toString() {
        return "Language{" +
                "name='" + this.name + '\'' +
                ", langCode2='" + this.langCode2 + '\'' +
                ", langCode3='" + this.langCode3 + '\'' +
                '}';
    }
}
