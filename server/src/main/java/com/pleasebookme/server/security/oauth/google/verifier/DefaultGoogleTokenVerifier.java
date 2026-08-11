package com.pleasebookme.server.security.oauth.google.verifier;

import com.pleasebookme.server.security.oauth.google.exception.InvalidGoogleIdTokenException;
import com.pleasebookme.server.security.oauth.google.identity.GoogleIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultGoogleTokenVerifier implements GoogleTokenVerifier {
    private final JwtDecoder googleIdTokenDecoder;

    @Override
    public GoogleIdentity verify(String idToken) {
        Jwt jwt;

        try {
            jwt = googleIdTokenDecoder.decode(idToken);
        } catch (JwtException exception) {
            throw new InvalidGoogleIdTokenException("Invalid Google ID token: " + exception.getMessage());
        }

        String sub = jwt.getClaimAsString("sub");
        String email = jwt.getClaimAsString("email");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        String name = jwt.getClaimAsString("name");
        String pictureUrl = jwt.getClaimAsString("picture");

        if (sub == null || email == null) {
            throw new InvalidGoogleIdTokenException("Google ID token is missing required claims");
        }

        return new GoogleIdentity(
            sub,
            email,
            Boolean.TRUE.equals(emailVerified),
            name,
            pictureUrl
        );
    }
}
