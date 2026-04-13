package org.usbtechno.collector;

import java.time.Instant;

public class ApiError {

    public String error;
    public String message;
    public String path;
    public Instant timestamp;

    public ApiError() {
    }

    public ApiError(String error, String message, String path) {
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }
}
