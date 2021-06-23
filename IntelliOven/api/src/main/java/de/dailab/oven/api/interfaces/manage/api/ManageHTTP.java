package de.dailab.oven.api.interfaces.manage.api;


import de.dailab.oven.api.helper.serialization.ErrorHandler;
import de.dailab.oven.api.interfaces.manage.ManageController;
import de.dailab.oven.api_common.error.ResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("oven/manage")
public class ManageHTTP {


    /**
     * MV01
     * Changes the Volume by command
     *
     * @param command up, down, mute, unmute
     * @return 500 if system cant handle the request, 422 if not a command, else 200
     */
    @PostMapping(value = "/volume/command/{command}")
    public ResponseEntity changeVolume(@PathVariable final String command) {
        try {
            ManageController.volumeCommand(command);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("");
        } catch (final ResponseException e) {
            return ErrorHandler.get(e.getStatus(), e.getResponse().getMessage());
        }
    }

    /**
     * MV02
     * Set the Volume by Int
     *
     * @param value 0 - 100
     * @return 500 if system cant handle the request, 422 if not a command, else 200
     */
    @PutMapping(value = "/volume/set/{value}")
    public ResponseEntity setVolume(@PathVariable final int value) {

        try {
            ManageController.volumeSet(value);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("");
        } catch (final ResponseException e) {
            return ErrorHandler.get(e.getStatus(), e.getResponse().getMessage());
        }
    }

    /**
     * MR01
     * Restarts the System
     *
     * @return 500 if system cant handle the request
     */
    @PutMapping(value = "/restart/")
    public ResponseEntity restart() {
            ManageController.restart();
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("");
    }

}