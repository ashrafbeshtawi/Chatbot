package de.dailab.oven.api;

import de.dailab.oven.database.AbstractDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserHTTPTest extends AbstractDatabaseTest {

    @Autowired
    private MockMvc mockMvc;
    private java.time.LocalDateTime userName;


    @Override
    public void initialize() throws Throwable {

    }

    @Before
    public void setUserName() throws Exception {
		this.userName = java.time.LocalDateTime.now();

        this.mockMvc.perform(put("/oven/user/put")
                .contentType(APPLICATION_JSON)
                .content("{\n" +
                        "            \"name\": \"" + this.userName + "\",\n" +
                        "            \"preferredCategories\": [],\n" +
                        "            \"likesIngredients\": [],\n" +
                        "            \"incompatibleIngredients\": [],\n" +
                        "            \"cookedRecipeIDs\": [],\n" +
                        "            \"recipeRatings\": {},\n" +
                        "            \"currentlySpokenLanguage\": \"tr\",\n" +
                        "            \"spokenLanguages\": [\n" +
                        "                \"tr\"\n" +
                        "            ],\n" +
                        "            \"household\": \"GUEST\"\n" +			
                        "        }"
                ))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("name")))
                .andExpect(content().string(containsString("en")));
    }

    //normal request
    @Test
    public void U01_searchByEmptyUserID() throws Exception {
        this.mockMvc.perform(get("/oven/user/get").header("userName", ""))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("name")))
                .andExpect(content().string(containsString("preferredCategories")));
    }

    //normal request
    //@Test
    public void U01_searchByUnavailableUserID() throws Exception {
        this.mockMvc.perform(get("/oven/user/get").header("userName", "xxyxyxyxyxx"))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("userList")))
                .andExpect(content().string(not(containsString("preferredCategories"))));
    }

    //normal request
    @Test
    public void U01_searchByEmptyUserName() throws Exception {
        this.mockMvc.perform(get("/oven/user/get").header("userID", ""))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("name")))
                .andExpect(content().string(containsString("preferredCategories")));
    }

    //normal request
    @Test
    public void U01_searchByUnavailableUserName() throws Exception {
        this.mockMvc.perform(get("/oven/user/get").header("userID", "xxyxyxyxyxx"))
                .andExpect(status().is(400))
                .andExpect(content().string(not(containsString("name"))))
                .andExpect(content().string(not(containsString("preferredCategories"))));
    }

    //normal request
    @Test
    public void U01_searchByUserNameAndUserID() throws Exception {
        this.mockMvc.perform(get("/oven/user/get").header("userID", "").header("userName", ""))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("name")))
                .andExpect(content().string(containsString("preferredCategories")));
    }

    //normal request
    //@Test
    public void U01_searchByUnavailableUserNameAndUserID() throws Exception {
        this.mockMvc.perform(get("/oven/user/get").header("userID", "112342424234242424").header("userName", "xx"))
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("userList")))
                .andExpect(content().string(not(containsString("preferredCategories"))));
    }

    //dont use a long
    @Test
    public void U01_searchByWrongUserID() throws Exception {
        this.mockMvc.perform(get("/oven/user/get").header("userID", "Test"))
                .andExpect(status().is(400))
                .andExpect(content().string(not(containsString("name"))))
                .andExpect(content().string(not(containsString("preferredCategories"))));
    }


    @Test
    public void U02_addWithoutName() throws Exception {
        this.mockMvc.perform(put("/oven/user/put")
                .contentType(APPLICATION_JSON)
                .content("{\n" +
                        "            \"preferredCategories\": [],\n" +
                        "            \"likesIngredients\": [],\n" +
                        "            \"incompatibleIngredients\": [],\n" +
                        "            \"cookedRecipeIDs\": [],\n" +
                        "            \"recipeRatings\": {},\n" +
                        "            \"currentlySpokenLanguage\": \"\",\n" +
                        "            \"spokenLanguages\": [\n" +
                        "                \"en\"\n" +
                        "            ],\n" +
                        "            \"household\": \"GUEST\"\n" +			
                        "        }"
                ))
                .andExpect(status().is(500));
    }

    //normal request
    @Test
    public void U03_addRating() throws Exception {
        this.mockMvc.perform(put("/oven/user/rate")
                .contentType(APPLICATION_JSON)
                .content("{" +
                        "\"userName\": \"" + this.userName + "\"," +
                        "\"rating\": 5," +
                        "\"recipeID\": 642" +
                        "}")
        )
                .andExpect(status().is(200))
                .andExpect(content().string(containsString("userList")))
                .andExpect(content().string(containsString("preferredCategories")));
    }

    //normal request
    @Test
    public void U03_addRatingWrongRatingToMuch() throws Exception {
        this.mockMvc.perform(put("/oven/user/rate")
                .contentType(APPLICATION_JSON)
                .content("{" +
                        "\"userName\": \"" + this.userName + "\"," +
                        "\"rating\": 11," +
                        "\"recipeID\": 1153" +
                        "}")
        )
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("Rating has to be between -10 and 10")));
    }

    //normal request
    @Test
    public void U03_addRatingWrongRatingToLess() throws Exception {
        this.mockMvc.perform(put("/oven/user/rate")
                .contentType(APPLICATION_JSON)
                .content("{" +
                        "\"userName\": \"" + this.userName + "\"," +
                        "\"rating\": -11," +
                        "\"recipeID\": 1153" +
                        "}")
        )
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("Rating has to be between -10 and 10")));
    }

    @Test
    public void U03_addRating_noUser() throws Exception {
        this.mockMvc.perform(put("/oven/user/rate")
                .contentType(APPLICATION_JSON)
                .content("{" +
                        "\"rating\": 5," +
                        "\"recipeID\": 31470" +
                        "}")
        )
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("Specify a User. Found Users:")));
    }

    @Test
    public void U03_addRating_noRating() throws Exception {
        this.mockMvc.perform(put("/oven/user/rate")
                .contentType(APPLICATION_JSON)
                .content("{" +
                        " \"userName\": \"" + this.userName + "\"," +
                        " \"recipeID\": 31470" +
                        "}")
        )
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("Rating and Recipe has to be set")));
    }

    @Test
    public void U03_addRating_noRecipe() throws Exception {
        this.mockMvc.perform(put("/oven/user/rate")
                .contentType(APPLICATION_JSON)
                .content("{" +
                        " \"userName\" : \"" + this.userName + "\"," +
                        " \"rating\": 5" +
                        "}")
        )
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("Rating and Recipe has to be set")));
    }

    @Test
    public void U03_addRating_wrongRecipe() throws Exception {
        this.mockMvc.perform(put("/oven/user/rate")
                .contentType(APPLICATION_JSON)
                .content("{" +
                        " \"userName\" : \"" + this.userName + "\"," +
                        " \"rating\": 5," +
                        "\"recipeID\": -64" +
                        "}")
        )
                .andExpect(status().is(500))
                .andExpect(content().string(containsString("Error")))
                .andExpect(content().string(containsString("invalid user")));
    }
}

