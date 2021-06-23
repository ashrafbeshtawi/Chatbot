package de.dailab.oven.api_common.error;

import de.dailab.oven.api_common.Sendable;

public class ErrorResponse implements Sendable {

    private boolean error;
    private int status;
    private String message;

    public ErrorResponse(final int errorCode, final String message) {
        this.error = true;
        this.status = errorCode;
        this.message = message;
    }

    public ErrorResponse() {
    }

    public boolean isError() {
        return this.error;
    }

    public int getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

}