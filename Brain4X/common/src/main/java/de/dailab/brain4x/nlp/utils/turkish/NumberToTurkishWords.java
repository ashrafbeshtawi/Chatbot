package de.dailab.brain4x.nlp.utils.turkish;

import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberToTurkishWords implements BiFunction<String, Language, String> {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)([,.](\\d+))?");

    private static final Map<String, String> NUMBER_NAME_MAP = new HashMap<>();

    static {
        NUMBER_NAME_MAP.put("0", "sıfır");
        NUMBER_NAME_MAP.put("1", "bir");
        NUMBER_NAME_MAP.put("2", "iki");
        NUMBER_NAME_MAP.put("3", "uç");
        NUMBER_NAME_MAP.put("4", "dört");
        NUMBER_NAME_MAP.put("5", "beş");
        NUMBER_NAME_MAP.put("6", "altı");
        NUMBER_NAME_MAP.put("7", "yedi");
        NUMBER_NAME_MAP.put("8", "sekiz");
        NUMBER_NAME_MAP.put("9", "dokuz");
        NUMBER_NAME_MAP.put("10", "on");
        NUMBER_NAME_MAP.put("20", "yirmi");
        NUMBER_NAME_MAP.put("30", "otuz");
        NUMBER_NAME_MAP.put("40", "kırk");
        NUMBER_NAME_MAP.put("50", "elli");
        NUMBER_NAME_MAP.put("60", "altmış");
        NUMBER_NAME_MAP.put("70", "yetmiş");
        NUMBER_NAME_MAP.put("80", "seksen");
        NUMBER_NAME_MAP.put("90", "doksan");
    }

    @Override
    public String apply(@Nonnull final String input, @Nonnull final Language language) {
        if (!Language.TURKISH.equals(language)) {
            return input;
        }
        String result = input;
        int offset = 0;
        final Matcher matcher = NUMBER_PATTERN.matcher(input);
        while (matcher.find()) {
            final String number = matcher.group();
            final String numberBeforeComma = matcher.group(1);
            final String numberAfterComma = matcher.group(3);
            String replacement = replaceNumber(numberBeforeComma, false).trim();
            if (countLeadingZeros(numberBeforeComma) == numberBeforeComma.length()) {
                replacement = "sıfır";
            }
            if (numberAfterComma != null && countLeadingZeros(numberAfterComma) != numberAfterComma.length()) {
                replacement += " virgul " + replaceNumber(numberAfterComma, true);
            }
            result = result.substring(0, matcher.start() + offset) + replacement + result.substring(matcher.end() + offset);
            offset += replacement.length() - number.length();
        }
        return result;
    }

    private static String replaceNumber(@Nonnull final String input, final boolean afterComma) {
        final StringBuilder sb = new StringBuilder();
        String number = input;
        if (afterComma) {
            while (!number.isEmpty() && number.charAt(number.length() - 1) == '0') {
                number = number.substring(0, number.length() - 1);
            }
        }
        final int numberOfLeadingZeros = countLeadingZeros(number);
        number = number.substring(numberOfLeadingZeros);
        if (afterComma && numberOfLeadingZeros > 0) {
            for (int i = 0; i < numberOfLeadingZeros; i++) {
                sb.append("sıfır ");
            }
        }
        if (number.length() == 1) {
            sb.append(NUMBER_NAME_MAP.get(number));
        } else if (number.length() == 2) {
            sb.append(NUMBER_NAME_MAP.get(number.substring(0, 1))).append("0");
            if (number.charAt(1) != '0') {
                sb.append(" ").append(NUMBER_NAME_MAP.get(number.substring(1)));
            }
        } else if (number.length() == 3) {
            if (number.charAt(0) != '1') {
                sb.append(NUMBER_NAME_MAP.get(number.substring(0, 1))).append(" ");
            }
            sb.append("yuz ").append(replaceNumber(number.substring(1), false));
        } else if (number.length() > 3 && number.length() < 7) {
            if (number.length() != 4 || number.charAt(0) != '1') {
                sb.append(replaceNumber(number.substring(0, number.length() - 3), false)).append(" ");
            }
            sb.append("bin ").append(replaceNumber(number.substring(number.length() - 3), false));
        } else {
            return number;
        }
        return sb.toString();
    }

    private static int countLeadingZeros(@Nonnull final String input) {
        int numberOfLeadingZeros = 0;
        while (numberOfLeadingZeros < input.length() && input.charAt(numberOfLeadingZeros) == '0') {
            numberOfLeadingZeros++;
        }
        return numberOfLeadingZeros;
    }
}
