package com.pleasebookme.server.security.identity.context;

import com.pleasebookme.server.security.identity.adapter.PrincipalUserDetails;
import com.pleasebookme.server.security.identity.context.exception.ForbiddenActorException;
import com.pleasebookme.server.security.identity.context.exception.UnauthenticatedException;
import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.identity.principal.UserPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

// The single unwrap path from Spring Security's Authentication back to the
// business identity. AuthenticationTokenFactory stores two different shapes:
// UserPrincipal wrapped in PrincipalUserDetails (UsernamePasswordAuthenticationToken),
// and WidgetPrincipal bare (PreAuthenticatedAuthenticationToken). Callers outside
// the security package should never have to know which one they are looking at.
@Component
public class DefaultCurrentPrincipalProvider implements CurrentPrincipalProvider {

    @Override
    public Optional<AuthenticatedPrincipal> find() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();

        if (
            authentication == null ||
            !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken
        ) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof PrincipalUserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUserPrincipal());
        }

        if (principal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
            return Optional.of(authenticatedPrincipal);
        }

        return Optional.empty();
    }

    @Override
    public AuthenticatedPrincipal require() {
        return find().orElseThrow(() -> new UnauthenticatedException(
            "No authenticated principal is present in the security context"
        ));
    }

    @Override
    public UserPrincipal requireUser() {
        AuthenticatedPrincipal principal = require();

        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }

        throw new ForbiddenActorException(
            "Invalid actor: " + principal.actorType()
        );
    }
}
