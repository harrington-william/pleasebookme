package com.pleasebookme.server.security.identity.context;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;

import java.util.Optional;

public interface CurrentPrincipalProvider {
    Optional<AuthenticatedPrincipal> find();

    AuthenticatedPrincipal require();

    UserPrincipal requireUser();
}
