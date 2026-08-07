package com.pleasebookme.server.integration.oauthconnection.repository;

import com.pleasebookme.server.integration.enums.OAuthProvider;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthConnectionRepository extends JpaRepository<OAuthConnectionEntity, BigInteger> {
    boolean existsByUserUserIdAndProviderAndProviderAccountId(
        BigInteger userId,
        OAuthProvider provider,
        String providerAccountId
    );

    Optional<OAuthConnectionEntity> findByUserUserIdAndProviderAndProviderAccountId(
        BigInteger userId,
        OAuthProvider provider,
        String providerAccountId
    );

    Optional<OAuthConnectionEntity> findByOauthConnectionUid(UUID oauthConnectionUid);

    List<OAuthConnectionEntity> findByUserUserIdOrderByConnectedAtDesc(BigInteger userId);
}
