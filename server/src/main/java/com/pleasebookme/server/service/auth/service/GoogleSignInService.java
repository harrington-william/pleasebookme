package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.service.auth.dto.LoginResponse;

public interface GoogleSignInService {
    LoginResponse signIn(String idToken);
}
