package com.pleasebookme.server.integration.oauthconnection.service;

import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;

import java.math.BigInteger;

// create/update/getAll were removed alongside their endpoints: writing an
// oauth_connection means writing live Google credentials, which only the
// consent flow is allowed to do.
public interface OAuthConnectionService {
    OAuthConnectionEntity getOAuthConnectionById(BigInteger oauthConnectionId);

    void deleteOAuthConnection(BigInteger oauthConnectionId);
}
