package com.pleasebookme.server.security.ratelimit.engine;

import java.time.Duration;

public interface RateLimiter {
    void check(
        String bucket,
        String key,
        int limit,
        Duration window
    );
}
