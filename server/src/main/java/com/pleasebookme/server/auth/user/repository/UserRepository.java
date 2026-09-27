package com.pleasebookme.server.auth.user.repository;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, BigInteger> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByPhone(String phone);

    Optional<UserEntity> findByUserUid(UUID uid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findLockedByUserId(BigInteger userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByUserUid(UUID uid);
}
