package com.pleasebookme.server.integration.oauthconnection.service;

import com.pleasebookme.server.integration.oauthconnection.dto.OAuthConnectionRequest;
import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;

import java.math.BigInteger;
import java.util.List;

public interface OAuthConnectionService {
    OAuthConnectionEntity createOAuthConnection(OAuthConnectionRequest request);

    OAuthConnectionEntity getOAuthConnectionById(BigInteger oauthConnectionId);

    List<OAuthConnectionEntity> getAllOAuthConnections();

    OAuthConnectionEntity updateOAuthConnection(
        BigInteger oauthConnectionId,
        OAuthConnectionRequest request
    );

    void deleteOAuthConnection(BigInteger oauthConnectionId);
}
