package de.dailab.oven.api;

import de.dailab.oven.database.AbstractDatabaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ManageHTTPTest extends AbstractDatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Override
    public void initialize() {

    }

    //normal request
    @Test
    public void MV01_VOLUME_Allowed() throws Exception {
        final String[] commands = {"up", "down", "mute", "unmute"};
        for (final String command : commands) {
            this.mockMvc.perform(post("/oven/manage/volume/command" + command))
                    .andExpect(status().is(404))
                    .andExpect(content().string(not(containsString("error"))))
                    .andExpect(content().string(equalTo("")));
        }
    }


    @Test
    public void MV01_VOLUME_NotCommand() throws Exception {
        this.mockMvc.perform(post("/oven/manage/volume/command/upp"))
                .andExpect(content().string(containsString("error")))
                .andExpect(content().string(containsString("")))
                .andExpect(status().is(422));
    }

    //normal request
    @Test
    public void MV02_VOLUME_SET_ALLOWED() throws Exception {
        this.mockMvc.perform(put("/oven/manage/volume/set/50"))
                .andExpect(status().is(not(500)))
                .andExpect(status().is(not(400)))
                .andExpect(content().string(containsString("")));
    }


    @Test
    public void MV01_VOLUME_SET_NotAllowed_String() throws Exception {
        this.mockMvc.perform(put("/oven/manage/volume/set/upp"))
                .andExpect(content().string(containsString("Bad Request")))
                .andExpect(status().is(400))
                .andExpect(content().string(containsString("Failed to convert")));
    }

    @Test
    public void MV01_VOLUME_SET_NotAllowed_Int() throws Exception {
        this.mockMvc.perform(put("/oven/manage/volume/set/101"))
                .andExpect(content().string(containsString("error")))
                .andExpect(status().is(422))
                .andExpect(content().string(containsString("")));
    }

}

