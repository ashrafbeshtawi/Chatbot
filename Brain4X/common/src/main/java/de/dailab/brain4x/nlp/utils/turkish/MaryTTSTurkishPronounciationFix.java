package de.dailab.brain4x.nlp.utils.turkish;

import zone.bot.vici.Language;

import java.util.function.BiFunction;

/**
 * Turkish voices in MaryTTS do not pronounce some words correctly. This implementation changes the words so that pronounciation will be better.
 *
 * @author Hendrik Motza
 * @since 19.10
 */
public class MaryTTSTurkishPronounciationFix implements BiFunction<String, Language, String> {

    @Override
    public String apply(final String input, final Language language) {
        if (!Language.TURKISH.equals(language)) {
            return input;
        }
        return input.replaceAll("ü", "u").replaceAll("Ü", "u");
    }
}
