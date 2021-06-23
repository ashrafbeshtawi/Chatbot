package de.dailab.oven.api;

import de.dailab.oven.controller.DatabaseController;
import de.dailab.oven.database.AbstractDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RecipeHTTPTest extends AbstractDatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Override
    public void initialize() throws Throwable {

    }

    @Before
    public void R00_wait() {
        try {
            sleep(5000);
        } catch (final Exception e) {
//empty
        }
    }

    @Test
    public void testLowerRatingByOneValidUser(){
        //invalid recipe
        boolean succ = true;
        try {
            DatabaseController.getInstance().lowerRatingByOne(1263, 20999999);
        }catch ( final Exception e){
            succ = false;
        }
        assertTrue(succ);
    }

    @Test
    public void testLowerRatingByOneInvalidUser(){
        //invalid recipe and user id
        boolean succ = true;
        try {
            DatabaseController.getInstance().lowerRatingByOne(1263999, 20);
        }catch ( final Exception e){
            succ = false;
        }
        assertTrue(succ);
    }

    @Test
    public void testLowerRatingByOneValid(){
        //valid user and recipe id
        boolean succ = true;
        try {
            DatabaseController.getInstance().lowerRatingByOne(3972, 70);
        }catch ( final Exception e){
            succ = false;
        }
        assertTrue(succ);
    }

    @Test
    public void contextLoads(){

    }

    //normal request
    //@Test
    public void R00_POST_Request() throws Exception {
        this.mockMvc.perform(post("/oven/recipe/get")
                .contentType(APPLICATION_JSON)
                .content("{\n" +
                        "\"persons\":5,\n" +
                        "\"recipeFilter\":{\n" +
                        "\"recipeName\":\"c\",\n" +
                        "\"maxNumberOfRecipesToParsePerLanguage\":1\n" +
                        "},\n" +
                        "\"userID\":1521\n" +
                        "}\n")
        )
                .andExpect(status().is(200))
                .andExpect(content().string(not(containsString("error"))))
                .andExpect(content().string(not(containsString("author"))));
    }

    //normal request
    @Test
    public void R00_POST_RequestRecommender() throws Exception {
        this.mockMvc.perform(post("/oven/recipe/get")
                .contentType(APPLICATION_JSON)
                .content("{\n" +
                        "\"persons\":5,\n" +
                        "\"recipeFilter\":{\n" +
                        "},\n" +
                        "\"userID\":3860,\n" +
                        "\"contentBasedRecommendation\": true"+
                        "}\n")
        )
                .andExpect(status().is(200))
                .andExpect(content().string(not(containsString("error"))))
                .andExpect(content().string(containsString("author")));
    }

    //no recipeFilter
    @Test
    public void R00_POST_Request_NorecipeFilter() throws Exception {
        this.mockMvc.perform(post("/oven/recipe/get")
                .contentType(APPLICATION_JSON)
                .content("{\n" +
                        "    \"userID\": 0\n" +
                        "}")
        )
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("RecipeFilter can't be empty")))
        ;
    }

    @Test
    public void TestRecipeImage() throws Exception {
        this.mockMvc.perform(get("/oven/recipe/image/"+1027))
                .andExpect(status().is(500));
    }

    @Test
    public void TestGetExample() throws Exception {
        this.mockMvc.perform(post("/oven/recipe/getExample")
                .contentType(APPLICATION_JSON)
                .content("{\n" +
                        "\"persons\":5,\n" +
                        "\"recipeFilter\":{\n" +
                        "\"recipeName\":\"c\",\n" +
                        "\"maxNumberOfRecipesToParsePerLanguage\":1\n" +
                        "},\n" +
                        "\"userID\":1521\n" +
                        "}\n")
        )
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("1521")))
                .andExpect(content().string(containsString("Recommendation")));
    }

    @Test
    public void TestGetExampleEmpty() throws Exception {
        this.mockMvc.perform(post("/oven/recipe/getExample")
                .contentType(APPLICATION_JSON)
                .content("{}")
        )
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("Hendrik")))
                .andExpect(content().string(containsString("Recommendation")));
    }

}
