package de.dailab.oven.api.interfaces.manage.api;

import de.dailab.oven.api.interfaces.manage.ManageController;
import de.dailab.oven.api_common.error.ResponseException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
public class ManageWS {

    /**
     * MV01
     * Changes the Volume by command
     *
     * @param command up, down, mute, unmute
     */
    @MessageMapping("/oven/manage/volume/command/{command}")
    public void changeVolume(@DestinationVariable final String command) {
        try {
            ManageController.volumeCommand(command);
        } catch (final ResponseException e) {
            //Do nothing, dont return error
        }
    }


    /**
     * MV02
     * Changes the Volume to value
     *
     * @param value 0-100
     */
    @MessageMapping("/oven/manage/volume/set/{value}")
    public void setVolume(@DestinationVariable final int value) {
        try {
            ManageController.volumeSet(value);
        } catch (final ResponseException e) {
            //Do nothing, dont return error
        }
    }

    /**
     * MR01
     * Restarts the System
     */
    @MessageMapping("/oven/manage/restart/")
    public void restart() {
            ManageController.restart();
    }
}
