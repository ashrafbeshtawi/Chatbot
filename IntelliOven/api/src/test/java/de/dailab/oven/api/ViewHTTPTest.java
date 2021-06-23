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
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ViewHTTPTest extends AbstractDatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Override
    public void initialize() throws Throwable {

    }

    //V01
    @Test
    public void V01() throws Exception {
        this.mockMvc.perform(get("/oven/view/get/-120"))
                .andExpect(status().is(200))
                .andExpect(content().string(not(containsString("error"))))
                .andExpect(content().string(containsString("{\"conversationList\":[]}")))
        ;
    }

    //V02
    @Test
    public void V02() throws Exception {
        this.mockMvc.perform(put("/oven/view/change/-120/1"))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("\"recipeList\":[],")))
                .andExpect(content().string(containsString("selection\":{")))

                .andExpect(content().string(containsString("\"recipeID\":-1,")))
                .andExpect(content().string(containsString("\"recipeIndex\":-1,")))

                .andExpect(content().string(containsString("\"recipeDetailJSONKey\":\"\"")))
                .andExpect(content().string(containsString("\"recipeDetailIndex\":-1")))
        ;
    }

    //V03
    @Test
    public void V03() throws Exception {
        this.mockMvc.perform(post("/oven/view/navigation/up/-120"))
                .andExpect(status().is(422))
                .andExpect(content().string(not(containsString("error"))))
                .andExpect(content().string(containsString("")));
    }

    //V04
    @Test
    public void V04() throws Exception {
        this.mockMvc.perform(put("/oven/view/set/0/99"))
                .andExpect(status().is(422))
                .andExpect(content().string(not(containsString("error"))))
                .andExpect(content().string(containsString("")));
    }

}

