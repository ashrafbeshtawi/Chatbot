package de.dailab.oven.api_common.manage;


import de.dailab.oven.api_common.Sendable;

public class ManageResponse implements Sendable {

    //possible return types
    public enum ResponseType {
        VOLUME, MIC_STATUS, DEVICE_STATUS, DEVICE_RESTART
	}

    private final String message;
    private final ResponseType responseType;

    public ManageResponse(final ResponseType responseType, final String message) {
        this.message = message;
        this.responseType = responseType;
    }

    public String getMessage() {
        return this.message;
    }

    public ResponseType getResponseType() {
        return this.responseType;
    }
}
