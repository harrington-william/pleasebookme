package com.pleasebookme.server.global.handler;

import com.pleasebookme.server.auth.account.exception.AccountNotFoundException;
import com.pleasebookme.server.auth.account.exception.DuplicateAccountException;
import com.pleasebookme.server.auth.apikey.exception.ApiKeyNotFoundException;
import com.pleasebookme.server.auth.apikey.exception.DuplicateApiKeyException;
import com.pleasebookme.server.auth.password.exception.DuplicateUserPasswordException;
import com.pleasebookme.server.auth.password.exception.UserPasswordNotFoundException;
import com.pleasebookme.server.auth.permission.exception.DuplicatePermissionException;
import com.pleasebookme.server.auth.permission.exception.PermissionNotFoundException;
import com.pleasebookme.server.auth.role.exception.DuplicateRoleException;
import com.pleasebookme.server.auth.role.exception.RoleNotFoundException;
import com.pleasebookme.server.auth.rolepermission.exception.DuplicateRolePermissionException;
import com.pleasebookme.server.auth.rolepermission.exception.RolePermissionNotFoundException;
import com.pleasebookme.server.auth.refreshtoken.exception.DuplicateRefreshTokenException;
import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenNotFoundException;
import com.pleasebookme.server.auth.user.exception.DuplicateUserException;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.userrole.exception.DuplicateUserRoleException;
import com.pleasebookme.server.auth.userrole.exception.UserRoleNotFoundException;
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

    @ExceptionHandler(UserPasswordNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserPasswordNotFoundException(
        UserPasswordNotFoundException exception,
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

    @ExceptionHandler(DuplicateUserPasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUserPasswordException(
        DuplicateUserPasswordException exception,
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

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRoleNotFoundException(
        RoleNotFoundException exception,
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

    @ExceptionHandler(DuplicateRoleException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateRoleException(
        DuplicateRoleException exception,
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

    @ExceptionHandler(PermissionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePermissionNotFoundException(
        PermissionNotFoundException exception,
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

    @ExceptionHandler(DuplicatePermissionException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicatePermissionException(
        DuplicatePermissionException exception,
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

    @ExceptionHandler(UserRoleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserRoleNotFoundException(
        UserRoleNotFoundException exception,
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

    @ExceptionHandler(DuplicateUserRoleException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUserRoleException(
        DuplicateUserRoleException exception,
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

    @ExceptionHandler(RolePermissionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRolePermissionNotFoundException(
        RolePermissionNotFoundException exception,
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

    @ExceptionHandler(DuplicateRolePermissionException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateRolePermissionException(
        DuplicateRolePermissionException exception,
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

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotFoundException(
        AccountNotFoundException exception,
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

    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateAccountException(
        DuplicateAccountException exception,
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

    @ExceptionHandler(ApiKeyNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleApiKeyNotFoundException(
        ApiKeyNotFoundException exception,
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

    @ExceptionHandler(DuplicateApiKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateApiKeyException(
        DuplicateApiKeyException exception,
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

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRefreshTokenNotFoundException(
        RefreshTokenNotFoundException exception,
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

    @ExceptionHandler(DuplicateRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateRefreshTokenException(
        DuplicateRefreshTokenException exception,
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
