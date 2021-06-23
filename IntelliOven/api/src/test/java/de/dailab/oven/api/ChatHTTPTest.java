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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ChatHTTPTest extends AbstractDatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Override
    public void initialize() {

    }

    //normal request
    @Test
    public void C00_POST_Request() throws Exception {
        this.mockMvc.perform(post("/oven/chat/")
                .contentType(APPLICATION_JSON)
                .content(
                        "{\n" +
                                "    \"chatInputMessages\": [\n" +
                                "        {\n" +
                                "            \"message\": \"Hallo Chatbot\",\n" +
                                "            \"language\": \"tr\",\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chatUserMatches\": [\n" +
                                "        {\n" +
                                "            \"userID\": 66,\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ]\n" +
                                "}"
                )

        )
                .andExpect(status().is(200))
                .andExpect(content().string(not(containsString("Error"))))
                .andExpect(content().string(containsString("Accepted")))
        ;
    }

    //normal request, TextArray
    @Test
    public void C00_POST_RequestTextArray() throws Exception {
        this.mockMvc.perform(post("/oven/chat/")
                .contentType(APPLICATION_JSON)
                .content(
                        "{\n" +
                                "    \"chatInputMessages\": [\n" +
                                "        {\n" +
                                "            \"message\": \"Cello Fettbock!\",\n" +
                                "            \"language\": \"en\",\n" +
                                "            \"probability\": 10\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"message\": \"Hallo Chatbot\",\n" +
                                "            \"language\": \"de\",\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chatUserMatches\": [\n" +
                                "        {\n" +
                                "            \"userID\": 9,\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ]\n" +
                                "}"
                )
        )
                .andExpect(status().is(200))
                .andExpect(content().string(not(containsString("Error"))))
                .andExpect(content().string(containsString("Accepted")))
        ;
    }

    @Test
    public void C00_POST_RequestWithEmptyUserObject() throws Exception {
        this.mockMvc.perform(post("/oven/chat/")
                .contentType(APPLICATION_JSON)
                .content(
                        "{\n" +
                                "    \"chatInputMessages\": [\n" +
                                "        {\n" +
                                "            \"message\": \"Cello Fettbock!\",\n" +
                                "            \"language\": \"en\",\n" +
                                "            \"probability\": 10\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"message\": \"Hallo Chatbot\",\n" +
                                "            \"language\": \"de\",\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chatUserMatches\": [\n" +
                                "        {}\n" +
                                "    ]\n" +
                                "}"
                )
        )
                //SHOULD WOKR, only commented due to Tests
                .andExpect(status().is(200))
                .andExpect(content().string(not(containsString("Error"))))
                .andExpect(content().string(containsString("Accepted")));
    }

    @Test
    public void C00_POST_RequestWithoutUserObject() throws Exception {
        this.mockMvc.perform(post("/oven/chat/")
                .contentType(APPLICATION_JSON)
                .content(
                        "{\n" +
                                "    \"chatInputMessages\": [\n" +
                                "        {\n" +
                                "            \"message\": \"Cello Fettbock!\",\n" +
                                "            \"language\": \"en\",\n" +
                                "            \"probability\": 10\n" +
                                "        },\n" +
                                "        {\n" +
                                "            \"message\": \"Hallo Chatbot\",\n" +
                                "            \"language\": \"de\",\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chatUserMatches\": []\n" +
                                "}"
                )
        )
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("body or user is missing")));
    }


    //request without text, ERROR
    @Test
    public void C00_POST_RequestWithWrongUserID() throws Exception {
        this.mockMvc.perform(post("/oven/chat/")
                .contentType(APPLICATION_JSON)
                .content(
                        "{\n" +
                                "    \"chatInputMessages\": [\n" +
                                "        {\n" +
                                "            \"message\": \"Hallo Chatbot\",\n" +
                                "            \"language\": \"de\",\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chatUserMaC00_POST_RequestWithWrongUserIDtches\": [\n" +
                                "        {\n" +
                                "            \"userID\": \"Leon\",\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ]\n" +
                                "}"
                )
        )
                .andExpect(status().is(200))
                .andExpect(content().string(not(containsString("Error"))))
                .andExpect(content().string(containsString("{\"response\":\"Accepted\"}")));
    }

    //request without body, ERROR
    @Test
    public void C00_POST_RequestWithoutBody() throws Exception {
        this.mockMvc.perform(post("/oven/chat/")
                .contentType(APPLICATION_JSON)
                .content("{}")
        )
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("Required message is not readable, body or text is missing")));
    }

    //request without text, ERROR
    @Test
    public void C00_POST_RequestWithoutTextObject() throws Exception {
        this.mockMvc.perform(post("/oven/chat/")
                .contentType(APPLICATION_JSON)
                .content(
                        "{\n" +
                                "    \"chatInputMessages\": [],\n" +
                                "    \"chatUserMatches\": [\n" +
                                "        {\n" +
                                "            \"userID\": 25,\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ]\n" +
                                "}"
                )
        )
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("Required message is not readable, body or text is missing")));
    }

    @Test
    public void C00_POST_RequestWithEmptyTextObjetc() throws Exception {
        this.mockMvc.perform(post("/oven/chat/")
                .contentType(APPLICATION_JSON)
                .content(
                        "{\n" +
                                "    \"chatInputMessages\": [\n" +
                                "        {}\n" +
                                "    ],\n" +
                                "    \"chatUserMatches\": [\n" +
                                "        {\n" +
                                "            \"userID\": 25,\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ]\n" +
                                "}"
                )
        )
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("Required message is not readable, body or text is missing")));
    }

    @Test
    public void C00_POST_RequestWithoutTextMessage() throws Exception {
        this.mockMvc.perform(post("/oven/chat/")
                .contentType(APPLICATION_JSON)
                .content(
                        "{\n" +
                                "    \"chatInputMessages\": [\n" +
                                "        {\n" +
                                "            \"chatLanguage\": \"de\",\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chatUserMatches\": [\n" +
                                "        {\n" +
                                "            \"userID\": 25,\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ]\n" +
                                "}"
                )
        )
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("Required message is not readable, body or text is missing")));
    }


    //request with unsopported Language
    @Test
    public void C00_POST_RequestWithWrongLanguage() throws Exception {
        this.mockMvc.perform(post("/oven/chat/")
                .contentType(APPLICATION_JSON)
                .content(
                        "{\n" +
                                "    \"chatInputMessages\": [\n" +
                                "        {\n" +
                                "            \"message\": \"Hallo Chatbot\",\n" +
                                "            \"language\": \"geheim\",\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"chatUserMatches\": [\n" +
                                "        {\n" +
                                "            \"userID\": -1,\n" +
                                "            \"probability\": 1\n" +
                                "        }\n" +
                                "    ]\n" +
                                "}"
                )
        )
                .andExpect(status().is(200))
                .andExpect(content().string(not(containsString("error"))))
                .andExpect(content().string(containsString("Accepted")));
    }

}

