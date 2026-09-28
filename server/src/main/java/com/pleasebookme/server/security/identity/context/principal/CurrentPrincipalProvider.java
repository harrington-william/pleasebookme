package com.pleasebookme.server.security.identity.context.principal;

import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import com.pleasebookme.server.security.identity.principal.WidgetPrincipal;

import java.util.Optional;

public interface CurrentPrincipalProvider {
    Optional<AuthenticatedPrincipal> find();

    AuthenticatedPrincipal require();

    UserPrincipal requireUser();

    WidgetPrincipal requireWidget();
}
