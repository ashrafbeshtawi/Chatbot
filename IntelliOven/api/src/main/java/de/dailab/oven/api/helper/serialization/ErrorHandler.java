package de.dailab.oven.api.helper.serialization;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    public static ResponseEntity get(final int status, final String message) {

        final HttpStatus httpStatus = HttpStatus.valueOf(status);

        final String errMsg = createJson(httpStatus, message);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<String>(errMsg, httpHeaders, httpStatus);
    }

    private static String createJson(final HttpStatus status, final String message) {
        return "{\"status\": " + status.value() + ",\n\"error\": \"" + status.getReasonPhrase() + "\",\n\"message\": \"" + message + "\"}";
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, final Object body, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(createJson(status, ex.getMessage()), httpHeaders, status);
    }

}