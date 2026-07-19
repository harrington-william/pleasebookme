package com.pleasebookme.server.auth.password.service;

import com.pleasebookme.server.auth.password.dto.PasswordRequest;
import com.pleasebookme.server.auth.password.entity.UserPasswordEntity;

import java.math.BigInteger;

public interface UserPasswordService {
    UserPasswordEntity createUserPassword(PasswordRequest request);

    UserPasswordEntity getUserPasswordById(BigInteger userId);

    UserPasswordEntity updateUserPassword(BigInteger userId, PasswordRequest request);

    void deleteUserPassword(BigInteger userId);
}
