package de.dailab.oven.api.interfaces.oven;

import de.dailab.oven.api_common.error.ErrorResponse;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.api_common.oven.Oven;
import de.dailab.oven.controller.OvenController;
import org.springframework.http.HttpStatus;

public class OvenWebController {

    private OvenWebController(){
        //SonarQube
    }

    /**
     * OV01
     */
    public static OvenController getOvenStatus() {
        return OvenController.getInstance();
    }

    /**
     * OV02
     *
     * @throws ResponseException to return HTTP request
     */
    public static OvenController setOvenMode(final Oven.ProgramRequest programRequest) throws ResponseException {
        try {
            OvenController.getInstance().setProgram(programRequest.getOvenMode(), programRequest.getTemperature());
        } catch (final Exception e) {
            throw new ResponseException(new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage()));
        }
        return OvenController.getInstance();
    }


}
