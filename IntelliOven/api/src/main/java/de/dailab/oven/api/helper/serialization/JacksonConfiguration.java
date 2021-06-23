package de.dailab.oven.api.helper.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.dailab.oven.model.data_model.Ingredient;

import de.dailab.oven.model.serialization.LocalDateTimeDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import zone.bot.vici.Language;

import java.time.LocalDateTime;

@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addSerializer(Language.class, new LanguageSerializer());
        module.addDeserializer(Language.class, new LanguageDeserializer());
        module.addSerializer(Ingredient.class, new IngredientSerializer());
        module.addDeserializer(Ingredient.class, new IngredientDeserializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.configure(objectMapper());
        return builder;
    }

}
