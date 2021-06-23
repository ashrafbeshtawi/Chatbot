package de.dailab.oven.api.interfaces.oven.api;

import de.dailab.oven.api.interfaces.oven.OvenWebController;
import de.dailab.oven.api_common.Sendable;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.api_common.oven.Oven;
import de.dailab.oven.controller.OvenController;
import de.dailab.oven.controller.WebsocketController;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
public class OvenWS {

    /**
     * O01
     * Gets the current Oven
     *
     * @return 200 and OvenStatus
     */
    @MessageMapping("/oven/oven/get")
    @SendTo(WebsocketController.OVEN_OVEN)
    public OvenController getOvenStatus() {
        return OvenWebController.getOvenStatus();
    }

    /**
     * O02
     * Sets a Program as current Program
     *
     * @return 200 and OvenStatus, or error on error
     */
    @MessageMapping("/oven/oven/setProgram")
    @SendTo(WebsocketController.OVEN_OVEN)
    public Sendable setProgram(final Oven.ProgramRequest programRequest) {
        try {
            return OvenWebController.setOvenMode(programRequest);
        } catch (final ResponseException e) {
            return e.getResponse();
        }
    }
}
