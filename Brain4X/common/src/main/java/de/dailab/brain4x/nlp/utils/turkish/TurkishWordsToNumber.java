package de.dailab.brain4x.nlp.utils.turkish;

import java.util.*;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;

import zone.bot.vici.Language;

/**
 * This tool converts numbers written as turkish words back into a numeric representation.
 *
 * @author Hendrik Motza
 * @since 20.01
 */
public class TurkishWordsToNumber implements BiFunction<String, Language, String> {

    public static int wordsToInt(@Nonnull final String input) {
        Objects.requireNonNull(input, "Parameter 'input' must not be null");
        final Map<String, String> digits = new HashMap<String, String>();
        digits.put("sıfır", "0");
        digits.put("bir", "1");
        digits.put("iki", "2");
        digits.put("üç", "3");
        digits.put("dört", "4");
        digits.put("beş", "5");
        digits.put("altı", "6");
        digits.put("yedi", "7");
        digits.put("sekiz", "8");
        digits.put("dokuz", "9");
        digits.put("on", "10");
        digits.put("yirmi", "20");
        digits.put("otuz", "30");
        digits.put("kırk", "40");
        digits.put("elli", "50");
        digits.put("altmış", "60");
        digits.put("yetmiş", "70");
        digits.put("seksen", "80");
        digits.put("doksan", "90");
        final String[] words = input.split(" ");
        int number = 0;
        if("yüz".equals(words[0])){
            number += 100;
            for(int i=1;i<words.length;i++){
                number += Integer.parseInt(digits.get(words[i]));
            }
        }
        else if(words.length>1 && "yüz".equals(words[1])){
            number += Integer.parseInt(digits.get(words[0]))*100;
            for(int i=2;i<words.length;i++){
                number += Integer.parseInt(digits.get(words[i]));
            }
        }
        else{
            for(final String word : words) {
                number += Integer.parseInt(digits.get(word));
            }
        }
        return number;
    }

    @Override
    public String apply(final String input, final Language language) {
        if(!Language.TURKISH.equals(language)) {
            return input;
        }
        try {
            return String.valueOf(Integer.parseInt(input));
        } catch (final NumberFormatException e) {
            return String.valueOf(wordsToInt(input));
        }
    }
}
