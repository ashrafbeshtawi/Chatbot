package de.dailab.oven.api.helper.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import zone.bot.vici.Language;

import java.io.IOException;

public class LanguageSerializer extends StdSerializer<Language> {

    public LanguageSerializer() {
        this(null);
    }

    public LanguageSerializer(final Class<Language> t) {
        super(t);
    }

    @Override
    public void serialize(final Language language, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(language.getLangCode2());
    }
}
