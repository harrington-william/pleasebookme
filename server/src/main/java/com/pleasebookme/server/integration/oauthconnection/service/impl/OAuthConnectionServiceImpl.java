package com.pleasebookme.server.integration.oauthconnection.service.impl;

import com.pleasebookme.server.integration.oauthconnection.entity.OAuthConnectionEntity;
import com.pleasebookme.server.integration.oauthconnection.exception.OAuthConnectionNotFoundException;
import com.pleasebookme.server.integration.oauthconnection.repository.OAuthConnectionRepository;
import com.pleasebookme.server.integration.oauthconnection.service.OAuthConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class OAuthConnectionServiceImpl implements OAuthConnectionService {
    private final OAuthConnectionRepository oauthConnectionRepository;

    @Override
    public OAuthConnectionEntity getOAuthConnectionById(BigInteger oauthConnectionId) {
        return oauthConnectionRepository.findById(oauthConnectionId)
            .orElseThrow(() -> new OAuthConnectionNotFoundException(
                "OAuth connection not found: " + oauthConnectionId
            ));
    }

    @Override
    public void deleteOAuthConnection(BigInteger oauthConnectionId) {
        oauthConnectionRepository.delete(getOAuthConnectionById(oauthConnectionId));
    }
}
