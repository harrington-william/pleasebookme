package com.pleasebookme.server.auth.password.repository;

import com.pleasebookme.server.auth.password.entity.UserPasswordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface UserPasswordRepository extends JpaRepository<UserPasswordEntity, BigInteger> {
}
