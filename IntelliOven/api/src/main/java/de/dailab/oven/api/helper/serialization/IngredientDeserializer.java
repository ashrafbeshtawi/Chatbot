package de.dailab.oven.api.helper.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import de.dailab.oven.model.data_model.Ingredient;
import zone.bot.vici.Language;

import java.io.IOException;

public class IngredientDeserializer extends StdDeserializer<Ingredient> {

	public IngredientDeserializer() {
        this(null);
    }

    public IngredientDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public Ingredient deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Ingredient i = new Ingredient(node.get("name").asText(), Language.getLanguage(node.get("language").asText()));
        final JsonNode idNode = node.get("id");
        if(idNode != null && !idNode.isNull()) {
            i.setID(idNode.asLong());
        }
        return i;
    }

}
