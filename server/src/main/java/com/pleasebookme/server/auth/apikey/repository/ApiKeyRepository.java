package com.pleasebookme.server.auth.apikey.repository;

import com.pleasebookme.server.auth.apikey.entity.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, BigInteger> {
    boolean existsByPublicKey(String publicKey);
}
