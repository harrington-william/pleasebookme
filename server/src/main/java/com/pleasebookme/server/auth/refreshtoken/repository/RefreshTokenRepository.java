package com.pleasebookme.server.auth.refreshtoken.repository;

import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, BigInteger> {
    boolean existsBySecret(String secret);

    Optional<RefreshTokenEntity> findBySecret(String secret);
}
