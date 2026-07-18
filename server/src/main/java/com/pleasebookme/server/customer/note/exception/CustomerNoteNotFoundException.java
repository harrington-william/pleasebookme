package com.pleasebookme.server.customer.note.exception;

public class CustomerNoteNotFoundException extends RuntimeException {
    public CustomerNoteNotFoundException(String message) {
        super(message);
    }
}
