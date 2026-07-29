package com.pleasebookme.server.security.identity.mapper;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;

public interface PrincipalMapper<T> {
    AuthenticatedPrincipal map(T actor);
}
