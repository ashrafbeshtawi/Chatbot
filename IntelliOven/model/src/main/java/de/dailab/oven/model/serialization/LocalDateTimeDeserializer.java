package de.dailab.oven.model.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    public LocalDateTimeDeserializer() {
        this(null);
    }

    public LocalDateTimeDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDateTime deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        final String value = node.textValue();
        final String[] splittedDate = value.split("-");
        if(splittedDate.length != 3) {
            throw new IllegalArgumentException("The value '"+value+"' does not match the expected date format 'YYYY-MM-DD'");
        }
        final int year = Integer.parseInt(splittedDate[0]);
        final int month = Integer.parseInt(splittedDate[1]);
        final int day = Integer.parseInt(splittedDate[2]);
        return LocalDateTime.of(year, month, day, 0, 0, 0, 0);
    }
}
