package com.pleasebookme.server.service.integration.service;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.security.oauth.google.client.dto.GoogleTokenResponse;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;

public interface GoogleConnectionWriter {
    void persist(
        UserEntity user,
        GoogleTokenResponse tokens,
        GoogleIdentity identity
    );
}
