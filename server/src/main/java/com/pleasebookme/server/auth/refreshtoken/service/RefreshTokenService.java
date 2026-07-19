package com.pleasebookme.server.auth.refreshtoken.service;

import com.pleasebookme.server.auth.refreshtoken.dto.RefreshTokenRequest;
import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;

import java.math.BigInteger;
import java.util.List;

public interface RefreshTokenService {
    RefreshTokenEntity createRefreshToken(RefreshTokenRequest request);

    RefreshTokenEntity getRefreshTokenById(BigInteger refreshTokenId);

    List<RefreshTokenEntity> getAllRefreshTokens();

    RefreshTokenEntity updateRefreshToken(BigInteger refreshTokenId, RefreshTokenRequest request);

    void deleteRefreshToken(BigInteger refreshTokenId);
}
