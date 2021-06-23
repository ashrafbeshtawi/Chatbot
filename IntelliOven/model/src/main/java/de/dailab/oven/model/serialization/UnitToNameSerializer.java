package de.dailab.oven.model.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.dailab.oven.model.data_model.Unit;

import java.io.IOException;

public class UnitToNameSerializer extends StdSerializer<Unit> {

    public UnitToNameSerializer() {
        this(null);
    }

    public UnitToNameSerializer(final Class<Unit> t) {
        super(t);
    }

    @Override
    public void serialize(final Unit unit, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(unit.name());
    }
}
