package de.dailab.oven.api.helper.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import zone.bot.vici.Language;

import java.io.IOException;

public class LanguageDeserializer extends StdDeserializer<Language> {

    public LanguageDeserializer() {
        this(null);
    }

    public LanguageDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public Language deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return Language.getLanguage(node.asText());
    }
}
