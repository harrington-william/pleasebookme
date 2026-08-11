package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;

public interface GoogleAccountResolver {
    UserEntity resolve(GoogleIdentity identity);
}
