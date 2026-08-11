package com.pleasebookme.server.auth.password.service.impl;

import com.pleasebookme.server.auth.password.dto.PasswordRequest;
import com.pleasebookme.server.auth.password.entity.UserPasswordEntity;
import com.pleasebookme.server.auth.password.exception.DuplicateUserPasswordException;
import com.pleasebookme.server.auth.password.exception.UserPasswordNotFoundException;
import com.pleasebookme.server.auth.password.repository.UserPasswordRepository;
import com.pleasebookme.server.auth.password.service.UserPasswordService;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class UserPasswordServiceImpl implements UserPasswordService {
    private final UserPasswordRepository userPasswordRepository;
    private final UserRepository userRepository;

    @Override
    public UserPasswordEntity createUserPassword(PasswordRequest request) {
        if (userPasswordRepository.existsById(request.userId())) {
            throw new DuplicateUserPasswordException(
                "Password already exists for user: " + request.userId()
            );
        }

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException(
                "User not found: " + request.userId()
            ));

        UserPasswordEntity userPassword = UserPasswordEntity.builder()
            .user(user)
            .hash(request.password())
            .build();

        return userPasswordRepository.save(userPassword);
    }

    @Override
    public UserPasswordEntity getUserPasswordById(BigInteger userId) {
        return userPasswordRepository.findById(userId)
            .orElseThrow(() -> new UserPasswordNotFoundException(
                "Password not found for user: " + userId
            ));
    }

    @Override
    public UserPasswordEntity updateUserPassword(
        BigInteger userId,
        PasswordRequest request
    ) {
        UserPasswordEntity userPassword = getUserPasswordById(userId);

        if (userPassword.getHash().equals(request.password())) {
            throw new DuplicateUserPasswordException(
                "New password must be different from the current password: " + userId
            );
        }

        userPassword.setHash(request.password());

        return userPasswordRepository.save(userPassword);
    }

    @Override
    public void deleteUserPassword(BigInteger userId) {
        userPasswordRepository.delete(getUserPasswordById(userId));
    }
}
