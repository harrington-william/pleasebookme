package com.pleasebookme.server.security.oauth.google.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class GoogleOAuthConfig {
    private static final String GOOGLE_JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";

    private static final Set<String> GOOGLE_ISSUERS = Set.of(
        "https://accounts.google.com",
        "accounts.google.com"
    );

    @Bean
    public ClientRegistration googleClientRegistration(
        ClientRegistrationRepository clientRegistrationRepository
    ) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("google");

        if (registration == null) {
            throw new IllegalStateException(
                "Missing 'google' OAuth2 client registration"
            );
        }

        return registration;
    }

    @Bean
    public JwtDecoder googleIdTokenDecoder(ClientRegistration googleClientRegistration) {
        String clientId = googleClientRegistration.getClientId();

        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withJwkSetUri(GOOGLE_JWK_SET_URI)
            .build();

        // Keep iss as String instead of URL
        Converter<Object, Object> keepAsString = claim -> claim;
        decoder.setClaimSetConverter(
            MappedJwtClaimSetConverter.withDefaults(Map.of(JwtClaimNames.ISS, keepAsString))
        );

        decoder.setJwtValidator(googleTokenValidator(clientId));

        return decoder;
    }

    private OAuth2TokenValidator<Jwt> googleTokenValidator(String clientId) {
        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator();

        OAuth2TokenValidator<Jwt> issuerValidator = jwt -> {
            String issuer = jwt.getClaimAsString(JwtClaimNames.ISS);

            if (issuer != null && GOOGLE_ISSUERS.contains(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
                new OAuth2Error(
                    "invalid_issuer",
                    "Unexpected issuer: " + issuer,
                    null
                )
            );
        };

        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            List<String> audience = jwt.getAudience();

            if (audience != null && audience.contains(clientId)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
                new OAuth2Error(
                    "invalid_audience",
                    "Unexpected audience",
                    null
                )
            );
        };

        return new DelegatingOAuth2TokenValidator<>(
            timestampValidator,
            issuerValidator,
            audienceValidator
        );
    }
}
