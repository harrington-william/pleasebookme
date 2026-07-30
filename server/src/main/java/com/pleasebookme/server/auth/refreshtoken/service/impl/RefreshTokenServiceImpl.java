package com.pleasebookme.server.auth.refreshtoken.service.impl;

import com.pleasebookme.server.auth.refreshtoken.dto.RefreshTokenRequest;
import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;
import com.pleasebookme.server.auth.refreshtoken.exception.DuplicateRefreshTokenException;
import com.pleasebookme.server.auth.refreshtoken.exception.RefreshTokenNotFoundException;
import com.pleasebookme.server.auth.refreshtoken.repository.RefreshTokenRepository;
import com.pleasebookme.server.auth.refreshtoken.service.RefreshTokenService;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.widget.widgets.entity.WidgetEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    public RefreshTokenEntity createRefreshToken(RefreshTokenRequest request) {
        if (refreshTokenRepository.existsBySecret(request.secret())) {
            throw new DuplicateRefreshTokenException("Secret already exists: " + request.secret());
        }

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
            .secret(request.secret())
            .owner(request.owner())
            .user(user)
            .deviceName(request.deviceName())
            .oauthClientId(request.oauthClientId())
            .expiresAt(request.expiresAt())
            .revokedAt(request.revokedAt())
            .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshTokenEntity getRefreshTokenById(BigInteger refreshTokenId) {
        return refreshTokenRepository.findById(refreshTokenId)
            .orElseThrow(() -> new RefreshTokenNotFoundException(
                "Refresh token not found: " + refreshTokenId
            ));
    }

    @Override
    public List<RefreshTokenEntity> getAllRefreshTokens() {
        return refreshTokenRepository.findAll();
    }

    @Override
    public RefreshTokenEntity updateRefreshToken(
        BigInteger refreshTokenId,
        RefreshTokenRequest request
    ) {
        RefreshTokenEntity refreshToken = getRefreshTokenById(refreshTokenId);

        UserEntity user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + request.userId()));

        refreshToken.setSecret(request.secret());
        refreshToken.setOwner(request.owner());
        refreshToken.setUser(user);
        refreshToken.setDeviceName(request.deviceName());
        refreshToken.setOauthClientId(request.oauthClientId());
        refreshToken.setExpiresAt(request.expiresAt());
        refreshToken.setRevokedAt(request.revokedAt());

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void deleteRefreshToken(BigInteger refreshTokenId) {
        refreshTokenRepository.delete(getRefreshTokenById(refreshTokenId));
    }
}
