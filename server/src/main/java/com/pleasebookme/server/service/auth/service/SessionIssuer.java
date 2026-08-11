package com.pleasebookme.server.service.auth.service;

import com.pleasebookme.server.auth.user.entity.UserEntity;
import com.pleasebookme.server.service.auth.dto.LoginResponse;

// Mints an access/refresh pair for a user who has already been authenticated by
// some other means, and persists the refresh token.
//
// Takes a UserEntity rather than a principal or an Authentication because the
// handoff exchange has neither: the user was authenticated by Google, minutes
// earlier, in a different request.
public interface SessionIssuer {
    LoginResponse issue(UserEntity user);
}
