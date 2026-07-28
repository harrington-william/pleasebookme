package com.pleasebookme.server.security.token.filter;

import com.pleasebookme.server.security.identity.loader.user.UserIdentityLoader;
import com.pleasebookme.server.security.identity.loader.widget.WidgetIdentityLoader;
import com.pleasebookme.server.security.identity.mapper.UserPrincipalMapper;
import com.pleasebookme.server.security.identity.principal.AuthenticatedPrincipal;
import com.pleasebookme.server.security.token.authentication.AuthenticationTokenFactory;
import com.pleasebookme.server.security.token.jwt.claims.JwtClaims;
import com.pleasebookme.server.security.token.jwt.engine.JwtEngine;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtEngine jwtEngine;
    private final UserIdentityLoader userIdentityLoader;
    private final WidgetIdentityLoader widgetIdentityLoader;
    private final UserPrincipalMapper userPrincipalMapper;
    private final AuthenticationTokenFactory authenticationTokenFactory;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (
            authHeader == null ||
            !authHeader.startsWith("Bearer ")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            JwtClaims claims = jwtEngine.verify(token);

            // Create principal
            AuthenticatedPrincipal principal = switch (claims.actorType()) {
                case USER -> userPrincipalMapper.map(
                    userIdentityLoader.loadByUid(claims.subject())
                );

                case WIDGET -> widgetIdentityLoader.loadByUid(claims.subject());

                default -> throw new JwtException(
                    "Unsupported actor type: " + claims.actorType()
                );
            };

            Authentication authentication =
                authenticationTokenFactory.create(principal);

            SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
