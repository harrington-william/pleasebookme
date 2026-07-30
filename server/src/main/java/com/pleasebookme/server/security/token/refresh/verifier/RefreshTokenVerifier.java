package com.pleasebookme.server.security.token.refresh.verifier;

import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;

public interface RefreshTokenVerifier {
    RefreshTokenEntity verify(String secret);
}
