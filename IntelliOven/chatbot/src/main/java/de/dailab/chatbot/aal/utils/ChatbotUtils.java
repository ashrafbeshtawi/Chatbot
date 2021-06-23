package de.dailab.chatbot.aal.utils;

import de.dailab.oven.model.data_model.Ingredient;
import de.dailab.oven.model.data_model.IngredientWithAmount;
import zone.bot.vici.Language;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatbotUtils {
    
    private ChatbotUtils(){}
    
    public static String ingredientListAsNiceString(final List<IngredientWithAmount> ingredients, final Language language) {
        final ArrayList<String> ingredientsList = new ArrayList<>();
        final DecimalFormat df = new DecimalFormat("#.##");
        
        //Stream is not working anymore
        for(final IngredientWithAmount ingredientEntry : ingredients) {
            final Ingredient ingredient = ingredientEntry.getIngredient();
            String amountAndUnit = "";
            if(ingredientEntry.getQuantity() >= 0.005) {
                final String roundedAmount = df.format(ingredientEntry.getQuantity());
                final String unitName = ingredientEntry.getUnit().getPluralLabel(language);
                amountAndUnit = String.format("%s %s ", roundedAmount, unitName);
            }
            ingredientsList.add(amountAndUnit + ingredient.getName());
        }
        return String.join(", ", ingredientsList);
    }

}
