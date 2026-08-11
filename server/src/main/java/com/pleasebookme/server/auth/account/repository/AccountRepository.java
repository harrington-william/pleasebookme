package com.pleasebookme.server.auth.account.repository;

import com.pleasebookme.server.auth.account.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, BigInteger> {
    boolean existsByProviderAndProviderAccountId(String provider, String providerAccountId);

    Optional<AccountEntity> findByProviderAndProviderAccountId(String provider, String providerAccountId);
}
