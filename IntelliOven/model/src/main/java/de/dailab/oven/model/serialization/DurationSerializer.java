package de.dailab.oven.model.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.dailab.oven.model.data_model.Unit;

import java.io.IOException;
import java.time.Duration;

public class DurationSerializer extends StdSerializer<Duration> {

    public DurationSerializer() {
        this(null);
    }

    public DurationSerializer(final Class<Duration> t) {
        super(t);
    }

    @Override
    public void serialize(final Duration duration, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeNumber(duration.toMillis()/1000);
    }
}
