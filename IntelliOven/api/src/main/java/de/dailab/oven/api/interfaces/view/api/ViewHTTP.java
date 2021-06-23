package de.dailab.oven.api.interfaces.view.api;


import de.dailab.oven.api.interfaces.view.ViewController;
import de.dailab.oven.api_common.view.Viewable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("oven/view")
public class ViewHTTP {


    /**
     * V01
     * receive the current View
     *
     * @param userID userID as long
     * @return returns the current View
     */
    @GetMapping(value = "/get/{userID}")
    public ResponseEntity getCurrentView(@PathVariable final long userID) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ViewController.getInstance().getCurrentView(userID));
    }

    /*
     * V02
     * change the current View
     *
     * @param userID userID as long
     * @param viewID viewID (0 = chatView; 1= RecipeView)
     */
    @PutMapping(value = "/change/{userID}/{viewID}")
    public ResponseEntity changeCurrentView(@PathVariable final long userID, @PathVariable final int viewID) {
        final Viewable view = ViewController.getInstance().changeView(userID, viewID);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(view);
    }


    /**
     * V03
     * Navigate on current VIew
     *
     * @param userID           userID as long
     * @param navigationString String to navigate (e.g. 'up', 'down', 'left'...)
     * @return returns 200 or 422, if it is possible/processed or not
     */
    @PostMapping(value = "/navigation/{navigationString}/{userID}")
    public ResponseEntity navigate(@PathVariable final long userID, @PathVariable final String navigationString) {
        final boolean successful = ViewController.getInstance().navigate(userID, navigationString);
        if (successful) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("");
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("");
        }
    }

    /**
     * V04
     * Set a new Viewed Object
     *
     * @param userID userID as long
     * @param index  index of Array position of new Viewed Object
     * @return returns 200 or 422, if it is possible/processed or not
     */
    @PutMapping(value = "/set/{index}/{userID}")
    public ResponseEntity set(@PathVariable final long userID, @PathVariable final int index) {
        final boolean successful = ViewController.getInstance().set(userID, index);
        if (successful) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("");
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("");
        }
    }

}