package com.pleasebookme.server.auth.password.controller;

import com.pleasebookme.server.auth.password.dto.PasswordRequest;
import com.pleasebookme.server.auth.password.dto.PasswordResponse;
import com.pleasebookme.server.auth.password.service.UserPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/v1/user-passwords")
@RequiredArgsConstructor
public class UserPasswordController {
    private final UserPasswordService userPasswordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PasswordResponse createUserPassword(@Valid @RequestBody PasswordRequest request) {
        return PasswordResponse.from(userPasswordService.createUserPassword(request));
    }

    @GetMapping("/{userId}")
    public PasswordResponse getUserPassword(@PathVariable BigInteger userId) {
        return PasswordResponse.from(userPasswordService.getUserPasswordById(userId));
    }

    @PutMapping("/{userId}")
    public PasswordResponse updateUserPassword(
        @PathVariable BigInteger userId,
        @Valid @RequestBody PasswordRequest request
    ) {
        return PasswordResponse.from(userPasswordService.updateUserPassword(userId, request));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserPassword(@PathVariable BigInteger userId) {
        userPasswordService.deleteUserPassword(userId);
    }
}
