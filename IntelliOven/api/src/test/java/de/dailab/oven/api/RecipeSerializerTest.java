package de.dailab.oven.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dailab.oven.api.helper.serialization.JacksonConfiguration;
import de.dailab.oven.model.data_model.Recipe;
import org.junit.Test;

import javax.annotation.Nonnull;

public class RecipeSerializerTest {

	@Nonnull
	private final ObjectMapper mapper = new JacksonConfiguration().objectMapper();

//	@Test
	public void test() throws JsonProcessingException {
		final String recipeAsJson = "{\n" +
				"    \"author\": \"hendrik\",\n" +
				"    \"language\": \"de\",\n" +
				"    \"name\": \"test1\",\n" +
				"    \"totalDuration\": 5,\n" +
				"    \"ingredients\": {},\n" +
				"    \"instructions\": [\n" +
				"        \"Step 1\",\n" +
				"        \"Step 2\"\n" +
				"    ]\n" +
				"}";
		final Recipe recipe = this.mapper.readValue(recipeAsJson, Recipe.class);
	}

}
