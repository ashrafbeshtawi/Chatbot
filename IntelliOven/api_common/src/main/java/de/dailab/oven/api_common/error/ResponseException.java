package de.dailab.oven.api_common.error;

public class ResponseException extends Exception {

    private final int status;
    private final transient ErrorResponse response;

    public ResponseException(final ErrorResponse response) {
        this.response = response;
        this.status = response.getStatus();
    }

    public int getStatus() {
        return this.status;
    }

    public ErrorResponse getResponse() {
        return this.response;
    }

    @Override
    public String getMessage() {
        return this.response.getMessage();
    }
}
