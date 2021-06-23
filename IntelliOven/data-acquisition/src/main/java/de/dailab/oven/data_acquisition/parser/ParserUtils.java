package de.dailab.oven.data_acquisition.parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.dailab.oven.model.data_model.*;
import zone.bot.vici.Language;

/**
 * TODO add missing JavaDoc
 *
 * @author Hendrik Motza
 * @since 18.09
 */
public class ParserUtils {

    private static final Pattern fractionPattern = Pattern.compile("^(\\d)\\/(\\d{1,2})");
    private static final Pattern amountPattern = Pattern.compile("^\\d+([,\\.]\\d*)?");

    private static class AmountResult {
        private float amount;
        private String rawValue;
        
		public void setAmount(float amount) {
			this.amount = amount;
		}

		public void setRawValue(String rawValue) {
			this.rawValue = rawValue;
		}
    }

    private static class UnitResult {
        private Unit unit;
        private String rawValue;
    }

    private ParserUtils() {}

    public static List<IngredientWithAmount> parseIngredients(final List<String> entries, final String language) {
        final List<IngredientWithAmount> result = new ArrayList<>(entries.size());
        entries.forEach(e -> result.addAll(parseIngredient(e, language)));
        return result;
    }

    private static AmountResult parseAmount(final String input) {
        final AmountResult result = new AmountResult();
        final Matcher amountMatcher = amountPattern.matcher(input);
        final Matcher fractionMatcher = fractionPattern.matcher(input);
        if(fractionMatcher.find()) {
            final String fractionString = fractionMatcher.group();
            result.setAmount(Integer.parseInt(fractionMatcher.group(1)) / (float) Integer.parseInt(fractionMatcher.group(2)));
            result.setRawValue(input.substring(0, fractionString.length()));
        }
        else if(amountMatcher.find()) {
            final String amountString = amountMatcher.group().replace(',', '.');
            result.setAmount(Float.parseFloat(amountString));
            result.setRawValue(input.substring(0, amountString.length()));
        }
        else if(input.startsWith("¼")) {
            result.setAmount(0.25f);
            result.setRawValue(input.substring(0, 1));
        }
        else if(input.startsWith("½")) {
        	result.setAmount(0.5f);
        	result.setRawValue(input.substring(0, 1));
        }
        else if(input.startsWith("¾")) {
            result.amount = 0.75f;
            result.rawValue = input.substring(0, 1);
        }
        return result;
    }

    public static List<IngredientWithAmount> parseIngredient(final String input, final String language) {
        String partialContent = input.trim();
        // get amount
        final AmountResult amountResult = parseAmount(partialContent);
        if(amountResult.rawValue != null) {
            partialContent = partialContent.substring(amountResult.rawValue.length()).trim();
        }
        final StringTokenizer tokenizer = new StringTokenizer(partialContent);
        if(!tokenizer.hasMoreTokens()) {
            return Collections.emptyList();
        }
        final String nextToken = tokenizer.nextToken();
        if(nextToken.equals("x")) {
            // multiplied amount
            partialContent = partialContent.substring(1).trim();
            final AmountResult amountResult2 = parseAmount(partialContent);
            if(amountResult2.rawValue != null) {
                amountResult.amount *= amountResult2.amount;
                partialContent = partialContent.substring(amountResult2.rawValue.length()).trim();
            }
        }
        else if(nextToken.startsWith("-")) {
            // range
            partialContent = partialContent.substring(1).trim();
            final AmountResult amountResultMax = parseAmount(partialContent);
            if(amountResultMax.rawValue != null) {
                partialContent = partialContent.substring(amountResultMax.rawValue.length()).trim();
            }
        }
        // get unit
        Unit unit = Unit.UNDEF;
        if(amountResult.rawValue != null) {
            unit = Unit.PIECES;
            final UnitResult unitResult = findUnitAbbreviationsMatch(partialContent);
            if(unitResult.rawValue != null) {
                unit = unitResult.unit;
                partialContent = partialContent.substring(unitResult.rawValue.length()).trim();
            }
        }
        
        final Ingredient ingredient = new Ingredient(partialContent.replace(",", ""), Collections.emptyMap(), Language.getLanguage(language));
        return Collections.singletonList(new IngredientWithAmount(ingredient, amountResult.amount, unit));
    }

    private static UnitResult findUnitAbbreviationsMatch(final String input) {
        final UnitResult result = new UnitResult();
        final StringTokenizer tokenizer = new StringTokenizer(input);
        final List<String> tokens = new LinkedList<>();
        while(tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken().toLowerCase());
        }
        final Unit[] units = Unit.values();
        for(final Unit unit : units) {
            for(final String abbreviation : unit.getUnitAbbrevations()) {
                StringTokenizer abbrTokenizer = new StringTokenizer(abbreviation);
                boolean match = true;
                for(int i=0;abbrTokenizer.hasMoreTokens();i++) {
                    if(tokens.size()-1 < i || !tokens.get(i).equalsIgnoreCase(abbrTokenizer.nextToken())) {
                        match = false;
                        break;
                    }
                }
                if(match) {
                    result.unit = unit;
                    String restOfInput = input;
                    abbrTokenizer = new StringTokenizer(abbreviation);
                    while(abbrTokenizer.hasMoreTokens()) {
                        restOfInput = restOfInput.trim().substring(abbrTokenizer.nextToken().length());
                    }
                    result.rawValue = input.substring(0, input.length()-restOfInput.length());
                    return result;
                }
            }
        }
        return result;
    }


}
