package com.croptalk.exception;

import org.springframework.http.HttpStatus;

/** Carries an HTTP status and JSON body, equivalent to `return res.status(x).json(body)` in the Express controllers. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final transient Object body;

    public ApiException(HttpStatus status, Object body) {
        super(String.valueOf(body));
        this.status = status;
        this.body = body;
    }

    public HttpStatus getStatus() { return status; }

    public Object getBody() { return body; }
}
