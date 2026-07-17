package com.pleasebookme.server.auth.user.service.impl;

import com.pleasebookme.server.auth.user.dto.UserRequest;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.DuplicateUserException;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.auth.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserEntity createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("Username already exists: " + request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("Email already exists: " + request.email());
        }

        if (userRepository.existsByPhone(request.phone())) {
            throw new DuplicateUserException("Phone already exists: " + request.phone());
        }

        UserEntity.UserEntityBuilder user = UserEntity.builder()
            .username(request.username())
            .name(request.name())
            .email(request.email())
            .phone(request.phone())
            .bio(request.bio())
            .avatarUrl(request.avatarUrl());

        if (request.locale() != null) user.locale(request.locale());
        if (request.timezone() != null) user.timezone(request.timezone());
        if (request.theme() != null) user.theme(request.theme());
        if (request.weekStart() != null) user.weekStart(request.weekStart());

        return userRepository.save(user.build());
    }

    @Override
    public UserEntity getUserById(BigInteger userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found: " + userId
            ));
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found: " + email
            ));
    }

    @Override
    public UserEntity getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found: " + phone
            ));
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserEntity updateUser(
        BigInteger userId,
        UserRequest request
    ) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                    "User not found: " + userId
                ));

        user.setUsername(request.username());
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setBio(request.bio());
        user.setAvatarUrl(request.avatarUrl());

        if (request.locale() != null) user.setLocale(request.locale());
        if (request.timezone() != null) user.setTimezone(request.timezone());
        if (request.theme() != null) user.setTheme(request.theme());
        if (request.weekStart() != null) user.setWeekStart(request.weekStart());

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(BigInteger userId) {
        userRepository.delete(getUserById(userId));
    }
}
