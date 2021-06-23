package de.dailab.oven.api;

import de.dailab.oven.database.AbstractDatabaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OvenHTTPTest extends AbstractDatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Override
    public void initialize() {

    }

    //normal request
    @Test
    public void O01_GetOvenStatus() throws Exception {
        this.mockMvc.perform(get("/oven/oven/get"))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("CELSIUS")))
                .andExpect(content().string(containsString("lightOn")))
                .andExpect(content().string(containsString("off")));
    }

    //normal request
    @Test
    public void O02_SetProgram() throws Exception {
        this.mockMvc.perform(post("/oven/oven/setProgram")
                .contentType(APPLICATION_JSON)
                .content("{\n" +
                        "\"ovenMode\": \"clean\",\n" +
                        "\"temperature\":{\n" +
                        "\"temperatureUnit\": \"FAHRENHEIT\",\n" +
                        "\"temp\": -20\n" +
                        "}\n" +
                        "}"))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("-20")))
                .andExpect(content().string(containsString("FAHRENHEIT")))
                .andExpect(content().string(containsString("off")));
    }

    //Error, OvenMode not found
    @Test
    public void O02_SetProgramERROR() throws Exception {
        this.mockMvc.perform(post("/oven/oven/setProgram")
                .contentType(APPLICATION_JSON)
                .content("{\n" +
                        "\"ovenMode\": \"notavaiable\",\n" +
                        "\"temperature\":{\n" +
                        "\"temperatureUnit\": \"FAHRENHEIT\",\n" +
                        "\"temp\": -20\n" +
                        "}\n" +
                        "}"))
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("Unprocessable Entity")));
    }

}

