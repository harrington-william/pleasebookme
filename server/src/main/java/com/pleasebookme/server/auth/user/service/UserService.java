package com.pleasebookme.server.auth.user.service;

import com.pleasebookme.server.auth.user.dto.UserRequest;
import com.pleasebookme.server.auth.user.entity.UserEntity;

import java.math.BigInteger;
import java.util.List;

public interface UserService {
    UserEntity createUser(UserRequest request);

    UserEntity getUserById(BigInteger userId);

    UserEntity getUserByEmail(String email);

    UserEntity getUserByPhone(String phone);

    List<UserEntity> getAllUsers();

    UserEntity updateUser(BigInteger userId, UserRequest request);

    void deleteUser(BigInteger userId);
}
