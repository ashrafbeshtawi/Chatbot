package de.dailab.oven.api.interfaces.chat.api;

import de.dailab.oven.api.interfaces.chat.ChatController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("oven/chat")
public class DialogState {

    //Singleton
    private static DialogState singleInstance = null;

    private DialogState() {
    }

    public static DialogState getInstance() {
        if (singleInstance == null)
            singleInstance = new DialogState();
        return singleInstance;
    }

    @Nonnull
    private String getDialogStateMap() {
        try{
            return ChatController.getInstance().getAppState().getDialogState().getState();
        }catch (final Exception e){
            return e.getMessage();
        }
    }

    @GetMapping(value = "/dialogState")
    public ResponseEntity getDialogState() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DialogState.getInstance().getDialogStateMap());
    }
}