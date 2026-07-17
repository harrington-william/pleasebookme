package com.pleasebookme.server.auth.password.repository;

import com.pleasebookme.server.auth.password.entity.UserPasswordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface UserPasswordRepository extends JpaRepository<UserPasswordEntity, BigInteger> {
}
