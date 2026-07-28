package com.pleasebookme.server.service.auth.service.impl;

import com.pleasebookme.server.security.token.jwt.engine.JwtEngine;
import com.pleasebookme.server.service.auth.dto.LoginRequest;
import com.pleasebookme.server.service.auth.dto.LoginResponse;
import com.pleasebookme.server.service.auth.dto.RefreshResponse;
import com.pleasebookme.server.service.auth.dto.RegisterRequest;
import com.pleasebookme.server.service.auth.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtEngine jwtEngine;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        return null;
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest registerRequest) {
        return null;
    }

    @Override
    public RefreshResponse refreshToken(String refreshToken) {
        return null;
    }
}
