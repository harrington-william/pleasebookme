package com.pleasebookme.server.security.ratelimit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
    @Name("public") @Valid @NotNull Public publicLimits
) {
    public record Public(
        @Valid @NotNull Limit read,
        @Valid @NotNull Limit write
    ) {}

    public record Limit(
        @Positive int limit,
        @NotNull Duration window
    ) {}
}
