package de.dailab.oven.api.interfaces.manage;

import de.dailab.oven.api_common.error.ErrorResponse;
import de.dailab.oven.api_common.error.ResponseException;
import de.dailab.oven.api_common.manage.ManageResponse;
import de.dailab.oven.controller.WebsocketController;
import org.springframework.http.HttpStatus;

public class ManageController {

    public enum VolumeCommands {
        MUTE, UNMUTE, UP, DOWN
    }

    /**
     * MV01
     *
     * @param volumeKeyString allowed are enum VolumeCommands
     * @throws ResponseException to return HTTP request
     */
    public static void volumeCommand(final String volumeKeyString) throws ResponseException {
        //check for allowed keys
        for (final VolumeCommands volumeKey : VolumeCommands.values()) {
            if (volumeKey.name().equalsIgnoreCase(volumeKeyString)) {

                //try different OS
                volumeCommandUnix(volumeKey);
                sendWebsocket(ManageResponse.ResponseType.VOLUME, volumeKey.name());
            }
        }
        final ErrorResponse response = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), "command not valid");
        throw new ResponseException(response);

    }

    /**
     * MV02
     *
     * @param volumeValue int between 0 and 100 representing the volume level
     * @throws ResponseException to return HTTP request
     */
    public static void volumeSet(final int volumeValue) throws ResponseException {
        if (volumeValue <= 100 && volumeValue >= 0) {
            //TRY OS
            volumeSetUnix(volumeValue);
            sendWebsocket(ManageResponse.ResponseType.VOLUME, volumeValue + "");
        } else {
            final ErrorResponse response = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), "value must be between 0-100");
            throw new ResponseException(response);
        }
    }

    /**
     * MR01
     */
    public static void restart() {
        //TRY OS

        restartUnix();
        sendWebsocket(ManageResponse.ResponseType.DEVICE_RESTART, "");

        restartWindows();
        sendWebsocket(ManageResponse.ResponseType.DEVICE_RESTART, "");
    }

    /**
     * Send a Message/Notification to all users (uderID = 0)
     *
     * @param responseType    choose one of VolumeCommands.
     * @param responseMessage set a String as message.
     */
    private static void sendWebsocket(final ManageResponse.ResponseType responseType, final String responseMessage) {
        final ManageResponse manageResponse = new ManageResponse(responseType, responseMessage);
        WebsocketController.getInstance().send(WebsocketController.OVEN_MANAGE, WebsocketController.BROADCAST, manageResponse);
    }



    /*
    OS - LEVEL
     */

    /**
     * MV01
     *
     * @param volumeKey enum of VolumeCommands
     */
    private static void volumeCommandUnix(final VolumeCommands volumeKey) {
        //unix
        final String key;
        switch (volumeKey) {
            case MUTE:
                key = "mute";
                break;
            case UNMUTE:
                key = "unmute; amixer -M set Master unmute";
                break;
            case UP:
                key = "3%+";
                break;
            case DOWN:
                key = "3%-";
                break;
            default:
                return;
        }
        new ProcessBuilder("bash", "-c", "amixer -M set PCM " + key);

        //wait 4 response

        //check result (true / false)
    }

    /**
     * MV02
     *
     * @param volumeValue int between 0 and 100, already checked
     */
    private static void volumeSetUnix(final int volumeValue) {
        //unix
        new ProcessBuilder("bash", "-c", "amixer -M set PCM " + volumeValue + "%");
    }

    /**
     * MR01
     */
    private static void restartUnix() {
        //unix
        new ProcessBuilder("bash", "-c", "reboot");
    }

    /**
     * MR01
     */
    private static void restartWindows() {
        //windoof
        new ProcessBuilder("powershell.exe", "Restart-Computer");
    }

}
