package com.pleasebookme.server.security.token.authentication;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;

public interface AuthenticationTokenFactory {
    Authentication create(AuthenticatedPrincipal principal);
}
