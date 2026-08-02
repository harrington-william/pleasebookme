package com.pleasebookme.server.integration.oauthconnection.service.impl;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.integration.oauthconnection.dto.OAuthConnectionRequest;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.exception.DuplicateOAuthConnectionException;
import com.pleasebookme.server.integration.oauthconnection.exception.OAuthConnectionNotFoundException;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
import com.pleasebookme.server.integration.oauthconnection.service.OAuthConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OAuthConnectionServiceImpl implements OAuthConnectionService {
    private final OAuthConnectionRepository oauthConnectionRepository;
    private final UserRepository userRepository;

    @Override
    public OAuthConnectionEntity createOAuthConnection(OAuthConnectionRequest request) {
        if (oauthConnectionRepository.existsByUserUserIdAndProviderAndProviderAccountId(
            request.userId(),
            request.provider(),
            request.providerAccountId()
        )) {
            throw new DuplicateOAuthConnectionException(
                "OAuth connection already exists for user " + request.userId()
                    + " and provider " + request.provider()
            );
        }

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        OAuthConnectionEntity.OAuthConnectionEntityBuilder oauthConnection = OAuthConnectionEntity.builder()
            .user(user)
            .provider(request.provider())
            .providerAccountId(request.providerAccountId())
            .providerEmail(request.providerEmail())
            .scopes(request.scopes())
            .accessToken(request.accessToken())
            .refreshToken(request.refreshToken())
            .tokenExpiresAt(request.tokenExpiresAt())
            .lastRefreshedAt(request.lastRefreshedAt())
            .lastUsedAt(request.lastUsedAt())
            .revokedAt(request.revokedAt());

        if (request.tokenKeyVersion() != null) oauthConnection.tokenKeyVersion(request.tokenKeyVersion());
        if (request.status() != null) oauthConnection.status(request.status());

        return oauthConnectionRepository.save(oauthConnection.build());
    }

    @Override
    public OAuthConnectionEntity getOAuthConnectionById(BigInteger oauthConnectionId) {
        return oauthConnectionRepository.findById(oauthConnectionId)
            .orElseThrow(() -> new OAuthConnectionNotFoundException(
                "OAuth connection not found: " + oauthConnectionId
            ));
    }

    @Override
    public List<OAuthConnectionEntity> getAllOAuthConnections() {
        return oauthConnectionRepository.findAll();
    }

    @Override
    public OAuthConnectionEntity updateOAuthConnection(
        BigInteger oauthConnectionId,
        OAuthConnectionRequest request
    ) {
        OAuthConnectionEntity oauthConnection = getOAuthConnectionById(oauthConnectionId);

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        oauthConnection.setUser(user);
        oauthConnection.setProvider(request.provider());
        oauthConnection.setProviderAccountId(request.providerAccountId());
        oauthConnection.setProviderEmail(request.providerEmail());
        oauthConnection.setScopes(request.scopes());
        oauthConnection.setAccessToken(request.accessToken());
        oauthConnection.setRefreshToken(request.refreshToken());
        oauthConnection.setTokenExpiresAt(request.tokenExpiresAt());
        oauthConnection.setLastRefreshedAt(request.lastRefreshedAt());
        oauthConnection.setLastUsedAt(request.lastUsedAt());
        oauthConnection.setRevokedAt(request.revokedAt());

        if (request.tokenKeyVersion() != null) oauthConnection.setTokenKeyVersion(request.tokenKeyVersion());
        if (request.status() != null) oauthConnection.setStatus(request.status());

        return oauthConnectionRepository.save(oauthConnection);
    }

    @Override
    public void deleteOAuthConnection(BigInteger oauthConnectionId) {
        oauthConnectionRepository.delete(getOAuthConnectionById(oauthConnectionId));
    }
}
