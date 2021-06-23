package de.dailab.oven.api.interfaces.database.api;


import de.dailab.oven.api.helper.serialization.ErrorHandler;
import de.dailab.oven.data_acquisition.controller.ImportExportController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/database")
public class StorageHTTP {

    @PostMapping(value = "/import/arcelik")
    public ResponseEntity addIngredient(@RequestBody final String baseDirectory) {
        final ImportExportController controller = new ImportExportController();
        if(controller.importArcelikRecipes(baseDirectory)) {
            return ResponseEntity
                    .status(HttpStatus.OK).build();
        }
        return ErrorHandler.get(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Please check logs for more details");
    }

}
