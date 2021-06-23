package de.dailab.oven.api.interfaces.oven.api;


import de.dailab.oven.api.helper.serialization.ErrorHandler;
import de.dailab.oven.api.interfaces.oven.OvenWebController;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.api_common.oven.Oven;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("oven/oven")
public class OvenHTTP {


    /**
     * O01
     * Gets the current Oven
     *
     * @return 200 and OvenStatus
     */
    @GetMapping(value = "/get")
    public ResponseEntity getOvenStatus() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(OvenWebController.getOvenStatus());
    }

    /**
     * O02
     * Sets a Program as current Program
     *
     * @return 200 and OvenStatus, or error on error
     */
    @PostMapping(value = "/setProgram")
    public ResponseEntity setProgram(@RequestBody final Oven.ProgramRequest programRequest) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(OvenWebController.setOvenMode(programRequest));
        } catch (final ResponseException e) {
            return ErrorHandler.get(e.getStatus(), e.getResponse().getMessage());
        }
    }
}