package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.service.auth.dto.LoginRequest;
import com.pleasebookme.server.service.auth.dto.LoginResponse;
import com.pleasebookme.server.service.auth.dto.RefreshResponse;
import com.pleasebookme.server.service.auth.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    LoginResponse register(RegisterRequest registerRequest);

    RefreshResponse refreshToken(String refreshToken);
}
