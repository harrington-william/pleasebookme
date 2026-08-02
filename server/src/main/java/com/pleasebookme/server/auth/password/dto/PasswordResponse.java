package com.pleasebookme.server.auth.password.dto;

import com.pleasebookme.server.auth.password.entity.UserPasswordEntity;

import java.math.BigInteger;
import java.time.Instant;

public record PasswordResponse(
    BigInteger userId,
    String hash,
    Instant createdAt,
    Instant updatedAt
) {
    public static PasswordResponse from(UserPasswordEntity userPassword) {
        return new PasswordResponse(
            userPassword.getUserId(),
            userPassword.getHash(),
            userPassword.getCreatedAt(),
            userPassword.getUpdatedAt()
        );
    }
}
