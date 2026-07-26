package com.pleasebookme.server.security.token.filter;

import com.pleasebookme.server.security.identity.aggregation.AuthenticationAggregation;
import com.pleasebookme.server.security.identity.loader.IdentityLoader;
import com.pleasebookme.server.security.identity.mapper.PrincipalMapper;
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
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtEngine jwtEngine;
    private final IdentityLoader identityLoader;
    private final PrincipalMapper<AuthenticationAggregation> principalMapper;
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

        // Get token
        String token = authHeader.substring(7);

        try {
            JwtClaims claims = jwtEngine.verify(token);

            UUID userUid = claims.subject();

            AuthenticationAggregation aggregation =
                identityLoader.loadByUid(userUid);

            AuthenticatedPrincipal principal =
                principalMapper.map(aggregation);

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
