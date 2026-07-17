package com.pleasebookme.server.global.handler;

import com.pleasebookme.server.auth.user.exception.DuplicateUserException;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.global.exception.ResourceNotFoundException;
import com.pleasebookme.server.global.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
        ResourceNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(
        UserNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUserException(
        DuplicateUserException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
            "error",
            exception.getMessage(),
            null,
            request.getRemoteAddr(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}
