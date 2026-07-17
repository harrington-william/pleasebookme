package com.pleasebookme.server.global.response;

import java.time.Instant;

public class ApiErrorResponse {
    private String status;
    private String message;
    private Object data;
    private String client;
    private Instant timestamp;
    private String path;

    public ApiErrorResponse(
        String status,
        String message,
        Object data,
        String client,
        String path
    ) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.client = client;
        this.timestamp = Instant.now();
        this.path = path;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
    public String getClient() { return client; }
    public Instant getTimestamp() { return timestamp; }
    public String getPath() { return path; }
}
