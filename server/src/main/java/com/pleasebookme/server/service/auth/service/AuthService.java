package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.service.auth.dto.*;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    RefreshResponse refreshToken(String token);

    WidgetBootstrapResponse bootstrapWidget(WidgetBootstrapRequest request);
}
