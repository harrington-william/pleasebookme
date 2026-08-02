package com.pleasebookme.server.integration.oauthconnection.repository;

import com.pleasebookme.server.integration.enums.OAuthProvider;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface OAuthConnectionRepository extends JpaRepository<OAuthConnectionEntity, BigInteger> {
    boolean existsByUserUserIdAndProviderAndProviderAccountId(
        BigInteger userId,
        OAuthProvider provider,
        String providerAccountId
    );
}
