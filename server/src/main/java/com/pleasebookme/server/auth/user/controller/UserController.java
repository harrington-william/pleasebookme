package com.pleasebookme.server.auth.user.controller;

import com.pleasebookme.server.auth.user.dto.UserRequest;
import com.pleasebookme.server.auth.user.dto.UserResponse;
import com.pleasebookme.server.auth.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        return UserResponse.from(userService.createUser(request));
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable BigInteger userId) {
        return UserResponse.from(userService.getUserById(userId));
    }

    @GetMapping("/email/{email}")
    public UserResponse getUserByEmail(@PathVariable String email) {
        return UserResponse.from(userService.getUserByEmail(email));
    }

    @GetMapping("/phone/{phone}")
    public UserResponse getUserByPhone(@PathVariable String phone) {
        return UserResponse.from(userService.getUserByPhone(phone));
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getAllUsers().stream()
            .map(UserResponse::from)
            .toList();
    }

    @PutMapping("/{userId}")
    public UserResponse updateUser(
        @PathVariable BigInteger userId,
        @Valid @RequestBody UserRequest request
    ) {
        return UserResponse.from(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable BigInteger userId) {
        userService.deleteUser(userId);
    }
}
