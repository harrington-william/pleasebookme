package com.pleasebookme.server.service.auth.service.impl;

import com.pleasebookme.server.auth.password.entity.UserPasswordEntity;
import com.pleasebookme.server.auth.password.repository.UserPasswordRepository;
import com.pleasebookme.server.auth.refreshtoken.entity.RefreshTokenEntity;
import com.pleasebookme.server.auth.refreshtoken.enums.RefreshOwner;
import com.pleasebookme.server.auth.refreshtoken.repository.RefreshTokenRepository;
import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.auth.user.exception.UserEmailAlreadyExistException;
import com.pleasebookme.server.auth.user.exception.UserNotFoundException;
import com.pleasebookme.server.auth.user.exception.UserPhoneNumberAlreadyExistException;
import com.pleasebookme.server.auth.user.exception.UsernameAlreadyExistException;
import com.pleasebookme.server.auth.user.repository.UserRepository;
import com.pleasebookme.server.security.identity.adapter.PrincipalUserDetails;
import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.security.identity.loader.user.UserIdentityLoader;
import com.pleasebookme.server.security.identity.loader.widget.WidgetIdentityLoader;
import com.pleasebookme.server.security.identity.mapper.UserPrincipalMapper;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.identity.principal.WidgetPrincipal;
import com.pleasebookme.server.security.token.jwt.config.JwtProperties;
import com.pleasebookme.server.security.token.jwt.engine.JwtEngine;
import com.pleasebookme.server.security.token.refresh.TokenRefresher;
import com.pleasebookme.server.service.auth.dto.*;
import com.pleasebookme.server.service.auth.service.AuthService;
import com.pleasebookme.server.service.auth.service.UserProvisioningService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final JwtEngine jwtEngine;
    private final TokenRefresher tokenRefresher;

    private final UserIdentityLoader userIdentityLoader;
    private final UserPrincipalMapper userPrincipalMapper;
    private final UserProvisioningService userProvisioningService;

    private final WidgetIdentityLoader widgetIdentityLoader;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
            )
        );

        PrincipalUserDetails userDetails = (PrincipalUserDetails) authentication.getPrincipal();

        if (userDetails == null) {
            throw new UserNotFoundException("User not found");
        }

        UserPrincipal principal = userDetails.getUserPrincipal();

        String accessToken = jwtEngine.issueAccessToken(principal);
        String refreshToken = jwtEngine.issueRefreshToken(principal);

        UserEntity user = userRepository.findByUsername(principal.username())
            .orElseThrow(() -> new UserNotFoundException(
                "User not found with username: " + principal.username()
            ));

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
            .secret(refreshToken)
            .owner(RefreshOwner.USER)
            .user(user)
            .deviceName(null)
            .createdAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(
                jwtProperties.refreshTokenLifeTime().toSeconds()
            ))
            .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistException(
                "Username already exists: " + request.username()
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserEmailAlreadyExistException(
                "Email already exists: " + request.email()
            );
        }

        if (request.phone() != null && userRepository.existsByPhone(request.phone())) {
            throw new UserPhoneNumberAlreadyExistException(
                "Phone number already exists: " + request.phone()
            );
        }

        Instant now = Instant.now();

        UserEntity user = userProvisioningService.provisionUser(
            request.username(),
            request.name(),
            request.email(),
            request.phone(),
            request.locale(),
            request.timezone()
        );

        UserPasswordEntity userPasswordEntity = UserPasswordEntity.builder()
            .user(user)
            .hash(passwordEncoder.encode(request.password()))
            .createdAt(now)
            .updatedAt(now)
            .build();
        userPasswordRepository.save(userPasswordEntity);

        // Create principal
        AuthenticationAggregation aggregation = userIdentityLoader.loadByUsername(user.getUsername());
        UserPrincipal principal = userPrincipalMapper.map(aggregation);

        // Issue tokens
        String accessToken = jwtEngine.issueAccessToken(principal);
        String refreshToken = jwtEngine.issueRefreshToken(principal);

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
            .secret(refreshToken)
            .owner(RefreshOwner.USER)
            .user(user)
            .deviceName(null)
            .createdAt(now)
            .expiresAt(now.plusSeconds(
                jwtProperties.refreshTokenLifeTime().toSeconds()
            ))
            .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public RefreshResponse refreshToken(String token) {
        return tokenRefresher.refresh(token);
    }

    @Override
    @Transactional
    public WidgetBootstrapResponse bootstrapWidget(WidgetBootstrapRequest request) {
        WidgetPrincipal principal = widgetIdentityLoader.loadByPublicKey(
            request.publicKey(),
            request.secretKey(),
            request.origin()
        );

        String accessToken = jwtEngine.issueAccessToken(principal);

        return new WidgetBootstrapResponse(accessToken);
    }
}
