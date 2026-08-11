package com.pleasebookme.server.integration.syncjob.exception;

public class SyncJobNotFoundException extends RuntimeException {
    public SyncJobNotFoundException(String message) {
        super(message);
    }
}
