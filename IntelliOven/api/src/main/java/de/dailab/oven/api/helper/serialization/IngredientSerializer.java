package de.dailab.oven.api.helper.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.dailab.oven.model.data_model.Ingredient;
import java.io.IOException;

public class IngredientSerializer extends StdSerializer<Ingredient> {

	public IngredientSerializer() {
        this(null);
    }

    public IngredientSerializer(final Class<Ingredient> t) {
        super(t);
    }

    @Override
    public void serialize(final Ingredient ingredient, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
    	jsonGenerator.writeStartObject();
    	jsonGenerator.writeStringField("name", ingredient.getName());
    	jsonGenerator.writeStringField("language", ingredient.getLanguage().getLangCode2());
    	jsonGenerator.writeNumberField("id", ingredient.getID());
    	jsonGenerator.writeEndObject();
    }
}
