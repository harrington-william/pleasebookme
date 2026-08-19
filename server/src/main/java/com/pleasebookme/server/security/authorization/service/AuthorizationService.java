package com.pleasebookme.server.security.authorization.service;

import com.pleasebookme.server.security.authorization.context.AuthorizationContext;
import com.pleasebookme.server.security.authorization.decision.AuthorizationDecision;

public interface AuthorizationService {
    AuthorizationDecision authorize(AuthorizationContext context);

    void require(AuthorizationContext context);
}
